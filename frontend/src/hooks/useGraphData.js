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
        // Force left-to-right connections
        sourcePosition: "right",
        targetPosition: "left",
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
          type: 'straight',
          data: {
            wikidata_property_id: edge.wikidata_property_id,
            original_label: edge.label,
          },
          animated: false,
          style: {
            stroke: isWikidata ? '#0072B2' : '#4A90E2',
            strokeWidth: isWikidata ? 4 : 3,
            strokeLinecap: 'round',
            strokeLinejoin: 'round',
          },
          markerEnd: {
            type: "arrowclosed",
            color: isWikidata ? '#0072B2' : '#4A90E2',
            width: 40,
            height: 40,
            markerUnits: 'userSpaceOnUse',
            orient: 'auto',
          },
          labelStyle: {
            background: isWikidata ? '#E8F4FD' : '#F0F8FF',
            color: '#1B1F3B',
            padding: '6px 12px',
            fontWeight: 600,
            fontSize: 12,
            borderRadius: 8,
            border: isWikidata 
              ? '2px solid #0072B2' 
              : '1.5px solid #4A90E2',
            zIndex: 10,
            boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
          },
          // Add sourceHandle and targetHandle to use specific connection points
          sourceHandle: null,
          targetHandle: null,
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
