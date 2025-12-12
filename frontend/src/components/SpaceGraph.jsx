import PropTypes from "prop-types";
import { useTranslation } from "../contexts/TranslationContext";
import ReactFlow, { Controls, Background, applyNodeChanges, applyEdgeChanges } from "reactflow";
import "reactflow/dist/style.css";
import CircularNode from "./CircularNode";
import { useMemo, useState, useCallback, useEffect } from "react";

const nodeTypes = {
  circular: CircularNode,
};

const SpaceGraph = ({ nodes, edges, loading, error, onNodeClick, onEdgeClick }) => {
  const { t } = useTranslation();
  
  const [localNodes, setLocalNodes] = useState([]);
  const [localEdges, setLocalEdges] = useState([]);
  
  const processedNodes = useMemo(() => {
    return nodes || [];
  }, [nodes]);
  
  useEffect(() => {
    if (processedNodes && processedNodes.length > 0) {
      setLocalNodes(processedNodes);
    }
  }, [processedNodes]);

  useEffect(() => {
    if (edges && edges.length > 0) {
      setLocalEdges(edges);
    }
  }, [edges]);
  
  const onNodesChange = useCallback((changes) => {
    setLocalNodes((nds) => applyNodeChanges(changes, nds));
  }, []);

  const onEdgesChange = useCallback((changes) => {
    setLocalEdges((eds) => applyEdgeChanges(changes, eds));
  }, []);
  
  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner" />
        <p>{t("graph.loadingGraph")}</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <p className="error-message">{t("common.error")}: {error}</p>
      </div>
    );
  }

  return (
    <div className="graph-container" style={{ width: "100%", height: "100%", position: "relative" }}>
      <ReactFlow 
        nodes={localNodes} 
        edges={localEdges} 
        nodeTypes={nodeTypes} 
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={onNodeClick}
        onEdgeClick={onEdgeClick}
        nodesDraggable={true}
        nodesConnectable={false}
        elementsSelectable={true}
        fitView
        fitViewOptions={{
          padding: 0.2,
          minZoom: 0.5,
          maxZoom: 1.5,
        }}
        minZoom={0.1}
        maxZoom={2}
        defaultEdgeOptions={{
          type: 'straight',
        }}
        connectionLineType="straight"
        proOptions={{ hideAttribution: true }}
      >
        <Background />
        <Controls />
      </ReactFlow>
    </div>
  );
};

SpaceGraph.propTypes = {
  nodes: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired,
      position: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
      }).isRequired,
      data: PropTypes.shape({
        label: PropTypes.string.isRequired,
        wikidata_id: PropTypes.string,
        size: PropTypes.number,
        degree: PropTypes.number,
      }).isRequired,
    })
  ).isRequired,
  edges: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      source: PropTypes.string.isRequired,
      target: PropTypes.string.isRequired,
      label: PropTypes.string,
      animated: PropTypes.bool,
      markerEnd: PropTypes.shape({
        type: PropTypes.string.isRequired,
      }),
    })
  ).isRequired,
  loading: PropTypes.bool.isRequired,
  error: PropTypes.string,
  onNodeClick: PropTypes.func,
  onEdgeClick: PropTypes.func,
};

export default SpaceGraph;
