import { useLocation, useParams, useNavigate } from "react-router-dom";
import { useEffect, useState, useCallback, useMemo, useRef } from "react";
import ReactFlow, { Controls, Background } from "reactflow";
import "reactflow/dist/style.css";
import api from "../axiosConfig";
import CircularNode from "../components/CircularNode";
import NodeDetailModal from "../components/NodeDetailModal";
import "../components/NodeDetailModal.css";
import useGraphData from "../hooks/useGraphData";
import useWikidataSearch from "../hooks/useWikidataSearch";
import { API_ENDPOINTS } from "../constants/config";
import EdgeDetailModal from "../components/EdgeDetailModal";
import SpaceDiscussions from "../components/SpaceDiscussions";
import PropertySearch from "../components/PropertySearch";
import SpaceMapModal from "../components/SpaceMapModal";
import ReportModal from "../components/ReportModal";

const propertySelectionStyles = `
.property-selection-container {
  border: 1px solid var(--color-gray-300);
  border-radius: 4px;
  margin-bottom: 15px;
}

.property-selection-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 0;
}

.property-selection-item {
  display: flex;
  align-items: flex-start;
  padding: 8px 12px;
  border-bottom: 1px solid var(--color-gray-200);
  cursor: pointer;
  transition: background-color 0.2s;
}

.property-selection-item:hover {
  background-color: var(--color-item-bg);
}

.property-selection-item.selected {
  background-color: var(--color-item-own-bg);
}

.property-checkbox {
  margin-right: 10px;
  margin-top: 4px;
  min-width: 16px;
}

.property-selection-label {
  display: block;
  flex: 1;
  cursor: pointer;
  line-height: 1.4;
}

.entity-link {
  color: var(--color-accent);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.entity-link:hover {
  text-decoration: underline;
  color: var(--color-accent-hover);
}

.entity-indicator {
  font-size: 0.85em;
  color: var(--color-text-secondary);
  font-style: italic;
}

.selection-help-text {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  margin-bottom: 8px;
}

.property-label {
  font-weight: 600;
  color: var(--color-text);
}
`;

const nodeTypes = {
  circular: CircularNode,
};

const getPropertyLabelWithId = (prop) => {
  const label =
    prop.property_label ||
    (prop.display && prop.display.includes(":")
      ? prop.display.split(":")[0].trim()
      : prop.property_label);
  const propId = prop.property || prop.property_id;
  if (!label) {
    return propId || "Unknown Property";
  }
  if (propId && label.includes(propId)) {
    return label;
  }
  return propId ? `${label} (${propId})` : label;
};

const PropertySelectionList = ({
  properties,
  selectedProperties,
  onChange,
}) => {
  const scrollContainerRef = useRef(null);

  const handleItemClick = (statementId) => {
    let scrollPos = 0;
    if (scrollContainerRef.current) {
      scrollPos = scrollContainerRef.current.scrollTop;
    }

    const newSelection = selectedProperties.includes(statementId)
      ? selectedProperties.filter((id) => id !== statementId)
      : [...selectedProperties, statementId];

    onChange(newSelection);

    if (scrollContainerRef.current) {
      setTimeout(() => {
        scrollContainerRef.current.scrollTop = scrollPos;
      }, 0);
    }
  };

  const renderPropertyValue = (prop) => {
    if (
      prop &&
      prop.value &&
      typeof prop.value === "object" &&
      prop.value.type === "entity"
    ) {
      return (
        <a
          href={`https://www.wikidata.org/wiki/${prop.value.id}`}
          target="_blank"
          rel="noopener noreferrer"
          className="entity-link"
          onClick={(e) => {
            e.stopPropagation();
            e.preventDefault();
            window.open(
              `https://www.wikidata.org/wiki/${prop.value.id}`,
              "_blank"
            );
          }}
        >
          {prop.value.text}
        </a>
      );
    }

    return prop?.value ? String(prop.value) : "No value available";
  };

  return (
    <div className="property-selection-container">
      <div className="property-selection-list" ref={scrollContainerRef}>
        {properties.filter((prop) => prop && prop.statement_id).map((prop) => (
          <div
            key={prop.statement_id}
            className={`property-selection-item ${
              selectedProperties.includes(prop.statement_id) ? "selected" : ""
            }`}
            onClick={() => handleItemClick(prop.statement_id)}
          >
            <input
              type="checkbox"
              id={`prop-${prop.statement_id}`}
              checked={selectedProperties.includes(prop.statement_id)}
              onChange={() => handleItemClick(prop.statement_id)}
              className="property-checkbox"
            />
            <label
              htmlFor={`prop-${prop.statement_id}`}
              className="property-selection-label"
              onClick={(e) => e.stopPropagation()}
            >
              <span className="property-label">
                {getPropertyLabelWithId(prop)}:
              </span>{" "}
              {renderPropertyValue(prop)}
            </label>
          </div>
        ))}
      </div>
    </div>
  );
};

