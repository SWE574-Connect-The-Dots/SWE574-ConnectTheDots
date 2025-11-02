import React, { useState, useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup, Polyline } from "react-leaflet";
import L from "leaflet";
import api from "../axiosConfig";

// Create custom icon for nodes
const createNodeIcon = (isSelected = false) => {
  const backgroundColor = isSelected ? '#FF5722' : '#4CAF50';
  const size = isSelected ? 35 : 30;
  const borderWidth = isSelected ? 4 : 3;
  
  return L.divIcon({
    html: `<div style="
      background-color: ${backgroundColor}; 
      width: ${size}px; 
      height: ${size}px; 
      border-radius: 50%; 
      border: ${borderWidth}px solid white; 
      box-shadow: 0 3px 10px rgba(0,0,0,0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: bold;
      font-size: 14px;
      cursor: pointer;
      ${isSelected ? 'animation: pulse 2s infinite;' : ''}
    ">🔗</div>
    ${isSelected ? `<style>
      @keyframes pulse {
        0% { transform: scale(1); }
        50% { transform: scale(1.1); }
        100% { transform: scale(1); }
      }
    </style>` : ''}`,
    iconSize: [size, size],
    iconAnchor: [size/2, size/2],
    className: isSelected ? 'selected-node-marker' : 'node-marker-icon'
  });
};

