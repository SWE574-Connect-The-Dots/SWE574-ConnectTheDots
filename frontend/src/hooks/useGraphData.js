import { useState, useCallback } from "react";
import api from "../axiosConfig";
import dagre from "dagre";

const useGraphData = (spaceId) => {
  const [nodes, setNodes] = useState([]);
  const [edges, setEdges] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const nodeWidth = 160;
  const nodeHeight = 160;

  function calculateHandlePositions(sourcePos, targetPos) {
    const dx = targetPos.x - sourcePos.x;
    const dy = targetPos.y - sourcePos.y;

    const angle = Math.atan2(dy, dx) * (180 / Math.PI);

    let sourcePosition, targetPosition;

    if (angle >= -45 && angle < 45) {
      sourcePosition = 'right';
      targetPosition = 'left';
    } else if (angle >= 45 && angle < 135) {
      sourcePosition = 'bottom';
      targetPosition = 'top';
    } else if (angle >= 135 || angle < -135) {
      sourcePosition = 'left';
      targetPosition = 'right';
    } else {
      sourcePosition = 'top';
      targetPosition = 'bottom';
    }

    return { sourcePosition, targetPosition };
  }

  function getNodeColor() {
    return 'var(--color-success)';
  }

  function layoutNodesWithDagre(nodes, edges, direction = "TB") {
    const dagreGraph = new dagre.graphlib.Graph();
    dagreGraph.setDefaultEdgeLabel(() => ({}));

    dagreGraph.setGraph({
      rankdir: direction,
      nodesep: 80,
      ranksep: 100,
      edgesep: 30,
      ranker: 'tight-tree',
      align: 'UL',
    });

    const nodeDegrees = {};
    nodes.forEach((node) => {
      nodeDegrees[node.id] = 0;
    });
    edges.forEach((edge) => {
      if (nodeDegrees[edge.source] !== undefined) nodeDegrees[edge.source]++;
      if (nodeDegrees[edge.target] !== undefined) nodeDegrees[edge.target]++;
    });

    nodes.forEach((node) => {
      const degree = nodeDegrees[node.id] || 0;
      const dynamicSize = Math.min(nodeWidth, Math.max(80, 60 + degree * 8));
      dagreGraph.setNode(node.id, {
        width: dynamicSize,
        height: dynamicSize
      });
    });
    edges.forEach((edge) => {
      dagreGraph.setEdge(edge.source, edge.target);
    });

    dagre.layout(dagreGraph);

    const layoutedNodes = nodes.map((node) => {
      const dagreNode = dagreGraph.node(node.id);
      const degree = nodeDegrees[node.id] || 0;
      const dynamicSize = Math.min(nodeWidth, Math.max(80, 60 + degree * 8));

      return {
        ...node,
        data: {
          ...node.data,
          degree,
          color: getNodeColor(),
          size: dynamicSize,
        },
        position: {
          x: dagreNode.x - dynamicSize / 2,
          y: dagreNode.y - dynamicSize / 2,
        },
      };
    });

    return layoutedNodes;
  }
  function calculateEdgeOffset(edgeIndex, totalEdges) {
    if (totalEdges === 1) return { x: 0, y: 0 };

    const spread = 20;
    const step = spread / Math.max(1, totalEdges - 1);
    const offset = (edgeIndex * step) - (spread / 2);

    return offset;
  }

  const fetchGraphData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const nodesResponse = await api.get(`/spaces/${spaceId}/nodes/`, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      });

      let edgesData = [];
      try {
        const edgesResponse = await api.get(`/spaces/${spaceId}/edges/`, {
          headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        });
        edgesData = edgesResponse.data;
      } catch (err) {
        console.warn("Edges endpoint not implemented yet");
      }

      const nodesData = Array.isArray(nodesResponse.data)
        ? nodesResponse.data
        : [];
      const validNodes = nodesData.filter((n) => n && n.id && n.label);

      const flowNodes = validNodes.map((node) => ({
        id: node.id.toString(),
        type: "circular",
        position: { x: 0, y: 0 },
        data: {
          label: node.label,
          wikidata_id: node.wikidata_id || null,
          // Include location data
          country: node.country || null,
          city: node.city || null,
          district: node.district || null,
          street: node.street || null,
          latitude: node.latitude || null,
          longitude: node.longitude || null,
          location_name: node.location_name || null,
          description: node.description || null,
        },
        // Also include location data at top level for backward compatibility
        country: node.country || null,
        city: node.city || null,
        district: node.district || null,
        street: node.street || null,
        latitude: node.latitude || null,
        longitude: node.longitude || null,
        location_name: node.location_name || null,
        description: node.description || null,
        instance_type: node.instance_type || null,
      }));

      const edgesDataArray = Array.isArray(edgesData) ? edgesData : [];
      const validEdges = edgesDataArray.filter(
        (e) => e && e.id && e.source && e.target
      );

      const layoutedNodes = layoutNodesWithDagre(flowNodes, validEdges);

      const nodePositionMap = {};
      layoutedNodes.forEach((node) => {
        nodePositionMap[node.id] = {
          x: node.position.x + (node.data.size || 80) / 2,
          y: node.position.y + (node.data.size || 80) / 2,
        };
      });

      const edgesBySource = {};
      validEdges.forEach((edge) => {
        const sourceId = edge.source.toString();
        if (!edgesBySource[sourceId]) {
          edgesBySource[sourceId] = [];
        }
        edgesBySource[sourceId].push(edge);
      });

      const flowEdges = validEdges.map((edge, index) => {
        const isWikidata = edge.wikidata_property_id;
        const displayLabel = isWikidata
          ? `${edge.label} [${edge.wikidata_property_id}]`
          : edge.label;

        const sourceId = edge.source.toString();
        const targetId = edge.target.toString();

        const sourcePos = nodePositionMap[sourceId];
        const targetPos = nodePositionMap[targetId];

        let sourcePosition = 'right';
        let targetPosition = 'left';

        if (sourcePos && targetPos) {
          const positions = calculateHandlePositions(sourcePos, targetPos);
          sourcePosition = positions.sourcePosition;
          targetPosition = positions.targetPosition;
        }

        const sourceEdges = edgesBySource[sourceId] || [];
        const edgeIndexInSource = sourceEdges.findIndex(e => e.id === edge.id);

        return {
          id: edge.id.toString(),
          source: sourceId,
          target: targetId,
          sourceHandle: sourcePosition,
          targetHandle: targetPosition,
          type: 'straight',
          label: displayLabel,
          data: {
            wikidata_property_id: edge.wikidata_property_id,
            original_label: edge.label,
            edgeOffset: calculateEdgeOffset(edgeIndexInSource, sourceEdges.length),
          },
          animated: false,
          style: {
            stroke: isWikidata ? 'var(--color-text)' : 'var(--color-text)',
            strokeWidth: isWikidata ? 2.5 : 2,
            strokeDasharray: isWikidata ? '0' : '5,5',
          },
          markerEnd: {
            type: "arrowclosed",
            color: isWikidata ? 'var(--color-text)' : 'var(--color-text)',
            width: 20,
            height: 20,
          },
          labelStyle: {
            background: isWikidata ? 'var(--color-accent)' : 'var(--color-white)',
            color: 'var(--color-text)',
            padding: isWikidata ? '6px 10px' : '4px 8px',
            fontWeight: 600,
            fontSize: 11,
            borderRadius: 6,
            border: isWikidata
              ? '1.5px solid var(--color-accent)'
              : '1px solid var(--color-border)',
            zIndex: 10,
            boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
          },
          labelBgPadding: [8, 4],
          labelBgBorderRadius: 6,
        };
      });

      setNodes(layoutedNodes);
      setEdges(flowEdges);
    } catch (err) {
      setError(err.message || "Failed to fetch graph data");
      console.error("Error fetching graph data:", err);
    } finally {
      setLoading(false);
    }
  }, [spaceId]);

  return {
    nodes,
    edges,
    loading,
    error,
    fetchGraphData,
  };
};

export default useGraphData;
