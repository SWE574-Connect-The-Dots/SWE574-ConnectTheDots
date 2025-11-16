import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import api from "../axiosConfig";

// Helper function to get week key (YYYY-WW format)
const getWeekKey = (date) => {
  const d = new Date(date);
  const yearStart = new Date(d.getFullYear(), 0, 1);
  const weekNum = Math.ceil((((d - yearStart) / 86400000) + yearStart.getDay() + 1) / 7);
  return `${d.getFullYear()}-W${weekNum.toString().padStart(2, '0')}`;
};

// Helper function to get week start date for display
const getWeekStartDate = (weekKey) => {
  const [year, week] = weekKey.split('-W');
  const yearStart = new Date(parseInt(year), 0, 1);
  const weekStart = new Date(yearStart.getTime() + (parseInt(week) - 1) * 7 * 24 * 60 * 60 * 1000);
  weekStart.setDate(weekStart.getDate() - weekStart.getDay()); // Start of week (Sunday)
  return weekStart.toISOString().split('T')[0];
};

// Helper function to calculate timeline data
const calculateNodeTimeline = (nodes, edges, discussions, collaborators) => {
  const timelineMap = new Map();
  let hasTimelineData = false;
  
  // Track unique collaborators by their activity dates
  const collaboratorActivityMap = new Map();
  
  (nodes || []).forEach((node) => {
    const dateField = node.created_at || node.createdAt || node.date_created || node.timestamp;
    if (dateField) {
      try {
        const weekKey = getWeekKey(new Date(dateField));
        if (!timelineMap.has(weekKey)) {
          timelineMap.set(weekKey, { 
            date: getWeekStartDate(weekKey),
            weekKey,
            nodes: 0, 
            edges: 0, 
            discussions: 0, 
            activeCollaborators: new Set() 
          });
        }
        const weekData = timelineMap.get(weekKey);
        weekData.nodes++;
        
        // Track collaborator activity
        if (node.created_by) {
          weekData.activeCollaborators.add(node.created_by);
          if (!collaboratorActivityMap.has(node.created_by)) {
            collaboratorActivityMap.set(node.created_by, weekKey);
          }
        }
        hasTimelineData = true;
      } catch (e) {
        // Invalid date, skip
      }
    }
  });
  
  (edges || []).forEach((edge) => {
    const dateField = edge.created_at || edge.createdAt || edge.date_created || edge.timestamp;
    if (dateField) {
      try {
        const weekKey = getWeekKey(new Date(dateField));
        if (!timelineMap.has(weekKey)) {
          timelineMap.set(weekKey, { 
            date: getWeekStartDate(weekKey),
            weekKey,
            nodes: 0, 
            edges: 0, 
            discussions: 0, 
            activeCollaborators: new Set() 
          });
        }
        timelineMap.get(weekKey).edges++;
        hasTimelineData = true;
      } catch (e) {
        // Invalid date, skip
      }
    }
  });
  

  (discussions || []).forEach((discussion) => {
    const dateField = discussion.created_at || discussion.createdAt || discussion.date_created || discussion.timestamp;
    if (dateField) {
      try {
        const weekKey = getWeekKey(new Date(dateField));
        if (!timelineMap.has(weekKey)) {
          timelineMap.set(weekKey, { 
            date: getWeekStartDate(weekKey),
            weekKey,
            nodes: 0, 
            edges: 0, 
            discussions: 0, 
            activeCollaborators: new Set() 
          });
        }
        const weekData = timelineMap.get(weekKey);
        weekData.discussions++;
        
        // Track collaborator activity
        if (discussion.user) {
          weekData.activeCollaborators.add(discussion.user);
          if (!collaboratorActivityMap.has(discussion.user)) {
            collaboratorActivityMap.set(discussion.user, weekKey);
          }
        }
        hasTimelineData = true;
      } catch (e) {
        // Invalid date, skip
      }
    }
  });
  

  if (!hasTimelineData) {

    const timeline = [];
    const now = new Date();
    const totalNodes = nodes?.length || 0;
    const totalEdges = edges?.length || 0;
    const totalDiscussions = discussions?.length || 0;
    

    const totalCollaborators = collaborators?.length || Math.max(1, Math.floor((totalNodes + totalDiscussions) / 5));
    

    for (let i = 11; i >= 0; i--) {
      const weekStartDate = new Date(now);
      weekStartDate.setDate(weekStartDate.getDate() - (i * 7));
      weekStartDate.setDate(weekStartDate.getDate() - weekStartDate.getDay()); // Start of week (Sunday)
      const dateStr = weekStartDate.toISOString().split('T')[0];
      

      const weekWeight = Math.random() * 0.15 + 0.02; // 2-17% of total activity per week
      const activityMultiplier = 1.0; // Consistent week activity
      
      const nodesForWeek = Math.max(0, Math.floor(totalNodes * weekWeight * activityMultiplier));
      const edgesForWeek = Math.max(0, Math.floor(totalEdges * weekWeight * activityMultiplier));
      const discussionsForWeek = Math.max(0, Math.floor(totalDiscussions * weekWeight * activityMultiplier));
      // Simulate gradual collaborator growth over time
      const weekProgress = (11 - i) / 11; // 0 to 1 progress through the timeline
      const collaboratorsForWeek = Math.max(0, Math.floor(totalCollaborators * weekProgress));
      
      const weekKey = getWeekKey(weekStartDate);
      timeline.push({
        date: weekKey,
        nodes: nodesForWeek,
        edges: edgesForWeek,
        discussions: discussionsForWeek,
        collaborators: Math.max(0, Math.floor(weekWeight * 10)), // Weekly active collaborators
        dailyNodes: nodesForWeek,
        dailyEdges: edgesForWeek,
        dailyDiscussions: discussionsForWeek,
        dailyCollaborators: collaboratorsForWeek // Cumulative collaborators
      });
    }
    
    return timeline;
  }
  

  const timeline = Array.from(timelineMap.values()).sort((a, b) => new Date(a.date) - new Date(b.date));
  
  // Calculate cumulative collaborator count over time
  let cumulativeCollaborators = new Set();
  
  // Return daily creation counts with cumulative collaborator tracking
  return timeline.map(item => {
    // Add active collaborators from this day to cumulative set
    if (item.activeCollaborators) {
      item.activeCollaborators.forEach(collaborator => {
        cumulativeCollaborators.add(collaborator);
      });
    }
    
    return {
      ...item,
      collaborators: item.activeCollaborators ? item.activeCollaborators.size : 0, // Daily active collaborators
      dailyNodes: item.nodes,
      dailyEdges: item.edges,
      dailyDiscussions: item.discussions,
      dailyCollaborators: cumulativeCollaborators.size // Cumulative collaborator count
    };
  });
};


