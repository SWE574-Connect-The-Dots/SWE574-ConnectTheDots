import React, { useCallback, useEffect, useMemo, useState } from "react";
import PropTypes from "prop-types";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "../contexts/TranslationContext";
import api from "../axiosConfig";
import "./ActivityStream.css";

const RELATIVE_TIME_BREAKPOINTS = [
  { limit: 60, divisor: 1, unit: "seconds" },
  { limit: 3600, divisor: 60, unit: "minutes" },
  { limit: 86400, divisor: 3600, unit: "hours" },
  { limit: 604800, divisor: 86400, unit: "days" },
  { limit: 2629800, divisor: 604800, unit: "weeks" },
  { limit: 31557600, divisor: 2629800, unit: "months" },
];

const formatRelativeTime = (timestamp) => {
  if (!timestamp) return "";
  const published = new Date(timestamp);
  if (Number.isNaN(published.getTime())) return "";
  const diffSeconds = Math.max(1, Math.floor((Date.now() - published.getTime()) / 1000));

  const breakpoint =
    RELATIVE_TIME_BREAKPOINTS.find((bp) => diffSeconds < bp.limit) ||
    { divisor: 31557600, unit: "years" };

  const value = Math.floor(diffSeconds / breakpoint.divisor);
  return `${value} ${breakpoint.unit}`;
};

const pickSummary = (item, fallback) => {
  if (item?.summary) return item.summary;
  if (item?.type && item?.object) {
    return `${item.type} ${typeof item.object === "string" ? item.object : ""}`.trim();
  }
  return fallback;
};

const normalizeActor = (actor, fallback) => {
  if (!actor) return fallback;
  if (typeof actor === "string") return actor;
  return actor.name || actor.id || fallback;
};

const extractActorInfo = (actor) => {
  if (!actor) return null;
  if (typeof actor === "string") return { username: actor, type: "Person" };
  if (typeof actor === "object") {
    return {
      username: actor.id || actor.name,
      type: actor.type || "Person",
    };
  }
  return null;
};

const extractNodeInfo = (ref) => {
  if (!ref) return null;
  if (typeof ref === "string") {
    const match = ref.match(/^(?:Node|node):(\d+)$/);
    if (match) {
      return { nodeId: match[1], label: ref };
    }
  } else if (typeof ref === "object" && ref.type && ref.id) {
    const refType = String(ref.type).toLowerCase();
    if (refType === "node") {
      return { nodeId: ref.id, label: ref.name || `Node:${ref.id}` };
    }
  }
  return null;
};

const extractSpaceIdFromRef = (ref) => {
  if (!ref) return null;
  if (typeof ref === "string") {
    const match = ref.match(/^(?:Space|space):(\d+)$/);
    return match ? match[1] : null;
  }
  if (typeof ref === "object" && ref.type && ref.id) {
    const refType = String(ref.type).toLowerCase();
    if (refType === "space") return String(ref.id);
  }
  return null;
};

const isNodeDeleteActivity = (item) => {
  if (!item) return false;
  if (String(item.type || "").toLowerCase() !== "delete") return false;
  const payloadNodeId = item?.payload?.node_id ?? item?.payload?.nodeId ?? null;
  const objectStr = typeof item.object === "string" ? item.object : "";
  const objectLooksLikeNode = objectStr.toLowerCase().startsWith("node:");
  return Boolean(payloadNodeId || objectLooksLikeNode);
};

