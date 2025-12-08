import PropTypes from "prop-types";
import { useTranslation } from "../contexts/TranslationContext";
import ReactFlow, { Controls, Background, applyNodeChanges, getBezierPath } from "reactflow";
import "reactflow/dist/style.css";
import CircularNode from "./CircularNode";
import { useMemo, useState, useCallback, useEffect } from "react";
import api from "../axiosConfig";


const customStyles = `
  .react-flow__edge-path {
    stroke-linecap: round !important;
    stroke-linejoin: round !important;
  }
  
  .react-flow__arrowclosed {
    fill: currentColor !important;
    stroke: currentColor !important;
  }
  
  .react-flow__marker {
    overflow: visible !important;
  }
  
  .react-flow__edge {
    pointer-events: all;
  }
  
  .react-flow__edge:hover .react-flow__edge-path {
    stroke-width: 6px !important;
  }
  
  .react-flow__edge-text {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
    font-size: 12px !important;
    font-weight: 500;
    user-select: none;
    pointer-events: none;
  }
  
  .react-flow__edge-textbg {
    fill: rgba(255, 255, 255, 0.9) !important;
    stroke: #ddd !important;
    stroke-width: 1px !important;
  }
  
  .react-flow__edge:hover .react-flow__edge-text {
    font-weight: 600;
  }
  
  .react-flow__edge:hover .react-flow__edge-textbg {
    fill: rgba(255, 255, 255, 1) !important;
    stroke: #3A0CA3 !important;
  }
`;

// Helper function to calculate edge distribution around nodes
const calculateEdgeDistribution = (nodes, edges) => {
  if (!nodes || !edges || edges.length === 0) return edges;

  // Create node lookup for sizes
  const nodeMap = new Map();
  nodes.forEach(node => {
    nodeMap.set(String(node.id), {
      size: node.data?.size || 60
    });
  });

  // Process each edge with distributed connection points
  const processedEdges = edges.map((edge, index) => {
    const sourceId = String(edge.source);
    const targetId = String(edge.target);
    
    const sourceNode = nodeMap.get(sourceId);
    const targetNode = nodeMap.get(targetId);
    
    if (!sourceNode || !targetNode) {
      return {
        ...edge,
        type: 'straight',
        markerEnd: {
          type: 'arrowclosed',
          color: '#3A0CA3',
          width: 20,
          height: 20,
          markerUnits: 'userSpaceOnUse',
          orient: 'auto',
        },
      };
    }
    
    return {
      ...edge,
      type: 'distributed',
      data: {
        ...edge.data,
        sourceOffset: 0,
        targetOffset: 0,
        sourceRadius: sourceNode.size / 2,
        targetRadius: targetNode.size / 2,
      },
      label: edge.label || edge.data?.label,
      labelStyle: edge.labelStyle || { fontSize: 12, fill: '#333' },
      labelShowBg: true,
      labelBgStyle: { fill: 'rgba(255, 255, 255, 0.8)', stroke: '#ddd' },
      markerEnd: {
        type: 'arrowclosed',
        color: '#3A0CA3',
        width: 20,
        height: 20,
        markerUnits: 'userSpaceOnUse',
        orient: 'auto',
      },
    };
  });

  return processedEdges;
};