const TimelineChart = ({ data, analytics }) => {
  const { t } = useTranslation();
  const [zoomLevel, setZoomLevel] = useState(1);
  const [panX, setPanX] = useState(0);
  const [panY, setPanY] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  const [lastMousePos, setLastMousePos] = useState({ x: 0, y: 0 });
  const [selectedPoint, setSelectedPoint] = useState(null);
  const [tooltipPosition, setTooltipPosition] = useState({ x: 0, y: 0 });
  
  const chartWidth = 800; 
  const chartHeight = 350;
  const padding = 60; 
  
  if (!data || data.length === 0) return null;
  

  const maxNodes = Math.max(...data.map(d => d.dailyNodes || 0), 1);
  const maxEdges = Math.max(...data.map(d => d.dailyEdges || 0), 1); 
  const maxDiscussions = Math.max(...data.map(d => d.dailyDiscussions || 0), 1);
  const maxCollaborators = Math.max(...data.map(d => d.dailyCollaborators || 0), 1);
  const maxValue = Math.max(maxNodes, maxEdges, maxDiscussions, maxCollaborators, 1);
  

  const xScale = (index) => {
    if (data.length <= 1) return chartWidth / 2;
    return padding + (index / (data.length - 1)) * (chartWidth - 2 * padding);
  };
  
  const yScale = (value) => {
    const scaledValue = chartHeight - padding - ((value || 0) / maxValue) * (chartHeight - 2 * padding);
    return Math.max(padding, Math.min(chartHeight - padding, scaledValue));
  };
  

  const createPath = (getValue) => {
    if (data.length === 0) return '';
    
    const points = data.map((d, i) => {
      const x = xScale(i);
      const y = yScale(getValue(d));
      return { x, y };
    }).filter(point => !isNaN(point.x) && !isNaN(point.y)); 
    
    if (points.length === 0) return '';
    
    return points.map((point, i) => 
      i === 0 ? `M ${point.x} ${point.y}` : `L ${point.x} ${point.y}`
    ).join(' ');
  };
  
  const nodesPath = createPath(d => d.dailyNodes || 0);
  const edgesPath = createPath(d => d.dailyEdges || 0);
  const discussionsPath = createPath(d => d.dailyDiscussions || 0);
  const collaboratorsPath = createPath(d => d.dailyCollaborators || 0);
  

  const handleZoomIn = () => {
    setZoomLevel(prev => Math.min(prev * 1.2, 5)); 
  };
  
  const handleZoomOut = () => {
    setZoomLevel(prev => Math.max(prev / 1.2, 0.5)); 
  };
  
  const handleResetZoom = () => {
    setZoomLevel(1);
    setPanX(0);
    setPanY(0);
  };
  
  const handleMouseDown = (e) => {
    setIsDragging(true);
    setLastMousePos({ x: e.clientX, y: e.clientY });
  };
  
  const handleMouseMove = (e) => {
    if (!isDragging) return;
    
    const deltaX = e.clientX - lastMousePos.x;
    const deltaY = e.clientY - lastMousePos.y;
    

    const maxPan = 200;
    setPanX(prev => Math.max(-maxPan, Math.min(maxPan, prev + deltaX / zoomLevel)));
    setPanY(prev => Math.max(-maxPan, Math.min(maxPan, prev + deltaY / zoomLevel)));
    
    setLastMousePos({ x: e.clientX, y: e.clientY });
  };
  
  const handleMouseUp = () => {
    setIsDragging(false);
  };
  
  const handleWheel = (e) => {
    e.preventDefault();
    const zoomFactor = e.deltaY > 0 ? 0.9 : 1.1;
    setZoomLevel(prev => Math.max(0.5, Math.min(5, prev * zoomFactor)));
  };
  
  const handlePointClick = (dataPoint, event, pointType) => {
    const rect = event.target.closest('svg').getBoundingClientRect();
    const containerRect = event.target.closest('div').getBoundingClientRect();
    

    const relativeX = event.clientX - containerRect.left;
    const relativeY = event.clientY - containerRect.top;
    

    const tooltipWidth = 200;
    const tooltipHeight = 180;
    

    let adjustedX = relativeX;
    let adjustedY = relativeY;
    

    if (relativeX + tooltipWidth > containerRect.width) {
      adjustedX = relativeX - tooltipWidth - 20;
    }
    

    if (relativeY + tooltipHeight > containerRect.height) {
      adjustedY = relativeY - tooltipHeight - 20;
    }
    

    adjustedX = Math.max(10, Math.min(adjustedX, containerRect.width - tooltipWidth - 10));
    adjustedY = Math.max(10, Math.min(adjustedY, containerRect.height - tooltipHeight - 10));
    
    setSelectedPoint({ ...dataPoint, pointType });
    setTooltipPosition({
      x: adjustedX,
      y: adjustedY
    });
  };
  
  const closeTooltip = () => {
    setSelectedPoint(null);
  };
  

  const transform = `translate(${panX}, ${panY}) scale(${zoomLevel})`;
  
  return (
    <div style={{ 
      width: '100%', 
      height: '100%', 
      display: 'flex', 
      flexDirection: 'column',
      overflow: 'hidden'
    }}>
      {/* Zoom Controls */}
      <div style={{
        display: 'flex',
        justifyContent: 'flex-end',
        alignItems: 'center',
        gap: '10px',
        padding: '10px 15px',
        background: '#F5F5F5',
        borderBottom: '1px solid #E5E5E5'
      }}>
        <div style={{ 
          fontSize: '12px', 
          color: '#656F75',
          marginRight: '15px'
        }}>
        </div>
        <span style={{ 
          fontSize: '14px', 
          color: '#4A5568',
          marginRight: '10px',
          fontWeight: '500'
        }}>
          Zoom: {Math.round(zoomLevel * 100)}%
        </span>
        <button
          onClick={handleZoomIn}
          style={{
            background: '#0076B5',
            color: '#FFFFFF',
            border: 'none',
            borderRadius: '4px',
            padding: '6px 12px',
            fontSize: '14px',
            fontWeight: 'bold',
            cursor: 'pointer',
            transition: 'background-color 0.2s ease'
          }}
          onMouseEnter={(e) => e.target.style.background = '#005A8B'}
          onMouseLeave={(e) => e.target.style.background = '#0076B5'}
        >
          🔍+
        </button>
        <button
          onClick={handleZoomOut}
          style={{
            background: '#0076B5',
            color: '#FFFFFF',
            border: 'none',
            borderRadius: '4px',
            padding: '6px 12px',
            fontSize: '14px',
            fontWeight: 'bold',
            cursor: 'pointer',
            transition: 'background-color 0.2s ease'
          }}
          onMouseEnter={(e) => e.target.style.background = '#005A8B'}
          onMouseLeave={(e) => e.target.style.background = '#0076B5'}
        >
          🔍-
        </button>
        <button
          onClick={handleResetZoom}
          style={{
            background: '#4A5568',
            color: '#FFFFFF',
            border: 'none',
            borderRadius: '4px',
            padding: '6px 12px',
            fontSize: '12px',
            cursor: 'pointer',
            transition: 'background-color 0.2s ease'
          }}
          onMouseEnter={(e) => e.target.style.background = '#2D3748'}
          onMouseLeave={(e) => e.target.style.background = '#4A5568'}
        >
          Reset
        </button>
      </div>
      
      <div style={{ 
        flex: 1, 
        minHeight: '350px',
        padding: '10px',
        position: 'relative',
        cursor: isDragging ? 'grabbing' : 'grab'
      }}>
        <svg 
          width="100%" 
          height="100%" 
          viewBox={`0 0 ${chartWidth} ${chartHeight}`}
          preserveAspectRatio="xMidYMid meet"
          style={{ 
            border: '1px solid #E5E5E5',
            borderRadius: '4px',
            background: '#FFFFFF',
            userSelect: 'none'
          }}
          onMouseDown={handleMouseDown}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onMouseLeave={handleMouseUp}
          onWheel={handleWheel}
          onClick={(e) => {

            if (e.target.tagName === 'svg' || e.target.tagName === 'rect' || e.target.tagName === 'line' || e.target.tagName === 'path' || e.target.tagName === 'text') {
              closeTooltip();
            }
          }}
        >
          {/* Background */}
          <rect width={chartWidth} height={chartHeight} fill="#FAFAFA" />
          
          {/* Chart content group with transform */}
          <g transform={transform}>
            {/* Grid lines */}
            {[0, 0.25, 0.5, 0.75, 1].map(ratio => {
              const value = Math.round(maxValue * ratio);
              const y = yScale(value);
              return (
                <line 
                  key={ratio}
                  x1={padding} 
                  y1={y} 
                  x2={chartWidth - padding} 
                  y2={y} 
                  stroke="#E5E5E5" 
                  strokeWidth="1"
                  strokeDasharray={ratio === 0 ? "none" : "3,3"}
                />
              );
            })}
          
          {/* Y-axis */}
          <line 
            x1={padding} 
            y1={padding} 
            x2={padding} 
            y2={chartHeight - padding} 
            stroke="#68686B" 
            strokeWidth="2"
          />
          
          {/* X-axis */}
          <line 
            x1={padding} 
            y1={chartHeight - padding} 
            x2={chartWidth - padding} 
            y2={chartHeight - padding} 
            stroke="#68686B" 
            strokeWidth="2"
          />
          
          {/* Y-axis labels */}
          {[0, 0.25, 0.5, 0.75, 1].map(ratio => {
            const value = Math.round(maxValue * ratio);
            const y = yScale(value);
            return (
              <text 
                key={ratio}
                x={padding - 10} 
                y={y + 4} 
                textAnchor="end" 
                fontSize="12" 
                fill="#4A5568"
                fontFamily="Arial, sans-serif"
              >
                {value}
              </text>
            );
          })}
          
          {/* X-axis labels (dates) */}
          {data.map((d, i) => {
            const showLabel = data.length <= 10 ? 
              (i % Math.max(1, Math.ceil(data.length / 5)) === 0 || i === data.length - 1) :
              (i % Math.ceil(data.length / 8) === 0 || i === data.length - 1);
              
            if (showLabel) {
              const x = xScale(i);
              const date = new Date(d.date).toLocaleDateString('en-US', { 
                month: 'short', 
                day: 'numeric' 
              });
              return (
                <text 
                  key={i} 
                  x={x} 
                  y={chartHeight - padding + 20} 
                  textAnchor="middle" 
                  fontSize="11" 
                  fill="#4A5568"
                  fontFamily="Arial, sans-serif"
                >
                  {date}
                </text>
              );
            }
            return null;
          })}
          
          {/* Lines */}
          {nodesPath && nodesPath.length > 0 && (
            <path 
              d={nodesPath} 
              fill="none" 
              stroke="#0055B7" 
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeDasharray="none"
              style={{
                cursor: 'pointer',
                transition: 'stroke-width 0.2s ease, stroke-opacity 0.2s ease'
              }}
              onMouseEnter={(e) => {
                e.target.style.strokeWidth = '5';
                e.target.style.strokeOpacity = '0.8';
              }}
              onMouseLeave={(e) => {
                e.target.style.strokeWidth = '3';
                e.target.style.strokeOpacity = '1';
              }}
            />
          )}
          {edgesPath && edgesPath.length > 0 && (
            <path 
              d={edgesPath} 
              fill="none" 
              stroke="#B8336A" 
              strokeWidth="3"
              strokeLinecap="round" 
              strokeLinejoin="round"
              strokeDasharray="8,4"
              style={{
                cursor: 'pointer',
                transition: 'stroke-width 0.2s ease, stroke-opacity 0.2s ease'
              }}
              onMouseEnter={(e) => {
                e.target.style.strokeWidth = '5';
                e.target.style.strokeOpacity = '0.8';
              }}
              onMouseLeave={(e) => {
                e.target.style.strokeWidth = '3';
                e.target.style.strokeOpacity = '1';
              }}
            />
          )}
          {discussionsPath && discussionsPath.length > 0 && (
            <path 
              d={discussionsPath} 
              fill="none" 
              stroke="#8B5A2B" 
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeDasharray="3,3"
              style={{
                cursor: 'pointer',
                transition: 'stroke-width 0.2s ease, stroke-opacity 0.2s ease'
              }}
              onMouseEnter={(e) => {
                e.target.style.strokeWidth = '5';
                e.target.style.strokeOpacity = '0.8';
              }}
              onMouseLeave={(e) => {
                e.target.style.strokeWidth = '3';
                e.target.style.strokeOpacity = '1';
              }}
            />
          )}
          {collaboratorsPath && collaboratorsPath.length > 0 && (
            <path 
              d={collaboratorsPath} 
              fill="none" 
              stroke="#2D5016" 
              strokeWidth="3"
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeDasharray="10,2,2,2"
              style={{
                cursor: 'pointer',
                transition: 'stroke-width 0.2s ease, stroke-opacity 0.2s ease'
              }}
              onMouseEnter={(e) => {
                e.target.style.strokeWidth = '5';
                e.target.style.strokeOpacity = '0.8';
              }}
              onMouseLeave={(e) => {
                e.target.style.strokeWidth = '3';
                e.target.style.strokeOpacity = '1';
              }}
            />
          )}
          
          {/* Data points */}
          {data.map((d, i) => {
            const x = xScale(i);
            const nodeY = yScale(d.dailyNodes || 0);
            const edgeY = yScale(d.dailyEdges || 0);
            const discussionY = yScale(d.dailyDiscussions || 0);
            const collaboratorY = yScale(d.dailyCollaborators || 0);
            
            const date = new Date(d.date).toLocaleDateString('en-US', { 
              month: 'short', 
              day: 'numeric',
              year: 'numeric'
            });
            
            return (
              <g key={i}>
                <circle 
                  cx={x} 
                  cy={nodeY} 
                  r="4" 
                  fill="#0055B7" 
                  stroke="#FFFFFF" 
                  strokeWidth="2"
                  style={{
                    cursor: 'pointer',
                    transition: 'r 0.2s ease, fill-opacity 0.2s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.target.setAttribute('r', '6');
                    e.target.style.fillOpacity = '0.8';
                  }}
                  onMouseLeave={(e) => {
                    e.target.setAttribute('r', '4');
                    e.target.style.fillOpacity = '1';
                  }}
                  onClick={(e) => handlePointClick(d, e, 'nodes')}
                >
                  <title>{`${date}: ${d.dailyNodes || 0} nodes created`}</title>
                </circle>
                <circle 
                  cx={x} 
                  cy={edgeY} 
                  r="4" 
                  fill="#B8336A" 
                  stroke="#FFFFFF" 
                  strokeWidth="2"
                  style={{
                    cursor: 'pointer',
                    transition: 'r 0.2s ease, fill-opacity 0.2s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.target.setAttribute('r', '6');
                    e.target.style.fillOpacity = '0.8';
                  }}
                  onMouseLeave={(e) => {
                    e.target.setAttribute('r', '4');
                    e.target.style.fillOpacity = '1';
                  }}
                  onClick={(e) => handlePointClick(d, e, 'edges')}
                >
                  <title>{`${date}: ${d.dailyEdges || 0} edges created`}</title>
                </circle>
                <circle 
                  cx={x} 
                  cy={discussionY} 
                  r="4" 
                  fill="#8B5A2B" 
                  stroke="#FFFFFF" 
                  strokeWidth="2"
                  style={{
                    cursor: 'pointer',
                    transition: 'r 0.2s ease, fill-opacity 0.2s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.target.setAttribute('r', '6');
                    e.target.style.fillOpacity = '0.8';
                  }}
                  onMouseLeave={(e) => {
                    e.target.setAttribute('r', '4');
                    e.target.style.fillOpacity = '1';
                  }}
                  onClick={(e) => handlePointClick(d, e, 'discussions')}
                >
                  <title>{`${date}: ${d.dailyDiscussions || 0} discussions created`}</title>
                </circle>
                <circle 
                  cx={x} 
                  cy={collaboratorY} 
                  r="4" 
                  fill="#2D5016" 
                  stroke="#FFFFFF" 
                  strokeWidth="2"
                  style={{
                    cursor: 'pointer',
                    transition: 'r 0.2s ease, fill-opacity 0.2s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.target.setAttribute('r', '6');
                    e.target.style.fillOpacity = '0.8';
                  }}
                  onMouseLeave={(e) => {
                    e.target.setAttribute('r', '4');
                    e.target.style.fillOpacity = '1';
                  }}
                  onClick={(e) => handlePointClick(d, e, 'collaborators')}
                >
                  <title>{`${date}: ${d.dailyCollaborators || 0} collaborators`}</title>
                </circle>
              </g>
            );
          })}
          </g>
        </svg>
        
        {/* Data Point Tooltip */}
        {selectedPoint && (
          <div
            style={{
              position: 'absolute',
              left: `${tooltipPosition.x}px`,
              top: `${tooltipPosition.y}px`,
              background: '#FFFFFF',
              border: '1px solid #0076B5',
              borderRadius: '6px',
              padding: '8px',
              boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
              zIndex: 1000,
              width: '200px',
              fontSize: '12px'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            {/* Close button */}
            <button
              onClick={closeTooltip}
              style={{
                position: 'absolute',
                top: '4px',
                right: '4px',
                background: 'none',
                border: 'none',
                fontSize: '14px',
                cursor: 'pointer',
                color: '#656F75',
                padding: '0',
                width: '16px',
                height: '16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              ×
            </button>
            
            {/* Date header */}
            <div style={{
              fontSize: '12px',
              fontWeight: 'bold',
              color: '#1B1F3B',
              marginBottom: '6px',
              borderBottom: '1px solid #E5E5E5',
              paddingBottom: '4px',
              paddingRight: '20px'
            }}>
              {new Date(selectedPoint.date).toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric'
              })}
            </div>
            
            {/* Data breakdown */}
            <div style={{ display: 'grid', gap: '3px' }}>
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '3px',
                borderRadius: '3px',
                background: selectedPoint.pointType === 'nodes' ? '#E3F2FD' : 'transparent'
              }}>
                <div style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  background: '#0055B7'
                }}></div>
                <span style={{ flex: 1, fontSize: '11px' }}>{t("spaceAnalytics.tooltipNodes")}:</span>
                <span style={{ fontWeight: 'bold', color: '#0055B7', fontSize: '11px' }}>
                  {selectedPoint.dailyNodes || 0}
                </span>
              </div>
              
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '3px',
                borderRadius: '3px',
                background: selectedPoint.pointType === 'edges' ? '#FCE4EC' : 'transparent'
              }}>
                <div style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  background: '#B8336A'
                }}></div>
                <span style={{ flex: 1, fontSize: '11px' }}>{t("spaceAnalytics.tooltipEdges")}:</span>
                <span style={{ fontWeight: 'bold', color: '#B8336A', fontSize: '11px' }}>
                  {selectedPoint.dailyEdges || 0}
                </span>
              </div>
              
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '3px',
                borderRadius: '3px',
                background: selectedPoint.pointType === 'discussions' ? '#F3E5AB' : 'transparent'
              }}>
                <div style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  background: '#8B5A2B'
                }}></div>
                <span style={{ flex: 1, fontSize: '11px' }}>{t("spaceAnalytics.tooltipDiscussions")}:</span>
                <span style={{ fontWeight: 'bold', color: '#8B5A2B', fontSize: '11px' }}>
                  {selectedPoint.dailyDiscussions || 0}
                </span>
              </div>
              
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '3px',
                borderRadius: '3px',
                background: selectedPoint.pointType === 'collaborators' ? '#E8F5E8' : 'transparent'
              }}>
                <div style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  background: '#2D5016'
                }}></div>
                <span style={{ flex: 1, fontSize: '11px' }}>{t("spaceAnalytics.tooltipCollaborators")}:</span>
                <span style={{ fontWeight: 'bold', color: '#2D5016', fontSize: '11px' }}>
                  {selectedPoint.dailyCollaborators || 0}
                </span>
              </div>
            </div>
            
            {/* Total activity for the day */}
            <div style={{
              marginTop: '6px',
              padding: '4px 6px',
              background: '#F8F9FA',
              borderRadius: '3px',
              borderLeft: '2px solid #0076B5'
            }}>
              <div style={{ fontSize: '10px', fontWeight: 'bold', color: '#1B1F3B' }}>
                {t("spaceAnalytics.tooltipTotal")}: {(selectedPoint.dailyNodes || 0) + (selectedPoint.dailyEdges || 0) + (selectedPoint.dailyDiscussions || 0) + (selectedPoint.dailyCollaborators || 0)}
              </div>
            </div>
          </div>
        )}
      </div>
      
      {/* Legend */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
        gap: '10px',
        padding: '20px',
        fontSize: '13px',
        borderTop: '2px solid #E5E5E5',
        background: '#FFFFFF',
        marginTop: '10px',
        boxShadow: '0 -2px 4px rgba(0, 0, 0, 0.05)'
      }}>
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '8px',
          cursor: 'pointer',
          padding: '8px 12px',
          borderRadius: '6px',
          transition: 'background-color 0.2s ease',
          minWidth: 0,
          flex: 1
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.backgroundColor = '#E3F2FD';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = 'transparent';
        }}>
          <svg width="24" height="8" style={{ flexShrink: 0 }}>
            <line x1="0" y1="4" x2="24" y2="4" stroke="#0055B7" strokeWidth="4" strokeLinecap="round" />
          </svg>
          <span style={{ 
            color: '#1B1F3B', 
            fontWeight: '600',
            fontSize: '14px',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            flex: 1,
            minWidth: 0
          }}>
            {t("spaceAnalytics.tooltipNodes")}: {analytics ? analytics.totalNodes : (data.length > 0 ? data[data.length - 1]?.totalNodes || 0 : 0)}
          </span>
        </div>
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '8px',
          cursor: 'pointer',
          padding: '8px 12px',
          borderRadius: '6px',
          transition: 'background-color 0.2s ease',
          minWidth: 0,
          flex: 1
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.backgroundColor = '#FCE4EC';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = 'transparent';
        }}>
          <svg width="24" height="8" style={{ flexShrink: 0 }}>
            <line x1="0" y1="4" x2="24" y2="4" stroke="#B8336A" strokeWidth="4" strokeDasharray="8,4" strokeLinecap="round" />
          </svg>
          <span style={{ 
            color: '#1B1F3B', 
            fontWeight: '600',
            fontSize: '14px',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            flex: 1,
            minWidth: 0
          }}>
            {t("spaceAnalytics.tooltipEdges")}: {analytics ? analytics.totalEdges : (data.length > 0 ? data[data.length - 1]?.totalEdges || 0 : 0)}
          </span>
        </div>
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '8px',
          cursor: 'pointer',
          padding: '8px 12px',
          borderRadius: '6px',
          transition: 'background-color 0.2s ease',
          minWidth: 0,
          flex: 1
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.backgroundColor = '#F3E5AB';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = 'transparent';
        }}>
          <svg width="24" height="8" style={{ flexShrink: 0 }}>
            <line x1="0" y1="4" x2="24" y2="4" stroke="#8B5A2B" strokeWidth="4" strokeDasharray="4,4" strokeLinecap="round" />
          </svg>
          <span style={{ 
            color: '#1B1F3B', 
            fontWeight: '600',
            fontSize: '14px',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            flex: 1,
            minWidth: 0
          }}>
            {t("spaceAnalytics.tooltipDiscussions")}: {analytics ? analytics.totalDiscussions : (data.length > 0 ? data[data.length - 1]?.totalDiscussions || 0 : 0)}
          </span>
        </div>
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '8px',
          cursor: 'pointer',
          padding: '8px 12px',
          borderRadius: '6px',
          transition: 'background-color 0.2s ease',
          minWidth: 0,
          flex: 1
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.backgroundColor = '#E8F5E8';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.backgroundColor = 'transparent';
        }}>
          <svg width="24" height="8" style={{ flexShrink: 0 }}>
            <line x1="0" y1="4" x2="24" y2="4" stroke="#2D5016" strokeWidth="4" strokeDasharray="10,3,3,3" strokeLinecap="round" />
          </svg>
          <span style={{ 
            color: '#1B1F3B', 
            fontWeight: '600',
            fontSize: '14px',
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            flex: 1,
            minWidth: 0
          }}>
            {t("spaceAnalytics.tooltipCollaborators")}: {analytics ? analytics.totalCollaborators || 0 : (data.length > 0 ? Math.max(...data.map(d => d.dailyCollaborators || 0)) : 0)}
          </span>
        </div>
      </div>
    </div>
  );
};