const SpaceMapModal = ({ isOpen, onClose, spaceId, spaceTitle }) => {
  const [nodes, setNodes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [geocodedNodes, setGeocodedNodes] = useState([]);
  const [geocodingProgress, setGeocodingProgress] = useState({ current: 0, total: 0 });
  const [selectedNodes, setSelectedNodes] = useState([]);
  const [selectedConnections, setSelectedConnections] = useState([]);

  // Calculate distance between two coordinates in kilometers
  const calculateDistance = (coords1, coords2) => {
    const [lat1, lon1] = coords1;
    const [lat2, lon2] = coords2;
    const R = 6371; // Earth's radius in km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
      Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  };

  // Handle node selection
  const handleNodeClick = (node) => {
    setSelectedNodes(prev => {
      const isSelected = prev.some(n => n.id === node.id);
      if (isSelected) {
        // Deselect node
        return prev.filter(n => n.id !== node.id);
      } else {
        // Select node (limit to reasonable number for performance)
        const newSelection = [...prev, node];
        if (newSelection.length > 10) {
          // Remove oldest selection if more than 10 nodes selected
          newSelection.shift();
        }
        return newSelection;
      }
    });
  };

  // Generate connections between selected nodes
  const generateSelectedConnections = (selectedNodes) => {
    if (selectedNodes.length < 2) return [];
    
    const connections = [];
    
    for (let i = 0; i < selectedNodes.length; i++) {
      for (let j = i + 1; j < selectedNodes.length; j++) {
        const node1 = selectedNodes[i];
        const node2 = selectedNodes[j];
        
        if (node1.latitude && node1.longitude && node2.latitude && node2.longitude) {
          const distance = calculateDistance(
            [node1.latitude, node1.longitude],
            [node2.latitude, node2.longitude]
          );
          
          connections.push({
            id: `${node1.id}-${node2.id}`,
            node1,
            node2,
            distance: distance.toFixed(1),
            coordinates: [
              [node1.latitude, node1.longitude],
              [node2.latitude, node2.longitude]
            ],
            midpoint: [
              (node1.latitude + node2.latitude) / 2,
              (node1.longitude + node2.longitude) / 2
            ]
          });
        }
      }
    }
    
    return connections;
  };

  // Update connections when selected nodes change
  useEffect(() => {
    setSelectedConnections(generateSelectedConnections(selectedNodes));
  }, [selectedNodes]);

  // Geocoding function for nodes without coordinates
  const geocodeNode = async (node) => {
    try {
      // Create a cache key from location data 
      const cacheKey = `node_${node.city}_${node.country}_${node.district || ''}`.toLowerCase();
      
      // Check if we already have coordinates for this location
      const cached = localStorage.getItem(`geo_${cacheKey}`);
      if (cached) {
        const coords = JSON.parse(cached);
        console.log(`Using cached coordinates for node "${node.label}": ${coords.lat}, ${coords.lon}`);
        return { ...node, latitude: coords.lat, longitude: coords.lon };
      }
      
      // Build address for geocoding
      let address = '';
      if (node.city && node.country) {
        address = `${node.city}, ${node.country}`;
      } else if (node.location_name) {
        address = node.location_name;
      } else {
        return null; // No location data available
      }
      
      console.log(`Geocoding node: "${node.label}" -> ${address}`);
      
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(address)}&format=json&limit=1`,
        {
          headers: {
            'User-Agent': 'ConnectTheDots-SpaceMap/1.0'
          }
        }
      );
      
      if (response.ok) {
        const data = await response.json();
        
        if (data && data.length > 0) {
          const result = data[0];
          const lat = parseFloat(result.lat);
          const lon = parseFloat(result.lon);
          
          if (!isNaN(lat) && !isNaN(lon)) {
            // Cache the result
            localStorage.setItem(`geo_${cacheKey}`, JSON.stringify({ lat, lon }));
            console.log(`Geocoded node "${node.label}": ${lat}, ${lon}`);
            
            // Add delay to respect API limits
            await new Promise(resolve => setTimeout(resolve, 500));
            
            return { ...node, latitude: lat, longitude: lon };
          }
        }
      }
      
      console.log(`Could not geocode node "${node.label}"`);
      return null;
      
    } catch (error) {
      console.error(`Geocoding error for node "${node.label}":`, error);
      return null;
    }
  };

  useEffect(() => {
    if (!isOpen || !spaceId) return;

    const fetchNodes = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem("token");
        const response = await api.get(`/spaces/${spaceId}/nodes/`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        
        console.log("Fetched nodes for space:", response.data);
        setNodes(response.data);
        
        // Separate nodes with and without coordinates
        const nodesWithCoords = [];
        const nodesToGeocode = [];
        
        response.data.forEach(node => {
          if (node.latitude && node.longitude) {
            nodesWithCoords.push(node);
          } else if (node.city || node.location_name) {
            // Only try to geocode nodes that have some location data
            nodesToGeocode.push(node);
          }
        });
        
        // Show nodes with existing coordinates immediately
        setGeocodedNodes(nodesWithCoords);
        setLoading(false);
        
        // Geocode remaining nodes in background
        if (nodesToGeocode.length > 0) {
          setGeocodingProgress({ current: 0, total: nodesToGeocode.length });
          geocodeNodesProgressively(nodesWithCoords, nodesToGeocode);
        }
        
      } catch (error) {
        console.error("Failed to fetch nodes:", error);
        setLoading(false);
      }
    };

    const geocodeNodesProgressively = async (initialNodes, nodesToGeocode) => {
      console.log(`Background geocoding ${nodesToGeocode.length} nodes...`);
      const validGeocodedNodes = [...initialNodes];
      
      for (let i = 0; i < nodesToGeocode.length; i++) {
        const node = nodesToGeocode[i];
        setGeocodingProgress({ current: i + 1, total: nodesToGeocode.length });
        
        const geocodedNode = await geocodeNode(node);
        if (geocodedNode) {
          validGeocodedNodes.push(geocodedNode);
          setGeocodedNodes([...validGeocodedNodes]);
          console.log(`Added node "${geocodedNode.label}" to map (${i + 1}/${nodesToGeocode.length})`);
        }
      }
      
      console.log(`Node geocoding complete! ${validGeocodedNodes.length} total nodes on map`);
      setGeocodingProgress({ current: 0, total: 0 });
    };

    fetchNodes();
  }, [isOpen, spaceId]);

  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      zIndex: 1000,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }}>
      <div style={{
        backgroundColor: 'white',
        width: '90vw',
        height: '90vh',
        borderRadius: '10px',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden'
      }}>
        {/* Header */}
        <div style={{
          padding: '20px',
          borderBottom: '1px solid #e0e0e0',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <div>
            <h2 style={{ margin: 0, color: '#333' }}>🗺️ {spaceTitle} - Node Locations</h2>
            <p style={{ margin: '5px 0 0 0', color: '#666', fontSize: '14px' }}>
              {geocodedNodes.length} node{geocodedNodes.length !== 1 ? 's' : ''} with location data
              {selectedNodes.length > 0 && (
                <span style={{ color: '#FF5722', fontWeight: 'bold' }}>
                  • {selectedNodes.length} selected
                </span>
              )}
              {selectedConnections.length > 0 && (
                <span style={{ color: '#007bff' }}>
                  • {selectedConnections.length} distance{selectedConnections.length !== 1 ? 's' : ''} shown
                </span>
              )}
            </p>
            {geocodingProgress.total > 0 && (
              <p style={{ margin: '5px 0 0 0', color: '#007bff', fontSize: '12px' }}>
                Geocoding progress: {geocodingProgress.current}/{geocodingProgress.total}
              </p>
            )}
            <div style={{ marginTop: '10px', display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              <p style={{ margin: 0, fontSize: '12px', color: '#666', flex: '1 0 100%' }}>
                💡 Click nodes to select/deselect and see distances between them
              </p>
              {selectedNodes.length > 0 && (
                <button
                  onClick={() => setSelectedNodes([])}
                  style={{
                    background: '#dc3545',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    padding: '4px 8px',
                    fontSize: '12px',
                    cursor: 'pointer',
                    transition: 'background 0.2s'
                  }}
                >
                  Clear Selection ({selectedNodes.length})
                </button>
              )}
            </div>
          </div>
          <button
            onClick={onClose}
            style={{
              background: '#f44336',
              color: 'white',
              border: 'none',
              borderRadius: '50%',
              width: '40px',
              height: '40px',
              fontSize: '18px',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            ✕
          </button>
        </div>

        {/* Map Container */}
        <div style={{ flex: 1, position: 'relative' }}>
          {loading && geocodedNodes.length === 0 ? (
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
              flexDirection: 'column'
            }}>
              <div style={{
                width: '50px',
                height: '50px',
                border: '4px solid #f3f3f3',
                borderTop: '4px solid #007bff',
                borderRadius: '50%',
                animation: 'spin 1s linear infinite'
              }}></div>
              <p style={{ marginTop: '20px', color: '#666' }}>Loading node locations...</p>
              <style jsx>{`
                @keyframes spin {
                  0% { transform: rotate(0deg); }
                  100% { transform: rotate(360deg); }
                }
              `}</style>
            </div>
          ) : geocodedNodes.length === 0 ? (
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              height: '100%',
              flexDirection: 'column'
            }}>
              <div style={{ fontSize: '48px', marginBottom: '20px' }}>📍</div>
              <h3 style={{ color: '#666', margin: 0 }}>No nodes with location data found</h3>
              <p style={{ color: '#999', marginTop: '10px' }}>
                Nodes need location information (coordinates or city/country) to appear on the map
              </p>
            </div>
          ) : (
            <MapContainer
              center={geocodedNodes.length > 0 ? [geocodedNodes[0].latitude, geocodedNodes[0].longitude] : [41.0082, 28.9784]} // Default to Istanbul
              zoom={6}
              style={{ height: '100%', width: '100%' }}
            >
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              />
              {geocodedNodes.map((node) => {
                const isSelected = selectedNodes.some(n => n.id === node.id);
                return (
                  <Marker
                    key={node.id}
                    position={[node.latitude, node.longitude]}
                    icon={createNodeIcon(isSelected)}
                    eventHandlers={{
                      click: () => handleNodeClick(node)
                    }}
                  >
                    <Popup>
                      <div style={{ minWidth: '200px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                          <h4 style={{ margin: 0, color: '#333' }}>{node.label}</h4>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              handleNodeClick(node);
                            }}
                            style={{
                              background: isSelected ? '#dc3545' : '#28a745',
                              color: 'white',
                              border: 'none',
                              borderRadius: '4px',
                              padding: '2px 6px',
                              fontSize: '11px',
                              cursor: 'pointer'
                            }}
                          >
                            {isSelected ? 'Deselect' : 'Select'}
                          </button>
                        </div>
                        <div style={{ fontSize: '12px', color: '#666' }}>
                          {node.location_name && (
                            <div><strong>Location:</strong> {node.location_name}</div>
                          )}
                          {node.city && (
                            <div><strong>City:</strong> {node.city}</div>
                          )}
                          {node.country && (
                            <div><strong>Country:</strong> {node.country}</div>
                          )}
                          {node.district && (
                            <div><strong>District:</strong> {node.district}</div>
                          )}
                          {node.street && (
                            <div><strong>Street:</strong> {node.street}</div>
                          )}
                          <div style={{ marginTop: '5px', fontStyle: 'italic' }}>
                            📍 {node.latitude.toFixed(6)}, {node.longitude.toFixed(6)}
                          </div>
                          {isSelected && (
                            <div style={{ marginTop: '8px', padding: '4px', background: '#fff3cd', borderRadius: '3px', fontSize: '11px', color: '#856404' }}>
                              ✓ Selected for distance calculation
                            </div>
                          )}
                        </div>
                      </div>
                    </Popup>
                  </Marker>
                );
              })}
              
              {/* Distance lines between selected nodes */}
              {selectedConnections.map((connection) => (
                <React.Fragment key={connection.id}>
                  <Polyline
                    positions={connection.coordinates}
                    pathOptions={{
                      color: '#FF5722',
                      weight: 3,
                      opacity: 0.8,
                      dashArray: '8, 12'
                    }}
                  />
                  <Marker
                    position={connection.midpoint}
                    icon={L.divIcon({
                      html: `<div style="
                        background: rgba(255, 87, 34, 0.95);
                        color: white;
                        padding: 3px 8px;
                        border-radius: 15px;
                        font-size: 12px;
                        font-weight: bold;
                        white-space: nowrap;
                        box-shadow: 0 3px 6px rgba(0,0,0,0.4);
                        border: 2px solid white;
                        text-align: center;
                      ">${connection.distance} km</div>`,
                      iconSize: [70, 25],
                      iconAnchor: [35, 12],
                      className: 'distance-label-selected'
                    })}
                  >
                    <Popup>
                      <div style={{ textAlign: 'center', fontSize: '12px' }}>
                        <strong>Distance Between Selected Nodes</strong><br/>
                        <div style={{ color: '#FF5722', fontSize: '16px', fontWeight: 'bold', margin: '8px 0' }}>
                          {connection.distance} km
                        </div>
                        <div style={{ fontSize: '11px', color: '#666', borderTop: '1px solid #eee', paddingTop: '8px' }}>
                          <div style={{ marginBottom: '4px' }}><strong>From:</strong> {connection.node1.label}</div>
                          <div><strong>To:</strong> {connection.node2.label}</div>
                        </div>
                        <div style={{ marginTop: '8px', fontSize: '10px', color: '#999', fontStyle: 'italic' }}>
                          Click nodes to change selection
                        </div>
                      </div>
                    </Popup>
                  </Marker>
                </React.Fragment>
              ))}
            </MapContainer>
          )}
        </div>

        {/* Bottom info bar */}
        {geocodedNodes.length > 0 && (
          <div style={{
            padding: '15px 20px',
            borderTop: '1px solid #e0e0e0',
            backgroundColor: '#f8f9fa',
            fontSize: '14px',
            color: '#666'
          }}>
            {selectedNodes.length === 0 ? (
              <>� Click on nodes to select them and see distances • Selected nodes appear in orange with pulsing animation</>
            ) : selectedNodes.length === 1 ? (
              <>📍 Select one more node to see distance • Currently selected: <strong>{selectedNodes[0].label}</strong></>
            ) : (
              <>📏 Showing {selectedConnections.length} distance{selectedConnections.length !== 1 ? 's' : ''} between {selectedNodes.length} selected nodes • Click distance labels for details</>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default SpaceMapModal;