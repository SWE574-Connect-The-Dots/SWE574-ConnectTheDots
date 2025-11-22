import React, { useState, useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import L from "leaflet";
import api from "../axiosConfig";
import { useNavigate } from "react-router-dom";

// Create different icons for different marker types
const createCustomIcon = (type, count = 1, isMain = false) => {
  if (type === 'single') {
    return L.divIcon({
      html: `<div style="
        background-color: #2196F3; 
        width: 35px; 
        height: 35px; 
        border-radius: 50%; 
        border: 3px solid white; 
        box-shadow: 0 3px 10px rgba(0,0,0,0.4);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-weight: bold;
        font-size: 16px;
        cursor: pointer;
      ">📍</div>`,
      iconSize: [35, 35],
      iconAnchor: [17, 17],
      className: 'single-space-icon'
    });
  } else if (type === 'clustered' && isMain) {
    return L.divIcon({
      html: `<div style="
        background-color: #FF5722; 
        width: 45px; 
        height: 45px; 
        border-radius: 50%; 
        border: 4px solid white; 
        box-shadow: 0 4px 15px rgba(0,0,0,0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-weight: bold;
        font-size: 14px;
        cursor: pointer;
        animation: pulse 2s infinite;
      ">${count}</div>
      <style>
        @keyframes pulse {
          0% { transform: scale(1); }
          50% { transform: scale(1.05); }
          100% { transform: scale(1); }
        }
      </style>`,
      iconSize: [45, 45],
      iconAnchor: [22, 22],
      className: 'cluster-main-icon'
    });
  } else {
    return L.divIcon({
      html: `<div style="
        background-color: #4CAF50; 
        width: 30px; 
        height: 30px; 
        border-radius: 50%; 
        border: 2px solid white; 
        box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-weight: bold;
        font-size: 12px;
        cursor: pointer;
      ">•</div>`,
      iconSize: [30, 30],
      iconAnchor: [15, 15],
      className: 'cluster-member-icon'
    });
  }
};

const MapView = () => {
  const [spaces, setSpaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [geocodedSpaces, setGeocodedSpaces] = useState([]);
  const [clusteredMarkers, setClusteredMarkers] = useState([]);
  const [geocodingProgress, setGeocodingProgress] = useState({ current: 0, total: 0 });
  const [mapReady, setMapReady] = useState(false);
  const navigate = useNavigate();

  // Function to group nearby spaces and create clusters
  const createClusters = (spaces) => {
    const clusters = [];
    const processed = new Set();
    
    spaces.forEach((space, index) => {
      if (processed.has(index)) return;
      
      // Find all spaces within a small radius (same city/very close)
      const cluster = [space];
      const baseCoords = [space.latitude, space.longitude];
      
      spaces.forEach((otherSpace, otherIndex) => {
        if (otherIndex === index || processed.has(otherIndex)) return;
        
        const otherCoords = [otherSpace.latitude, otherSpace.longitude];
        const distance = getDistance(baseCoords, otherCoords);
        
        // Group if within 5km or same city
        if (distance < 5 || (space.city === otherSpace.city && space.country === otherSpace.country)) {
          cluster.push(otherSpace);
          processed.add(otherIndex);
        }
      });
      
      processed.add(index);
      
      // Create cluster info
      if (cluster.length === 1) {
        // Single marker
        clusters.push({
          type: 'single',
          space: cluster[0],
          position: [cluster[0].latitude, cluster[0].longitude]
        });
      } else {
        // Multiple markers - create cluster with slight offsets
        cluster.forEach((space, i) => {
          const offset = i * 0.01; // Small offset for each additional marker
          clusters.push({
            type: 'clustered',
            space: space,
            clusterSpaces: cluster,
            position: [
              space.latitude + (Math.cos(i * 60 * Math.PI / 180) * offset),
              space.longitude + (Math.sin(i * 60 * Math.PI / 180) * offset)
            ],
            isMainCluster: i === 0
          });
        });
      }
    });
    
    return clusters;
  };

  // Calculate distance between two coordinates in km
  const getDistance = (coords1, coords2) => {
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

  // Fast geocoding with caching and optimized fallback
  const geocodeSpace = async (space) => {
    try {
      // Create a cache key from location data 
      const cacheKey = `${space.city}_${space.country}_${space.district || ''}`.toLowerCase();
      
      // Check if we already have coordinates for this location
      const cached = localStorage.getItem(`geo_${cacheKey}`);
      if (cached) {
        const coords = JSON.parse(cached);
        console.log(`Using cached coordinates for "${space.title}": ${coords.lat}, ${coords.lon}`);
        return { ...space, latitude: coords.lat, longitude: coords.lon };
      }
      
      // Quick geocoding - try city,country first (most reliable)
      const address = `${space.city}, ${space.country}`;
      console.log(` Fast geocoding: "${space.title}" -> ${address}`);
      
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(address)}&format=json&limit=1`,
        {
          headers: {
            'User-Agent': 'ConnectTheDots-FastGeo/1.0'
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
            console.log(` Geocoded "${space.title}": ${lat}, ${lon}`);
            
            // Shorter delay for speed
            await new Promise(resolve => setTimeout(resolve, 300));
            
            return { ...space, latitude: lat, longitude: lon };
          }
        }
      }
      
      console.log(` Could not geocode "${space.title}"`);
      return null;
      
    } catch (error) {
      console.error(` Geocoding error for "${space.title}":`, error);
      return null;
    }
  };  useEffect(() => {
    const fetchSpaces = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await api.get("/spaces/", {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        
        console.log(" Fetched spaces:", response.data);
        setSpaces(response.data);
        
        // Separate spaces with and without coordinates
        const spacesWithCoords = [];
        const spacesToGeocode = [];
        
        response.data.forEach(space => {
          if (space.latitude && space.longitude) {
            spacesWithCoords.push(space);
          } else {
            spacesToGeocode.push(space);
          }
        });
        
        // Show map immediately with existing coordinates
        if (spacesWithCoords.length > 0) {
          setGeocodedSpaces(spacesWithCoords);
          const initialClusters = createClusters(spacesWithCoords);
          setClusteredMarkers(initialClusters);
        }
        
        // Show map immediately, don't wait for geocoding
        setMapReady(true);
        setLoading(false);
        
        // Geocode remaining spaces in background
        if (spacesToGeocode.length > 0) {
          setGeocodingProgress({ current: 0, total: spacesToGeocode.length });
          geocodeSpacesProgressively(spacesWithCoords, spacesToGeocode);
        }
        
      } catch (error) {
        console.error("Failed to fetch spaces:", error);
        setLoading(false);
        setMapReady(true);
      }
    };

    const geocodeSpacesProgressively = async (initialSpaces, spacesToGeocode) => {
      console.log(` Background geocoding ${spacesToGeocode.length} spaces...`);
      const validGeocodedSpaces = [...initialSpaces];
      
      for (let i = 0; i < spacesToGeocode.length; i++) {
        const space = spacesToGeocode[i];
        setGeocodingProgress({ current: i + 1, total: spacesToGeocode.length });
        
        const geocodedSpace = await geocodeSpace(space);
        if (geocodedSpace) {
          validGeocodedSpaces.push(geocodedSpace);
          
          // Update map progressively - add markers as they're geocoded
          const updatedClusters = createClusters(validGeocodedSpaces);
          setGeocodedSpaces([...validGeocodedSpaces]);
          setClusteredMarkers(updatedClusters);
          
          console.log(` Added "${geocodedSpace.title}" to map (${i + 1}/${spacesToGeocode.length})`);
        }
      }
      
      console.log(` Geocoding complete! ${validGeocodedSpaces.length} total spaces on map`);
      setGeocodingProgress({ current: 0, total: 0 });
    };

    fetchSpaces();
  }, []);

  if (loading && !mapReady) {
    return (
      <div style={{ padding: "20px", textAlign: "center" }}>
        <h2> Loading Map...</h2>
        <div style={{ margin: "20px 0" }}>
          <div style={{ 
            width: "200px", 
            height: "4px", 
            backgroundColor: "#f0f0f0", 
            borderRadius: "2px",
            margin: "0 auto",
            overflow: "hidden"
          }}>
            <div style={{
              width: "50%",
              height: "100%",
              backgroundColor: "#2196F3",
              animation: "loading 1.5s ease-in-out infinite"
            }}></div>
          </div>
          <p style={{ marginTop: "10px", color: "#666" }}>Fetching collaboration spaces...</p>
        </div>
        <style jsx>{`
          @keyframes loading {
            0% { transform: translateX(-100%); }
            50% { transform: translateX(200%); }
            100% { transform: translateX(-100%); }
          }
        `}</style>
      </div>
    );
  }

  return (
    <div style={{ 
      display: "flex", 
      height: "100vh", 
      backgroundColor: "#fafafa",
      position: "absolute",
      top: "0",
      left: "0",
      right: "0",
      bottom: "0",
      marginTop: "60px" // Account for header height
    }}>
      {/* Left sidebar with space list - narrow and fixed */}
      <div style={{ width: "300px", flexShrink: 0, padding: "15px 0", borderRight: "1px solid #e0e0e0", backgroundColor: "white" }}>
        <h3 style={{ margin: "0 15px 15px 15px", fontSize: "16px", color: "#333", fontWeight: "600" }}>📍 Spaces ({geocodedSpaces.length})</h3>
        <div style={{ 
          height: "calc(100vh - 150px)", 
          overflowY: "auto", 
          border: "1px solid #e0e0e0", 
          borderRadius: "6px",
          padding: "10px",
          backgroundColor: "#fafafa",
          margin: "0 15px"
        }}>
          {geocodingProgress.total > 0 && (
            <div style={{ 
              padding: "10px", 
              backgroundColor: "#e3f2fd", 
              borderRadius: "4px", 
              marginBottom: "10px",
              fontSize: "12px"
            }}>
               Adding more spaces... ({geocodingProgress.current}/{geocodingProgress.total})
              <div style={{ 
                width: "100%", 
                height: "4px", 
                backgroundColor: "#bbdefb", 
                borderRadius: "2px",
                marginTop: "5px",
                overflow: "hidden"
              }}>
                <div style={{
                  width: `${(geocodingProgress.current / geocodingProgress.total) * 100}%`,
                  height: "100%",
                  backgroundColor: "#2196F3",
                  transition: "width 0.3s ease"
                }}></div>
              </div>
            </div>
          )}
          {geocodedSpaces.map(space => (
            <div 
              key={space.id}
              style={{ 
                padding: "10px", 
                margin: "5px 0",
                border: "1px solid #e0e0e0",
                borderRadius: "4px",
                backgroundColor: "white",
                cursor: "pointer",
                transition: "all 0.2s"
              }}
              onClick={() => navigate(`/spaces/${space.id}`)}
              onMouseEnter={(e) => e.target.style.boxShadow = "0 2px 6px rgba(0,0,0,0.1)"}
              onMouseLeave={(e) => e.target.style.boxShadow = "none"}
            >
              <div style={{ fontWeight: "600", color: "#333", marginBottom: "5px", fontSize: "14px", lineHeight: "1.3", display: "flex", alignItems: "center", gap: "6px" }}>
                <span>{space.title}</span>
                {space.is_archived && (
                  <span style={{
                    backgroundColor: "#757575",
                    color: "white",
                    padding: "1px 6px",
                    borderRadius: "3px",
                    fontSize: "10px",
                    fontWeight: "600",
                  }}>
                    ARCHIVED
                  </span>
                )}
              </div>
              <div style={{ fontSize: "12px", color: "#666", marginBottom: "5px", lineHeight: "1.3" }}>
                {space.description?.slice(0, 80)}
                {space.description?.length > 80 ? '...' : ''}
              </div>
              <div style={{ fontSize: "11px", color: "#888" }}>
                📍 {[space.city, space.country].filter(Boolean).join(', ')}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Map container - takes remaining space */}
      <div style={{ flex: 1, display: "flex", flexDirection: "column", padding: "15px 0" }}>
        <div style={{ marginBottom: "8px", paddingLeft: "15px" }}>
          <h2 style={{ margin: "0 0 6px 0", fontSize: "18px", color: "#333" }}>🗺️ Collaboration Spaces Map</h2>
          
          <div style={{ 
            padding: "8px 12px", 
            backgroundColor: "#f8f9fa", 
            borderRadius: "4px",
            fontSize: "12px", 
            color: "#666",
            border: "1px solid #e9ecef",
            display: "inline-block"
          }}>
            <span style={{ marginRight: "15px" }}>🔵 Single space</span>
            <span style={{ marginRight: "15px" }}>🔴 Multiple spaces (click to see list)</span>
            <span>🟢 Individual space in cluster</span>
          </div>
        </div>
        
        <div style={{ 
          flex: 1, 
          height: "calc(100vh - 150px)", 
          border: "2px solid #ddd", 
          borderRadius: "8px 0 0 8px", 
          boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
          overflow: "hidden",
          marginLeft: "15px"
        }}>
          <MapContainer
            center={[39.9334, 32.8597]} // Ankara, Turkey
            zoom={6}
            style={{ height: "100%", width: "100%" }}
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />
          
          {clusteredMarkers.map((marker, index) => {
            const space = marker.space;
            const icon = createCustomIcon(
              marker.type, 
              marker.clusterSpaces ? marker.clusterSpaces.length : 1,
              marker.isMainCluster
            );
            
            return (
              <Marker
                key={`${space.id}-${index}`}
                position={marker.position}
                icon={icon}
                eventHandlers={{
                  click: () => {
                    if (marker.type === 'single') {
                      navigate(`/spaces/${space.id}`);
                    }
                    // For clustered markers, the popup will show the list
                  }
                }}
              >
                <Popup maxWidth={300}>
                  <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                    {marker.type === 'single' ? (
                      // Single space popup
                      <div>
                        <h3 style={{ margin: '0 0 10px 0', color: '#333' }}>{space.title}</h3>
                        <p style={{ margin: '5px 0', fontSize: '14px' }}>
                          {space.description?.slice(0, 100)}
                          {space.description?.length > 100 ? '...' : ''}
                        </p>
                        <p style={{ margin: '5px 0', fontSize: '12px', color: '#666' }}>
                          <strong>📍 Location:</strong> {[space.street, space.district, space.city, space.country].filter(Boolean).join(', ')}
                        </p>
                        <button 
                          onClick={() => navigate(`/spaces/${space.id}`)}
                          style={{ 
                            background: '#2196F3', 
                            color: 'white', 
                            border: 'none', 
                            padding: '8px 15px', 
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '14px',
                            marginTop: '10px'
                          }}
                        >
                          View Details →
                        </button>
                      </div>
                    ) : (
                      // Clustered spaces popup
                      <div>
                        <h3 style={{ margin: '0 0 15px 0', color: '#333' }}>
                          {marker.clusterSpaces.length} Spaces in this area
                        </h3>
                        <div style={{ maxHeight: '200px', overflowY: 'auto' }}>
                          {marker.clusterSpaces.map((clusterSpace, i) => (
                            <div 
                              key={clusterSpace.id}
                              style={{ 
                                padding: '8px', 
                                margin: '5px 0',
                                border: '1px solid #eee',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                backgroundColor: i % 2 === 0 ? '#f9f9f9' : 'white'
                              }}
                              onClick={() => navigate(`/spaces/${clusterSpace.id}`)}
                            >
                              <div style={{ fontWeight: 'bold', color: '#333', fontSize: '14px' }}>
                                {clusterSpace.title}
                              </div>
                              <div style={{ fontSize: '12px', color: '#666', marginTop: '2px' }}>
                                {clusterSpace.description?.slice(0, 60)}
                                {clusterSpace.description?.length > 60 ? '...' : ''}
                              </div>
                              <div style={{ fontSize: '11px', color: '#888', marginTop: '3px' }}>
                                📍 {clusterSpace.city}, {clusterSpace.country}
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </Popup>
              </Marker>
            );
            })}
          </MapContainer>
        </div>
      </div>
    </div>
  );
};

export default MapView;
