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
      }));

      const edgesDataArray = Array.isArray(edgesData) ? edgesData : [];
      const validEdges = edgesDataArray.filter(
        (e) => e && e.id && e.source && e.target
      );

      const flowEdges = validEdges.map((edge) => {
        const isWikidata = edge.wikidata_property_id;
        const displayLabel = isWikidata 
          ? `${edge.label} [${edge.wikidata_property_id}]`
          : edge.label;

        return {
          id: edge.id.toString(),
          source: edge.source.toString(),
          target: edge.target.toString(),
          label: displayLabel,
          data: {
            wikidata_property_id: edge.wikidata_property_id,
            original_label: edge.label,
          },
          animated: false,
          style: {
            stroke: isWikidata ? 'var(--color-wikidata)' : 'var(--color-border-1)',
            strokeWidth: isWikidata ? 3 : 2,
            strokeDasharray: isWikidata ? '0' : '5,5',
          },
          markerEnd: {
            type: "arrowclosed",
            color: isWikidata ? 'var(--color-wikidata)' : 'var(--color-border-1)',
          },
          labelStyle: {
            background: isWikidata ? 'var(--color-wikidata-bg)' : 'var(--color-white)',
            color: 'var(--color-text)',
            padding: isWikidata ? '6px 10px' : '4px 8px',
            fontWeight: 600,
            fontSize: 12,
            borderRadius: 6,
            border: isWikidata 
              ? '2px solid var(--color-wikidata-border)' 
              : '1px solid var(--color-border-2)',
            zIndex: 10,
            boxShadow: isWikidata ? '0 2px 4px rgba(0,114,178,0.2)' : 'none',
          },
        };
      });

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
