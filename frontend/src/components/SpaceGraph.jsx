import PropTypes from "prop-types";
import { useTranslation } from "../contexts/TranslationContext";
import ReactFlow, { Controls, Background, applyNodeChanges } from "reactflow";
import "reactflow/dist/style.css";
import CircularNode from "./CircularNode";
import { useMemo, useState, useCallback, useEffect } from "react";

const nodeTypes = {
  circular: CircularNode,
};

const SpaceGraph = ({ nodes, edges, loading, error, onNodeClick, onEdgeClick }) => {
  const { t } = useTranslation();
  
  const [localNodes, setLocalNodes] = useState([]);
  const nodesWithSizes = useMemo(() => {
    if (!nodes || !edges) return nodes;

    const nodeDegrees = {};
    const nodeIdSet = new Set();
    
    nodes.forEach((node) => {
      const nodeIdStr = String(node.id);
      nodeDegrees[nodeIdStr] = 0;
      nodeIdSet.add(nodeIdStr);
    });
    
    edges.forEach((edge) => {
      const sourceId = String(edge.source);
      const targetId = String(edge.target);
      
      if (nodeIdSet.has(sourceId)) {
        nodeDegrees[sourceId]++;
      }
      if (nodeIdSet.has(targetId)) {
        nodeDegrees[targetId]++;
      }
    });
    
    const baseSize = 60;
    const sizePerDegree = 8;

    const nodesWithSizes = nodes.map((node) => {
      const nodeIdStr = String(node.id);
      const degree = nodeDegrees[nodeIdStr] || 0;
      const size = baseSize + (degree * sizePerDegree);

      return {
        ...node,
        data: {
          ...node.data,
          size: Math.round(size),
          degree: degree,
        },
      };
    });


    return nodesWithSizes;
  }, [nodes, edges]);
  
  useEffect(() => {
    if (nodesWithSizes && nodesWithSizes.length > 0) {
      setLocalNodes(nodesWithSizes);
    }
  }, [nodesWithSizes]);
  
  const onNodesChange = useCallback((changes) => {
    setLocalNodes((nds) => applyNodeChanges(changes, nds));
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
        nodes={localNodes.length > 0 ? localNodes : (nodesWithSizes || [])} 
        edges={edges || []} 
        nodeTypes={nodeTypes} 
        onNodesChange={onNodesChange}
        onNodeClick={onNodeClick}
        onEdgeClick={onEdgeClick}
        nodesDraggable={true}
        nodesConnectable={false}
        elementsSelectable={true}
        fitView
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
