import PropTypes from "prop-types";
import { useTranslation } from "../contexts/TranslationContext";
import ReactFlow, { Controls, Background, applyNodeChanges, applyEdgeChanges, useReactFlow, ReactFlowProvider } from "reactflow";
import "reactflow/dist/style.css";
import CircularNode from "./CircularNode";
import { useMemo, useState, useCallback, useEffect, useRef } from "react";

const nodeTypes = {
  circular: CircularNode,
};

const SpaceGraphInner = ({ nodes, edges, loading, error, onNodeClick, onEdgeClick, selectedInstanceTypes, isFullscreen }) => {
  const { t } = useTranslation();
  const { fitView } = useReactFlow();
  const prevFullscreenRef = useRef(isFullscreen);
  
  const [localNodes, setLocalNodes] = useState([]);
  const [localEdges, setLocalEdges] = useState([]);
  
  const nodesWithFilterState = useMemo(() => {
    if (!nodes) return [];
    
    if (!selectedInstanceTypes || selectedInstanceTypes.size === 0) {
      return nodes.map(node => ({
        ...node,
        data: {
          ...node.data,
          isFiltered: false,
        },
      }));
    }
    
    return nodes.map(node => {
      const instanceType = node.instance_type;
      
      const isFiltered = !instanceType || !instanceType.group_id || 
                        !selectedInstanceTypes.has(instanceType.group_id);
      
      return {
        ...node,
        data: {
          ...node.data,
          isFiltered: isFiltered,
        },
      };
    });
  }, [nodes, selectedInstanceTypes]);
  
  const nodesWithSizes = useMemo(() => {
    if (!nodesWithFilterState) return [];
    
    const edgesForCalculation = edges || [];

    const nodeDegrees = {};
    const nodeIdSet = new Set();
    
    nodesWithFilterState.forEach((node) => {
      const nodeIdStr = String(node.id);
      nodeDegrees[nodeIdStr] = 0;
      nodeIdSet.add(nodeIdStr);
    });
    
    edgesForCalculation.forEach((edge) => {
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

    const nodesWithSizes = nodesWithFilterState.map((node) => {
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
  }, [nodesWithFilterState, edges]);
  
  const visibleNodeCount = useMemo(() => {
    if (!nodesWithFilterState) return 0;
    return nodesWithFilterState.filter(node => !node.data.isFiltered).length;
  }, [nodesWithFilterState]);
  
  useEffect(() => {
    if (nodesWithSizes && nodesWithSizes.length > 0) {
      setLocalNodes(nodesWithSizes);
    } else {
      setLocalNodes([]);
    }
  }, [nodesWithSizes]);

  useEffect(() => {
    if (edges && edges.length > 0 && nodesWithSizes) {
      const nodeFilterMap = new Map();
      nodesWithSizes.forEach(node => {
        nodeFilterMap.set(String(node.id), node.data.isFiltered || false);
      });
      
      const styledEdges = edges.map(edge => {
        const sourceFiltered = nodeFilterMap.get(String(edge.source)) || false;
        const targetFiltered = nodeFilterMap.get(String(edge.target)) || false;
        const isFiltered = sourceFiltered || targetFiltered;
        
        return {
          ...edge,
          style: {
            ...edge.style,
            stroke: isFiltered ? 'var(--color-gray-300)' : (edge.style?.stroke || 'var(--color-gray-400)'),
            opacity: isFiltered ? 0.3 : (edge.style?.opacity || 1),
          },
          labelStyle: {
            ...edge.labelStyle,
            fill: isFiltered ? 'var(--color-text-secondary)' : (edge.labelStyle?.fill || 'var(--color-text)'),
            opacity: isFiltered ? 0.4 : (edge.labelStyle?.opacity || 1),
          },
        };
      });
      
      setLocalEdges(styledEdges);
    } else if (edges && edges.length > 0) {
      setLocalEdges(edges);
    } else {
      setLocalEdges([]);
    }
  }, [edges, nodesWithSizes]);
  
  const onNodesChange = useCallback((changes) => {
    setLocalNodes((nds) => applyNodeChanges(changes, nds));
  }, []);

  const onEdgesChange = useCallback((changes) => {
    setLocalEdges((eds) => applyEdgeChanges(changes, eds));
  }, []);

  useEffect(() => {
    if (prevFullscreenRef.current !== isFullscreen && localNodes.length > 0) {
      setTimeout(() => {
        fitView({ 
          padding: isFullscreen ? 0.3 : 0.2,
          duration: 300,
          includeHiddenNodes: false
        });
      }, 150);
      prevFullscreenRef.current = isFullscreen;
    }
  }, [isFullscreen, fitView, localNodes.length]);
  
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
      {/* Show filter info if active */}
      {selectedInstanceTypes && selectedInstanceTypes.size > 0 && nodes && (
        <div style={{
          position: 'absolute',
          top: '10px',
          left: '10px',
          zIndex: 5,
          backgroundColor: 'var(--color-white)',
          padding: '8px 12px',
          borderRadius: '4px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          fontSize: '12px',
          color: 'var(--color-text-secondary)'
        }}>
          {t('instanceTypes.filtering')}: {visibleNodeCount} / {nodes.length} nodes
        </div>
      )}
      
      <ReactFlow 
        nodes={localNodes.length > 0 ? localNodes : (nodesWithSizes || [])} 
        edges={localEdges.length > 0 ? localEdges : (edges || [])} 
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
          padding: isFullscreen ? 0.3 : 0.2,
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

const SpaceGraph = (props) => {
  return (
    <ReactFlowProvider>
      <SpaceGraphInner {...props} />
    </ReactFlowProvider>
  );
};

SpaceGraphInner.propTypes = {
  nodes: PropTypes.array.isRequired,
  edges: PropTypes.array.isRequired,
  loading: PropTypes.bool.isRequired,
  error: PropTypes.string,
  onNodeClick: PropTypes.func,
  onEdgeClick: PropTypes.func,
  selectedInstanceTypes: PropTypes.instanceOf(Set),
  isFullscreen: PropTypes.bool,
};

export default SpaceGraph;