const CustomDistributedEdge = ({ 
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  data,
  markerEnd,
  style,
  label,
  labelStyle,
  labelShowBg,
  labelBgStyle,
  labelBgPadding,
  labelBgBorderRadius
}) => {
  const { sourceOffset = 0, targetOffset = 0, sourceRadius = 30, targetRadius = 30 } = data || {};
  
  // Calculate the angle between nodes
  const dx = targetX - sourceX;
  const dy = targetY - sourceY;
  const distance = Math.sqrt(dx * dx + dy * dy);
  
  if (distance === 0) {
    return null;
  }
  
  // Calculate base angle between nodes
  const baseAngle = Math.atan2(dy, dx);
  
  // Apply relative offsets to distribute edges around the connection point
  const sourceAngle = baseAngle + sourceOffset;
  const targetAngle = baseAngle + Math.PI + targetOffset; // Opposite side + offset
  
  // Calculate connection points on the circumference of the circles
  const sourceConnectX = sourceX + sourceRadius * Math.cos(sourceAngle);
  const sourceConnectY = sourceY + sourceRadius * Math.sin(sourceAngle);
  const targetConnectX = targetX + targetRadius * Math.cos(targetAngle);
  const targetConnectY = targetY + targetRadius * Math.sin(targetAngle);
  
 
  const edgePath = `M ${sourceConnectX} ${sourceConnectY} L ${targetConnectX} ${targetConnectY}`;

  // Label position
  const labelX = (sourceConnectX + targetConnectX) / 2;
  const labelY = (sourceConnectY + targetConnectY) / 2;
  
  // Calculate arrow angle for proper orientation
  const arrowAngle = Math.atan2(targetConnectY - sourceConnectY, targetConnectX - sourceConnectX);
  const arrowSize = 10;
  
  const arrowOffset = 12; 
  const arrowTipX = targetConnectX - arrowOffset * Math.cos(arrowAngle);
  const arrowTipY = targetConnectY - arrowOffset * Math.sin(arrowAngle);
  const arrowBase1X = arrowTipX - arrowSize * Math.cos(arrowAngle - Math.PI / 6);
  const arrowBase1Y = arrowTipY - arrowSize * Math.sin(arrowAngle - Math.PI / 6);
  const arrowBase2X = arrowTipX - arrowSize * Math.cos(arrowAngle + Math.PI / 6);
  const arrowBase2Y = arrowTipY - arrowSize * Math.sin(arrowAngle + Math.PI / 6);
  
  return (
    <g>
      <path
        id={id}
        style={{
          strokeWidth: 4,
          stroke: '#3A0CA3',
          fill: 'none',
          strokeLinecap: 'round',
          strokeLinejoin: 'round',
          ...style
        }}
        className="react-flow__edge-path"
        d={edgePath}
      />
      {/* Custom arrow head */}
      <path
        d={`M ${arrowTipX} ${arrowTipY} L ${arrowBase1X} ${arrowBase1Y} L ${arrowBase2X} ${arrowBase2Y} Z`}
        fill="#3A0CA3"
        stroke="#3A0CA3"
        strokeWidth="1"
      />
      {label && (
        <>
          {labelShowBg && (
            <rect
              x={labelX - (label.length * 3)}
              y={labelY - 8}
              width={label.length * 6}
              height={16}
              style={{
                fill: 'rgba(255, 255, 255, 0.9)',
                stroke: '#ddd',
                strokeWidth: 1,
                rx: labelBgBorderRadius || 2,
                ...labelBgStyle
              }}
              className="react-flow__edge-textbg"
            />
          )}
          <text
            x={labelX}
            y={labelY}
            style={{
              fontSize: 12,
              fill: '#333',
              textAnchor: 'middle',
              dominantBaseline: 'middle',
              pointerEvents: 'none',
              ...labelStyle
            }}
            className="react-flow__edge-text"
          >
            {label}
          </text>
        </>
      )}
    </g>
  );
};

const nodeTypes = {
  circular: CircularNode,
};

const edgeTypes = {
  distributed: CustomDistributedEdge,
};

