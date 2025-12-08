import { useLocation, useParams, useNavigate } from "react-router-dom";
import { useEffect, useState, useCallback, useMemo, useRef } from "react";
import { useTranslation } from "../contexts/TranslationContext";
import api from "../axiosConfig";
import SpaceGraph from "../components/SpaceGraph";
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
import ActivityStream from "../components/ActivityStream";

const infoModalStyles = `
.info-icon-btn {
  background: none;
  color: var(--color-gray-400);
  border: 1px solid var(--color-gray-400);
  border-radius: 50%;
  width: 32px;
  height: 32px;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  padding: 0;
}

.info-icon-btn:hover {
  background: var(--color-gray-400);
  color: var(--color-white);
  border: 2px solid var(--color-gray-400);
}

.info-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

.info-modal-content {
  background: var(--color-white);
  border-radius: 12px;
  max-width: 900px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.info-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 2px solid var(--color-gray-200);
  background: var(--color-accent);
  color: var(--color-white);
  border-radius: 12px 12px 0 0;
}

.info-modal-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-modal-close-btn {
  background: none;
  border: none;
  color: var(--color-white);
  font-size: 28px;
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-modal-body {
  padding: 24px;
}

.info-section {
  margin-bottom: 32px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.info-section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--color-gray-200);
}

.info-section-icon {
  font-size: 28px;
  line-height: 1;
}

.info-section h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text);
}

.info-section-content {
  line-height: 1.7;
}

.info-section-content p {
  margin: 0 0 12px 0;
}

.info-section-content p:last-child {
  margin-bottom: 0;
}

.info-section-content ul {
  margin: 12px 0;
  padding-left: 20px;
  list-style-type: none;
  border-left: 3px solid var(--color-gray-200);
}

.info-highlight {
  background: var(--color-item-own-bg);
  border-left: 4px solid var(--color-accent);
  padding: 12px 16px;
  border-radius: 4px;
  margin: 12px 0;
  font-size: 14px;
  color: var(--color-text);
}

.info-subsection ul{
  margin-top: 16px;
  padding-left: 16px;
  border-left: 3px solid var(--color-gray-200);
  list-style-type: none;
}

.info-subsection h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.info-badge {
  display: inline-block;
  background: var(--color-accent);
  color: var(--color-white);
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  margin: 0 4px;
}
`;

const advancedSearchStyles = `
.advanced-search-wrapper {
  margin-bottom: 30px;
}

.simple-search-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.simple-search-container {
  flex: 1;
  position: relative;
}

.simple-search-container input {
  width: 100%;
  padding: 12px 12px 12px 42px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #FFFFFF;
  color: #1B1F3B;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.simple-search-container input:focus {
  outline: none;
  border-color: #0076B5;
}

.simple-search-container input::placeholder {
  color: #999;
}

.simple-search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #757575;
  font-size: 18px;
}
`;

const nodeListStyles = `
.node-list-container {
  border: 1px solid var(--color-gray-300);
  border-radius: 8px;
  background: var(--color-white);
  overflow: hidden;
  margin-bottom: 20px;
}

.node-list-header {
  padding: 12px 16px;
  background: var(--color-bg);
  border-bottom: 1px solid var(--color-gray-300);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.node-list-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.node-list-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.node-sort-select {
  padding: 4px 8px;
  border: 1px solid var(--color-border-2);
  border-radius: 4px;
  font-size: 12px;
  background: var(--color-white);
  cursor: pointer;
  color: var(--color-text);
}

.node-list-toggle {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 12px;
  color: var(--color-text-secondary);
  padding: 4px;
  display: flex;
  align-items: center;
}

.node-list-content {
  max-height: 400px;
  overflow-y: auto;
}

.node-list-item {
  padding: 10px 16px;
  border-bottom: 1px solid var(--color-gray-200);
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s;
}

.node-list-item:last-child {
  border-bottom: none;
}

.node-list-item:hover {
  background-color: var(--color-item-bg);
}

.node-item-label {
  font-weight: 500;
  color: var(--color-text);
}

.node-item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.connection-badge {
  background: var(--color-gray-200);
  color: var(--color-text-secondary);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}
`;

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

.space-actions-dropdown {
  position: relative;
  display: inline-block;
}

.dropdown-toggle {
  background: #0076B5;
  color: #FFFFFF;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background 0.2s ease;
}

.dropdown-toggle:hover {
  background: #005A8C;
}

