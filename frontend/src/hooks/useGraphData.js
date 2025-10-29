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

  function layoutNodesWithDagre(nodes, edges, direction = "LR") {
    const dagreGraph = new dagre.graphlib.Graph();
    dagreGraph.setDefaultEdgeLabel(() => ({}));
    dagreGraph.setGraph({ rankdir: direction });

    nodes.forEach((node) => {
      dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
    });
    edges.forEach((edge) => {
      dagreGraph.setEdge(edge.source, edge.target);
    });

    dagre.layout(dagreGraph);

    return nodes.map((node) => {
      const dagreNode = dagreGraph.node(node.id);
      return {
        ...node,
        data: { ...node.data },
        position: {
          x: dagreNode.x - nodeWidth / 2,
          y: dagreNode.y - nodeHeight / 2,
        },
        sourcePosition: direction === "LR" ? "right" : "bottom",
        targetPosition: direction === "LR" ? "left" : "top",
      };
    });
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
        },
      }));

      const edgesDataArray = Array.isArray(edgesData) ? edgesData : [];
      const validEdges = edgesDataArray.filter(
        (e) => e && e.id && e.source && e.target
      );

      const flowEdges = validEdges.map((edge) => ({
        id: edge.id.toString(),
        source: edge.source.toString(),
        target: edge.target.toString(),
        label: edge.label,
        animated: false,
        style: {
          stroke: 'var(--color-border-1)',
          strokeWidth: 2,
        },
        markerEnd: {
          type: "arrowclosed",
          color: 'var(--color-border-1)',
        },
        labelStyle: {
          background: 'var(--color-white)',
          color: 'var(--color-text)',
          padding: 4,
          fontWeight: 600,
          fontSize: 12,
          borderRadius: 4,
          border: '1px solid var(--color-border-2)',
          zIndex: 10,
        },
      }));

      const layoutedNodes = layoutNodesWithDagre(flowNodes, flowEdges);

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