const SpaceGraph = ({ 
  nodes, 
  edges, 
  loading, 
  error, 
  onNodeClick, 
  onEdgeClick, 
  spaceId, 
  showFullscreenButton = true 
}) => {
  const { t } = useTranslation();
  
  const [localNodes, setLocalNodes] = useState([]);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [showAdvancedSearch, setShowAdvancedSearch] = useState(false);
  
  // Simple search states
  const [simpleSearchQuery, setSimpleSearchQuery] = useState("");
  const [simpleSearchResults, setSimpleSearchResults] = useState(null);
  const [searchingSimpleQuery, setSearchingSimpleQuery] = useState(false);
  
  // Advanced search states
  const [searchCriteria, setSearchCriteria] = useState([
    { id: 1, property: '', propertyId: '', operator: 'is', value: '', valueId: '', logicalOp: 'AND' }
  ]);
  const [availableProperties, setAvailableProperties] = useState([]);
  const [availableValues, setAvailableValues] = useState({});
  const [loadingProperties, setLoadingProperties] = useState(false);
  const [loadingValues, setLoadingValues] = useState({});
  const [advancedSearchResults, setAdvancedSearchResults] = useState(null);
  const [searchingQuery, setSearchingQuery] = useState(false);
  const [showPropertyDropdown, setShowPropertyDropdown] = useState({});
  const [showValueDropdown, setShowValueDropdown] = useState({});
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

  const processedEdges = useMemo(() => {
    return calculateEdgeDistribution(nodesWithSizes, edges);
  }, [nodesWithSizes, edges]);
  
  useEffect(() => {
    if (nodesWithSizes && nodesWithSizes.length > 0) {
      setLocalNodes(nodesWithSizes);
    }
  }, [nodesWithSizes]);

  useEffect(() => {
    const styleId = 'space-graph-custom-styles';
    if (!document.getElementById(styleId)) {
      const style = document.createElement('style');
      style.id = styleId;
      style.textContent = customStyles;
      document.head.appendChild(style);
    }
    
    return () => {
      const existingStyle = document.getElementById(styleId);
      if (existingStyle) {
        existingStyle.remove();
      }
    };
  }, []);
  
  const onNodesChange = useCallback((changes) => {
    setLocalNodes((nds) => applyNodeChanges(changes, nds));
  }, []);

  // Search functionality
  const handleSimpleSearch = async () => {
    if (!simpleSearchQuery.trim()) {
      setSimpleSearchResults(null);
      return;
    }

    setSearchingSimpleQuery(true);
    try {
      const response = await api.get(
        `/spaces/${spaceId}/search/text/?q=${encodeURIComponent(simpleSearchQuery)}`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      
      setSimpleSearchResults(response.data);
      setAdvancedSearchResults(null);
    } catch (error) {
      console.error("Error executing simple search:", error);
      alert("Failed to execute search. Please try again.");
    } finally {
      setSearchingSimpleQuery(false);
    }
  };

  const fetchAvailableProperties = async () => {
    if (!spaceId) return;
    
    setLoadingProperties(true);
    try {
      const response = await api.get(`/spaces/${spaceId}/properties/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      setAvailableProperties(response.data || []);
    } catch (error) {
      console.error("Error fetching properties:", error);
      setAvailableProperties([]);
    } finally {
      setLoadingProperties(false);
    }
  };

  const fetchPropertyValues = async (criteriaId, propertyId, searchText = '') => {
    setLoadingValues(prev => ({ ...prev, [criteriaId]: true }));
    try {
      const params = searchText ? `?q=${encodeURIComponent(searchText)}` : '';
      const response = await api.get(`/spaces/${spaceId}/search/properties/${propertyId}/values/${params}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      setAvailableValues(prev => ({ ...prev, [criteriaId]: response.data }));
    } catch (error) {
      console.error("Error fetching property values:", error);
      setAvailableValues(prev => ({ ...prev, [criteriaId]: [] }));
    } finally {
      setLoadingValues(prev => ({ ...prev, [criteriaId]: false }));
    }
  };

  const handleAdvancedSearch = async () => {
    const validCriteria = searchCriteria.filter(c => c.propertyId && (c.value || c.valueId));
    
    if (validCriteria.length === 0) {
      alert("Please add at least one complete search criterion (property and value).");
      return;
    }

    const logic = validCriteria.length > 0 ? validCriteria[0].logicalOp : 'AND';
    const rules = validCriteria.map(c => ({
      property_id: c.propertyId,
      value_id: c.valueId || null,
      value_text: c.value || null
    }));

    setSearchingQuery(true);
    try {
      const response = await api.post(
        `/spaces/${spaceId}/search/query/`,
        {
          rules: rules,
          logic: logic
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
            "Content-Type": "application/json",
          },
        }
      );
      
      setAdvancedSearchResults(response.data);
      setSimpleSearchResults(null);
    } catch (error) {
      console.error("Error executing search:", error);
      alert("Failed to execute search. Please try again.");
    } finally {
      setSearchingQuery(false);
    }
  };

  // Helper functions for advanced search
  const handleAddCriteria = () => {
    const newId = Math.max(...searchCriteria.map(c => c.id), 0) + 1;
    setSearchCriteria([
      ...searchCriteria,
      { id: newId, property: '', propertyId: '', operator: 'is', value: '', valueId: '', logicalOp: 'AND' }
    ]);
  };

  const handleRemoveCriteria = (id) => {
    if (searchCriteria.length > 1) {
      setSearchCriteria(searchCriteria.filter(c => c.id !== id));
    }
  };

  const handleCriteriaChange = (id, field, value) => {
    setSearchCriteria(searchCriteria.map(c =>
      c.id === id ? { ...c, [field]: value } : c
    ));
  };

  const handleToggleLogicalOp = (id) => {
    setSearchCriteria(searchCriteria.map(c =>
      c.id === id ? { ...c, logicalOp: c.logicalOp === 'AND' ? 'OR' : 'AND' } : c
    ));
  };

  const handleClearSearch = () => {
    setSearchCriteria([
      { id: 1, property: '', propertyId: '', operator: 'is', value: '', valueId: '', logicalOp: 'AND' }
    ]);
    setAdvancedSearchResults(null);
  };

  const fetchSpaceProperties = async () => {
    if (availableProperties.length > 0) return;
    
    setLoadingProperties(true);
    try {
      const response = await api.get(`/spaces/${spaceId}/search/properties/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      setAvailableProperties(response.data);
    } catch (error) {
      console.error("Error fetching properties:", error);
    } finally {
      setLoadingProperties(false);
    }
  };

  const handlePropertySelect = (criteriaId, property) => {
    setSearchCriteria(searchCriteria.map(c =>
      c.id === criteriaId 
        ? { ...c, property: property.property_label, propertyId: property.property_id, value: '', valueId: '' } 
        : c
    ));
    setShowPropertyDropdown(prev => ({ ...prev, [criteriaId]: false }));
    setAvailableValues(prev => ({ ...prev, [criteriaId]: [] }));
    
    if (property.property_id) {
      fetchPropertyValues(criteriaId, property.property_id);
    }
  };

  const handleValueSelect = (criteriaId, valueItem) => {
    setSearchCriteria(searchCriteria.map(c =>
      c.id === criteriaId 
        ? { ...c, value: valueItem.value_text, valueId: valueItem.value_id } 
        : c
    ));
    setShowValueDropdown(prev => ({ ...prev, [criteriaId]: false }));
  };

  const handlePropertyInputFocus = (criteriaId) => {
    fetchSpaceProperties();
    setShowPropertyDropdown(prev => ({ ...prev, [criteriaId]: true }));
  };

  const handleValueInputFocus = (criteriaId, propertyId) => {
    if (propertyId) {
      setShowValueDropdown(prev => ({ ...prev, [criteriaId]: true }));
      if (!availableValues[criteriaId] || availableValues[criteriaId].length === 0) {
        fetchPropertyValues(criteriaId, propertyId);
      }
    }
  };

  // Load properties when fullscreen opens
  useEffect(() => {
    if (isFullscreen && spaceId) {
      fetchSpaceProperties();
    }
  }, [isFullscreen, spaceId]);
  
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
    <>
      <style>{customStyles}</style>
      {/* Regular Graph View */}
      <div className="graph-container" style={{ width: "100%", height: "100%", position: "relative" }}>
        {showFullscreenButton && (
          <button
            onClick={() => setIsFullscreen(true)}
            style={{
              position: "absolute",
              top: "10px",
              right: "10px",
              zIndex: 1000,
              background: "#0076B5",
              color: "white",
              border: "none",
              borderRadius: "4px",
              padding: "8px 12px",
              cursor: "pointer",
              fontSize: "14px",
              fontWeight: "600",
            }}
          >
            ⛶ Fullscreen
          </button>
        )}
        
        <ReactFlow 
          nodes={localNodes.length > 0 ? localNodes : (nodesWithSizes || [])} 
          edges={processedEdges || []} 
          nodeTypes={nodeTypes}
          edgeTypes={edgeTypes}
          onNodesChange={onNodesChange}
          onNodeClick={onNodeClick}
          onEdgeClick={onEdgeClick}
          nodesDraggable={true}
          nodesConnectable={false}
          elementsSelectable={true}
          fitView
          connectionLineStyle={{
            strokeWidth: 4,
            stroke: '#3A0CA3',
          }}
        >
          <Background />
          <Controls />
        </ReactFlow>
      </div>

      {/* Fullscreen Modal */}
      {isFullscreen && (
        <div
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100vw",
            height: "100vh",
            backgroundColor: "white",
            zIndex: 10000,
            display: "flex",
            flexDirection: "column",
          }}
        >
          {/* Fullscreen Header */}
          <div
            style={{
              padding: "16px 24px",
              backgroundColor: "#f8f9fa",
              borderBottom: "1px solid #ddd",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
              <h3 style={{ margin: 0, color: "#1B1F3B" }}>
                Graph Visualization
              </h3>
              
              {/* Search Controls */}
              <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
                <div style={{ position: "relative" }}>
                  <span style={{ position: "absolute", left: "14px", top: "50%", transform: "translateY(-50%)", color: "#757575", fontSize: "18px" }}>🔍</span>
                  <input
                    type="text"
                    value={simpleSearchQuery}
                    onChange={(e) => setSimpleSearchQuery(e.target.value)}
                    placeholder="Search within this space..."
                    onKeyPress={(e) => {
                      if (e.key === 'Enter') {
                        handleSimpleSearch();
                      }
                    }}
                    disabled={searchingSimpleQuery}
                    style={{
                      padding: "8px 12px 8px 42px",
                      border: "1px solid #ddd",
                      borderRadius: "4px",
                      background: "#FFFFFF",
                      color: "#1B1F3B",
                      fontSize: "14px",
                      minWidth: "300px",
                    }}
                  />
                </div>
                
                <button 
                  onClick={() => setShowAdvancedSearch(!showAdvancedSearch)}
                  style={{
                    padding: "8px 16px",
                    background: "#0076B5",
                    color: "#FFFFFF",
                    border: "none",
                    borderRadius: "4px",
                    fontSize: "14px",
                    fontWeight: "600",
                    cursor: "pointer",
                  }}
                >
                  Advanced Search
                </button>
              </div>
            </div>
            
            <button
              onClick={() => {
                setIsFullscreen(false);
                setShowAdvancedSearch(false);
                setSimpleSearchResults(null);
                setAdvancedSearchResults(null);
              }}
              style={{
                background: "transparent",
                border: "none",
                fontSize: "24px",
                cursor: "pointer",
                padding: "4px 8px",
                color: "#666",
              }}
            >
              ✕
            </button>
          </div>

          {/* Advanced Search Panel */}
          {showAdvancedSearch && (
            <div
              style={{
                padding: "20px 24px",
                backgroundColor: "#f8f9fa",
                borderBottom: "1px solid #ddd",
                maxHeight: "40vh",
                overflowY: "auto",
              }}
            >
              <div style={{ marginBottom: "20px" }}>
                <h4 style={{ margin: "0 0 8px 0", fontSize: "18px", fontWeight: "600", color: "#1B1F3B" }}>
                  Advanced Search
                </h4>
                <p style={{ margin: "0 0 20px 0", fontSize: "14px", color: "#666" }}>
                  Construct structured queries to find exactly what you're looking for.
                </p>
              </div>

              <div
                style={{
                  background: "#FFFFFF",
                  border: "1px solid #ddd",
                  borderRadius: "8px",
                  padding: "20px",
                  marginBottom: "16px",
                }}
              >
                {searchCriteria.map((criteria, index) => (
                  <div key={criteria.id}>
                    <div
                      style={{
                        display: "grid",
                        gridTemplateColumns: "1fr auto 1fr auto",
                        gap: "12px",
                        alignItems: "center",
                        marginBottom: "16px",
                      }}
                    >
                      {/* Property Field */}
                      <div style={{ display: "flex", flexDirection: "column", position: "relative" }}>
                        <label style={{ marginBottom: "6px", fontSize: "13px", fontWeight: "500", color: "#666" }}>
                          Property
                        </label>
                        <input
                          type="text"
                          value={criteria.property}
                          onChange={(e) => handleCriteriaChange(criteria.id, 'property', e.target.value)}
                          onFocus={() => handlePropertyInputFocus(criteria.id)}
                          onBlur={() => setTimeout(() => setShowPropertyDropdown(prev => ({ ...prev, [criteria.id]: false })), 200)}
                          placeholder="Click to select property..."
                          readOnly={loadingProperties}
                          style={{
                            padding: "8px 12px",
                            border: "1px solid #ddd",
                            borderRadius: "4px",
                            background: "#FFFFFF",
                            color: "#1B1F3B",
                            fontSize: "14px",
                            cursor: "pointer",
                          }}
                        />
                        {showPropertyDropdown[criteria.id] && availableProperties.length > 0 && (
                          <div style={{
                            position: 'absolute',
                            top: '100%',
                            left: 0,
                            right: 0,
                            backgroundColor: '#fff',
                            border: '1px solid #ddd',
                            borderRadius: '4px',
                            maxHeight: '200px',
                            overflowY: 'auto',
                            zIndex: 1000,
                            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                          }}>
                            {availableProperties
                              .filter(prop => 
                                !criteria.property || 
                                prop.property_label.toLowerCase().includes(criteria.property.toLowerCase()) ||
                                prop.property_id.toLowerCase().includes(criteria.property.toLowerCase())
                              )
                              .map(prop => (
                                <div
                                  key={prop.property_id}
                                  onClick={() => handlePropertySelect(criteria.id, prop)}
                                  style={{
                                    padding: '8px 12px',
                                    cursor: 'pointer',
                                    borderBottom: '1px solid #f0f0f0',
                                    transition: 'background 0.2s'
                                  }}
                                  onMouseEnter={(e) => e.currentTarget.style.background = '#f5f5f5'}
                                  onMouseLeave={(e) => e.currentTarget.style.background = '#fff'}
                                >
                                  <div style={{ fontWeight: '500', color: '#1B1F3B' }}>
                                    {prop.property_label}
                                  </div>
                                  <div style={{ fontSize: '12px', color: '#666' }}>
                                    {prop.property_id} • {prop.count} {prop.count === 1 ? 'item' : 'items'} • {prop.source}
                                  </div>
                                </div>
                              ))}
                          </div>
                        )}
                        {loadingProperties && (
                          <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                            Loading properties...
                          </div>
                        )}
                      </div>

                      {/* Operator Field */}
                      <div
                        style={{
                          paddingTop: "22px",
                          fontSize: "13px",
                          color: "#666",
                          textAlign: "center",
                        }}
                      >
                        is
                      </div>

                      {/* Value Field */}
                      <div style={{ display: "flex", flexDirection: "column", position: "relative" }}>
                        <label style={{ marginBottom: "6px", fontSize: "13px", fontWeight: "500", color: "#666" }}>
                          Value
                        </label>
                        <input
                          type="text"
                          value={criteria.value}
                          onChange={(e) => handleCriteriaChange(criteria.id, 'value', e.target.value)}
                          onFocus={() => handleValueInputFocus(criteria.id, criteria.propertyId)}
                          onBlur={() => setTimeout(() => setShowValueDropdown(prev => ({ ...prev, [criteria.id]: false })), 200)}
                          placeholder={criteria.propertyId ? "Click to select value..." : "Select property first"}
                          disabled={!criteria.propertyId}
                          readOnly={loadingValues[criteria.id]}
                          style={{
                            padding: "8px 12px",
                            border: "1px solid #ddd",
                            borderRadius: "4px",
                            background: "#FFFFFF",
                            color: "#1B1F3B",
                            fontSize: "14px",
                          }}
                        />
                        {showValueDropdown[criteria.id] && availableValues[criteria.id] && availableValues[criteria.id].length > 0 && (
                          <div style={{
                            position: 'absolute',
                            top: '100%',
                            left: 0,
                            right: 0,
                            backgroundColor: '#fff',
                            border: '1px solid #ddd',
                            borderRadius: '4px',
                            maxHeight: '200px',
                            overflowY: 'auto',
                            zIndex: 1000,
                            boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                          }}>
                            {availableValues[criteria.id]
                              .filter(valueItem => 
                                !criteria.value || 
                                (valueItem.value_text && valueItem.value_text.toLowerCase().includes(criteria.value.toLowerCase()))
                              )
                              .map((valueItem, idx) => (
                                <div
                                  key={`${valueItem.value_id}-${idx}`}
                                  onClick={() => handleValueSelect(criteria.id, valueItem)}
                                  style={{
                                    padding: '8px 12px',
                                    cursor: 'pointer',
                                    borderBottom: '1px solid #f0f0f0',
                                    transition: 'background 0.2s'
                                  }}
                                  onMouseEnter={(e) => e.currentTarget.style.background = '#f5f5f5'}
                                  onMouseLeave={(e) => e.currentTarget.style.background = '#fff'}
                                >
                                  <div style={{ fontWeight: '500', color: '#1B1F3B' }}>
                                    {valueItem.value_text || valueItem.value_id || 'Unknown'}
                                  </div>
                                  <div style={{ fontSize: '12px', color: '#666' }}>
                                    {valueItem.count} {valueItem.count === 1 ? 'occurrence' : 'occurrences'}
                                  </div>
                                </div>
                              ))}
                          </div>
                        )}
                        {loadingValues[criteria.id] && (
                          <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                            Loading values...
                          </div>
                        )}
                      </div>

                      {/* Delete Button */}
                      {searchCriteria.length > 1 && (
                        <button
                          onClick={() => handleRemoveCriteria(criteria.id)}
                          title="Remove criteria"
                          style={{
                            padding: "8px",
                            marginTop: "22px",
                            background: "transparent",
                            border: "none",
                            color: "#BD4902",
                            cursor: "pointer",
                            fontSize: "18px",
                          }}
                        >
                          ❌
                        </button>
                      )}
                    </div>

                    {/* Logical Operator Row */}
                    {index < searchCriteria.length - 1 && (
                      <div style={{ display: "flex", gap: "8px", alignItems: "center", marginBottom: "16px" }}>
                        <button
                          onClick={() => handleToggleLogicalOp(criteria.id)}
                          style={{
                            padding: "6px 16px",
                            border: "1px solid #ddd",
                            borderRadius: "4px",
                            fontSize: "13px",
                            fontWeight: "600",
                            cursor: "pointer",
                            background: criteria.logicalOp === 'AND' ? "#2D6A4F" : "#FFFFFF",
                            color: criteria.logicalOp === 'AND' ? "#FFFFFF" : "#999",
                            borderColor: criteria.logicalOp === 'AND' ? "#2D6A4F" : "#ddd",
                          }}
                        >
                          AND
                        </button>
                        <button
                          onClick={() => handleToggleLogicalOp(criteria.id)}
                          style={{
                            padding: "6px 16px",
                            border: "1px solid #ddd",
                            borderRadius: "4px",
                            fontSize: "13px",
                            fontWeight: "600",
                            cursor: "pointer",
                            background: criteria.logicalOp === 'OR' ? "#F57C00" : "#FFFFFF",
                            color: criteria.logicalOp === 'OR' ? "#FFFFFF" : "#999",
                            borderColor: criteria.logicalOp === 'OR' ? "#F57C00" : "#ddd",
                          }}
                        >
                          OR
                        </button>
                      </div>
                    )}
                  </div>
                ))}

                <button
                  onClick={handleAddCriteria}
                  style={{
                    width: "100%",
                    padding: "12px",
                    background: "#FFFFFF",
                    border: "2px dashed #ddd",
                    borderRadius: "4px",
                    color: "#666",
                    fontSize: "14px",
                    fontWeight: "500",
                    cursor: "pointer",
                    marginBottom: "20px",
                  }}
                >
                  + Add new property search
                </button>
              </div>

              <div style={{ display: "flex", justifyContent: "flex-end", gap: "12px" }}>
                <button
                  onClick={handleClearSearch}
                  disabled={searchingQuery}
                  style={{
                    padding: "10px 24px",
                    background: "#FFFFFF",
                    color: "#666",
                    border: "1px solid #ddd",
                    borderRadius: "4px",
                    fontSize: "14px",
                    fontWeight: "600",
                    cursor: "pointer",
                  }}
                >
                  Clear All
                </button>
                <button
                  onClick={handleAdvancedSearch}
                  disabled={searchingQuery}
                  style={{
                    padding: "10px 24px",
                    background: "#0076B5",
                    color: "#FFFFFF",
                    border: "none",
                    borderRadius: "4px",
                    fontSize: "14px",
                    fontWeight: "600",
                    cursor: searchingQuery ? "not-allowed" : "pointer",
                    opacity: searchingQuery ? 0.6 : 1,
                  }}
                >
                  {searchingQuery ? 'Searching...' : 'Search'}
                </button>
              </div>
            </div>
          )}

          {/* Search Results Panel */}
          {(simpleSearchResults || advancedSearchResults) && (
            <div
              style={{
                padding: "20px 24px",
                backgroundColor: "#fff",
                borderBottom: "1px solid #ddd",
                maxHeight: "30vh",
                overflowY: "auto",
              }}
            >
              <h4 style={{ margin: "0 0 16px 0", color: "#1B1F3B" }}>
                Search Results
                {simpleSearchQuery && ` for "${simpleSearchQuery}"`}
              </h4>
              
              {/* Display results */}
              {(simpleSearchResults || advancedSearchResults) && (
                <div>
                  <div style={{ marginBottom: "16px" }}>
                    <h5 style={{ color: "#0076B5", marginBottom: "8px" }}>
                      Nodes ({(simpleSearchResults?.nodes || advancedSearchResults?.nodes || []).length})
                    </h5>
                    <div style={{ display: "grid", gap: "8px", gridTemplateColumns: "repeat(auto-fill, minmax(250px, 1fr))" }}>
                      {(simpleSearchResults?.nodes || advancedSearchResults?.nodes || []).map(node => (
                        <div
                          key={node.id}
                          onClick={() => onNodeClick && onNodeClick(null, { id: node.id.toString() })}
                          style={{
                            padding: "8px 12px",
                            backgroundColor: "#f8f9fa",
                            borderRadius: "4px",
                            border: "1px solid #e0e0e0",
                            cursor: "pointer",
                            fontSize: "14px",
                          }}
                        >
                          <strong>{node.label}</strong>
                          {node.wikidata_id && (
                            <div style={{ fontSize: "12px", color: "#666" }}>{node.wikidata_id}</div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>

                  <div>
                    <h5 style={{ color: "#2D6A4F", marginBottom: "8px" }}>
                      Edges ({(simpleSearchResults?.edges || advancedSearchResults?.edges || []).length})
                    </h5>
                    <div style={{ display: "grid", gap: "8px", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))" }}>
                      {(simpleSearchResults?.edges || advancedSearchResults?.edges || []).map(edge => {
                        const sourceNode = localNodes.find(n => n.id === edge.source.toString()) || 
                                         nodesWithSizes?.find(n => n.id === edge.source.toString());
                        const targetNode = localNodes.find(n => n.id === edge.target.toString()) || 
                                         nodesWithSizes?.find(n => n.id === edge.target.toString());
                        return (
                          <div
                            key={edge.id}
                            onClick={() => onEdgeClick && onEdgeClick(null, { id: edge.id })}
                            style={{
                              padding: "8px 12px",
                              backgroundColor: "#f8f9fa",
                              borderRadius: "4px",
                              border: "1px solid #e0e0e0",
                              cursor: "pointer",
                              fontSize: "14px",
                            }}
                          >
                            <div>
                              <strong>{sourceNode?.data?.label || `Node ${edge.source}`}</strong>
                              {" → "}
                              <strong>{targetNode?.data?.label || `Node ${edge.target}`}</strong>
                            </div>
                            <div style={{ fontSize: "12px", color: "#666", fontStyle: "italic" }}>
                              {edge.label || edge.relation_property || "No label"}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Fullscreen Graph */}
          <div style={{ flex: 1, position: "relative" }}>
            <ReactFlow 
              nodes={localNodes.length > 0 ? localNodes : (nodesWithSizes || [])} 
              edges={processedEdges || []} 
              nodeTypes={nodeTypes}
              edgeTypes={edgeTypes}
              onNodesChange={onNodesChange}
              onNodeClick={onNodeClick}
              onEdgeClick={onEdgeClick}
              nodesDraggable={true}
              nodesConnectable={false}
              elementsSelectable={true}
              fitView
              connectionLineStyle={{
                strokeWidth: 4,
                stroke: '#3A0CA3',
              }}
            >
              <Background />
              <Controls />
            </ReactFlow>
          </div>
        </div>
      )}
    </>
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
  spaceId: PropTypes.string,
  showFullscreenButton: PropTypes.bool,
};

export default SpaceGraph;
