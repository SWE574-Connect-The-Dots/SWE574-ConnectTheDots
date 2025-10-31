import { useState, useEffect, useMemo, useRef } from "react";
import PropTypes from "prop-types";
import api from "../axiosConfig";
import useWikidataSearch from "../hooks/useWikidataSearch";
import PropertySearch from "./PropertySearch";
import useClickOutside from "../hooks/useClickOutside";
import ReportModal from "./ReportModal";
import "./NodeDetailModal.css";

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

  const renderSelectionPropertyValue = (prop) => {
    if (
      prop && // Ensure prop exists
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
        {properties
          .filter((prop) => prop && prop.statement_id)
          .map((prop) => (
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
                onClick={(e) => e.stopPropagation()}
              />
              <label
                htmlFor={`prop-${prop.statement_id}`}
                className="property-selection-label"
                onClick={(e) => e.stopPropagation()}
              >
                <span className="property-label">
                  {getPropertyLabelWithId(prop)}:
                </span>{" "}
                {renderSelectionPropertyValue(prop)}
              </label>
            </div>
          ))}
      </div>
    </div>
  );
};

const NodeDetailModal = ({
  node,
  onClose,
  onNodeDelete,
  onNodeUpdate,
  spaceId,
  currentUser,
  spaceCreator,
}) => {
  const [nodeProperties, setNodeProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [availableProperties, setAvailableProperties] = useState([]);
  const [selectedProperties, setSelectedProperties] = useState([]);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [allNodes, setAllNodes] = useState([]);
  const [allEdges, setAllEdges] = useState([]);
  const [addEdgeTarget, setAddEdgeTarget] = useState("");
  const [addEdgeProperty, setAddEdgeProperty] = useState({
    id: null,
    label: "",
  });
  const [addEdgeError, setAddEdgeError] = useState(null);
  const [addEdgeLoading, setAddEdgeLoading] = useState(false);
  const [isCurrentNodeSource, setIsCurrentNodeSource] = useState(true);
  const [propertySearch, setPropertySearch] = useState("");
  const [showReportModal, setShowReportModal] = useState(false);

  // Report modal state
  const [showReportModal, setShowReportModal] = useState(false);

  // Location editing states
  const [isEditingLocation, setIsEditingLocation] = useState(false);
  const [nodeLocation, setNodeLocation] = useState({
    country: '',
    city: '',
    district: '',
    street: '',
    latitude: null,
    longitude: null,
    location_name: ''
  });
  const [countries, setCountries] = useState([]);
  const [cities, setCities] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [streets, setStreets] = useState([]);
  const [loadingCities, setLoadingCities] = useState(false);
  const [loadingDistricts, setLoadingDistricts] = useState(false);
  const [loadingStreets, setLoadingStreets] = useState(false);
  const [updatingLocation, setUpdatingLocation] = useState(false);
  const [forwardGeocodingLoading, setForwardGeocodingLoading] = useState(false);
  const [originalNodeLocation, setOriginalNodeLocation] = useState({
    country: '',
    city: '',
    district: '',
    street: '',
    latitude: null,
    longitude: null,
    location_name: ''
  });

  const { fetchProperties } = useWikidataSearch();

  // Extract location information from node properties
  const extractLocationFromNodeProperties = (properties) => {
    const location = {
      country: null,
      city: null,
      district: null,
      street: null,
      latitude: null,
      longitude: null,
      location_name: null
    };

    for (const prop of properties) {
      const propId = prop.property_id || prop.property;
      const value = prop.value;

      if (!propId || !value) continue;

      // Extract coordinates (P625)
      if (propId === 'P625') {
        const coordText = String(value);
        if (coordText.includes('Point(')) {
          try {
            const coords = coordText.replace('Point(', '').replace(')', '').trim();
            const [lon, lat] = coords.split(' ').map(parseFloat);
            if (!isNaN(lat) && !isNaN(lon)) {
              location.latitude = lat;
              location.longitude = lon;
            }
          } catch (error) {
            console.error('Error parsing coordinates:', error);
          }
        }
      }
      // Extract country (P17)
      else if (propId === 'P17') {
        location.country = String(value);
      }
      // Extract other location properties
      else if (['P131', 'P276', 'P159', 'P740', 'P19', 'P20', 'P551', 'P937'].includes(propId)) {
        if (!location.location_name) {
          location.location_name = String(value);
        }
        
        // Try to parse city/district from location text
        const locationText = String(value);
        if (locationText.includes(',')) {
          const parts = locationText.split(',').map(part => part.trim());
          if (parts.length >= 1 && !location.city) {
            location.city = parts[0];
          }
          if (parts.length >= 2 && !location.district) {
            location.district = parts[1];
          }
        }
      }
    }

    // Set a default location name if we have coordinates but no address info
    if (location.latitude && location.longitude && 
        !location.location_name && !location.country && !location.city && !location.district && !location.street) {
      location.location_name = `Location at ${location.latitude.toFixed(4)}, ${location.longitude.toFixed(4)}`;
    }

    return location;
  };

  const filteredAndSortedProperties = useMemo(() => {
    if (!availableProperties) return [];
    return availableProperties
      .filter((prop) => prop && prop.display && prop.property)
      .filter((prop) =>
        prop.display.toLowerCase().includes(propertySearch.toLowerCase())
      )
      .sort((a, b) => {
        const numA = parseInt(a.property.substring(1), 10);
        const numB = parseInt(b.property.substring(1), 10);
        return numA - numB;
      });
  }, [availableProperties, propertySearch]);

  useEffect(() => {
    const fetchNodeProperties = async () => {
      try {
        setLoading(true);
        const response = await api.get(
          `/spaces/${spaceId}/nodes/${node.id}/properties/`,
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }
        );
        setNodeProperties(response.data);

        // Extract location from current node properties
        console.log('Node properties for location extraction:', response.data);
        const extractedLocation = extractLocationFromNodeProperties(response.data);
        console.log('Extracted location:', extractedLocation);
        if (extractedLocation && Object.values(extractedLocation).some(val => val)) {
          setNodeLocation(prev => ({
            ...prev,
            ...extractedLocation
          }));
        }

        const wikidataId = node.data?.wikidata_id || node.wikidata_id;
        if (wikidataId) {
          const properties = await fetchProperties(wikidataId);
          setAvailableProperties(properties);

          const selectedPropertyIds = response.data.map(
            (prop) => prop.statement_id
          );
          setSelectedProperties(selectedPropertyIds);
        }
      } catch (err) {
        setError("Failed to load node properties");
      } finally {
        setLoading(false);
      }
    };

    fetchNodeProperties();
  }, [node.id, spaceId, fetchProperties]);

  useEffect(() => {
    const fetchNodesAndEdges = async () => {
      try {
        const [nodesRes, edgesRes] = await Promise.all([
          api.get(`/spaces/${spaceId}/nodes/`, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }),
          api.get(`/spaces/${spaceId}/edges/`, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }),
        ]);
        setAllNodes(nodesRes.data);
        setAllEdges(edgesRes.data);
      } catch (err) {}
    };
    fetchNodesAndEdges();
  }, [spaceId, node.id]);

  // Initialize node location from node data
  useEffect(() => {
    console.log('Initializing node location from node:', node);
    const locationData = {
      country: node.data?.country || node.country || '',
      city: node.data?.city || node.city || '',
      district: node.data?.district || node.district || '',
      street: node.data?.street || node.street || '',
      latitude: node.data?.latitude || node.latitude || null,
      longitude: node.data?.longitude || node.longitude || null,
      location_name: node.data?.location_name || node.location_name || ''
    };
    console.log('Setting node location to:', locationData);
    setNodeLocation(locationData);
    setOriginalNodeLocation(locationData); // Store original for comparison
  }, [node]);

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
    if (nodeLocation.country && isEditingLocation) {
      const fetchCities = async () => {
        setLoadingCities(true);
        try {
          // Create abort controller for timeout
          const controller = new AbortController();
          const timeoutId = setTimeout(() => controller.abort(), 5000); // 5 second timeout
          
          const res = await fetch("https://countriesnow.space/api/v0.1/countries/cities", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ country: nodeLocation.country }),
            signal: controller.signal
          });
          
          clearTimeout(timeoutId);
          
          const data = await res.json();
          if (data.error === false) {
            setCities(data.data || []);
          } else {
            console.error("API Error:", data.msg);
            // Fallback to major cities for Turkey
            if (nodeLocation.country.toLowerCase() === 'turkey') {
              setCities([
                'Istanbul', 'Ankara', 'Izmir', 'Bursa', 'Adana', 'Gaziantep', 
                'Konya', 'Antalya', 'Kayseri', 'Mersin', 'Eskişehir', 'Diyarbakır',
                'Samsun', 'Denizli', 'Şanlıurfa', 'Adapazarı', 'Malatya', 'Kahramanmaraş',
                'Erzurum', 'Van', 'Batman', 'Elazığ', 'İzmit', 'Manisa', 'Sivas',
                'Gebze', 'Balıkesir', 'Tarsus', 'Kütahya', 'Trabzon', 'Çorum',
                'Çorlu', 'Adıyaman', 'Osmaniye', 'Kırıkkale', 'Antakya', 'Aydın',
                'İskenderun', 'Uşak', 'Aksaray', 'Afyon', 'Isparta', 'İnegöl',
                'Tekirdağ', 'Edirne', 'Darıca', 'Ordu', 'Karaman', 'Gölcük',
                'Siirt', 'Körfez', 'Kızıltepe', 'Düzce', 'Tokat', 'Bolu',
                'Derince', 'Turgutlu', 'Bandırma', 'Nazilli', 'Kırşehir', 'Niğde'
              ]);
            } else {
              setCities([]);
            }
          }
        } catch (err) {
          console.error("Failed to fetch cities", err);
          // Fallback for timeout or network error
          if (err.name === 'AbortError') {
            console.log('Cities API request timed out, using fallback');
            if (nodeLocation.country.toLowerCase() === 'turkey') {
              setCities([
                'Istanbul', 'Ankara', 'Izmir', 'Bursa', 'Adana', 'Gaziantep', 
                'Konya', 'Antalya', 'Kayseri', 'Mersin', 'Eskişehir', 'Diyarbakır',
                'Samsun', 'Denizli', 'Şanlıurfa', 'Adapazarı', 'Malatya', 'Kahramanmaraş',
                'Erzurum', 'Van', 'Batman', 'Elazığ', 'İzmit', 'Manisa', 'Sivas'
              ]);
            } else {
              setCities([]);
            }
          } else {
            setCities([]);
          }
        } finally {
          setLoadingCities(false);
        }
      };
      fetchCities();
    } else {
      setCities([]);
    }
  }, [nodeLocation.country, isEditingLocation]);

  // Fetch districts when city changes using Nominatim API
  useEffect(() => {
    if (nodeLocation.city && nodeLocation.country && isEditingLocation) {
      const fetchDistricts = async () => {
        setLoadingDistricts(true);
        try {
          const query = `${nodeLocation.city}, ${nodeLocation.country}`;
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
              if (district && district !== nodeLocation.city) {
                districtSet.add(district);
              }
            }
          });
          
          const uniqueDistricts = Array.from(districtSet).slice(0, 20);
          setDistricts(uniqueDistricts.length > 0 ? uniqueDistricts : [
            `${nodeLocation.city} Central`,
            `${nodeLocation.city} Downtown`,
            `${nodeLocation.city} Old Town`
          ]);
        } catch (err) {
          console.error("Failed to fetch districts", err);
          setDistricts([
            `${nodeLocation.city} Central`,
            `${nodeLocation.city} Downtown`,
            `${nodeLocation.city} Old Town`
          ]);
        } finally {
          setLoadingDistricts(false);
        }
      };
      fetchDistricts();
    } else {
      setDistricts([]);
    }
  }, [nodeLocation.city, nodeLocation.country, isEditingLocation]);

  // Fetch streets when district changes using Nominatim API
  useEffect(() => {
    if (nodeLocation.district && nodeLocation.city && nodeLocation.country && isEditingLocation) {
      const fetchStreets = async () => {
        setLoadingStreets(true);
        try {
          const query = `${nodeLocation.district}, ${nodeLocation.city}, ${nodeLocation.country}`;
          const res = await fetch(
            `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&limit=20&featuretype=way&class=highway`
          );
          const data = await res.json();
          
          const streetSet = new Set();
          data.forEach(item => {
            if (item.address && item.address.road) {
              streetSet.add(item.address.road);
            }
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
          
          const uniqueStreets = Array.from(streetSet).slice(0, 15);
          setStreets(uniqueStreets.length > 0 ? uniqueStreets : [
            `${nodeLocation.district} Main Street`,
            `${nodeLocation.district} Central Avenue`,
            `${nodeLocation.district} Park Road`
          ]);
        } catch (err) {
          console.error("Failed to fetch streets", err);
          setStreets([
            `${nodeLocation.district} Main Street`,
            `${nodeLocation.district} Central Avenue`,
            `${nodeLocation.district} Park Road`
          ]);
        } finally {
          setLoadingStreets(false);
        }
      };
      fetchStreets();
    } else {
      setStreets([]);
    }
  }, [nodeLocation.district, nodeLocation.city, nodeLocation.country, isEditingLocation]);

  // Forward geocoding function
  const forwardGeocode = async () => {
    const { country, city, district, street } = nodeLocation;
    const addressParts = [street, district, city, country].filter(Boolean);
    if (addressParts.length === 0) {
      alert('Please provide at least one address component (country, city, district, or street)');
      return;
    }
    
    setForwardGeocodingLoading(true);
    
    // Try multiple approaches to find coordinates
    const searchQueries = [
      // Full address
      addressParts.join(', '),
      // Without street if it exists
      [district, city, country].filter(Boolean).join(', '),
      // Just city and country
      [city, country].filter(Boolean).join(', ')
    ].filter(query => query.length > 0);
    
    try {
      for (const searchQuery of searchQueries) {
        console.log('Trying geocoding with query:', searchQuery);
        
        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(searchQuery)}&format=json&addressdetails=1&limit=3&accept-language=en`,
          {
            headers: {
              'User-Agent': 'ConnectTheDots/1.0'
            }
          }
        );
        
        if (response.ok) {
          const data = await response.json();
          console.log('Geocoding response:', data);
          
          if (data && data.length > 0) {
            // Find the most relevant result
            const result = data[0];
            const coordinates = {
              latitude: parseFloat(result.lat),
              longitude: parseFloat(result.lon),
              location_name: result.display_name ? result.display_name.split(',').slice(0, 4).join(', ') : ''
            };
            
            // Update the nodeLocation state with coordinates
            setNodeLocation(prev => ({
              ...prev,
              ...coordinates
            }));
            
            alert(`Coordinates found!\nSearch: ${searchQuery}\nLatitude: ${coordinates.latitude}\nLongitude: ${coordinates.longitude}\nLocation: ${coordinates.location_name}`);
            return; // Success, exit the function
          }
        }
        
        // Small delay between requests to be respectful to the API
        await new Promise(resolve => setTimeout(resolve, 500));
      }
      
      // If we get here, none of the queries found results
      alert('No coordinates found for this address. Please try a different address or check the spelling.');
      
    } catch (error) {
      console.error('Forward geocoding error:', error);
      alert('Error getting coordinates. Please try again later.');
    } finally {
      setForwardGeocodingLoading(false);
    }
  };

  // Reverse geocoding function
  const reverseGeocode = async (lat, lng) => {
    if (!lat || !lng) return null;
    
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&addressdetails=1&zoom=18&accept-language=en`,
        {
          headers: {
            'User-Agent': 'ConnectTheDots/1.0'
          }
        }
      );
      
      if (response.ok) {
        const data = await response.json();
        if (data && data.address) {
          const address = data.address;
          return {
            country: address.country || address.country_code?.toUpperCase() || '',
            city: address.city || address.town || address.municipality || address.village || address.hamlet || '',
            district: address.suburb || address.district || address.neighbourhood || address.quarter || address.city_district || address.state_district || '',
            street: address.road || address.pedestrian || address.path || '',
            location_name: data.display_name ? data.display_name.split(',').slice(0, 3).join(', ') : ''
          };
        }
      }
    } catch (error) {
      console.error('Reverse geocoding error:', error);
    }
    
    return null;
  };

  const connectedNodeIds = new Set();
  allEdges.forEach((edge) => {
    if (String(edge.source) === String(node.id))
      connectedNodeIds.add(String(edge.target));
    if (String(edge.target) === String(node.id))
      connectedNodeIds.add(String(edge.source));
  });
  const possibleNodes = allNodes.filter(
    (n) =>
      String(n.id) !== String(node.id) && !connectedNodeIds.has(String(n.id))
  );

  const handleLocationChange = (field, value) => {
    setNodeLocation((prev) => ({ ...prev, [field]: value }));
    
    // Reset dependent fields when parent changes
    if (field === 'country') {
      setNodeLocation((prev) => ({ ...prev, city: "", district: "", street: "" }));
      setDistricts([]);
      setStreets([]);
    } else if (field === 'city') {
      setNodeLocation((prev) => ({ ...prev, district: "", street: "" }));
      setStreets([]);
    } else if (field === 'district') {
      setNodeLocation((prev) => ({ ...prev, street: "" }));
    }
  };

  const handleUpdateLocation = async () => {
    setUpdatingLocation(true);
    try {
      await api.put(
        `/spaces/${spaceId}/nodes/${node.id}/update-location/`,
        { location: nodeLocation },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
            "Content-Type": "application/json",
          },
        }
      );

      setIsEditingLocation(false);
      setOriginalNodeLocation(nodeLocation); // Update original after successful save
      onNodeUpdate(); // Refresh the graph
      alert("Node location updated successfully!");
    } catch (error) {
      console.error("Error updating node location:", error);
      alert("Failed to update node location. Please try again.");
    } finally {
      setUpdatingLocation(false);
    }
  };

  // Check if location has changed
  const hasLocationChanged = () => {
    return JSON.stringify(nodeLocation) !== JSON.stringify(originalNodeLocation);
  };

  // Check if current user can edit this node's location
  const canEditNodeLocation = () => {
    if (!currentUser) return false;
    
    // For now, we'll implement basic checks. The backend will do the comprehensive permission check
    // including system admin/moderator roles and space moderator status which require API calls
    return (
      // Node creator can edit their own node
      (node.data?.created_by === currentUser || node.created_by === currentUser) ||
      // Space creator can edit any node in their space
      (spaceCreator === currentUser)
      // Note: System admin/moderator and space moderator checks are handled by the backend
      // as they require database queries that are not available in the frontend
    );
  };

  // Auto-save location when modal closes if there are changes
  const handleModalClose = async () => {
    if (hasLocationChanged() && isEditingLocation) {
      const confirmSave = window.confirm(
        "You have unsaved location changes. Would you like to save them before closing?"
      );
      
      if (confirmSave) {
        try {
          await api.put(
            `/spaces/${spaceId}/nodes/${node.id}/update-location/`,
            { location: nodeLocation },
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
                "Content-Type": "application/json",
              },
            }
          );
          onNodeUpdate(); // Refresh the graph
        } catch (error) {
          console.error("Error auto-saving location:", error);
          alert("Failed to save location changes.");
          return; // Don't close modal if save failed
        }
      }
    }
    onClose();
  };

  const handlePropertySelection = (newSelectedProperties) => {
    setSelectedProperties(newSelectedProperties);
  };

  const handleSaveChanges = async () => {
    try {
      const fullSelectedProperties = selectedProperties.map((statementId) =>
        availableProperties.find((p) => p.statement_id === statementId)
      );
      await api.put(
        `/spaces/${spaceId}/nodes/${node.id}/update-properties/`,
        {
          selected_properties: fullSelectedProperties,
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      const response = await api.get(
        `/spaces/${spaceId}/nodes/${node.id}/properties/`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      setNodeProperties(response.data);

      onNodeUpdate();
    } catch (err) {
      alert("Failed to update node properties");
    }
  };

  const handleDeleteNode = async () => {
    if (!confirmDelete) {
      setConfirmDelete(true);
      return;
    }

    try {
      await api.delete(`/spaces/${spaceId}/nodes/${node.id}/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      onNodeDelete();
      onClose();
    } catch (err) {
      alert("Failed to delete node");
    }
  };

  const handleDeleteProperty = async (statementId) => {
    try {
      await api.delete(
        `/spaces/${spaceId}/nodes/${node.id}/properties/${statementId}/`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      setSelectedProperties(
        selectedProperties.filter((id) => id !== statementId)
      );

      const response = await api.get(
        `/spaces/${spaceId}/nodes/${node.id}/properties/`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      setNodeProperties(response.data);

      onNodeUpdate();
    } catch (err) {
      alert("Failed to delete property");
    }
  };

  const renderPropertyValue = (prop) => {
    if (
      prop.property_value &&
      typeof prop.property_value === "object" &&
      prop.property_value.type === "entity"
    ) {
      return (
        <a
          href={`https://www.wikidata.org/wiki/${prop.property_value.id}`}
          target="_blank"
          rel="noopener noreferrer"
          className="entity-link"
        >
          {prop.property_value.text}
        </a>
      );
    }

    return prop.property_value || "No value available";
  };

  const handleAddEdge = async () => {
    setAddEdgeError(null);
    if (!addEdgeTarget || !addEdgeProperty.label.trim()) {
      setAddEdgeError("Please select a node and enter a label.");
      return;
    }
    setAddEdgeLoading(true);
    try {
      await api.post(
        `/spaces/${spaceId}/edges/add/`,
        {
          source_id: isCurrentNodeSource ? node.id : addEdgeTarget,
          target_id: isCurrentNodeSource ? addEdgeTarget : node.id,
          label: addEdgeProperty.label.trim(),
          wikidata_property_id: addEdgeProperty.id,
        },
        {
          headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        }
      );
      setAddEdgeTarget("");
      setAddEdgeProperty({ id: null, label: "" });
      onNodeUpdate();
    } catch (err) {
      setAddEdgeError(
        err.response?.data?.error ||
          "Failed to add edge. Maybe edge already exists."
      );
    } finally {
      setAddEdgeLoading(false);
    }
  };

  const modalRef = useClickOutside(() => {
    if (!showReportModal) {
      handleModalClose();
    }
  });

  return (
    <div className="modal-backdrop">
      <div className="modal-content" ref={modalRef}>
        {/* Inject CSS for property selection */}
        <style>{propertySelectionStyles}</style>

        <div className="modal-header">
          <h2>Node Details</h2>
          <div style={{ display: "flex", gap: "10px" }}>
            <button
              onClick={() => setShowReportModal(true)}
              style={{
                background: "none",
                border: "none",
                color: "var(--color-text-secondary)",
                cursor: "pointer",
                fontSize: "0.9rem",
                padding: "5px 10px",
              }}
            >
              Report
            </button>
            <button onClick={handleModalClose} className="close-button">
              ×
            </button>
          </div>
        </div>

        {loading ? (
          <div className="loading">Loading node details...</div>
        ) : error ? (
          <div className="error">{error}</div>
        ) : (
          <div className="modal-body">
            <div className="node-info-section">
              <h3>{node.data?.label || node.label}</h3>
              {(node.data?.wikidata_id || node.wikidata_id) && (
                <p className="wikidata-id">
                  Wikidata ID:{" "}
                  <a
                    href={`https://www.wikidata.org/wiki/${
                      node.data?.wikidata_id || node.wikidata_id
                    }`}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    {node.data?.wikidata_id || node.wikidata_id}
                  </a>
                </p>
              )}
            </div>

            {/* Location Section */}
            <div className="location-section" style={{ marginBottom: "20px", padding: "15px", backgroundColor: "#f8f9fa", borderRadius: "8px" }}>
              <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "10px" }}>
                <h4 style={{ margin: 0 }}>Location:</h4>
                {hasLocationChanged() && (
                  <span style={{
                    backgroundColor: '#dc3545',
                    color: 'white',
                    fontSize: '11px',
                    padding: '2px 6px',
                    borderRadius: '10px',
                    fontWeight: 'bold'
                  }}>
                    Unsaved Changes
                  </span>
                )}
                {!isEditingLocation && canEditNodeLocation() && (
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
                {!isEditingLocation && !canEditNodeLocation() && (
                  <small style={{ color: '#666', fontSize: '11px', fontStyle: 'italic' }}>
                    Only the node creator, space owner, or moderators can edit location
                  </small>
                )}
              </div>
              
              {!isEditingLocation ? (
                // Display current location
                <div style={{ 
                  padding: "10px", 
                  backgroundColor: "#ffffff", 
                  borderRadius: "4px",
                  border: "1px solid #dee2e6",
                  fontSize: "14px",
                  color: "#666"
                }}>
                  {nodeLocation.country || nodeLocation.city || nodeLocation.district || nodeLocation.street || nodeLocation.location_name || (nodeLocation.latitude && nodeLocation.longitude) ? (
                    <div>
                      {[nodeLocation.street, nodeLocation.district, nodeLocation.city, nodeLocation.country, nodeLocation.location_name]
                        .filter(Boolean).length > 0 ? (
                        [nodeLocation.street, nodeLocation.district, nodeLocation.city, nodeLocation.country, nodeLocation.location_name]
                          .filter(Boolean)
                          .join(', ')
                      ) : (
                        nodeLocation.latitude && nodeLocation.longitude ? (
                          `Coordinates available: ${nodeLocation.latitude}, ${nodeLocation.longitude}`
                        ) : (
                          "Location not specified"
                        )
                      )}
                      {nodeLocation.latitude && nodeLocation.longitude && [nodeLocation.street, nodeLocation.district, nodeLocation.city, nodeLocation.country, nodeLocation.location_name].filter(Boolean).length > 0 && (
                        <div style={{ marginTop: "5px", fontSize: "12px", color: "#888" }}>
                          Coordinates: {nodeLocation.latitude}, {nodeLocation.longitude}
                        </div>
                      )}
                    </div>
                  ) : (
                    "Location not specified"
                  )}
                </div>
              ) : (
                // Edit location form
                <div style={{ 
                  padding: "15px", 
                  backgroundColor: "#ffffff", 
                  borderRadius: "4px",
                  border: "1px solid #dee2e6"
                }}>
                  {/* Country */}
                  <div style={{ marginBottom: "10px" }}>
                    <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                      Country:
                    </label>
                    <select 
                      value={nodeLocation.country} 
                      onChange={(e) => handleLocationChange('country', e.target.value)}
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
                  {nodeLocation.country && (
                    <div style={{ marginBottom: "10px" }}>
                      <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                        City:
                      </label>
                      <select 
                        value={nodeLocation.city} 
                        onChange={(e) => handleLocationChange('city', e.target.value)}
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
                        {cities.map((city) => (
                          <option key={city} value={city}>
                            {city}
                          </option>
                        ))}
                      </select>
                      {loadingCities && (
                        <small style={{ color: "#666", fontSize: "11px" }}>
                          Fetching cities for {nodeLocation.country}...
                        </small>
                      )}
                    </div>
                  )}

                  {/* District */}
                  {nodeLocation.city && (
                    <div style={{ marginBottom: "10px" }}>
                      <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                        District (optional):
                      </label>
                      <select 
                        value={nodeLocation.district} 
                        onChange={(e) => handleLocationChange('district', e.target.value)}
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
                          Fetching districts for {nodeLocation.city}...
                        </small>
                      )}
                    </div>
                  )}

                  {/* Street */}
                  {nodeLocation.district && (
                    <div style={{ marginBottom: "15px" }}>
                      <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                        Street (optional):
                      </label>
                      <select 
                        value={nodeLocation.street} 
                        onChange={(e) => handleLocationChange('street', e.target.value)}
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
                          Fetching streets for {nodeLocation.district}...
                        </small>
                      )}
                    </div>
                  )}

                  {/* Get Coordinates from Address Button */}
                  {(nodeLocation.country || nodeLocation.city || nodeLocation.district || nodeLocation.street) && (
                    <div style={{ marginBottom: "15px" }}>
                      <button
                        onClick={forwardGeocode}
                        disabled={forwardGeocodingLoading}
                        style={{
                          backgroundColor: "#28a745",
                          color: "white",
                          border: "none",
                          padding: "8px 16px",
                          borderRadius: "4px",
                          cursor: forwardGeocodingLoading ? "not-allowed" : "pointer",
                          fontSize: "12px",
                          opacity: forwardGeocodingLoading ? 0.6 : 1
                        }}
                      >
                        {forwardGeocodingLoading ? "Getting Coordinates..." : "Get Coordinates from Address"}
                      </button>
                      {forwardGeocodingLoading && (
                        <small style={{ color: "#666", fontSize: "11px", marginLeft: "10px" }}>
                          Looking up coordinates for the address...
                        </small>
                      )}
                    </div>
                  )}

                  {/* Manual Location Name */}
                  <div style={{ marginBottom: "15px" }}>
                    <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                      Location Name (optional):
                    </label>
                    <input 
                      type="text"
                      value={nodeLocation.location_name} 
                      onChange={(e) => handleLocationChange('location_name', e.target.value)}
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

                  {/* Manual Coordinates */}
                  <div style={{ display: "flex", gap: "10px", marginBottom: "15px" }}>
                    <div style={{ flex: 1 }}>
                      <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                        Latitude:
                      </label>
                      <input 
                        type="number"
                        step="any"
                        value={nodeLocation.latitude || ''} 
                        onChange={(e) => handleLocationChange('latitude', e.target.value ? parseFloat(e.target.value) : null)}
                        placeholder="e.g., 40.7128"
                        style={{
                          width: "100%",
                          padding: "6px",
                          border: "1px solid #ccc",
                          borderRadius: "4px",
                          fontSize: "13px"
                        }}
                      />
                    </div>
                    <div style={{ flex: 1 }}>
                      <label style={{ display: "block", marginBottom: "5px", fontWeight: "bold", fontSize: "13px" }}>
                        Longitude:
                      </label>
                      <input 
                        type="number"
                        step="any"
                        value={nodeLocation.longitude || ''} 
                        onChange={(e) => handleLocationChange('longitude', e.target.value ? parseFloat(e.target.value) : null)}
                        placeholder="e.g., -74.0060"
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

                  {/* Reverse Geocoding Button */}
                  {nodeLocation.latitude && nodeLocation.longitude && (
                    <div style={{ marginBottom: "15px" }}>
                      <button
                        type="button"
                        onClick={async () => {
                          const addressInfo = await reverseGeocode(nodeLocation.latitude, nodeLocation.longitude);
                          if (addressInfo) {
                            setNodeLocation(prev => ({
                              ...prev,
                              country: addressInfo.country || prev.country,
                              city: addressInfo.city || prev.city,
                              district: addressInfo.district || prev.district,
                              street: addressInfo.street || prev.street,
                              location_name: addressInfo.location_name || prev.location_name
                            }));
                          } else {
                            alert('Could not get address information for these coordinates.');
                          }
                        }}
                        style={{
                          background: '#17a2b8',
                          color: 'white',
                          border: 'none',
                          borderRadius: '4px',
                          padding: '6px 12px',
                          fontSize: '13px',
                          cursor: 'pointer',
                          width: '100%'
                        }}
                        onMouseOver={(e) => e.currentTarget.style.background = '#138496'}
                        onMouseOut={(e) => e.currentTarget.style.background = '#17a2b8'}
                      >
                        🌍 Get Address from Coordinates
                      </button>
                    </div>
                  )}

                  {/* Action buttons */}
                  <div style={{ display: "flex", gap: "8px" }}>
                    <button
                      onClick={handleUpdateLocation}
                      disabled={updatingLocation}
                      style={{
                        background: updatingLocation ? '#ccc' : (hasLocationChanged() ? '#dc3545' : '#28a745'),
                        color: 'white',
                        border: hasLocationChanged() ? '2px solid #dc3545' : 'none',
                        borderRadius: '4px',
                        padding: '6px 12px',
                        fontSize: '13px',
                        cursor: updatingLocation ? 'not-allowed' : 'pointer',
                        fontWeight: hasLocationChanged() ? 'bold' : 'normal',
                        boxShadow: hasLocationChanged() ? '0 0 5px rgba(220, 53, 69, 0.5)' : 'none',
                      }}
                    >
                      {updatingLocation 
                        ? "Updating..." 
                        : hasLocationChanged() 
                          ? "Save Changes" 
                          : "Update Location"
                      }
                    </button>
                    <button
                      onClick={() => {
                        setIsEditingLocation(false);
                        // Reset to original values
                        setNodeLocation({
                          country: node.data?.country || node.country || '',
                          city: node.data?.city || node.city || '',
                          district: node.data?.district || node.district || '',
                          street: node.data?.street || node.street || '',
                          latitude: node.data?.latitude || node.latitude || null,
                          longitude: node.data?.longitude || node.longitude || null,
                          location_name: node.data?.location_name || node.location_name || ''
                        });
                      }}
                      disabled={updatingLocation}
                      style={{
                        background: '#6c757d',
                        color: 'white',
                        border: 'none',
                        borderRadius: '4px',
                        padding: '6px 12px',
                        fontSize: '13px',
                        cursor: updatingLocation ? 'not-allowed' : 'pointer',
                      }}
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              )}
            </div>

            <div className="properties-section">
              <h4>Node Properties</h4>
              {nodeProperties.length > 0 ? (
                <div className="current-properties">
                  <ul>
                    {nodeProperties.map((prop) => (
                      <li key={prop.statement_id} className="property-item">
                        <span className="property-content">
                          <span className="property-label">
                            {getPropertyLabelWithId(prop)}:
                          </span>{" "}
                          {renderPropertyValue(prop)}
                        </span>
                        <button
                          className="delete-property-button"
                          onClick={() =>
                            handleDeleteProperty(prop.statement_id)
                          }
                          title="Delete property"
                        >
                          ×
                        </button>
                      </li>
                    ))}
                  </ul>
                </div>
              ) : (
                <p>No properties found for this node</p>
              )}
            </div>

            {/* Property selection UI for editing node properties */}
            {availableProperties.length > 0 && (
              <div className="edit-properties-section">
                <h4>Edit Node Properties</h4>
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
                <button
                  className="save-button"
                  style={{ marginTop: 10 }}
                  onClick={handleSaveChanges}
                  disabled={loading}
                >
                  Save Properties
                </button>
              </div>
            )}

            <div className="edit-properties-section">
              <h4>Add New Edge</h4>
              <div style={{ marginBottom: 10 }}>
                <label htmlFor="add-edge-target">Connect to node:</label>
                <select
                  id="add-edge-target"
                  value={addEdgeTarget}
                  onChange={(e) => setAddEdgeTarget(e.target.value)}
                  style={{ width: "100%", marginBottom: 8 }}
                  disabled={addEdgeLoading || possibleNodes.length === 0}
                >
                  <option value="">-- Select a node --</option>
                  {possibleNodes.map((n) => (
                    <option key={n.id} value={n.id}>
                      {n.label} (ID: {n.id})
                    </option>
                  ))}
                </select>
              </div>
              <div style={{ marginBottom: 10 }}>
                <label htmlFor="add-edge-label">Edge Label:</label>
                <PropertySearch
                  onSelect={setAddEdgeProperty}
                  initialLabel={addEdgeProperty.label}
                />
              </div>
              <div style={{ marginBottom: 10 }}>
                <label>Direction:</label>
                <button
                  type="button"
                  onClick={() => setIsCurrentNodeSource((v) => !v)}
                  style={{
                    marginLeft: 10,
                    padding: "5px 10px",
                    backgroundColor: isCurrentNodeSource
                      ? "#4CAF50"
                      : "#f44336",
                    color: "white",
                    border: "none",
                    borderRadius: "4px",
                    cursor: "pointer",
                  }}
                  disabled={addEdgeLoading || !addEdgeTarget}
                >
                  {isCurrentNodeSource
                    ? `${node.data?.label || node.label} → ${
                        addEdgeTarget
                          ? allNodes.find(
                              (n) => String(n.id) === String(addEdgeTarget)
                            )?.label || addEdgeTarget
                          : "Target"
                      }`
                    : `${
                        addEdgeTarget
                          ? allNodes.find(
                              (n) => String(n.id) === String(addEdgeTarget)
                            )?.label || addEdgeTarget
                          : "Target"
                      } → ${node.data?.label || node.label}`}
                </button>
              </div>
              <button
                onClick={handleAddEdge}
                className="save-button"
                disabled={addEdgeLoading || possibleNodes.length === 0}
              >
                Add Edge
              </button>
              {addEdgeError && <div className="error">{addEdgeError}</div>}
              {possibleNodes.length === 0 && (
                <div style={{ color: "#888", marginTop: 8 }}>
                  All possible nodes are already connected.
                </div>
              )}
            </div>

            <div className="danger-zone">
              <h4>Danger Zone</h4>
              <p className="warning-text">
                Deleting this node will remove it and all its connections from
                the graph. This action cannot be undone.
              </p>
              <button
                onClick={handleDeleteNode}
                className={`delete-button ${confirmDelete ? "confirm" : ""}`}
              >
                {confirmDelete
                  ? "Click again to confirm deletion"
                  : "Delete Node"}
              </button>
            </div>
          </div>
        )}
      </div>

      {showReportModal && (
        <div>
          <ReportModal
            contentId={node.id}
            contentType="Node"
            contentTitle={node.data.label}
            onClose={() => setShowReportModal(false)}
          />
        </div>
      )}
    </div>
  );
};

NodeDetailModal.propTypes = {
  node: PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
    data: PropTypes.shape({
      label: PropTypes.string.isRequired,
      wikidata_id: PropTypes.string,
    }).isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
  onNodeDelete: PropTypes.func.isRequired,
  onNodeUpdate: PropTypes.func.isRequired,
  spaceId: PropTypes.string.isRequired,
  currentUser: PropTypes.string,
  spaceCreator: PropTypes.string,
};

export default NodeDetailModal;