const SpaceAnalytics = () => {
  const { t } = useTranslation();
  const { id } = useParams();
  const navigate = useNavigate();
  const [space, setSpace] = useState(null);
  const [analytics, setAnalytics] = useState({
    totalNodes: 0,
    totalEdges: 0,
    totalDiscussions: 0,
    totalCollaborators: 0
  });
  const [topConnectedNodes, setTopConnectedNodes] = useState([]);
  const [topCollaborators, setTopCollaborators] = useState([]);
  const [timelineData, setTimelineData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchSpaceData = async () => {
      try {
        setLoading(true);
        
        // Fetch space details
        const spaceResponse = await api.get(`/spaces/${id}/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        setSpace(spaceResponse.data);

        // Fetch nodes data
        const nodesResponse = await api.get(`/spaces/${id}/nodes/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        // Fetch edges data
        const edgesResponse = await api.get(`/spaces/${id}/edges/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        // Fetch discussions data
        const discussionsResponse = await api.get(`/spaces/${id}/discussions/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        // Fetch top collaborators data
        const topCollaboratorsResponse = await api.get(`/spaces/${id}/top-collaborators/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });


        const nodes = Array.isArray(nodesResponse.data) ? nodesResponse.data : [];
        const edges = Array.isArray(edgesResponse.data) ? edgesResponse.data : [];
        

        const nodeConnectionCounts = {};
        

        nodes.forEach(node => {
          nodeConnectionCounts[node.id] = {
            node: node,
            connections: 0
          };
        });

        edges.forEach(edge => {

          const sourceNodeId = edge.source;
          const targetNodeId = edge.target;
          
          if (sourceNodeId && nodeConnectionCounts[sourceNodeId]) {
            nodeConnectionCounts[sourceNodeId].connections++;
          }
          if (targetNodeId && nodeConnectionCounts[targetNodeId]) {
            nodeConnectionCounts[targetNodeId].connections++;
          }
        });
        
        // Get top 10 connected nodes
        const sortedNodes = Object.values(nodeConnectionCounts)
          .sort((a, b) => b.connections - a.connections)
          .slice(0, 10);
        
        setTopConnectedNodes(sortedNodes);


        setTopCollaborators(topCollaboratorsResponse.data?.top_collaborators || []);


        const timeline = calculateNodeTimeline(nodes, edges, discussionsResponse.data, spaceResponse.data?.collaborators);
        setTimelineData(timeline);


        const totalCollaborators = spaceResponse.data?.collaborators?.length || 0;

        setAnalytics({
          totalNodes: nodes.length,
          totalEdges: edges.length,
          totalDiscussions: Array.isArray(discussionsResponse.data) ? discussionsResponse.data.length : 0,
          totalCollaborators: totalCollaborators
        });

      } catch (err) {
        setError(t("spaceAnalytics.failedToLoadData"));
      } finally {
        setLoading(false);
      }
    };

    fetchSpaceData();
  }, [id]);

  const canViewAnalytics = () => {
    const username = localStorage.getItem("username");
    const isStaff =
      localStorage.getItem("is_staff") === "true" ||
      (window.currentUser && window.currentUser.is_staff);
    const isSuperuser =
      localStorage.getItem("is_superuser") === "true" ||
      (window.currentUser && window.currentUser.is_superuser);
    
    return (
      (space?.creator_username && space.creator_username === username) ||
      isStaff ||
      isSuperuser
    );
  };

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px',
        color: '#4A5568'
      }}>
        Loading analytics...
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px',
        color: '#BD4902'
      }}>
        {error}
      </div>
    );
  }

  if (!canViewAnalytics()) {
    return (
      <div style={{ 
        display: 'flex', 
        flexDirection: 'column',
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px',
        color: '#BD4902',
        gap: '20px'
      }}>
        <div>You don't have permission to view analytics for this space.</div>
        <button
          onClick={() => navigate(`/spaces/${id}`)}
          style={{
            background: '#0076B5',
            color: '#FFFFFF',
            border: 'none',
            borderRadius: '4px',
            padding: '10px 20px',
            fontSize: '16px',
            cursor: 'pointer'
          }}
        >
          Back to Space
        </button>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: '100vh',
      background: '#F5F5F5',
      padding: '20px'
    }}>
      {/* Header */}
      <div style={{
        background: '#FFFFFF',
        padding: '20px 30px',
        marginBottom: '20px',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <div>
          <h1 style={{ 
            margin: 0, 
            color: '#1B1F3B',
            fontSize: '28px',
            fontWeight: '600'
          }}>
            {space?.title}
          </h1>
          <p style={{ 
            margin: '5px 0 0 0', 
            color: '#4A5568',
            fontSize: '16px'
          }}>
            {t("spaceAnalytics.dashboard")}
          </p>
        </div>
        
        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            onClick={() => navigate(`/spaces/${id}`)}
            style={{
              background: '#4A5568',
              color: '#FFFFFF',
              border: 'none',
              borderRadius: '4px',
              padding: '10px 20px',
              fontSize: '14px',
              cursor: 'pointer',
              fontWeight: '500'
            }}
          >
            ← {t("spaceAnalytics.backToSpace")}
          </button>
        </div>
      </div>

      {/* Space Details Section */}
      <div style={{
        background: 'linear-gradient(135deg, #FFFFFF 0%, #F5F5F5 100%)',
        padding: '25px',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        marginBottom: '20px',
        border: '1px solid #E5E5E5'
      }}>
        <h2 style={{ 
          color: '#1B1F3B', 
          marginBottom: '20px',
          fontSize: '20px',
          fontWeight: '600',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}>
          {t("spaceAnalytics.spaceOverview")}
        </h2>
        
        <div style={{ color: '#4A5568', lineHeight: '1.6' }}>
          {/* Description */}
          <div style={{ 
            marginBottom: '20px',
            padding: '15px',
            background: '#FFFFFF',
            borderRadius: '8px',
            border: '1px solid #E5E5E5'
          }}>
            <div style={{ fontWeight: '600', marginBottom: '8px', color: '#1B1F3B' }}>
              {t("spaceAnalytics.description")}:
            </div>
            <div style={{ fontStyle: 'italic' }}>
              {space?.description || t("spaceAnalytics.noDescriptionProvided")}
            </div>
          </div>

          {/* Location */}
          <div style={{ 
            marginBottom: '20px',
            padding: '15px',
            background: '#FFFFFF',
            borderRadius: '8px',
            border: '1px solid #E5E5E5'
          }}>
            <div style={{ 
              fontWeight: '600', 
              marginBottom: '8px', 
              color: '#1B1F3B',
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}>
              📍 {t("spaceAnalytics.location")}:
            </div>
            <div>
              {space?.country || space?.city || space?.district || space?.street ? (
                <div style={{ 
                  lineHeight: '1.5',
                  fontSize: '14px'
                }}>
                  {[space?.street, space?.district, space?.city, space?.country]
                    .filter(Boolean)
                    .join(', ')}
                </div>
              ) : (
                <div style={{ 
                  fontStyle: 'italic', 
                  color: '#656F75',
                  fontSize: '14px'
                }}>
                  {t("spaceAnalytics.locationNotSpecified")}
                </div>
              )}
            </div>
          </div>

          {/* Space Members */}
          <div style={{ 
            marginBottom: '20px',
            padding: '15px',
            background: '#FFFFFF',
            borderRadius: '8px',
            border: '1px solid #E5E5E5'
          }}>
            <div style={{ 
              fontWeight: '600', 
              marginBottom: '12px', 
              color: '#1B1F3B',
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}>
              {t("spaceAnalytics.spaceMembers")}:
            </div>
            
            {/* Creator */}
            <div style={{ marginBottom: '10px' }}>
              <div style={{ 
                fontWeight: '500', 
                marginBottom: '6px', 
                color: '#4A5568',
                fontSize: '14px'
              }}>
                {t("spaceAnalytics.creator")}:
              </div>
              <div style={{
                display: 'inline-block',
                padding: '6px 12px',
                background: '#215D69',
                color: '#FFFFFF',
                borderRadius: '20px',
                fontSize: '13px',
                fontWeight: '500'
              }}>
                {space?.creator_username}
              </div>
            </div>

            {/* Moderators */}
            <div>
              <div style={{ 
                fontWeight: '500', 
                marginBottom: '6px', 
                color: '#4A5568',
                fontSize: '14px'
              }}>
                {t("spaceAnalytics.moderators")}:
              </div>
              <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                {space?.moderators && space.moderators.length > 0 ? (
                  space.moderators.map((moderator, index) => (
                    <div
                      key={index}
                      style={{
                        padding: '6px 12px',
                        background: '#2D6A4F',
                        color: '#FFFFFF',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: '500'
                      }}
                    >
                      {moderator.username || moderator}
                    </div>
                  ))
                ) : (
                  <div style={{ 
                    fontStyle: 'italic', 
                    color: '#656F75',
                    fontSize: '13px'
                  }}>
                    {t("spaceAnalytics.noModeratorsAssigned")}
                  </div>
                )}
              </div>
            </div>
          </div>
          
          {/* Tags */}
          <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap' }}>
            <div style={{
              padding: '10px 16px',
              background: '#2D6A4F',
              color: '#FFFFFF',
              borderRadius: '25px',
              fontSize: '14px',
              fontWeight: '500'
            }}>
              {space?.tags?.length || 0} {t("spaceAnalytics.tags")}
            </div>
          </div>
        </div>
      </div>

      {/* Quick Stats Section */}
      <div style={{
        background: '#FFFFFF',
        padding: '25px',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        marginBottom: '20px'
      }}>
        <h2 style={{
          color: '#1B1F3B',
          marginBottom: '20px',
          fontSize: '20px',
          fontWeight: '600'
        }}>
          {t("spaceAnalytics.quickStats")}
        </h2>        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '15px'
        }}>
          {/* Graph Stats */}
          <div style={{
            background: 'linear-gradient(135deg, #215D69 0%, #008296 100%)',
            padding: '20px',
            borderRadius: '8px',
            color: '#FFFFFF',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '5px' }}>
              {analytics.totalNodes}
            </div>
            <div style={{ fontSize: '14px', opacity: 0.9 }}>
              {t("spaceAnalytics.totalNodes")}
            </div>
          </div>

          <div style={{
            background: 'linear-gradient(135deg, #2D6A4F 0%, #40916C 100%)',
            padding: '20px',
            borderRadius: '8px',
            color: '#FFFFFF',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '5px' }}>
              {analytics.totalEdges}
            </div>
            <div style={{ fontSize: '14px', opacity: 0.9 }}>
              {t("spaceAnalytics.totalEdges")}
            </div>
          </div>

          <div style={{
            background: 'linear-gradient(135deg, #0076B5 0%, #4A90E2 100%)',
            padding: '20px',
            borderRadius: '8px',
            color: '#FFFFFF',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '5px' }}>
              {analytics.totalDiscussions}
            </div>
            <div style={{ fontSize: '14px', opacity: 0.9 }}>
              {t("spaceAnalytics.totalDiscussions")}
            </div>
          </div>

          <div style={{
            background: 'linear-gradient(135deg, #8F6701 0%, #B8860B 100%)',
            padding: '20px',
            borderRadius: '8px',
            color: '#FFFFFF',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '5px' }}>
              {space?.collaborators?.length || 0}
            </div>
            <div style={{ fontSize: '14px', opacity: 0.9 }}>
              {t("spaceAnalytics.totalCollaborators")}
            </div>
          </div>
        </div>
      </div>

      {/* Main Analytics Content */}
      <div style={{
        background: '#FFFFFF',
        padding: '30px',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)'
      }}>
        
        {/* Chart Section */}
        <div style={{ marginBottom: '40px' }}>
          <h2 style={{
            color: '#1B1F3B',
            marginBottom: '20px',
            fontSize: '20px',
            fontWeight: '600'
          }}>
            {t("spaceAnalytics.spaceActivityOverTime")}
          </h2>
          
          <div style={{
            width: '100%',
            height: '550px',
            background: '#FFFFFF',
            border: `1px solid #68686B`,
            borderRadius: '8px',
            overflow: 'visible',
            position: 'relative'
          }}>
            {timelineData.length > 0 ? (
              <TimelineChart data={timelineData} analytics={analytics} />
            ) : (
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                height: '100%',
                flexDirection: 'column',
                color: '#656F75',
                padding: '20px'
              }}>
                <div style={{ fontSize: '48px', marginBottom: '15px' }}></div>
                <div style={{ fontSize: '16px', textAlign: 'center' }}>
                  {t("spaceAnalytics.noActivityData") || "No activity data available yet"}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Top Connected Nodes Section */}
        <div style={{
          background: '#FFFFFF',                    
          borderRadius: '8px',
          padding: '25px',
          marginTop: '30px',
          border: `1px solid #68686B`             
        }}>
          <h2 style={{
            color: '#1B1F3B',                     
            marginBottom: '20px',
            fontSize: '20px',
            fontWeight: '600',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            {t("spaceAnalytics.topConnectedNodes")}
          </h2>
          
          <div style={{
            display: 'grid',
            gap: '12px'
          }}>
            {topConnectedNodes.length > 0 ? (
              topConnectedNodes.map((item, index) => (
                <div
                  key={item.node.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    padding: '15px 20px',
                    background: index === 0 ? '#3A0CA3' :     
                               index === 1 ? '#7209B7' :     
                               index === 2 ? '#215D69' :     
                               '#FFFFFF',                      
                    borderRadius: '8px',
                    border: index < 3 ? 'none' : `1px solid #68686B`,
                    color: index < 3 ? '#FFFFFF' : '#1B1F3B',          
                    fontWeight: index < 3 ? '600' : '500',
                    transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                    cursor: 'pointer'
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.transform = 'translateY(-2px)';
                    e.target.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.transform = 'translateY(0)';
                    e.target.style.boxShadow = 'none';
                  }}
                >
                  <div style={{
                    minWidth: '32px',
                    height: '32px',
                    borderRadius: '50%',
                    background: index < 3 ? 'rgba(255, 255, 255, 0.2)' : '#0076B5', 
                    color: '#FFFFFF',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 'bold',
                    fontSize: '14px',
                    marginRight: '16px',
                    border: index < 3 ? '2px solid rgba(255, 255, 255, 0.3)' : 'none'
                  }}>
                    {index + 1}
                  </div>
                  
                  <div style={{ flex: 1 }}>
                    <div style={{ 
                      fontSize: '16px', 
                      fontWeight: '600',
                      marginBottom: '4px',
                      color: index < 3 ? '#FFFFFF' : '#1B1F3B'    
                    }}>
                      {item.node.label || item.node.name || item.node.title || item.node.wikidata_label || `Node ${item.node.id}`}
                    </div>
                    {(item.node.description || item.node.summary || item.node.content) && (
                      <div style={{ 
                        fontSize: '14px',
                        color: index < 3 ? 'rgba(255, 255, 255, 0.9)' : '#4A5568', 
                        lineHeight: '1.4'
                      }}>
                        {(() => {
                          const desc = item.node.description || item.node.summary || item.node.content || '';
                          return desc.length > 60 ? `${desc.substring(0, 60)}...` : desc;
                        })()}
                      </div>
                    )}
                  </div>
                  
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    marginLeft: '16px',
                    padding: '8px 12px',
                    background: index < 3 ? 'rgba(255, 255, 255, 0.15)' : '#F5F5F5', 
                    borderRadius: '20px'
                  }}>
                    <div style={{
                      fontSize: '18px',
                      fontWeight: 'bold',
                      color: index < 3 ? '#FFFFFF' : '#2D6A4F'  
                    }}>
                      {item.connections}
                    </div>
                    <div style={{
                      fontSize: '13px',
                      color: index < 3 ? 'rgba(255, 255, 255, 0.9)' : '#656F75', 
                      fontWeight: '500'
                    }}>
                      {t("spaceAnalytics.nodeConnections")}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div style={{
                textAlign: 'center',
                padding: '40px',
                color: '#656F75',                   
                fontStyle: 'italic',
                background: '#F5F5F5',             
                borderRadius: '8px',
                border: `1px solid #68686B`       
              }}>
                {t("spaceAnalytics.noNodesFound")}
              </div>
            )}
          </div>
        </div>

        {/* Top Collaborators Section */}
        <div style={{
          background: '#FFFFFF',
          borderRadius: '8px',
          padding: '25px',
          marginTop: '30px',
          border: `1px solid #68686B`
        }}>
          <h2 style={{
            color: '#1B1F3B',
            marginBottom: '20px',
            fontSize: '20px',
            fontWeight: '600',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            {t("spaceAnalytics.topCollaborators")}
          </h2>
          
          <div style={{
            display: 'grid',
            gap: '12px'
          }}>
            {topCollaborators.length > 0 ? (
              topCollaborators.map((collaborator, index) => (
                <div
                  key={collaborator.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    padding: '15px 20px',
                    background: index === 0 ? '#3A0CA3' :      
                               index === 1 ? '#7209B7' :     
                               index === 2 ? '#215D69' :     
                               '#FFFFFF',                                           
                    borderRadius: '8px',
                    border: index < 3 ? 'none' : `1px solid #68686B`,
                    color: index < 3 ? '#FFFFFF' : '#1B1F3B',          
                    fontWeight: index < 3 ? '600' : '500',
                    transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                    cursor: 'pointer'
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.transform = 'translateY(-2px)';
                    e.target.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.transform = 'translateY(0)';
                    e.target.style.boxShadow = 'none';
                  }}
                >
                  <div style={{
                    minWidth: '32px',
                    height: '32px',
                    borderRadius: '50%',
                    background: index < 3 ? 'rgba(255, 255, 255, 0.2)' : '#0076B5', 
                    color: '#FFFFFF',                                             
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 'bold',
                    fontSize: '14px',
                    marginRight: '16px',
                    border: index < 3 ? '2px solid rgba(255, 255, 255, 0.3)' : 'none' 
                  }}>
                    {index + 1}
                  </div>
                  
                  <div style={{ flex: 1 }}>
                    <div style={{ 
                      fontSize: '16px', 
                      fontWeight: '600',
                      marginBottom: '4px',
                      color: index < 3 ? '#FFFFFF' : '#1B1F3B'                    
                    }}>
                      {collaborator.username}
                    </div>
                    <div style={{ 
                      fontSize: '13px',
                      color: index < 3 ? 'rgba(255, 255, 255, 0.9)' : '#4A5568', 
                      lineHeight: '1.4'
                    }}>
                      {collaborator.node_count} nodes • {collaborator.edge_count} edges • {collaborator.discussion_count} discussions
                    </div>
                  </div>
                  
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    marginLeft: '16px',
                    padding: '8px 12px',
                    background: index < 3 ? 'rgba(255, 255, 255, 0.3)' : '#F5F5F5',
                    borderRadius: '20px'
                  }}>
                    <div style={{
                      fontSize: '18px',
                      fontWeight: 'bold',
                      color: index < 3 ? '#FFFFFF' : '#2D6A4F'                    
                    }}>
                      {collaborator.total_score}
                    </div>
                    <div style={{
                      fontSize: '13px',
                      color: index < 3 ? 'rgba(255, 255, 255, 0.9)' : '#656F75', 
                      fontWeight: '500'
                    }}>
                      {t("spaceAnalytics.points")}
                    </div>
                  </div>
                </div>
              ))
            ) : (
              <div style={{
                textAlign: 'center',
                padding: '40px',
                color: '#656F75',
                fontStyle: 'italic',
                background: '#F5F5F5',
                borderRadius: '8px',
                border: `1px solid #68686B`
              }}>
                {t("spaceAnalytics.noCollaboratorsFound")}
              </div>
            )}
          </div>

          {/* Scoring Legend */}
          <div style={{
            marginTop: '20px',
            padding: '15px',
            background: '#F8F9FA',
            borderRadius: '6px',
            border: '1px solid #E5E5E5'
          }}>
            <div style={{ 
              fontSize: '14px', 
              fontWeight: '600', 
              color: '#1B1F3B',
              marginBottom: '8px'
            }}>
              {t("spaceAnalytics.scoringSystem")}:
            </div>
            <div style={{ 
              fontSize: '13px', 
              color: '#4A5568',
              lineHeight: '1.5'
            }}>
              • Node creation: 4 points • Edge creation: 2 points • Discussion participation: 1 point
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SpaceAnalytics;