const parseSummaryForNodes = (summary, item) => {
  if (!summary) return [];
  const matches = [];
  
  const nodeIdPattern = /(?:Node|node):(\d+)/g;
  let match;
  while ((match = nodeIdPattern.exec(summary)) !== null) {
    matches.push({ 
      nodeId: match[1], 
      text: match[0], 
      index: match.index,
      length: match[0].length
    });
  }
  
  const payload = item?.payload || {};
  const nodeId =
    payload.node_id ||
    (typeof item?.object === "object" &&
    item.object?.type &&
    String(item.object.type).toLowerCase() === "node"
      ? item.object.id
      : null) ||
    (typeof item?.object === "string" &&
    (item.object.startsWith("Node:") || item.object.startsWith("node:"))
      ? item.object.split(":")[1]
      : null);

  const sourceId = payload.source_id || null;
  const targetId = payload.target_id || null;

  const addMatchIfNotOverlapping = ({ nodeId: id, text, index, length }) => {
    if (!id || !text || typeof index !== "number" || typeof length !== "number") return;
    const overlaps = matches.some((m) => (m.index < index + length) && (index < m.index + m.length));
    if (overlaps) return;
    matches.push({ nodeId: String(id), text, index, length });
  };

  if (nodeId) {
    const nodeLabelPattern = /node\s+['"]([^'"]+)['"]/gi;
    let foundSpecificNodeLabel = false;
    while ((match = nodeLabelPattern.exec(summary)) !== null) {
      foundSpecificNodeLabel = true;
      const fullMatch = match[0];
      const label = match[1];
      const labelStartIndex = match.index + fullMatch.indexOf(label);
      addMatchIfNotOverlapping({
        nodeId,
        text: label,
        index: labelStartIndex,
        length: label.length,
      });
    }

    if (!foundSpecificNodeLabel) {
      const firstQuoted = /['"]([^'"]+)['"]/g.exec(summary);
      if (firstQuoted) {
        const label = firstQuoted[1];
        const labelStartIndex = firstQuoted.index + 1;
        addMatchIfNotOverlapping({
          nodeId,
          text: label,
          index: labelStartIndex,
          length: label.length,
        });
      }
    }

    const hasAnyReadableLabel =
      matches.some((m) => m.nodeId === String(nodeId) && !/^(?:Node|node):\d+$/.test(m.text));
    if (!hasAnyReadableLabel) {
      const reportedMatch = /reported\s+(.+?)\s*$/i.exec(summary);
      if (reportedMatch?.[1]) {
        const label = reportedMatch[1].trim();
        const startIndex = summary.toLowerCase().lastIndexOf(reportedMatch[0].toLowerCase());
        const labelIndex = startIndex >= 0 ? startIndex + reportedMatch[0].toLowerCase().indexOf(label.toLowerCase()) : -1;
        if (labelIndex >= 0) {
          addMatchIfNotOverlapping({ nodeId, text: label, index: labelIndex, length: label.length });
        }
      }
    }
  }

  if (sourceId && targetId) {
    const quoted = [];
    const quotedPattern = /['"]([^'"]+)['"]/g;
    while ((match = quotedPattern.exec(summary)) !== null) {
      quoted.push({ label: match[1], index: match.index + 1, length: match[1].length });
      if (quoted.length >= 2) break;
    }
    if (quoted.length >= 2) {
      addMatchIfNotOverlapping({ nodeId: sourceId, text: quoted[0].label, index: quoted[0].index, length: quoted[0].length });
      addMatchIfNotOverlapping({ nodeId: targetId, text: quoted[1].label, index: quoted[1].index, length: quoted[1].length });
    }

    const arrowPattern = /(\d+)\s*(?:->|→)\s*(\d+)/g;
    while ((match = arrowPattern.exec(summary)) !== null) {
      const fullMatch = match[0];
      const left = match[1];
      const right = match[2];
      const leftIndex = match.index + fullMatch.indexOf(left);
      const rightIndex = match.index + fullMatch.indexOf(right);
      addMatchIfNotOverlapping({ nodeId: sourceId, text: left, index: leftIndex, length: left.length });
      addMatchIfNotOverlapping({ nodeId: targetId, text: right, index: rightIndex, length: right.length });
    }
  }
  
  return matches.sort((a, b) => a.index - b.index);
};

