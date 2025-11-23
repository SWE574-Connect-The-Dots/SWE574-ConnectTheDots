import React, { useCallback, useEffect, useMemo, useState } from "react";
import PropTypes from "prop-types";
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

const ActivityStream = ({ title, spaceId, maxItems = 100, dense = false, className = "" }) => {
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
            {activities.map((item) => (
              <li key={item.id} className="activity-stream__item">
                <div className="activity-stream__summary">
                  {pickSummary(item, t("activity.unknownEvent"))}
                </div>
                <div className="activity-stream__meta">
                  <span className="activity-stream__actor">
                    {normalizeActor(item.actor, t("activity.unknownActor"))}
                  </span>
                  <span className="activity-stream__dot" aria-hidden />
                  <span className="activity-stream__time">
                    {formatRelativeTime(item.published)}
                  </span>
                </div>
              </li>
            ))}
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
};

export default ActivityStream;