.dropdown-toggle::after {
  content: '▼';
  font-size: 10px;
  margin-left: 4px;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: #FFFFFF;
  border: 1px solid #68686B;
  border-radius: 4px;
  box-shadow: 0 4px 6px rgba(55, 65, 81, 0.15);
  z-index: 1000;
  min-width: 200px;
  color: var(--color-text);
  margin-top: 4px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border: none;
  cursor: pointer;
  width: 100%;
  text-align: left;
  font-size: 14px;
  font-weight: 500;
  gap: 8px;
  transition: background 0.2s ease;
  border-bottom: 1px solid #F5F5F5;
}

.dropdown-item:hover {
  background: #F5F5F5;
}

.dropdown-item:first-child {
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
}

.dropdown-item:last-child {
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
  border-bottom: none;
}
.dropdown-item.delete-action {
  color: var(--color-danger);
}

.dropdown-item.leave-action {
  color: var(--color-warning);
}
.dropdown-item.join-action {
  color: var(--color-success);
}

.dropdown-item:disabled {
  color: #656F75;
  cursor: not-allowed;
  opacity: 0.6;
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

  const groupedProperties = useMemo(() => {
    const groups = {};
    properties.forEach((prop) => {
      if (!prop || !prop.statement_id) return;
      const propId = prop.property || prop.property_id;
      const key = propId || getPropertyLabelWithId(prop) || "unknown";

      if (!groups[key]) {
        groups[key] = {
          key,
          label: getPropertyLabelWithId(prop).split(':')[0].trim(),
          values: [],
        };
      }
      groups[key].values.push(prop);
    });
    return Object.values(groups);
  }, [properties]);

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

  const handleGlobalSelectAll = () => {
    const allStatementIds = properties.map((p) => p.statement_id);
    const areAllSelected = allStatementIds.length > 0 && allStatementIds.every((id) => selectedProperties.includes(id));
    
    if (areAllSelected) {
      onChange([]);
    } else {
      onChange(allStatementIds);
    }
  };

  const handleGroupSelectAll = (group) => {
    const groupStatementIds = group.values.map((p) => p.statement_id);
    const areAllGroupSelected = groupStatementIds.every((id) => selectedProperties.includes(id));

    let newSelection = [...selectedProperties];

    if (areAllGroupSelected) {
      newSelection = newSelection.filter((id) => !groupStatementIds.includes(id));
    } else {
      const missingIds = groupStatementIds.filter((id) => !selectedProperties.includes(id));
      newSelection = [...newSelection, ...missingIds];
    }
    onChange(newSelection);
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

  const allStatementIds = properties.map((p) => p.statement_id);
  const areAllSelected = allStatementIds.length > 0 && allStatementIds.every((id) => selectedProperties.includes(id));

  return (
    <div className="property-selection-container">
      <div 
        className="property-selection-header" 
        style={{ 
          padding: '8px 12px', 
          borderBottom: '1px solid var(--color-gray-300)', 
          backgroundColor: 'var(--color-bg-secondary)', 
          display: 'flex', 
          alignItems: 'center',
          cursor: 'pointer'
        }}
        onClick={handleGlobalSelectAll}
      >
        <input
          type="checkbox"
          checked={areAllSelected}
          onChange={handleGlobalSelectAll}
          className="property-checkbox"
          onClick={(e) => e.stopPropagation()}
        />
        <span style={{ fontWeight: '600', fontSize: '0.9rem', color: 'var(--color-text)' }}>Select All Properties</span>
      </div>
      <div className="property-selection-list" ref={scrollContainerRef}>
        {groupedProperties.map((group) => {
          const groupStatementIds = group.values.map(p => p.statement_id);
          const isGroupSelected = groupStatementIds.every(id => selectedProperties.includes(id));
          
          return (
            <div key={group.key} className="property-group-item selection-group">
              <div 
                className="property-group-header selection-header"
                style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}
                onClick={() => handleGroupSelectAll(group)}
              >
                <input
                  type="checkbox"
                  checked={isGroupSelected}
                  onChange={(e) => {
                    e.stopPropagation();
                    handleGroupSelectAll(group);
                  }}
                  className="property-checkbox"
                  onClick={(e) => e.stopPropagation()}
                />
                <span className="property-group-label">{group.label}</span>
              </div>
              <ul className="property-values-list">
                {group.values.map((prop) => (
                  <div
                    key={prop.statement_id}
                    className={`property-selection-item ${
                      selectedProperties.includes(prop.statement_id)
                        ? "selected"
                        : ""
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
                      {renderPropertyValue(prop)}
                    </label>
                  </div>
                ))}
              </ul>
            </div>
          );
        })}
      </div>
    </div>
  );
};

const SpaceDetails = () => {
  const { t } = useTranslation();
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
  const [showReportModal, setShowReportModal] = useState(false);
  const [propertySearch, setPropertySearch] = useState("");
  const [showInfoModal, setShowInfoModal] = useState(false);

  const [simpleSearchQuery, setSimpleSearchQuery] = useState("");
  const [simpleSearchResults, setSimpleSearchResults] = useState(null);
  const [searchingSimpleQuery, setSearchingSimpleQuery] = useState(false);
  const [isNodeListExpanded, setIsNodeListExpanded] = useState(true);
  const [nodeSortOption, setNodeSortOption] = useState('recent');

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
  
  // Dropdown state
  const [showDropdown, setShowDropdown] = useState(false);

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

  const nodeDegrees = useMemo(() => {
    const degrees = {};
    if (!nodes || !edges) return degrees;

    nodes.forEach(node => {
      degrees[node.id] = 0;
    });

    edges.forEach(edge => {
      if (degrees[edge.source] !== undefined) degrees[edge.source]++;
      if (degrees[edge.target] !== undefined) degrees[edge.target]++;
    });

    return degrees;
  }, [nodes, edges]);

  const sortedNodes = useMemo(() => {
    if (!existingNodes) return [];
    
    return [...existingNodes].sort((a, b) => {
      if (nodeSortOption === 'recent') {
        return parseInt(b.id) - parseInt(a.id);
      } else if (nodeSortOption === 'connections') {
        const degreeA = nodeDegrees[a.id] || 0;
        const degreeB = nodeDegrees[b.id] || 0;
        return degreeB - degreeA;
      } else if (nodeSortOption === 'name') {
        return a.label.localeCompare(b.label);
      }
      return 0;
    });
  }, [existingNodes, nodeSortOption, nodeDegrees]);

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
        const spaceResponse = await api.get(API_ENDPOINTS.SPACES + `${id}/`, {
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
          is_archived: spaceResponse.data.is_archived || false,
        });

        const username = localStorage.getItem("username");
        const isUserCollaborator =
          spaceResponse.data.collaborators.includes(username) || 
          spaceResponse.data.creator_username === username;
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
        is_archived: spaceResponse.data.is_archived || false,
      });
    } catch (error) {
      console.error("Error joining/leaving space:", error);
    }
  };

  const handleNodeClick = useCallback((event, node) => {
    setSelectedNode(node);
  }, []);

  const handleReportSpace = () => {
    setShowReportModal(true);
  };

  const handleSimpleSearch = async () => {
    if (!simpleSearchQuery.trim()) {
      setSimpleSearchResults(null);
      return;
    }

    setSearchingSimpleQuery(true);
    try {
      const response = await api.get(
        `/spaces/${id}/search/text/?q=${encodeURIComponent(simpleSearchQuery)}`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      
      setSimpleSearchResults(response.data);
    } catch (error) {
      console.error("Error executing simple search:", error);
      alert("search.failedToExecuteSearch");
    } finally {
      setSearchingSimpleQuery(false);
    }
  };

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
              description: updatedNodeData.description || null,
            },
            // Also at top level for backward compatibility
            country: updatedNodeData.country || null,
            city: updatedNodeData.city || null,
            district: updatedNodeData.district || null,
            street: updatedNodeData.street || null,
            latitude: updatedNodeData.latitude || null,
            longitude: updatedNodeData.longitude || null,
            location_name: updatedNodeData.location_name || null,
            description: updatedNodeData.description || null,
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
        err.response?.data?.detail || t("space.deleteSpaceFailed")
      );
    } finally {
      setDeleting(false);
    }
  };

  // Space Actions Dropdown Component
  const SpaceActionsDropdown = () => {
    const dropdownRef = useRef(null);

    // Close dropdown when clicking outside
    useEffect(() => {
      const handleClickOutside = (event) => {
        if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
          setShowDropdown(false);
        }
      };

      if (showDropdown) {
        document.addEventListener('mousedown', handleClickOutside);
      }

      return () => {
        document.removeEventListener('mousedown', handleClickOutside);
      };
    }, [showDropdown]);

    return (
      <div className="space-actions-dropdown" ref={dropdownRef}>
        <button
          className="dropdown-toggle"
          onClick={() => setShowDropdown(!showDropdown)}
          aria-expanded={showDropdown}
          aria-haspopup="true"
        >
          Space Actions
        </button>
        
        {showDropdown && (
          <div className="dropdown-menu" role="menu">
            <button
              className="dropdown-item map-action"
              onClick={() => {
                setShowSpaceMap(true);
                setShowDropdown(false);
              }}
              role="menuitem"
            >
              Show Space Map
            </button>
            
            <button
              className={`dropdown-item ${isCollaborator ? 'leave-action' : 'join-action'}`}
              onClick={() => {
                if (!space.is_archived) {
                  handleJoinLeaveSpace();
                  setShowDropdown(false);
                }
              }}
              disabled={space.is_archived}
              style={space.is_archived ? { opacity: 0.5, cursor: 'not-allowed' } : {}}
              data-testid={isCollaborator ? "leave-space-button" : "header-join-space-button"}
              role="menuitem"
            >
              {isCollaborator ? `${t("space.leaveSpace")}` : `${t("space.joinSpace")}`}
            </button>
            
            {(canDeleteSpace() || canEditSpaceLocation()) && (
              <button
                className="dropdown-item analytics-action"
                onClick={() => {
                  navigate(`/spaces/${id}/analytics`);
                  setShowDropdown(false);
                }}
                role="menuitem"
              >
                Analytics
              </button>
            )}
            
            {canDeleteSpace() && (
              <button
                className="dropdown-item delete-action"
                onClick={() => {
                  if (!space.is_archived) {
                    handleDeleteClick();
                    setShowDropdown(false);
                  }
                }}
                disabled={deleting || space.is_archived}
                style={space.is_archived ? { opacity: 0.5, cursor: 'not-allowed' } : {}}
                role="menuitem"
              >
                {t("common.delete")}
              </button>
            )}
            
            <button
              className="dropdown-item report-action"
              onClick={() => {
                handleReportSpace();
                setShowDropdown(false);
              }}
              role="menuitem"
            >
              {t("common.report")}
            </button>
          </div>
        )}
      </div>
    );
  };

  // Info Modal
  const InfoModal = () => {
    if (!showInfoModal) return null;

    return (
      <div className="info-modal-backdrop" onClick={() => setShowInfoModal(false)}>
        <div className="info-modal-content" onClick={(e) => e.stopPropagation()}>
          <div className="info-modal-header">
            <h2>
              {t("infoModal.title")}
            </h2>
            <button 
              className="info-modal-close-btn"
              onClick={() => setShowInfoModal(false)}
              aria-label="Close"
            >
              ✕
            </button>
          </div>
          
          <div className="info-modal-body">
            {/* Space Graph Section */}
            <div className="info-section">
              <div className="info-section-header">
                <h3>{t("infoModal.spaceGraph.title")}</h3>
              </div>
              <div className="info-section-content">
                <p>
                  {t("infoModal.spaceGraph.intro")}
                </p>
                <div className="info-subsection">
                  <h4>{t("infoModal.spaceGraph.nodes.title")}</h4>
                  <ul>
                    <li><strong>{t("infoModal.spaceGraph.nodes.wikidataEntities").split(':')[0]}:</strong> {t("infoModal.spaceGraph.nodes.wikidataEntities").split(':').slice(1).join(':').trim()}</li>
                    <li><strong>{t("infoModal.spaceGraph.nodes.properties").split(':')[0]}:</strong> {t("infoModal.spaceGraph.nodes.properties").split(':').slice(1).join(':').trim()}</li>
                    <li><strong>{t("infoModal.spaceGraph.nodes.locationData").split(':')[0]}:</strong> {t("infoModal.spaceGraph.nodes.locationData").split(':').slice(1).join(':').trim()}</li>
                  </ul>
                </div>
                <div className="info-subsection">
                  <h4>{t("infoModal.spaceGraph.edges.title")}</h4>
                  <ul>
                    <li><strong>{t("infoModal.spaceGraph.edges.relationships").split(':')[0]}:</strong> {t("infoModal.spaceGraph.edges.relationships").split(':').slice(1).join(':').trim()}</li>
                    <li><strong>{t("infoModal.spaceGraph.edges.directionality").split(':')[0]}:</strong> {t("infoModal.spaceGraph.edges.directionality").split(':').slice(1).join(':').trim()}</li>
                    <li><strong>{t("infoModal.spaceGraph.edges.properties").split(':')[0]}:</strong> {t("infoModal.spaceGraph.edges.properties").split(':').slice(1).join(':').trim()}</li>
                  </ul>
                </div>
                <div className="info-highlight">
                  {t("infoModal.spaceGraph.tipClickNode")}
                </div>

                <div className="info-highlight">
                  {t("infoModal.spaceGraph.tipNodeExpansion")}
                </div>
              </div>
            </div>

            {/* Search Section */}
            <div className="info-section">
              <div className="info-section-header">
                <h3>{t("infoModal.search.title")}</h3>
              </div>
              <div className="info-section-content">
                <div className="info-subsection">
                  <h4>{t("infoModal.search.simpleSearch.title")}</h4>
                  <p>
                    {t("infoModal.search.simpleSearch.description")}
                  </p>
                </div>
              </div>
            </div>

            {/* Collaborators Section */}
            <div className="info-section">
              <div className="info-section-header">
                <h3>{t("infoModal.collaborators.title")}</h3>
              </div>
              <div className="info-section-content">
                <p>
                  {t("infoModal.collaborators.intro")}
                </p>
                <ul>
                  <li><strong>{t("infoModal.collaborators.joinCollaborator").split(':')[0]}:</strong> {t("infoModal.collaborators.joinCollaborator").split(':').slice(1).join(':').trim()}</li>
                  <li><strong>{t("infoModal.collaborators.addNodesEdges").split(':')[0]}:</strong> {t("infoModal.collaborators.addNodesEdges").split(':').slice(1).join(':').trim()}</li>
                  <li><strong>{t("infoModal.collaborators.editDelete").split(':')[0]}:</strong> {t("infoModal.collaborators.editDelete").split(':').slice(1).join(':').trim()}</li>
                </ul>
              </div>
            </div>

            {/* Discussions Section */}
            <div className="info-section">
              <div className="info-section-header">
                <h3>{t("infoModal.discussions.title")}</h3>
              </div>
              <div className="info-section-content">
                <p>
                  {t("infoModal.discussions.intro")}
                </p>
                <ul>
                  <li><strong>{t("infoModal.discussions.startDiscussions").split(':')[0]}:</strong> {t("infoModal.discussions.startDiscussions").split(':').slice(1).join(':').trim()}</li>
                  <li><strong>{t("infoModal.discussions.replyReact").split(':')[0]}:</strong> {t("infoModal.discussions.replyReact").split(':').slice(1).join(':').trim()}</li>
                  <li><strong>{t("infoModal.discussions.reportContent").split(':')[0]}:</strong> {t("infoModal.discussions.reportContent").split(':').slice(1).join(':').trim()}</li>
                </ul>
              </div>
            </div>

            {/* Activity Stream Section */}
            <div className="info-section">
              <div className="info-section-header">
                <h3>{t("infoModal.activity.title")}</h3>
              </div>
              <div className="info-section-content">
                <p>
                  {t("infoModal.activity.intro")}
                </p>
                <strong>{t("infoModal.activity.realtimeUpdates").split(':')[0]}:</strong> {t("infoModal.activity.realtimeUpdates").split(':').slice(1).join(':').trim()}
              
              </div>
            </div>

            {/* Location Features Section */}
            <div className="info-section">
              <div className="info-section-header">
                <h3>{t("infoModal.location.title")}</h3>
              </div>
              <div className="info-section-content">
                <p>
                  {t("infoModal.location.intro")}
                </p>
                <ul>
                  <li><strong>{t("infoModal.location.spaceLocation").split(':')[0]}:</strong> {t("infoModal.location.spaceLocation").split(':').slice(1).join(':').trim()}</li>
                  <li><strong>{t("infoModal.location.nodeLocations").split(':')[0]}:</strong> {t("infoModal.location.nodeLocations").split(':').slice(1).join(':').trim()}</li>
                  <li><strong>{t("infoModal.location.mapVisualization").split(':')[0]}:</strong> {t("infoModal.location.mapVisualization").split(':').slice(1).join(':').trim()}</li>
                </ul>
              </div>
            </div>

          </div>
        </div>
      </div>
    );
  };

  return (
    <div
      style={{
        width: "100%",
        margin: "0 auto",
        padding: "20px 20px 20px 60px",
        display: "flex",
        overflowX: "hidden",
        boxSizing: "border-box",
        maxWidth: "100vw",
      }}
    >
      {/* Inject CSS for property selection */}
      <style>{infoModalStyles}</style>
      <style>{nodeListStyles}</style>
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
          <h2 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '10px' }}>
            <span>{space.title}</span>
            {space.is_archived && (
              <span
                style={{
                  backgroundColor: "#757575",
                  color: "white",
                  padding: "4px 12px",
                  borderRadius: "4px",
                  fontSize: "14px",
                  fontWeight: "600",
                }}
              >
                {t("space.archived")}
              </span>
            )}
          </h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <button 
              className="info-icon-btn"
              onClick={() => setShowInfoModal(true)}
              title="Information"
              aria-label="Information"
            >
              ℹ︎
            </button>
            <SpaceActionsDropdown />
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
                backgroundColor: "var(--color-teal-dark)",
                color: "var(--color-white)",
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
                  style={{
                    marginLeft: "5px",
                    fontSize: "10px",
                    color: "var(--color-white)",
                    opacity: 0.8,
                  }}
                >
                  (Wikidata label: {tag.wikidata_label})
                </span>
              )}
            </span>
          ))}
        </div>

        {/* Search Section */}
        <div className="advanced-search-wrapper">
          <div className="simple-search-row">
            <div className="simple-search-container" style={{ position: 'relative' }}>
              <span className="simple-search-icon">🔍</span>
              <input
                type="text"
                value={simpleSearchQuery}
                onChange={(e) => setSimpleSearchQuery(e.target.value)}
                placeholder="Search within this space's properties, values or content..."
                onKeyUp={(e) => {
                  if (e.key === 'Enter') {
                    handleSimpleSearch();
                  }
                }}
                disabled={searchingSimpleQuery}
              />
              {searchingSimpleQuery && (
                <span style={{
                  position: 'absolute',
                  right: '14px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  color: '#0076B5',
                  fontSize: '14px',
                  fontWeight: '500'
                }}>
                  Searching...
                </span>
              )}
            </div>
          </div>

          {/* Simple Search Results */}
          {simpleSearchResults && (
            <div style={{ 
              marginTop: '16px',
              padding: '16px', 
              backgroundColor: '#fff', 
              borderRadius: '4px',
              border: '1px solid #ddd'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                <h4 style={{ margin: 0, color: '#1B1F3B', fontSize: '16px' }}>
                  Search Results for "{simpleSearchQuery}"
                </h4>
                <button 
                  onClick={() => {
                    setSimpleSearchResults(null);
                    setSimpleSearchQuery("");
                  }}
                  style={{
                    background: 'transparent',
                    border: 'none',
                    color: '#666',
                    cursor: 'pointer',
                    fontSize: '18px',
                    padding: '4px 8px'
                  }}
                  title="Clear search"
                >
                  ✕
                </button>
              </div>
              
              {/* Nodes Results */}
              {simpleSearchResults.nodes && simpleSearchResults.nodes.length > 0 && (
                <div style={{ marginBottom: '16px' }}>
                  <h5 style={{ color: '#0076B5', marginBottom: '8px', fontSize: '14px', fontWeight: '600' }}>
                    Nodes ({simpleSearchResults.nodes.length})
                  </h5>
                  <div style={{ display: 'grid', gap: '8px' }}>
                    {simpleSearchResults.nodes.map(node => (
                      <div 
                        key={node.id}
                        style={{
                          padding: '10px 12px',
                          backgroundColor: '#f8f9fa',
                          borderRadius: '4px',
                          border: '1px solid #e0e0e0',
                          cursor: 'pointer',
                          transition: 'all 0.2s',
                          fontSize: '14px'
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.borderColor = '#0076B5';
                          e.currentTarget.style.backgroundColor = '#f0f7fc';
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.borderColor = '#e0e0e0';
                          e.currentTarget.style.backgroundColor = '#f8f9fa';
                        }}
                        onClick={() => {
                          const nodeObj = nodes.find(n => n.id === node.id.toString());
                          if (nodeObj) setSelectedNode(nodeObj);
                        }}
                      >
                        <div style={{ fontWeight: '600', color: '#1B1F3B', marginBottom: '2px' }}>
                          {node.label}
                        </div>
                        <div style={{ fontSize: '12px', color: '#666' }}>
                          {node.wikidata_id}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Edges Results */}
              {simpleSearchResults.edges && simpleSearchResults.edges.length > 0 && (
                <div>
                  <h5 style={{ color: '#2D6A4F', marginBottom: '8px', fontSize: '14px', fontWeight: '600' }}>
                    Edges ({simpleSearchResults.edges.length})
                  </h5>
                  <div style={{ display: 'grid', gap: '8px' }}>
                    {simpleSearchResults.edges.map(edge => {
                      const sourceNode = nodes.find(n => n.id === edge.source.toString());
                      const targetNode = nodes.find(n => n.id === edge.target.toString());
                      return (
                        <div 
                          key={edge.id}
                          style={{
                            padding: '10px 12px',
                            backgroundColor: '#f8f9fa',
                            borderRadius: '4px',
                            border: '1px solid #e0e0e0',
                            cursor: 'pointer',
                            transition: 'all 0.2s',
                            fontSize: '14px'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.borderColor = '#2D6A4F';
                            e.currentTarget.style.backgroundColor = '#f0f7f5';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.borderColor = '#e0e0e0';
                            e.currentTarget.style.backgroundColor = '#f8f9fa';
                          }}
                          onClick={() => {
                            const edgeObj = edges.find(e => e.id === edge.id.toString());
                            if (edgeObj) setSelectedEdge(edgeObj);
                          }}
                        >
                          <div style={{ fontWeight: '600', color: '#1B1F3B', marginBottom: '2px' }}>
                            {sourceNode?.data?.label || `Node ${edge.source}`} 
                            <span style={{ margin: '0 6px', color: '#2D6A4F', fontWeight: '400' }}>→</span>
                            {targetNode?.data?.label || `Node ${edge.target}`}
                          </div>
                          <div style={{ fontSize: '12px', color: '#666', fontStyle: 'italic' }}>
                            {edge.label || 'No label'}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* No Results */}
              {(!simpleSearchResults.nodes || simpleSearchResults.nodes.length === 0) && 
               (!simpleSearchResults.edges || simpleSearchResults.edges.length === 0) && (
                <div style={{ textAlign: 'center', padding: '20px', color: '#666' }}>
                  <p style={{ margin: 0 }}>No results found for "{simpleSearchQuery}"</p>
                  <p style={{ margin: '8px 0 0 0', fontSize: '13px' }}>
                    Try different keywords
                  </p>
                </div>
              )}
            </div>
          )}


        </div>

        {/* Search Results Section */}


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
            <SpaceGraph
              nodes={nodes}
              edges={edges}
              loading={graphLoading}
              error={graphError}
              onNodeClick={handleNodeClick}
              onEdgeClick={handleEdgeClick}
            />
          </div>
        </div>

        {sortedNodes.length === 0 && (
          <p>{t("space.addNodeFromWikidata")}</p>
        )}
        
        {sortedNodes.length > 0 && (
          <div className="node-list-container">
            <div 
              className="node-list-header"
              onClick={() => setIsNodeListExpanded(!isNodeListExpanded)}>
              <h3 className="node-list-title">{t("space.nodes")} ({sortedNodes.length})</h3>
              <div className="node-list-controls">
                <select 
                  className="node-sort-select"
                  value={nodeSortOption}
                  onChange={(e) => {
                    e.stopPropagation();
                    setNodeSortOption(e.target.value);
                  }}
                  onClick={(e) => e.stopPropagation()}
                >
                  <option value="recent">{t("space.recentlyAdded")}</option>
                  <option value="connections">{t("space.mostConnections")}</option>
                  <option value="name">{t("space.nameAZ")}</option>
                </select>
                <button 
                  className="node-list-toggle"
                  onClick={(e) => {
                    e.stopPropagation();
                    setIsNodeListExpanded(!isNodeListExpanded);
                  }}
                >
                  {isNodeListExpanded ? '▼' : '▲'}
                </button>
              </div>
            </div>
            
            {isNodeListExpanded && (
              <div className="node-list-content">
                {sortedNodes.map((node) => (
                  <div 
                    key={node.id} 
                    className="node-list-item"
                    onClick={(e) => handleNodeClick(e, node)}
                  >
                    <span className="node-item-label">{node.label}</span>
                    <div className="node-item-meta">
                      <span className="connection-badge">
                        {nodeDegrees[node.id] || 0} {t("space.connections")}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Revert Graph Section - Only show if collaborator and not archived */}
        {isCollaborator && !space.is_archived && (
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
                      alert(t("space.graphRevertedSuccessfully"));
                      window.location.reload();
                    })
                    .catch((err) => {
                      alert(t("space.revertFailed"));
                      console.error(err);
                    });
                }}
              >
                <option value="">{t("space.selectSnapshotToRevert")}</option>
                {snapshots.map((snap) => (
                  <option key={snap.id} value={snap.id}>
                    {new Date(snap.created_at).toLocaleString()}
                  </option>
                ))}
              </select>
            ) : (
              <p>{t("space.noSnapshotsAvailable")}</p>
            )}
            <hr />
          </>
        )}

        {/* Add Node Section - Only show if collaborator and not archived */}
        {isCollaborator && !space.is_archived && (
          <>
            <h3>{t("space.addNodeFromWikidata")}</h3>
            <div>
              <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder={t("space.searchWikidataPlaceholder")}
              />
              <button onClick={handleSearch}>{t("common.search")}</button>
            </div>

            {searchResults.length > 0 && (
              <div>
                <h4>{t("space.selectEntity")}</h4>
                <select
                  value={selectedEntity?.id || ""}
                  onChange={handleEntitySelection}
                  style={{ width: "100%", maxWidth: "500px" }}
                >
                  <option value="">{t("space.selectEntityPlaceholder")}</option>
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
                <h4>{t("space.selectedEntity")}: {selectedEntity.label}</h4>
                <div>
                  <h5>{t("space.selectProperties")}</h5>
                  <p className="selection-help-text">
                    {t("graph.clickToSelectDeselect")}
                  </p>
                  <input
                    type="text"
                    placeholder={t("graph.searchProperties")}
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
                  <label>{t("space.edgeDirection")}:</label>
                  <button
                    onClick={() => setIsNewNodeSource(!isNewNodeSource)}
                    style={{
                      marginLeft: "10px",
                      padding: "5px 10px",
                      backgroundColor: isNewNodeSource
                        ? "var(--color-success)"
                        : "var(--color-danger)",
                      color: "var(--color-white)",
                      border: "none",
                      borderRadius: "4px",
                      cursor: "pointer",
                    }}
                  >
                    {isNewNodeSource ? t("space.newToExisting") : t("space.existingToNew")}
                  </button>
                </div>
                <div
                  style={{
                    marginTop: "10px",
                    color: "var(--color-text-secondary)",
                    fontSize: "14px",
                  }}
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
                  <label>{t("space.connectToNode")}:</label>
                  <select
                    value={relatedNodeId}
                    onChange={(e) => setRelatedNodeId(e.target.value)}
                    style={{ width: "100%", maxWidth: "500px" }}
                  >
                    <option value="">{t("space.selectNodeToRelate")}</option>
                    {existingNodes.map((node) => (
                      <option key={node.id} value={node.id}>
                        {node.label}
                      </option>
                    ))}
                  </select>
                </div>
                <div style={{ marginTop: "10px" }}>
                  <label>{t("graph.edgeLabel")}:</label>
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
                        alert(t("space.nodeAndEdgeAddedSuccessfully"));

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
                        alert(t("space.failedToAddNode"));
                      });
                  }}
                >
                  {t("space.addNodeWithEdge")}
                </button>
              </div>
            )}
          </>
        )}

        {!isCollaborator && (
          <div className="non-collaborator-box">
            <p>
              {space.is_archived 
                ? "This space is archived and cannot be joined or modified."
                : "Join this space as a collaborator to add nodes and modify the graph."}
            </p>
            {!space.is_archived && (
              <button
                className="join-button"
                onClick={handleJoinLeaveSpace}
                data-testid="bottom-join-space-button"
              >
                JOIN SPACE
              </button>
            )}
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
                  {t("common.cancel")}
                </button>
                <button
                  onClick={handleConfirmDelete}
                  style={{
                    background: "var(--color-danger)",
                    color: "var(--color-white)",
                  }}
                  disabled={deleting}
                >
                  {deleting ? t("space.deleting") : t("common.delete")}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Collaborators sidebar */}
      <div
        style={{
          width: "260px",
          display: "flex",
          flexDirection: "column",
          gap: "16px",
        }}
      >
        <ActivityStream spaceId={id} dense />

        <div
          style={{
            border: "1px solid #68686B",
            borderRadius: "4px",
            overflow: "hidden",
            backgroundColor: "#FFFFFF",
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
              color: "#1B1F3B",
            }}
            onClick={() => setIsCollaboratorsOpen(!isCollaboratorsOpen)}
          >
            <strong>
              {t("space.collaborators")} ({space.collaborators.length})
            </strong>
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
        <SpaceDiscussions spaceId={id} isCollaborator={isCollaborator} isArchived={space.is_archived} />
      </div>

      {/* Node detail modal */}
      {selectedNode && isCollaborator && (
        <NodeDetailModal
          node={selectedNode}
          onClose={handleCloseModal}
          onNodeDelete={space.is_archived ? null : handleNodeDelete}
          onNodeUpdate={space.is_archived ? null : handleNodeUpdate}
          spaceId={id}
          currentUser={localStorage.getItem("username")}
          spaceCreator={space.creator_username}
          isArchived={space.is_archived}
        />
      )}

      {selectedEdge && (
        <EdgeDetailModal
          edge={selectedEdge}
          sourceNode={nodes.find((n) => n.id === selectedEdge.source)}
          targetNode={nodes.find((n) => n.id === selectedEdge.target)}
          onClose={() => setSelectedEdge(null)}
          onEdgeUpdate={space.is_archived ? null : fetchGraphData}
          onEdgeDelete={space.is_archived ? null : fetchGraphData}
          spaceId={id}
          isArchived={space.is_archived}
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
          contentId={parseInt(id, 10)}
          contentType="space"
          contentTitle={space.title}
          onClose={() => setShowReportModal(false)}
        />
      )}

      {/* Info Modal */}
      <InfoModal />
    </div>
  );
};

export default SpaceDetails;