const ActivityStream = ({ title, spaceId, maxItems = 100, dense = false, className = "", onNodeClick, onUserClick }) => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const oneDayAgo = useMemo(() => {
    const date = new Date();
    date.setDate(date.getDate() - 1);
    return date.toISOString();
  }, []);

  const requestParams = useMemo(() => {
    const baseParams = { 
      limit: maxItems,
      since: oneDayAgo 
    };
    
    if (!spaceId) {
      return [
        {
          label: "global",
          params: baseParams,
        },
      ];
    }
    const spaceRef = `Space:${spaceId}`;
    return [
      { label: "object", params: { ...baseParams, object: spaceRef } },
      { label: "target", params: { ...baseParams, target: spaceRef } },
    ];
  }, [spaceId, maxItems, oneDayAgo]);

  const extractRef = useCallback((ref) => {
    if (!ref) return "";
    if (typeof ref === "string") return ref;
    if (typeof ref === "object") {
      if (ref.name && typeof ref.name === "string") return ref.name;
      if (ref.id && ref.type) return `${ref.type}:${ref.id}`;
      if (ref.id) return String(ref.id);
    }
    return "";
  }, []);

  const matchesSpace = useCallback(
    (item) => {
      if (!spaceId) return true;
      const reference = `Space:${spaceId}`;
      const normalizedRef = reference.toLowerCase();
      const candidates = [
        extractRef(item.object).toLowerCase(),
        extractRef(item.target).toLowerCase(),
      ];
      if (candidates.some((value) => value === normalizedRef)) {
        return true;
      }
      const payloadSpace =
        item?.payload?.space_id ??
        item?.payload?.spaceId ??
        item?.payload?.space ??
        null;
      if (
        payloadSpace &&
        Number.parseInt(payloadSpace, 10) === Number.parseInt(spaceId, 10)
      ) {
        return true;
      }
      return false;
    },
    [extractRef, spaceId]
  );

  const fetchActivities = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const requests = requestParams.map((entry) =>
        api.get("/activity-stream/", { params: entry.params })
      );
      const responses = await Promise.all(requests);

      const merged = responses.flatMap((response) => response.data?.orderedItems || []);
      const unique = Array.from(
        merged.reduce((map, item) => {
          if (!item?.id) return map;
          if (!map.has(item.id)) {
            map.set(item.id, item);
          }
          return map;
        }, new Map()).values()
      );

      unique.sort((a, b) => new Date(b.published) - new Date(a.published));
      const scoped = unique.filter(matchesSpace);
      const oneDayAgoDate = new Date(oneDayAgo);
      const lastDayActivities = scoped.filter(item => {
        if (!item.published) return false;
        const publishedDate = new Date(item.published);
        return publishedDate >= oneDayAgoDate;
      });
      setActivities(lastDayActivities);
    } catch (err) {
      console.error("Failed to fetch activity stream", err);
      setError(t("errors.failedToLoadActivity"));
    } finally {
      setLoading(false);
    }
  }, [requestParams, maxItems, matchesSpace, t, oneDayAgo]);

  useEffect(() => {
    fetchActivities();
  }, [fetchActivities]);

  return (
    <div className={`activity-stream ${dense ? "activity-stream--dense" : ""} ${className}`}>
      <div className="activity-stream__header">
        <div className="activity-stream__title">
          {title || (spaceId ? t("activity.spaceTitle") : t("activity.globalTitle"))}
        </div>
        <button
          className="activity-stream__refresh"
          type="button"
          onClick={fetchActivities}
          disabled={loading}
        >
          {loading ? t("common.loading") : t("activity.refresh")}
        </button>
      </div>

      {error && <div className="activity-stream__state">{error}</div>}
      {!error && loading && <div className="activity-stream__state">{t("common.loading")}</div>}

      {!error && !loading && activities.length === 0 && (
        <div className="activity-stream__state">{t("activity.empty")}</div>
      )}

      {!error && !loading && activities.length > 0 && (
        <div className="activity-stream__scrollable">
          <ul className="activity-stream__list">
             {activities.map((item) => {
               const actorInfo = extractActorInfo(item.actor);
               const summary = pickSummary(item, t("activity.unknownEvent"));
               const nodeMatches = parseSummaryForNodes(summary, item);
               const objectNodeInfo = extractNodeInfo(item.object);
              
              const handleUserClick = (e, username) => {
                e.preventDefault();
                if (onUserClick) {
                  onUserClick(username);
                } else {
                  navigate(`/profile/${username}`);
                }
              };
              
              const handleNodeClickInternal = (e, nodeId, nodeLabel) => {
                e.preventDefault();

                const inferredSpaceId =
                  item.payload?.space_id ||
                  item.payload?.spaceId ||
                  extractSpaceIdFromRef(item.target) ||
                  extractSpaceIdFromRef(item.object);

                if (onNodeClick) {
                  onNodeClick(nodeId, nodeLabel, inferredSpaceId);
                } else {
                  if (inferredSpaceId) {
                    navigate(`/spaces/${inferredSpaceId}?nodeId=${encodeURIComponent(nodeId)}`);
                  }
                }
              };
              
               const renderSummary = () => {
                 const tokens = [];
                 const isDeletedNode = isNodeDeleteActivity(item);

                 const addTokenIfNotOverlapping = ({ kind, index, length, ...rest }) => {
                   if (typeof index !== "number" || typeof length !== "number") return;
                   const overlaps = tokens.some((t) => (t.index < index + length) && (index < t.index + t.length));
                   if (overlaps) return;
                   tokens.push({ kind, index, length, ...rest });
                 };

                 if (actorInfo && actorInfo.type === "Person" && actorInfo.username && actorInfo.username !== "system") {
                   const actorText = summary.startsWith(actorInfo.username)
                     ? actorInfo.username
                     : (summary.startsWith(normalizeActor(item.actor, "")) ? normalizeActor(item.actor, "") : null);
                   if (actorText) {
                     addTokenIfNotOverlapping({
                       kind: "user",
                       index: 0,
                       length: actorText.length,
                       username: actorInfo.username,
                       text: actorText,
                     });
                   }
                 }

                 nodeMatches.forEach((m) => {
                   addTokenIfNotOverlapping({
                     kind: "node",
                     index: m.index,
                     length: m.length,
                     nodeId: m.nodeId,
                     text: m.text,
                   });
                 });

                 if (tokens.length === 0) return summary;
                 tokens.sort((a, b) => a.index - b.index);

                 const parts = [];
                 let lastIndex = 0;

                 tokens.forEach((tok, idx) => {
                   if (tok.index < lastIndex) return;

                   if (tok.index > lastIndex) {
                     parts.push(
                       <span key={`text-${idx}`}>
                         {summary.substring(lastIndex, tok.index)}
                       </span>
                     );
                   }

                   if (tok.kind === "user") {
                     parts.push(
                       <span
                         key={`user-${idx}`}
                         className="activity-stream__clickable"
                         onClick={(e) => handleUserClick(e, tok.username)}
                         role="button"
                         tabIndex={0}
                         onKeyDown={(e) => {
                           if (e.key === "Enter" || e.key === " ") {
                             handleUserClick(e, tok.username);
                           }
                         }}
                       >
                         {tok.text}
                       </span>
                     );
                   } else if (tok.kind === "node") {
                     if (isDeletedNode) {
                       parts.push(
                         <span key={`node-${idx}`} className="activity-stream__deleted-ref">
                           {tok.text}
                         </span>
                       );
                     } else {
                       parts.push(
                         <span
                           key={`node-${idx}`}
                           className="activity-stream__clickable"
                           onClick={(e) => handleNodeClickInternal(e, tok.nodeId, tok.text)}
                           role="button"
                           tabIndex={0}
                           onKeyDown={(e) => {
                             if (e.key === "Enter" || e.key === " ") {
                               handleNodeClickInternal(e, tok.nodeId, tok.text);
                             }
                           }}
                         >
                           {tok.text}
                         </span>
                       );
                     }
                   }

                   lastIndex = tok.index + tok.length;
                 });

                 if (lastIndex < summary.length) {
                   parts.push(<span key="text-final">{summary.substring(lastIndex)}</span>);
                 }

                 return parts;
               };
              
              return (
                <li key={item.id} className="activity-stream__item">
                  <div className="activity-stream__summary">
                    {renderSummary()}
                  </div>
                  <div className="activity-stream__meta">
                    <span className="activity-stream__time">
                      {formatRelativeTime(item.published)}
                    </span>
                  </div>
                </li>
              );
            })}
          </ul>
        </div>
      )}
    </div>
  );
};

ActivityStream.propTypes = {
  title: PropTypes.string,
  spaceId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  maxItems: PropTypes.number,
  dense: PropTypes.bool,
  className: PropTypes.string,
  onNodeClick: PropTypes.func,
  onUserClick: PropTypes.func,
};

export default ActivityStream;