const SpaceDetails = () => {
  const location = useLocation();
  const { id } = useParams();
  const navigate = useNavigate();
  const [space, setSpace] = useState({
    title: location.state?.title || "",
    description: location.state?.description || "",
    tags: location.state?.tags || [],
    collaborators: location.state?.collaborators || [],
    creator_username: location.state?.creator_username || "",
    country: location.state?.country || "",
    city: location.state?.city || "",
    district: location.state?.district || "",
    street: location.state?.street || "",
  });
  const [isCollaborator, setIsCollaborator] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isCollaboratorsOpen, setIsCollaboratorsOpen] = useState(false);
  const [snapshots, setSnapshots] = useState([]);
  const [query, setQuery] = useState("");
  const [selectedEntity, setSelectedEntity] = useState(null);
  const [entityProperties, setEntityProperties] = useState([]);
  const [selectedProperties, setSelectedProperties] = useState([]);
  const [edgeProperty, setEdgeProperty] = useState({ id: null, label: "" });
  const [existingNodes, setExistingNodes] = useState([]);
  const [relatedNodeId, setRelatedNodeId] = useState("");
  const [isNewNodeSource, setIsNewNodeSource] = useState(false);
  const [selectedNode, setSelectedNode] = useState(null);
  const [selectedEdge, setSelectedEdge] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");
  const [propertySearch, setPropertySearch] = useState("");

  // Location editing states
  const [isEditingLocation, setIsEditingLocation] = useState(false);
  const [countries, setCountries] = useState([]);
  const [cities, setCities] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [streets, setStreets] = useState([]);
  const [loadingCities, setLoadingCities] = useState(false);
  const [loadingDistricts, setLoadingDistricts] = useState(false);
  const [loadingStreets, setLoadingStreets] = useState(false);
  const [updatingLocation, setUpdatingLocation] = useState(false);
  
  // Node creation location states
  const [newNodeLocation, setNewNodeLocation] = useState({
    country: '',
    city: '',
    district: '',
    street: '',
    latitude: null,
    longitude: null,
    location_name: ''
  });
  const [showLocationSection, setShowLocationSection] = useState(false);
  
  // Space map modal state
  const [showSpaceMap, setShowSpaceMap] = useState(false);
  
  // Report modal state
  const [showReportModal, setShowReportModal] = useState(false);

  const {
    nodes,
    edges,
    loading: graphLoading,
    error: graphError,
    fetchGraphData,
  } = useGraphData(id);
  const {
    searchResults,
    loading: searchLoading,
    error: searchError,
    search,
    fetchProperties,
  } = useWikidataSearch();

  const filteredAndSortedProperties = useMemo(() => {
    if (!entityProperties) return [];
    return entityProperties
      .filter((prop) => prop && prop.display && prop.property)
      .filter((prop) =>
        prop.display.toLowerCase().includes(propertySearch.toLowerCase())
      )
      .sort((a, b) => {
        const numA = parseInt(a.property.substring(1), 10);
        const numB = parseInt(b.property.substring(1), 10);
        return numA - numB;
      });
  }, [entityProperties, propertySearch]);

  useEffect(() => {
    window.scrollTo(0, 0);
    const fetchData = async () => {
      setIsLoading(true);

      try {
        const spaceResponse = await api.get(API_ENDPOINTS.SPACES + `/${id}/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        setSpace({
          title: spaceResponse.data.title,
          description: spaceResponse.data.description,
          tags: spaceResponse.data.tags || [],
          collaborators: spaceResponse.data.collaborators || [],
          creator_username: spaceResponse.data.creator_username,
          country: spaceResponse.data.country || "",
          city: spaceResponse.data.city || "",
          district: spaceResponse.data.district || "",
          street: spaceResponse.data.street || "",
        });

        const username = localStorage.getItem("username");
        const isUserCollaborator =
          spaceResponse.data.collaborators.includes(username);
        setIsCollaborator(isUserCollaborator);

        const snapshotsResponse = await api.get(API_ENDPOINTS.SNAPSHOTS(id), {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        setSnapshots(snapshotsResponse.data);

        const nodesResponse = await api.get(API_ENDPOINTS.NODES(id), {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        const nodesData = Array.isArray(nodesResponse.data)
          ? nodesResponse.data
          : [];
        const validNodes = nodesData.filter((n) => n && n.id && n.label);
        setExistingNodes(validNodes);
        fetchGraphData();
      } catch (error) {
        console.error("Error fetching space data:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [id, fetchGraphData]);

  // Fetch countries on mount
  useEffect(() => {
    const fetchCountries = async () => {
      try {
        const res = await fetch("https://countriesnow.space/api/v0.1/countries/positions");
        const data = await res.json();
        setCountries(data.data || []);
      } catch (err) {
        console.error("Failed to fetch countries", err);
      }
    };
    fetchCountries();
  }, []);

  // Fetch cities when country changes
  useEffect(() => {
    if (space.country && isEditingLocation) {
      const fetchCities = async () => {
        setLoadingCities(true);
        try {
          const res = await fetch("https://countriesnow.space/api/v0.1/countries/cities", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ country: space.country }),
          });
          const data = await res.json();
          if (data.error === false) {
            setCities(data.data || []);
          } else {
            console.error("API Error:", data.msg);
            setCities([]);
          }
        } catch (err) {
          console.error("Failed to fetch cities", err);
          setCities([]);
        } finally {
          setLoadingCities(false);
        }
      };
      fetchCities();
    } else {
      setCities([]);
    }
  }, [space.country, isEditingLocation]);

  // Fetch districts when city changes using Nominatim API
  useEffect(() => {
    if (space.city && space.country && isEditingLocation) {
      const fetchDistricts = async () => {
        setLoadingDistricts(true);
        try {
          const query = `${space.city}, ${space.country}`;
          const res = await fetch(
            `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&limit=10&featuretype=settlement`
          );
          const data = await res.json();
          
          // Extract unique districts/suburbs from the results
          const districtSet = new Set();
          data.forEach(item => {
            if (item.address) {
              // Try different address components that might represent districts
              const district = item.address.suburb || 
                              item.address.district || 
                              item.address.neighbourhood || 
                              item.address.quarter ||
                              item.address.city_district;
              if (district && district !== space.city) {
                districtSet.add(district);
              }
            }
          });
          
          const uniqueDistricts = Array.from(districtSet).slice(0, 20); // Limit to 20
          setDistricts(uniqueDistricts.length > 0 ? uniqueDistricts : [
            `${space.city} Central`,
            `${space.city} Downtown`,
            `${space.city} Old Town`
          ]);
        } catch (err) {
          console.error("Failed to fetch districts", err);
          // Fallback to generic districts
          setDistricts([
            `${space.city} Central`,
            `${space.city} Downtown`,
            `${space.city} Old Town`
          ]);
        } finally {
          setLoadingDistricts(false);
        }
      };
      fetchDistricts();
    } else {
      setDistricts([]);
    }
  }, [space.city, space.country, isEditingLocation]);

  // Fetch streets when district changes using Nominatim API
  useEffect(() => {
    if (space.district && space.city && space.country && isEditingLocation) {
      const fetchStreets = async () => {
        setLoadingStreets(true);
        try {
          const query = `${space.district}, ${space.city}, ${space.country}`;
          const res = await fetch(
            `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&limit=20&featuretype=way&class=highway`
          );
          const data = await res.json();
          
          // Extract unique street names
          const streetSet = new Set();
          data.forEach(item => {
            if (item.address && item.address.road) {
              streetSet.add(item.address.road);
            }
            // Also check display_name for street patterns
            if (item.display_name) {
              const parts = item.display_name.split(',');
              if (parts.length > 0) {
                const streetPart = parts[0].trim();
                if (streetPart && (streetPart.includes('Street') || streetPart.includes('Avenue') || 
                    streetPart.includes('Road') || streetPart.includes('Boulevard') || 
                    streetPart.includes('Lane') || streetPart.includes('Way'))) {
                  streetSet.add(streetPart);
                }
              }
            }
          });
          
          const uniqueStreets = Array.from(streetSet).slice(0, 15); // Limit to 15
          setStreets(uniqueStreets.length > 0 ? uniqueStreets : [
            `${space.district} Main Street`,
            `${space.district} Central Avenue`,
            `${space.district} Park Road`
          ]);
        } catch (err) {
          console.error("Failed to fetch streets", err);
          // Fallback to generic streets
          setStreets([
            `${space.district} Main Street`,
            `${space.district} Central Avenue`,
            `${space.district} Park Road`
          ]);
        } finally {
          setLoadingStreets(false);
        }
      };
      fetchStreets();
    } else {
      setStreets([]);
    }
  }, [space.district, space.city, space.country, isEditingLocation]);

  // Fetch cities for new node when country changes
  useEffect(() => {
    if (newNodeLocation.country && showLocationSection) {
      const fetchCitiesForNewNode = async () => {
        setLoadingCities(true);
        
        // Turkish cities fallback for when API is slow
        const turkishCities = [
          "Istanbul", "Ankara", "Izmir", "Bursa", "Antalya", "Adana", "Konya", 
          "Gaziantep", "Mersin", "Diyarbakir", "Kayseri", "Eskisehir", "Urfa", 
          "Malatya", "Erzurum", "Van", "Batman", "Elazig", "Denizli", "Samsun",
          "Kahramanmaras", "Adapazari", "Trabzon", "Manisa", "Balikesir", "Aydin",
          "Tekirdag", "Sivas", "Afyon", "Isparta", "Inegol", "Ordu", "Usak",
          "Corlu", "Kutahya", "Edirne", "Soma", "Rize", "Giresun", "Tokat"
        ];

        try {
          const controller = new AbortController();
          const timeoutId = setTimeout(() => controller.abort(), 5000); // 5 second timeout

          const res = await fetch("https://countriesnow.space/api/v0.1/countries/cities", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ country: newNodeLocation.country }),
            signal: controller.signal
          });
          
          clearTimeout(timeoutId);
          
          const data = await res.json();
          if (data.error === false) {
            setCities(data.data || []);
          } else {
            console.error("API Error:", data.msg);
            // Use fallback for Turkey if API returns error
            if (newNodeLocation.country.toLowerCase() === 'turkey') {
              setCities(turkishCities);
            } else {
              setCities([]);
            }
          }
        } catch (err) {
          if (err.name === 'AbortError') {
            console.log('City fetch timed out, using fallback cities');
            // Use fallback cities for Turkey when request times out
            if (newNodeLocation.country.toLowerCase() === 'turkey') {
              setCities(turkishCities);
            } else {
              setCities([]);
            }
          } else {
            console.error("Failed to fetch cities", err);
            // Use fallback for Turkey on any error
            if (newNodeLocation.country.toLowerCase() === 'turkey') {
              setCities(turkishCities);
            } else {
              setCities([]);
            }
          }
        } finally {
          setLoadingCities(false);
        }
      };
      fetchCitiesForNewNode();
    } else {
      setCities([]);
    }
  }, [newNodeLocation.country, showLocationSection]);

  // Fetch districts for new node when city changes
  useEffect(() => {
    if (newNodeLocation.city && newNodeLocation.country && showLocationSection) {
      const fetchDistrictsForNewNode = async () => {
        setLoadingDistricts(true);
        try {
          const query = `${newNodeLocation.city}, ${newNodeLocation.country}`;
          const res = await fetch(
            `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&limit=10&featuretype=settlement`
          );
          const data = await res.json();
          
          const districtSet = new Set();
          data.forEach(item => {
            if (item.address) {
              const district = item.address.suburb || 
                              item.address.district || 
                              item.address.neighbourhood || 
                              item.address.quarter ||
                              item.address.city_district;
              if (district && district !== newNodeLocation.city) {
                districtSet.add(district);
              }
            }
          });
          
          if (districtSet.size > 0) {
            setDistricts(Array.from(districtSet).sort());
          } else {
            setDistricts([
              `${newNodeLocation.city} Central`,
              `${newNodeLocation.city} North`,
              `${newNodeLocation.city} South`
            ]);
          }
        } catch (err) {
          console.error("Failed to fetch districts", err);
          setDistricts([]);
        } finally {
          setLoadingDistricts(false);
        }
      };
      fetchDistrictsForNewNode();
    } else {
      setDistricts([]);
    }
  }, [newNodeLocation.city, newNodeLocation.country, showLocationSection]);

  // Fetch streets for new node when district changes
  useEffect(() => {
    if (newNodeLocation.district && newNodeLocation.city && newNodeLocation.country && showLocationSection) {
      const fetchStreetsForNewNode = async () => {
        setLoadingStreets(true);
        try {
          const query = `${newNodeLocation.district}, ${newNodeLocation.city}, ${newNodeLocation.country}`;
          const res = await fetch(
            `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&limit=20`
          );
          const data = await res.json();
          
          const streetSet = new Set();
          data.forEach(item => {
            if (item.address && item.address.road) {
              streetSet.add(item.address.road);
            }
          });
          
          if (streetSet.size > 0) {
            setStreets(Array.from(streetSet).sort());
          } else {
            setStreets([
              `${newNodeLocation.district} Main Street`,
              `${newNodeLocation.district} Central Avenue`,
              `${newNodeLocation.district} Park Road`
            ]);
          }
        } catch (err) {
          console.error("Failed to fetch streets", err);
          setStreets([]);
        } finally {
          setLoadingStreets(false);
        }
      };
      fetchStreetsForNewNode();
    } else {
      setStreets([]);
    }
  }, [newNodeLocation.district, newNodeLocation.city, newNodeLocation.country, showLocationSection]);

  const handleSearch = async () => {
    await search(query);
  };

  const handleEntitySelection = async (e) => {
    const entityId = e.target.value;
    if (!entityId) {
      setSelectedEntity(null);
      setEntityProperties([]);
      // Reset location when no entity is selected
      setNewNodeLocation({
        country: '',
        city: '',
        district: '',
        street: '',
        latitude: null,
        longitude: null,
        location_name: ''
      });
      return;
    }

    const entity = searchResults.find((entity) => entity.id === entityId);
    setSelectedEntity(entity);
    
    // Reset location for new entity
    setNewNodeLocation({
      country: '',
      city: '',
      district: '',
      street: '',
      latitude: null,
      longitude: null,
      location_name: ''
    });
    
    try {
      const properties = await fetchProperties(entityId);
      setEntityProperties(properties);
      setSelectedProperties([]);
    } catch (err) {
      console.error(err);
    }
  };

  const handlePropertySelection = (newSelectedProperties) => {
    setSelectedProperties(newSelectedProperties);
  };

  const handleLocationChange = (field, value) => {
    setSpace((prev) => ({ ...prev, [field]: value }));
    
    // Reset dependent fields when parent changes
    if (field === 'country') {
      setSpace((prev) => ({ ...prev, city: "", district: "", street: "" }));
      setDistricts([]);
      setStreets([]);
    } else if (field === 'city') {
      setSpace((prev) => ({ ...prev, district: "", street: "" }));
      setStreets([]);
    } else if (field === 'district') {
      setSpace((prev) => ({ ...prev, street: "" }));
    }
  };

  const handleUpdateLocation = async () => {
    setUpdatingLocation(true);
    try {
      const response = await api.patch(
        `/spaces/${id}/`,
        {
          country: space.country,
          city: space.city,
          district: space.district || null,
          street: space.street || null,
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
            "Content-Type": "application/json",
          },
        }
      );

      // Update the space with the response data
      setSpace(prev => ({
        ...prev,
        country: response.data.country || "",
        city: response.data.city || "",
        district: response.data.district || "",
        street: response.data.street || "",
      }));

      setIsEditingLocation(false);
      alert("Location updated successfully!");
    } catch (error) {
      console.error("Error updating location:", error);
      alert("Failed to update location. Please try again.");
    } finally {
      setUpdatingLocation(false);
    }
  };

  // Node creation location functions
  const handleNewNodeLocationChange = (field, value) => {
    setNewNodeLocation((prev) => ({ ...prev, [field]: value }));
    
    // Reset dependent fields when parent changes
    if (field === 'country') {
      setNewNodeLocation((prev) => ({ ...prev, city: "", district: "", street: "" }));
      setDistricts([]);
      setStreets([]);
    } else if (field === 'city') {
      setNewNodeLocation((prev) => ({ ...prev, district: "", street: "" }));
      setStreets([]);
    } else if (field === 'district') {
      setNewNodeLocation((prev) => ({ ...prev, street: "" }));
    }
  };

  const handleJoinLeaveSpace = async () => {
    try {
      const endpoint = isCollaborator ? "leave" : "join";
      await api.post(
        `/spaces/${id}/${endpoint}/`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      setIsCollaborator(!isCollaborator);

      const spaceResponse = await api.get(API_ENDPOINTS.SPACES + `/${id}/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      setSpace({
        title: spaceResponse.data.title,
        description: spaceResponse.data.description,
        tags: spaceResponse.data.tags || [],
        collaborators: spaceResponse.data.collaborators || [],
        creator_username: spaceResponse.data.creator_username,
        country: spaceResponse.data.country || "",
        city: spaceResponse.data.city || "",
        district: spaceResponse.data.district || "",
        street: spaceResponse.data.street || "",
      });
    } catch (error) {
      console.error("Error joining/leaving space:", error);
    }
  };

  const handleNodeClick = useCallback((event, node) => {
    setSelectedNode(node);
  }, []);

  const handleCloseModal = () => {
    setSelectedNode(null);
  };

  const handleNodeDelete = useCallback(async () => {
    try {
      await api.post(
        `/spaces/${id}/snapshots/create/`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      fetchGraphData();
      const nodesResponse = await api.get(API_ENDPOINTS.NODES(id), {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      setExistingNodes(nodesResponse.data);
    } catch (err) {
      console.error("Failed to refresh data after node deletion:", err);
    }
  }, [id, fetchGraphData]);

  const handleNodeUpdate = useCallback(async () => {
    try {
      await api.post(
        `/spaces/${id}/snapshots/create/`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      
      // Refresh graph data
      await fetchGraphData();
      
      // If there's a selected node, we need to update it with fresh data
      if (selectedNode) {
        // Re-fetch the nodes to get updated data
        const nodesResponse = await api.get(`/spaces/${id}/nodes/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        
        // Find the updated node data
        const updatedNodeData = nodesResponse.data.find(n => n.id.toString() === selectedNode.id);
        if (updatedNodeData) {
          // Create updated node object with new location data
          const updatedNode = {
            ...selectedNode,
            data: {
              ...selectedNode.data,
              country: updatedNodeData.country || null,
              city: updatedNodeData.city || null,
              district: updatedNodeData.district || null,
              street: updatedNodeData.street || null,
              latitude: updatedNodeData.latitude || null,
              longitude: updatedNodeData.longitude || null,
              location_name: updatedNodeData.location_name || null,
            },
            // Also at top level for backward compatibility
            country: updatedNodeData.country || null,
            city: updatedNodeData.city || null,
            district: updatedNodeData.district || null,
            street: updatedNodeData.street || null,
            latitude: updatedNodeData.latitude || null,
            longitude: updatedNodeData.longitude || null,
            location_name: updatedNodeData.location_name || null,
          };
          setSelectedNode(updatedNode);
        }
      }
    } catch (err) {
      console.error("Failed to refresh data after node update:", err);
    }
  }, [id, fetchGraphData, selectedNode]);

  const handleEdgeClick = useCallback((event, edge) => {
    setSelectedEdge(edge);
  }, []);

  const canDeleteSpace = () => {
    const username = localStorage.getItem("username");
    const isStaff =
      localStorage.getItem("is_staff") === "true" ||
      (window.currentUser && window.currentUser.is_staff);
    const isSuperuser =
      localStorage.getItem("is_superuser") === "true" ||
      (window.currentUser && window.currentUser.is_superuser);
    return (
      (space.creator_username && space.creator_username === username) ||
      isStaff ||
      isSuperuser
    );
  };

  const canEditSpaceLocation = () => {
    const username = localStorage.getItem("username");
    const isStaff =
      localStorage.getItem("is_staff") === "true" ||
      (window.currentUser && window.currentUser.is_staff);
    const isSuperuser =
      localStorage.getItem("is_superuser") === "true" ||
      (window.currentUser && window.currentUser.is_superuser);
    
    return (
      (space.creator_username && space.creator_username === username) ||
      isStaff ||
      isSuperuser
    );
  };

  const handleDeleteClick = () => {
    setShowDeleteModal(true);
    setDeleteError("");
  };

  const handleConfirmDelete = async () => {
    setDeleting(true);
    setDeleteError("");
    try {
      await api.delete(`/spaces/${id}/`);
      setShowDeleteModal(false);
      navigate("/");
    } catch (err) {
      setDeleteError(
        err.response?.data?.detail ||
          "Failed to delete space. Please try again."
      );
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div
      style={{
        width: "100%",
        margin: "0 auto",
        padding: "20px",
        display: "flex",
        overflowX: "hidden",
        boxSizing: "border-box",
        maxWidth: "100vw",
      }}
    >
      {/* Inject CSS for property selection */}
      <style>{propertySelectionStyles}</style>

      <div style={{ flex: 1, marginRight: "20px" }}>
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            gap: 8,
          }}
        >
          <h2 style={{ margin: 0 }}>{space.title}</h2>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <button
              onClick={() => setShowSpaceMap(true)}
              style={{
                background: '#28a745',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                padding: '8px 16px',
                fontSize: '14px',
                fontWeight: '600',
                cursor: 'pointer',
                transition: 'background 0.2s',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}
              onMouseOver={(e) => e.currentTarget.style.background = '#218838'}
              onMouseOut={(e) => e.currentTarget.style.background = '#28a745'}
            >
              🗺️ Show Space Map
            </button>
            <button
              className={isCollaborator ? "leave-button" : "join-button"}
              onClick={handleJoinLeaveSpace}
              data-testid={
                isCollaborator
                  ? "leave-space-button"
                  : "header-join-space-button"
              }
            >
              {isCollaborator ? "LEAVE SPACE" : "JOIN SPACE"}
            </button>
            {canDeleteSpace() && (
              <button
                className="delete-button"
                title="Delete"
                style={{
                  background: 'var(--color-danger-light)',
                  color: 'var(--color-white)',
                  border: "none",
                  borderRadius: 4,
                  fontWeight: 600,
                  padding: "6px 16px",
                  marginLeft: 8,
                  cursor: "pointer",
                  transition: "background 0.2s",
                }}
                onClick={handleDeleteClick}
                disabled={deleting}
                onMouseOver={(e) =>
                  (e.currentTarget.style.background = 'var(--color-danger-dark)')
                }
                onMouseOut={(e) =>
                  (e.currentTarget.style.background = 'var(--color-danger-light)')
                }
              >
                Delete
              </button>
            )}
          </div>
        </div>
        <p>{space.description}</p>
        
        {/* Location Section */}
        <div style={{ marginTop: "20px", marginBottom: "20px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "10px" }}>
            <h4 style={{ margin: 0 }}>Location:</h4>
            {canEditSpaceLocation() && !isEditingLocation && (
              <button
                onClick={() => setIsEditingLocation(true)}
                style={{
                  background: '#007bff',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  padding: '4px 8px',
                  fontSize: '12px',
                  cursor: 'pointer',
                  transition: 'background 0.2s',
                }}
                onMouseOver={(e) => e.currentTarget.style.background = '#0056b3'}
                onMouseOut={(e) => e.currentTarget.style.background = '#007bff'}
              >
                Edit Location
              </button>
            )}
          </div>
          
          {!isEditingLocation ? (
            // Display current location
            <div style={{ 
              padding: "10px", 
              backgroundColor: "#f8f9fa", 
              borderRadius: "4px",
              fontSize: "14px",
              color: "#666"
            }}>
              {space.country || space.city || space.district || space.street ? (
                [space.street, space.district, space.city, space.country]
                  .filter(Boolean)
                  .join(', ')
              ) : (
                "Location not specified"
              )}
            </div>
          ) : (
            // Edit location form
            <div style={{ 
              padding: "15px", 
              backgroundColor: "#f8f9fa", 
              borderRadius: "4px",
              border: "1px solid #ddd"
            }}>
              {/* Country */}
              <div style={{ marginBottom: "10px" }}>
                <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
                  Country:
                </label>
                <select 
                  value={space.country} 
                  onChange={(e) => handleLocationChange('country', e.target.value)}
                  style={{
                    width: "100%",
                    padding: "8px",
                    border: "1px solid #ccc",
                    borderRadius: "4px"
                  }}
                  required
                >
                  <option value="">-- Select Country --</option>
                  {countries.map((c) => (
                    <option key={c.name} value={c.name}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              {/* City */}
              {space.country && (
                <div style={{ marginBottom: "10px" }}>
                  <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
                    City:
                  </label>
                  <select 
                    value={space.city} 
                    onChange={(e) => handleLocationChange('city', e.target.value)}
                    disabled={loadingCities}
                    style={{
                      width: "100%",
                      padding: "8px",
                      border: "1px solid #ccc",
                      borderRadius: "4px"
                    }}
                    required
                  >
                    <option value="">{loadingCities ? "Loading cities..." : "-- Select City --"}</option>
                    {cities.map((city) => (
                      <option key={city} value={city}>
                        {city}
                      </option>
                    ))}
                  </select>
                  {loadingCities && (
                    <small style={{ color: "#666", fontSize: "12px" }}>
                      Fetching cities for {space.country}...
                    </small>
                  )}
                </div>
              )}

              {/* District */}
              {space.city && (
                <div style={{ marginBottom: "10px" }}>
                  <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
                    District (optional):
                  </label>
                  <select 
                    value={space.district} 
                    onChange={(e) => handleLocationChange('district', e.target.value)}
                    disabled={loadingDistricts}
                    style={{
                      width: "100%",
                      padding: "8px",
                      border: "1px solid #ccc",
                      borderRadius: "4px"
                    }}
                  >
                    <option value="">{loadingDistricts ? "Loading districts..." : "-- Select District --"}</option>
                    {districts.map((d) => (
                      <option key={d} value={d}>
                        {d}
                      </option>
                    ))}
                  </select>
                  {loadingDistricts && (
                    <small style={{ color: "#666", fontSize: "12px" }}>
                      Fetching districts for {space.city}...
                    </small>
                  )}
                </div>
              )}

              {/* Street */}
              {space.district && (
                <div style={{ marginBottom: "15px" }}>
                  <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold" }}>
                    Street (optional):
                  </label>
                  <select 
                    value={space.street} 
                    onChange={(e) => handleLocationChange('street', e.target.value)}
                    disabled={loadingStreets}
                    style={{
                      width: "100%",
                      padding: "8px",
                      border: "1px solid #ccc",
                      borderRadius: "4px"
                    }}
                  >
                    <option value="">{loadingStreets ? "Loading streets..." : "-- Select Street --"}</option>
                    {streets.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                  {loadingStreets && (
                    <small style={{ color: "#666", fontSize: "12px" }}>
                      Fetching streets for {space.district}...
                    </small>
                  )}
                </div>
              )}

              {/* Action buttons */}
              <div style={{ display: "flex", gap: "10px" }}>
                <button
                  onClick={handleUpdateLocation}
                  disabled={updatingLocation || !space.country}
                  style={{
                    background: updatingLocation ? '#ccc' : 'var(--color-success)',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    padding: '8px 16px',
                    cursor: updatingLocation || !space.country ? 'not-allowed' : 'pointer',
                  }}
                >
                  {updatingLocation ? "Updating..." : "Update Location"}
                </button>
                <button
                  onClick={() => setIsEditingLocation(false)}
                  disabled={updatingLocation}
                  style={{
                    background: '#6c757d',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    padding: '8px 16px',
                    cursor: updatingLocation ? 'not-allowed' : 'pointer',
                  }}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>

        <div style={{ marginTop: "10px", marginBottom: "20px" }}>
          {space.tags.map((tag) => (
            <span
              key={tag.id || tag.name}
              style={{
                display: "inline-block",
                backgroundColor: 'var(--color-teal-dark)',
                color: 'var(--color-white)',
                padding: "3px 8px",
                borderRadius: "12px",
                fontSize: "12px",
                marginRight: "5px",
                marginBottom: "5px",
              }}
            >
              {tag.name}
              {tag.wikidata_label && (
                <span
                  style={{ marginLeft: "5px", fontSize: "10px", color: 'var(--color-white)', opacity: 0.8 }}
                >
                  (Wikidata label: {tag.wikidata_label})
                </span>
              )}
            </span>
          ))}
        </div>

        {/* Graph Visualization */}
        <div style={{ marginBottom: "30px" }}>
          <h3>Space Graph</h3>
          <div
            style={{
              width: "100%",
              height: "600px",
              border: "1px solid #ddd",
              borderRadius: "8px",
              overflow: "hidden",
              background: "#f8f9fa",
            }}
          >
            {graphLoading ? (
              <div>Loading graph...</div>
            ) : graphError ? (
              <div>Error: {graphError}</div>
            ) : (
              <ReactFlow
                nodes={nodes}
                edges={edges}
                nodeTypes={nodeTypes}
                onNodeClick={handleNodeClick}
                onEdgeClick={handleEdgeClick}
                fitView
              >
                <Background />
                <Controls />
              </ReactFlow>
            )}
          </div>
        </div>

        {/* Existing nodes list */}
        {existingNodes.length === 0 && (
          <p>This space has no nodes yet. Start by adding one from Wikidata!</p>
        )}
        <ol className="nodes-list">
          {existingNodes.map((node) => (
            <li key={node.id} className="node-item">
              <strong>{node.label}</strong>
            </li>
          ))}
        </ol>

        {/* Revert Graph Section - Only show if collaborator */}
        {isCollaborator && (
          <>
            <h3>Revert Graph to Previous State</h3>
            {snapshots.length > 0 ? (
              <select
                onChange={(e) => {
                  const snapshotId = e.target.value;
                  if (!snapshotId) return;

                  api
                    .post(
                      `/spaces/${id}/snapshots/revert/`,
                      { snapshot_id: snapshotId },
                      {
                        headers: {
                          Authorization: `Bearer ${localStorage.getItem(
                            "token"
                          )}`,
                          "Content-Type": "application/json",
                        },
                      }
                    )
                    .then(() => {
                      alert("Graph reverted successfully!");
                      window.location.reload();
                    })
                    .catch((err) => {
                      alert("Revert failed");
                      console.error(err);
                    });
                }}
              >
                <option value="">Select a snapshot to revert...</option>
                {snapshots.map((snap) => (
                  <option key={snap.id} value={snap.id}>
                    {new Date(snap.created_at).toLocaleString()}
                  </option>
                ))}
              </select>
            ) : (
              <p>No snapshots available for this space yet.</p>
            )}
            <hr />
          </>
        )}

        {/* Add Node Section - Only show if collaborator */}
        {isCollaborator && (
          <>
            <h3>Add Node from Wikidata</h3>
            <div>
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Search Wikidata..."
              />
              <button onClick={handleSearch}>Search</button>
            </div>

            {searchResults.length > 0 && (
              <div>
                <h4>Select Entity</h4>
                <select
                  value={selectedEntity?.id || ""}
                  onChange={handleEntitySelection}
                  style={{ width: "100%", maxWidth: "500px" }}
                >
                  <option value="">-- Select an entity --</option>
                  {searchResults.map((entity) => (
                    <option key={entity.id} value={entity.id}>
                      {entity.label} ({entity.description})
                    </option>
                  ))}
                </select>
              </div>
            )}

            {selectedEntity && entityProperties.length > 0 && (
              <div>
                <h4>Selected Entity: {selectedEntity.label}</h4>
                <div>
                  <h5>Select Properties</h5>
                  <p className="selection-help-text">
                    Click on a property to select/deselect it
                  </p>
                  <input
                    type="text"
                    placeholder="Search properties..."
                    value={propertySearch}
                    onChange={(e) => setPropertySearch(e.target.value)}
                    style={{
                      width: "100%",
                      padding: "8px",
                      marginBottom: "10px",
                      boxSizing: "border-box",
                    }}
                  />
                  <PropertySelectionList
                    properties={filteredAndSortedProperties}
                    selectedProperties={selectedProperties}
                    onChange={handlePropertySelection}
                  />
                </div>
                <div style={{ marginTop: "10px" }}>
                  <label>Edge Direction:</label>
                  <button
                    onClick={() => setIsNewNodeSource(!isNewNodeSource)}
                  style={{
                    marginLeft: "10px",
                    padding: "5px 10px",
                    backgroundColor: isNewNodeSource ? 'var(--color-success)' : 'var(--color-danger)',
                    color: 'var(--color-white)',
                    border: "none",
                    borderRadius: "4px",
                    cursor: "pointer",
                  }}
                >
                    {isNewNodeSource ? "New → Existing" : "Existing → New"}
                  </button>
                </div>
                <div
                  style={{ marginTop: "10px", color: 'var(--color-text-secondary)', fontSize: "14px" }}
                >
                  {isNewNodeSource
                    ? `"${selectedEntity?.label || "New Node"}" → "${
                        existingNodes.find(
                          (n) => n.id === parseInt(relatedNodeId)
                        )?.label || "Selected Node"
                      }"`
                    : `"${
                        existingNodes.find(
                          (n) => n.id === parseInt(relatedNodeId)
                        )?.label || "Selected Node"
                      }" → "${selectedEntity?.label || "New Node"}"`}
                </div>
                <div style={{ marginTop: "20px" }}>
                  <label>Connect To Node:</label>
                  <select
                    value={relatedNodeId}
                    onChange={(e) => setRelatedNodeId(e.target.value)}
                    style={{ width: "100%", maxWidth: "500px" }}
                  >
                    <option value="">Select node to relate</option>
                    {existingNodes.map((node) => (
                      <option key={node.id} value={node.id}>
                        {node.label}
                      </option>
                    ))}
                  </select>
                </div>
                <div style={{ marginTop: "10px" }}>
                  <label>Edge Label:</label>
                  <PropertySearch 
                    onSelect={setEdgeProperty} 
                    initialLabel={edgeProperty.label}
                  />
                </div>

                {/* Location Section for New Node */}
                <div style={{ marginTop: "20px", padding: "15px", backgroundColor: "#f8f9fa", borderRadius: "8px" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "15px" }}>
                    <h5 style={{ margin: 0 }}>Location (Optional):</h5>
                    <button
                      type="button"
                      onClick={() => setShowLocationSection(!showLocationSection)}
                      style={{
                        background: showLocationSection ? '#dc3545' : '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        padding: '4px 8px',
                        fontSize: '12px',
                        cursor: 'pointer'
                      }}
                    >
                      {showLocationSection ? 'Hide Location' : 'Add Location'}
                    </button>
                  </div>

                  {showLocationSection && (
                    <div style={{ display: "grid", gap: "15px" }}>
                      {/* Country */}
                      <div>
                        <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                          Country:
                        </label>
                        <select 
                          value={newNodeLocation.country} 
                          onChange={(e) => handleNewNodeLocationChange('country', e.target.value)}
                          style={{
                            width: "100%",
                            padding: "6px",
                            border: "1px solid #ccc",
                            borderRadius: "4px",
                            fontSize: "13px"
                          }}
                        >
                          <option value="">-- Select Country --</option>
                          {countries.map((c) => (
                            <option key={c.name} value={c.name}>
                              {c.name}
                            </option>
                          ))}
                        </select>
                      </div>

                      {/* City */}
                      {newNodeLocation.country && (
                        <div>
                          <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                            City:
                          </label>
                          <select 
                            value={newNodeLocation.city} 
                            onChange={(e) => handleNewNodeLocationChange('city', e.target.value)}
                            disabled={loadingCities}
                            style={{
                              width: "100%",
                              padding: "6px",
                              border: "1px solid #ccc",
                              borderRadius: "4px",
                              fontSize: "13px"
                            }}
                          >
                            <option value="">{loadingCities ? "Loading cities..." : "-- Select City --"}</option>
                            {cities.map((c) => (
                              <option key={c} value={c}>
                                {c}
                              </option>
                            ))}
                          </select>
                          {loadingCities && (
                            <small style={{ color: "#666", fontSize: "11px" }}>
                              Fetching cities for {newNodeLocation.country}...
                            </small>
                          )}
                        </div>
                      )}

                      {/* District */}
                      {newNodeLocation.city && (
                        <div>
                          <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                            District (optional):
                          </label>
                          <select 
                            value={newNodeLocation.district} 
                            onChange={(e) => handleNewNodeLocationChange('district', e.target.value)}
                            disabled={loadingDistricts}
                            style={{
                              width: "100%",
                              padding: "6px",
                              border: "1px solid #ccc",
                              borderRadius: "4px",
                              fontSize: "13px"
                            }}
                          >
                            <option value="">{loadingDistricts ? "Loading districts..." : "-- Select District --"}</option>
                            {districts.map((d) => (
                              <option key={d} value={d}>
                                {d}
                              </option>
                            ))}
                          </select>
                          {loadingDistricts && (
                            <small style={{ color: "#666", fontSize: "11px" }}>
                              Fetching districts for {newNodeLocation.city}...
                            </small>
                          )}
                        </div>
                      )}

                      {/* Street */}
                      {newNodeLocation.district && (
                        <div>
                          <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                            Street (optional):
                          </label>
                          <select 
                            value={newNodeLocation.street} 
                            onChange={(e) => handleNewNodeLocationChange('street', e.target.value)}
                            disabled={loadingStreets}
                            style={{
                              width: "100%",
                              padding: "6px",
                              border: "1px solid #ccc",
                              borderRadius: "4px",
                              fontSize: "13px"
                            }}
                          >
                            <option value="">{loadingStreets ? "Loading streets..." : "-- Select Street --"}</option>
                            {streets.map((s) => (
                              <option key={s} value={s}>
                                {s}
                              </option>
                            ))}
                          </select>
                          {loadingStreets && (
                            <small style={{ color: "#666", fontSize: "11px" }}>
                              Fetching streets for {newNodeLocation.district}...
                            </small>
                          )}
                        </div>
                      )}

                      {/* Manual Coordinates */}
                      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "10px" }}>
                        <div>
                          <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                            Latitude:
                          </label>
                          <input 
                            type="number"
                            step="any"
                            value={newNodeLocation.latitude || ''} 
                            onChange={(e) => handleNewNodeLocationChange('latitude', parseFloat(e.target.value) || null)}
                            placeholder="e.g., 41.0082"
                            style={{
                              width: "100%",
                              padding: "6px",
                              border: "1px solid #ccc",
                              borderRadius: "4px",
                              fontSize: "13px"
                            }}
                          />
                        </div>
                        <div>
                          <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                            Longitude:
                          </label>
                          <input 
                            type="number"
                            step="any"
                            value={newNodeLocation.longitude || ''} 
                            onChange={(e) => handleNewNodeLocationChange('longitude', parseFloat(e.target.value) || null)}
                            placeholder="e.g., 28.9784"
                            style={{
                              width: "100%",
                              padding: "6px",
                              border: "1px solid #ccc",
                              borderRadius: "4px",
                              fontSize: "13px"
                            }}
                          />
                        </div>
                      </div>

                      {/* Location Name */}
                      <div>
                        <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                          Location Name (optional):
                        </label>
                        <input 
                          type="text"
                          value={newNodeLocation.location_name} 
                          onChange={(e) => handleNewNodeLocationChange('location_name', e.target.value)}
                          placeholder="Enter location name manually"
                          style={{
                            width: "100%",
                            padding: "6px",
                            border: "1px solid #ccc",
                            borderRadius: "4px",
                            fontSize: "13px"
                          }}
                        />
                      </div>
                    </div>
                  )}
                </div>

                <button
                  style={{ marginTop: "20px" }}
                  disabled={!selectedEntity}
                  onClick={() => {
                    const fullSelectedProperties = selectedProperties.map(
                      (statementId) =>
                        entityProperties.find(
                          (p) => p.statement_id === statementId
                        )
                    );
                    api
                      .post(
                        `/spaces/${id}/add-node/`,
                        {
                          related_node_id: relatedNodeId,
                          wikidata_entity: selectedEntity,
                          selected_properties: fullSelectedProperties,
                          edge_label: edgeProperty.label,
                          wikidata_property_id: edgeProperty.id,
                          is_new_node_source: isNewNodeSource,
                          location: newNodeLocation,
                        },
                        {
                          headers: {
                            Authorization: `Bearer ${localStorage.getItem(
                              "token"
                            )}`,
                          },
                        }
                      )
                      .then(async () => {
                        alert("Node and edge added successfully!");

                        try {
                          await api.post(
                            `/spaces/${id}/snapshots/create/`,
                            {},
                            {
                              headers: {
                                Authorization: `Bearer ${localStorage.getItem(
                                  "token"
                                )}`,
                              },
                            }
                          );
                        } catch (err) {
                          console.error("Failed to create snapshot:", err);
                        }

                        window.location.reload();
                      })
                      .catch((err) => {
                        console.error(err);
                        alert("Failed to add node.");
                      });
                  }}
                >
                  Add Node with Edge
                </button>
              </div>
            )}
          </>
        )}

        {!isCollaborator && (
          <div className="non-collaborator-box">
            <p>
              Join this space as a collaborator to add nodes and modify the
              graph.
            </p>
            <button
              className="join-button"
              onClick={handleJoinLeaveSpace}
              data-testid="bottom-join-space-button"
            >
              JOIN SPACE
            </button>
          </div>
        )}

        {/* Delete Confirmation Modal */}
        {showDeleteModal && (
          <div className="modal-backdrop">
            <div className="modal-content">
              <div className="modal-header">
                <h3>Confirm Delete</h3>
              </div>
              <div className="modal-body">
                <p>
                  Are you sure you want to delete the space "{space.title}"?
                  This action cannot be undone.
                </p>
                {deleteError && (
                  <div style={{ color: "#BD4902" }}>{deleteError}</div>
                )}
              </div>
              <div className="modal-footer">
                <button
                  onClick={() => setShowDeleteModal(false)}
                  disabled={deleting}
                >
                  Cancel
                </button>
                <button
                  onClick={handleConfirmDelete}
                  style={{ background: 'var(--color-danger)', color: 'var(--color-white)' }}
                  disabled={deleting}
                >
                  {deleting ? "Deleting..." : "Delete"}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Collaborators sidebar */}
      <div style={{ width: "250px" }}>
        <div
          style={{
            border: "1px solid #68686B",
            borderRadius: "4px",
            overflow: "hidden",
            backgroundColor: "#FFFFFF"
          }}
        >
          <div
            style={{
              backgroundColor: "#F5F5F5",
              padding: "10px",
              cursor: "pointer",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              color: "#1B1F3B"
            }}
            onClick={() => setIsCollaboratorsOpen(!isCollaboratorsOpen)}
          >
            <strong>Collaborators ({space.collaborators.length})</strong>
            <span>{isCollaboratorsOpen ? "▲" : "▼"}</span>
          </div>

          {isCollaboratorsOpen && (
            <div style={{ padding: "10px" }}>
              {space.collaborators.length > 0 ? (
                <ul style={{ listStyleType: "none", padding: 0 }}>
                  {space.collaborators.map((collaborator, index) => (
                    <li
                      key={index}
                      style={{
                        padding: "8px",
                        borderBottom:
                          index < space.collaborators.length - 1
                            ? `1px solid var(--color-gray-200)`
                            : "none",
                        cursor: "pointer",
                        color: "#1a73e8",
                        textDecoration: "underline",
                      }}
                      onClick={() => navigate(`/profile/${collaborator}`)}
                    >
                      {collaborator}
                    </li>
                  ))}
                </ul>
              ) : (
                <p>No collaborators yet</p>
              )}
            </div>
          )}
        </div>

        {/* Add discussions component */}
        <SpaceDiscussions spaceId={id} isCollaborator={isCollaborator} />
      </div>

      {/* Node detail modal */}
      {selectedNode && isCollaborator && (
        <NodeDetailModal
          node={selectedNode}
          onClose={handleCloseModal}
          onNodeDelete={handleNodeDelete}
          onNodeUpdate={handleNodeUpdate}
          spaceId={id}
          currentUser={localStorage.getItem("username")}
          spaceCreator={space.creator_username}
        />
      )}

      {selectedEdge && (
        <EdgeDetailModal
          edge={selectedEdge}
          sourceNode={nodes.find((n) => n.id === selectedEdge.source)}
          targetNode={nodes.find((n) => n.id === selectedEdge.target)}
          onClose={() => setSelectedEdge(null)}
          onEdgeUpdate={fetchGraphData}
          onEdgeDelete={fetchGraphData}
          spaceId={id}
        />
      )}

      {/* Space Map Modal */}
      <SpaceMapModal
        isOpen={showSpaceMap}
        onClose={() => setShowSpaceMap(false)}
        spaceId={id}
        spaceTitle={space.title}
      />

      {/* Report Modal */}
      {showReportModal && (
        <ReportModal
          contentId={id}
          contentType="Space"
          contentTitle={space.title}
          onClose={() => setShowReportModal(false)}
        />
      )}
    </div>
  );
};

export default SpaceDetails;
