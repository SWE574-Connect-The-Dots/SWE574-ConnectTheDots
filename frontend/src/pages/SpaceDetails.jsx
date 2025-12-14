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
import InstanceTypeFilter from "../components/InstanceTypeFilter";
import InstanceTypeLegend from "../components/InstanceTypeLegend";
import { getGroupById } from "../config/instanceTypes";
import { marked } from "marked";

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

.summarize-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #FFFFFF;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.3);
}

.summarize-btn:hover {
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.4);
}

.summarize-btn:active {
  transform: translateY(0);
}

.summarize-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
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

const fullscreenGraphStyles = `
.graph-container-fullscreen {
  background: var(--color-gray-200) !important;
  padding: 0 !important;
  margin: 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.graph-container-fullscreen::backdrop {
  background: rgba(0, 0, 0, 0.95);
}

.fullscreen-exit-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 10001;
  padding: 10px 20px;
  background: rgba(0, 0, 0, 0.7);
  color: var(--color-white);
  border: 2px solid var(--color-white);
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.fullscreen-exit-btn:hover {
  background: rgba(0, 0, 0, 0.9);
  transform: scale(1.05);
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
  const [topCollaborators, setTopCollaborators] = useState([]);
  const [showAllCollaborators, setShowAllCollaborators] = useState(false);
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

  // AI Summary states
  const [aiSummary, setAiSummary] = useState(null);
  const [showAiSummary, setShowAiSummary] = useState(false);
  const [loadingAiSummary, setLoadingAiSummary] = useState(false);
  const [aiSummaryError, setAiSummaryError] = useState(null);

  const [simpleSearchQuery, setSimpleSearchQuery] = useState("");
  const [simpleSearchResults, setSimpleSearchResults] = useState(null);
  const [searchingSimpleQuery, setSearchingSimpleQuery] = useState(false);
  const [graphSearchQuery, setGraphSearchQuery] = useState("");
  const [graphSearchResults, setGraphSearchResults] = useState(null);
  const [isGraphSearching, setIsGraphSearching] = useState(false);
  const [graphSearchDepth, setGraphSearchDepth] = useState(1);
  const [selectedNodeIds, setSelectedNodeIds] = useState([]);
  const [selectedEdgeTypes, setSelectedEdgeTypes] = useState([]);
  const [showNodeDropdown, setShowNodeDropdown] = useState(false);
  const [showEdgeDropdown, setShowEdgeDropdown] = useState(false);
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
  
  const [instanceTypes, setInstanceTypes] = useState([]);
  const [selectedInstanceTypes, setSelectedInstanceTypes] = useState(new Set());
  const [showLegend, setShowLegend] = useState(true);
  const [isLeftPanelCollapsed, setIsLeftPanelCollapsed] = useState(false);
  const [isRightPanelCollapsed, setIsRightPanelCollapsed] = useState(false);

  const [isGraphFullscreen, setIsGraphFullscreen] = useState(false);
  const graphContainerRef = useRef(null);
  const nodeDropdownRef = useRef(null);
  const edgeDropdownRef = useRef(null);

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

  const nodesWithColors = useMemo(() => {
    if (!nodes) return [];
    
    return nodes.map(node => {
      const instanceType = node.instance_type;
      let color = 'var(--color-success)';
      let label = null;
      let icon = null;
      
      if (instanceType && instanceType.group_id) {
        const group = getGroupById(instanceType.group_id);
        if (group) {
          color = group.color;
          label = group.label;
          icon = group.icon;
        }
      }
      
      return {
        ...node,
        data: {
          ...node.data,
          instanceTypeColor: color,
          instanceTypeLabel: label,
          instanceTypeIcon: icon
        }
      };
    });
  }, [nodes]);

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

        try {
          const topCollaboratorsResponse = await api.get(`/spaces/${id}/top-collaborators/`, {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          });
          setTopCollaborators(topCollaboratorsResponse.data?.top_collaborators || []);
        } catch (error) {
          console.error("Error fetching top collaborators:", error);
          setTopCollaborators([]);
        }

        fetchGraphData();
      } catch (error) {
        console.error("Error fetching space data:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [id, fetchGraphData]);

  useEffect(() => {
    const fetchInstanceTypes = async () => {
      try {
        const response = await api.get(`/spaces/${id}/instance-types/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        
        const groups = response.data.instance_groups || [];
        setInstanceTypes(groups);
        
        setSelectedInstanceTypes(new Set());
      } catch (error) {
        console.error('Error fetching instance groups:', error);
      }
    };
    
    if (id) {
      fetchInstanceTypes();
    }
  }, [id]);

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

  const handleActivityNodeClick = useCallback((nodeId) => {
    const node = nodes.find(n => n.id === nodeId.toString());
    if (node) {
      setSelectedNode(node);
    }
  }, [nodes]);

  useEffect(() => {
    const nodeIdFromQuery = new URLSearchParams(location.search).get("nodeId");
    if (!nodeIdFromQuery) return;
    if (!nodes || nodes.length === 0) return;

    const node = nodes.find((n) => n.id === nodeIdFromQuery.toString());
    if (!node) return;

    setSelectedNode(node);

    const params = new URLSearchParams(location.search);
    params.delete("nodeId");
    const newSearch = params.toString();
    navigate(`${location.pathname}${newSearch ? `?${newSearch}` : ""}`, { replace: true });
  }, [location.pathname, location.search, navigate, nodes]);

  // Handle click outside dropdowns
  useEffect(() => {
    function handleClickOutside(event) {
      if (nodeDropdownRef.current && !nodeDropdownRef.current.contains(event.target)) {
        setShowNodeDropdown(false);
      }
      if (edgeDropdownRef.current && !edgeDropdownRef.current.contains(event.target)) {
        setShowEdgeDropdown(false);
      }
    }

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleReportSpace = () => {
    setShowReportModal(true);
  };

  const handleAiSummarize = async () => {
    setLoadingAiSummary(true);
    setAiSummaryError(null);
    setShowAiSummary(true);
    
    try {
      const response = await api.post(
        `/spaces/${id}/summarize/`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      
      setAiSummary(response.data);
    } catch (error) {
      console.error("Error generating AI summary:", error);
      setAiSummaryError(error.response?.data?.error || "Failed to generate AI summary");
    } finally {
      setLoadingAiSummary(false);
    }
  };

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

  const [graphNodeQuery, setGraphNodeQuery] = useState("");
  const [graphEdgeQuery, setGraphEdgeQuery] = useState("");

  const handleGraphSearch = async () => {
    if (selectedNodeIds.length === 0 && selectedEdgeTypes.length === 0) return;
    
    setIsGraphSearching(true);
    try {
      const params = new URLSearchParams();
      if (selectedNodeIds.length > 0) params.append('node_q', selectedNodeIds.join(','));
      if (selectedEdgeTypes.length > 0) params.append('edge_q', selectedEdgeTypes.join(','));
      params.append('depth', graphSearchDepth.toString());
      
      const response = await api.get(`/spaces/${id}/graph-search/?${params.toString()}`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      setGraphSearchResults(response.data);
    } catch (error) {
      console.error("Graph search failed:", error);
    } finally {
      setIsGraphSearching(false);
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

  const handleFullscreenToggle = useCallback(() => {
    if (!graphContainerRef.current) return;

    if (!document.fullscreenElement) {
      graphContainerRef.current.requestFullscreen().then(() => {
        setIsGraphFullscreen(true);
      }).catch(err => {
        console.error("Error attempting to enable fullscreen:", err);
      });
    } else {
      document.exitFullscreen().then(() => {
        setIsGraphFullscreen(false);
      }).catch(err => {
        console.error("Error attempting to exit fullscreen:", err);
      });
    }
  }, []);

  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsGraphFullscreen(!!document.fullscreenElement);
    };

    const handleEscKey = (event) => {
      if (event.key === 'Escape' && isGraphFullscreen) {
        handleFullscreenToggle();
      }
    };

    document.addEventListener('fullscreenchange', handleFullscreenChange);
    document.addEventListener('keydown', handleEscKey);
    
    return () => {
      document.removeEventListener('fullscreenchange', handleFullscreenChange);
      document.removeEventListener('keydown', handleEscKey);
    };
  }, [isGraphFullscreen, handleFullscreenToggle]);

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
        padding: "10px",
        display: "flex",
        overflowX: "hidden",
        boxSizing: "border-box",
        maxWidth: "100%",
      }}
    >
      {/* Inject CSS for property selection */}
      <style>{infoModalStyles}</style>
      <style>{nodeListStyles}</style>
      <style>{propertySelectionStyles}</style>
      <style>{fullscreenGraphStyles}</style>

      <div style={{ 
        flex: 1, 
        marginRight: isRightPanelCollapsed ? "30px" : "20px",
        transition: "all 0.3s ease"
      }}>
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

          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <button
              className="summarize-btn"
              onClick={handleAiSummarize}
              disabled={loadingAiSummary || space.is_archived}
            >
              <span>✨</span>
              {loadingAiSummary ? 'Generating...' : 'Summarize'}
            </button>
            <SpaceActionsDropdown />
          </div>
        </div>
        </div>
        <p>{space.description}</p>
        
        {/* Location Section */}
        <div style={{ marginTop: "20px", marginBottom: "20px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "10px" }}>
            <h4 style={{ margin: 0 }}>{t("spaceAnalytics.location")}:</h4>
            {!isEditingLocation ? (
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
                t("spaceAnalytics.locationNotSpecified")
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
                  {t("spaceAnalytics.country")}:
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
                  <option value="">{t("spaceAnalytics.selectCountry")}</option>
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
                    {t("spaceAnalytics.city")}:
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
                    <option value="">{loadingCities ? t("spaceAnalytics.loadingCities") : t("spaceAnalytics.selectCity")}</option>
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
                    {t("spaceAnalytics.district")}:
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
                    <option value="">{loadingDistricts ? t("spaceAnalytics.loadingDistricts") : t("spaceAnalytics.selectDistrict")}</option>
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
                    {t("spaceAnalytics.street")}:
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
                    <option value="">{loadingStreets ? t("spaceAnalytics.loadingStreets") : t("spaceAnalytics.selectStreet")}</option>
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
            {canEditSpaceLocation() && !isEditingLocation && (
              <button
                onClick={() => setIsEditingLocation(true)}
                style={{
                  borderRadius: '4px',
                  padding: '4px 8px',
                  fontSize: '12px',
                }}
              >
                Edit Location
              </button>
            )}
          </div>
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

          {/* Graph Search Section */}
          <div style={{ 
            marginTop: '20px', 
            borderTop: '2px solid #0076B5', 
            paddingTop: '20px',
            backgroundColor: '#f8f9fa',
            padding: '20px',
            borderRadius: '8px'
          }}>
            <h4 style={{ marginBottom: '10px', color: '#1B1F3B', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span>🕸️</span> Advanced Graph Search
            </h4>
            <p style={{ fontSize: '13px', color: '#666', marginBottom: '15px' }}>
              Search for multiple nodes and edges by separating terms with commas. Adjust relation depth to include neighbors.
            </p>
            
            <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap', marginBottom: '15px' }}>
              <div style={{ flex: 1, minWidth: '250px' }}>
                <label style={{ display: 'block', marginBottom: '6px', fontSize: '13px', fontWeight: '600', color: '#1B1F3B' }}>
                  🔵 Select Nodes
                </label>
                <div ref={nodeDropdownRef} style={{ position: 'relative' }}>
                  <button
                    onClick={() => setShowNodeDropdown(!showNodeDropdown)}
                    style={{
                      width: '100%',
                      padding: '10px 12px',
                      border: '1px solid #ddd',
                      borderRadius: showNodeDropdown ? '4px 4px 0 0' : '4px',
                      fontSize: '14px',
                      backgroundColor: '#fff',
                      cursor: 'pointer',
                      textAlign: 'left',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <span>{selectedNodeIds.length === 0 ? `${nodes?.length || 0} nodes available` : `${selectedNodeIds.length} selected`}</span>
                    <span>{showNodeDropdown ? '▼' : '▶'}</span>
                  </button>
                  {showNodeDropdown && (
                    <div style={{
                      position: 'absolute',
                      top: '100%',
                      left: 0,
                      right: 0,
                      backgroundColor: '#fff',
                      border: '1px solid #ddd',
                      borderTop: 'none',
                      borderRadius: '0 0 4px 4px',
                      maxHeight: '200px',
                      overflowY: 'auto',
                      zIndex: 1000,
                      boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                    }}>
                      {nodes && nodes.length > 0 ? nodes.map((node) => (
                        <label key={node.id} style={{
                          display: 'flex',
                          alignItems: 'center',
                          padding: '10px 12px',
                          borderBottom: '1px solid #eee',
                          cursor: 'pointer',
                          fontSize: '14px'
                        }}>
                          <input
                            type="checkbox"
                            checked={selectedNodeIds.includes(node.id)}
                            onChange={(e) => {
                              if (e.target.checked) {
                                setSelectedNodeIds([...selectedNodeIds, node.id]);
                              } else {
                                setSelectedNodeIds(selectedNodeIds.filter(id => id !== node.id));
                              }
                            }}
                            style={{ marginRight: '8px', cursor: 'pointer' }}
                          />
                          <span>{node.data?.label || node.id}</span>
                        </label>
                      )) : <span style={{ padding: '10px 12px', color: '#888' }}>No nodes available</span>}
                    </div>
                  )}
                </div>
                <span style={{ fontSize: '11px', color: '#888', marginTop: '4px', display: 'block' }}>
                  Searches in node labels and descriptions
                </span>
              </div>
              
              <div style={{ flex: 1, minWidth: '250px' }}>
                <label style={{ display: 'block', marginBottom: '6px', fontSize: '13px', fontWeight: '600', color: '#1B1F3B' }}>
                  🔗 Select Edge Types
                </label>
                <div ref={edgeDropdownRef} style={{ position: 'relative' }}>
                  <button
                    onClick={() => setShowEdgeDropdown(!showEdgeDropdown)}
                    style={{
                      width: '100%',
                      padding: '10px 12px',
                      border: '1px solid #ddd',
                      borderRadius: showEdgeDropdown ? '4px 4px 0 0' : '4px',
                      fontSize: '14px',
                      backgroundColor: '#fff',
                      cursor: 'pointer',
                      textAlign: 'left',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}
                  >
                    <span>{selectedEdgeTypes.length === 0 ? `${edges?.length || 0} edge types available` : `${selectedEdgeTypes.length} selected`}</span>
                    <span>{showEdgeDropdown ? '▼' : '▶'}</span>
                  </button>
                  {showEdgeDropdown && (
                    <div style={{
                      position: 'absolute',
                      top: '100%',
                      left: 0,
                      right: 0,
                      backgroundColor: '#fff',
                      border: '1px solid #ddd',
                      borderTop: 'none',
                      borderRadius: '0 0 4px 4px',
                      maxHeight: '200px',
                      overflowY: 'auto',
                      zIndex: 1000,
                      boxShadow: '0 4px 6px rgba(0,0,0,0.1)'
                    }}>
                      {edges && edges.length > 0 ? [...new Set(edges.map(edge => edge.data?.original_label || edge.label || 'Unknown'))].map((edgeType) => (
                        <label key={edgeType} style={{
                          display: 'flex',
                          alignItems: 'center',
                          padding: '10px 12px',
                          borderBottom: '1px solid #eee',
                          cursor: 'pointer',
                          fontSize: '14px'
                        }}>
                          <input
                            type="checkbox"
                            checked={selectedEdgeTypes.includes(edgeType)}
                            onChange={(e) => {
                              if (e.target.checked) {
                                setSelectedEdgeTypes([...selectedEdgeTypes, edgeType]);
                              } else {
                                setSelectedEdgeTypes(selectedEdgeTypes.filter(type => type !== edgeType));
                              }
                            }}
                            style={{ marginRight: '8px', cursor: 'pointer' }}
                          />
                          <span>{edgeType}</span>
                        </label>
                      )) : <span style={{ padding: '10px 12px', color: '#888' }}>No edge types available</span>}
                    </div>
                  )}
                </div>
                <span style={{ fontSize: '11px', color: '#888', marginTop: '4px', display: 'block' }}>
                  Searches in relationship types and labels
                </span>
              </div>

              <div style={{ minWidth: '150px' }}>
                <label style={{ display: 'block', marginBottom: '6px', fontSize: '13px', fontWeight: '600', color: '#1B1F3B' }}>
                  📏 Relation Depth
                </label>
                <select
                  value={graphSearchDepth}
                  onChange={(e) => setGraphSearchDepth(parseInt(e.target.value))}
                  disabled={isGraphSearching}
                  style={{
                    width: '100%',
                    padding: '10px 12px',
                    border: '1px solid #ddd',
                    borderRadius: '4px',
                    fontSize: '14px',
                    backgroundColor: '#fff',
                    cursor: 'pointer'
                  }}
                >
                  <option value="1">1 Level</option>
                  <option value="2">2 Levels</option>
                  <option value="3">3 Levels</option>
                  <option value="4">4 Levels</option>
                  <option value="5">5 Levels</option>
                </select>
                <span style={{ fontSize: '11px', color: '#888', marginTop: '4px', display: 'block' }}>
                  How far to explore
                </span>
              </div>
            </div>

            <div style={{ marginBottom: '15px', padding: '10px', backgroundColor: '#e3f2fd', borderRadius: '4px', fontSize: '12px', color: '#1565c0' }}>
              <strong>💡 Tip:</strong> Depth {graphSearchDepth} will include nodes up to {graphSearchDepth} {graphSearchDepth === 1 ? 'relationship' : 'relationships'} away from your search terms.
              {graphSearchDepth === 1 && " (Direct connections only)"}
              {graphSearchDepth === 2 && " (Friends of friends)"}
              {graphSearchDepth >= 3 && " (Extended network)"}
            </div>

            <button 
                onClick={handleGraphSearch} 
                disabled={isGraphSearching} 
                style={{ 
                    padding: '12px 30px', 
                    backgroundColor: isGraphSearching ? '#ccc' : '#2D6A4F',
                    color: 'white',
                    border: 'none',
                    borderRadius: '4px',
                    cursor: isGraphSearching ? 'not-allowed' : 'pointer',
                    fontWeight: '600',
                    fontSize: '14px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    transition: 'background 0.2s'
                }}
                onMouseEnter={(e) => !isGraphSearching && (e.target.style.backgroundColor = '#1e4d38')}
                onMouseLeave={(e) => !isGraphSearching && (e.target.style.backgroundColor = '#2D6A4F')}
            >
                {isGraphSearching ? (
                  <>
                    <span>⏳</span> Searching...
                  </>
                ) : (
                  <>
                    <span>🔍</span> Search Graph
                  </>
                )}
            </button>
          </div>

          {graphSearchResults && (
            <div style={{ 
              marginTop: '16px', 
              padding: '20px', 
              backgroundColor: '#fff', 
              border: '2px solid #2D6A4F', 
              borderRadius: '8px',
              boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                    <h4 style={{ margin: 0, color: '#1B1F3B', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span>📊</span> Graph Search Results
                    </h4>
                    <button 
                      onClick={() => setGraphSearchResults(null)} 
                      style={{ 
                        background: '#f44336', 
                        color: 'white',
                        border: 'none', 
                        cursor: 'pointer', 
                        fontSize: '14px',
                        padding: '6px 12px',
                        borderRadius: '4px',
                        fontWeight: '600'
                      }}
                    >
                      Close
                    </button>
                </div>
                
                {(!graphSearchResults.nodes || graphSearchResults.nodes.length === 0) && (!graphSearchResults.edges || graphSearchResults.edges.length === 0) ? (
                    <div style={{ textAlign: 'center', padding: '30px', color: '#666' }}>
                      <p style={{ fontSize: '16px', marginBottom: '8px' }}>🔍 No results found</p>
                      <p style={{ fontSize: '13px' }}>Try different search terms or check your spelling</p>
                    </div>
                ) : (
                    <div>
                        {/* Summary */}
                        <div style={{ 
                          padding: '12px', 
                          backgroundColor: '#e8f5e9', 
                          borderRadius: '6px',
                          marginBottom: '20px',
                          border: '1px solid #c8e6c9'
                        }}>
                          <div style={{ fontSize: '14px', fontWeight: '600', color: '#2D6A4F' }}>
                            Found: {graphSearchResults.nodes?.length || 0} Nodes, {graphSearchResults.edges?.length || 0} Edges
                          </div>
                        </div>

                        {/* Graph Visualization */}
                        <div style={{ marginBottom: '20px' }}>
                          <h5 style={{ 
                            color: '#1B1F3B',
                            marginBottom: '10px',
                            fontSize: '15px',
                            fontWeight: '600',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px'
                          }}>
                            <span>🕸️</span> Subgraph Visualization
                          </h5>
                          <div style={{ 
                            height: '400px', 
                            border: '2px solid #ddd', 
                            borderRadius: '8px', 
                            overflow: 'hidden',
                            backgroundColor: '#fafafa'
                          }}>
                            <SpaceGraph
                              nodes={graphSearchResults.nodes.map((node, index) => {
                                // Convert to full node format
                                const fullNode = nodes.find(n => String(n.id) === String(node.id));
                                return fullNode || {
                                  id: String(node.id),
                                  type: 'circular',
                                  position: { 
                                    x: 100 + (index % 5) * 150 + Math.random() * 50, 
                                    y: 100 + Math.floor(index / 5) * 100 + Math.random() * 50 
                                  },
                                  data: {
                                    label: node.label,
                                    description: node.description
                                  }
                                };
                              })}
                              edges={graphSearchResults.edges.map(edge => {
                                // Convert to full edge format
                                const fullEdge = edges.find(e => String(e.id) === String(edge.id));
                                return fullEdge || {
                                  id: String(edge.id),
                                  source: String(edge.source),
                                  target: String(edge.target),
                                  label: edge.label
                                };
                              })}
                              loading={false}
                              error={null}
                              onNodeClick={handleNodeClick}
                              onEdgeClick={handleEdgeClick}
                            />
                          </div>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' }}>
                            {/* Nodes Section */}
                            {graphSearchResults.nodes && graphSearchResults.nodes.length > 0 && (
                                <div>
                                    <h5 style={{ 
                                      color: '#fff',
                                      backgroundColor: '#0076B5',
                                      padding: '10px 12px',
                                      marginBottom: '12px', 
                                      borderRadius: '4px',
                                      fontSize: '14px',
                                      fontWeight: '600'
                                    }}>
                                        🔵 Nodes ({graphSearchResults.nodes.length})
                                    </h5>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                        {graphSearchResults.nodes.map(node => (
                                            <div 
                                                key={node.id}
                                                style={{
                                                    padding: '14px',
                                                    backgroundColor: '#fff',
                                                    borderRadius: '6px',
                                                    border: '2px solid #e3f2fd',
                                                    cursor: 'pointer',
                                                    transition: 'all 0.2s',
                                                    boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
                                                }}
                                                onClick={() => {
                                                    const fullNode = nodes.find(n => n.id === node.id) || node;
                                                    handleNodeClick(null, fullNode);
                                                }}
                                                onMouseEnter={(e) => {
                                                    e.currentTarget.style.borderColor = '#0076B5';
                                                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,118,181,0.2)';
                                                    e.currentTarget.style.transform = 'translateY(-2px)';
                                                }}
                                                onMouseLeave={(e) => {
                                                    e.currentTarget.style.borderColor = '#e3f2fd';
                                                    e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.05)';
                                                    e.currentTarget.style.transform = 'translateY(0)';
                                                }}
                                            >
                                                <div style={{ 
                                                  fontWeight: '700', 
                                                  color: '#0076B5', 
                                                  marginBottom: '6px',
                                                  fontSize: '15px'
                                                }}>
                                                    {node.label}
                                                </div>
                                                {node.description && (
                                                    <div style={{ 
                                                      fontSize: '13px', 
                                                      color: '#555',
                                                      lineHeight: '1.4'
                                                    }}>
                                                        {node.description}
                                                    </div>
                                                )}
                                                <div style={{ 
                                                  marginTop: '8px', 
                                                  fontSize: '11px', 
                                                  color: '#999',
                                                  fontStyle: 'italic'
                                                }}>
                                                  Click to view details
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Edges Section */}
                            {graphSearchResults.edges && graphSearchResults.edges.length > 0 && (
                                <div>
                                    <h5 style={{ 
                                      color: '#fff',
                                      backgroundColor: '#2D6A4F',
                                      padding: '10px 12px',
                                      marginBottom: '12px', 
                                      borderRadius: '4px',
                                      fontSize: '14px',
                                      fontWeight: '600'
                                    }}>
                                        🔗 Edges ({graphSearchResults.edges.length})
                                    </h5>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                        {graphSearchResults.edges.map(edge => {
                                            const sourceNode = graphSearchResults.nodes.find(n => n.id === edge.source) || nodes.find(n => n.id === edge.source);
                                            const targetNode = graphSearchResults.nodes.find(n => n.id === edge.target) || nodes.find(n => n.id === edge.target);
                                            const sourceName = sourceNode ? sourceNode.label : `Node ${edge.source}`;
                                            const targetName = targetNode ? targetNode.label : `Node ${edge.target}`;

                                            return (
                                                <div 
                                                    key={edge.id}
                                                    style={{
                                                        padding: '14px',
                                                        backgroundColor: '#fff',
                                                        borderRadius: '6px',
                                                        border: '2px solid #e8f5e9',
                                                        cursor: 'pointer',
                                                        transition: 'all 0.2s',
                                                        boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
                                                    }}
                                                    onClick={() => {
                                                         const fullEdge = edges.find(e => e.id === edge.id) || edge;
                                                         handleEdgeClick(null, fullEdge);
                                                    }}
                                                    onMouseEnter={(e) => {
                                                        e.currentTarget.style.borderColor = '#2D6A4F';
                                                        e.currentTarget.style.boxShadow = '0 4px 12px rgba(45,106,79,0.2)';
                                                        e.currentTarget.style.transform = 'translateY(-2px)';
                                                    }}
                                                    onMouseLeave={(e) => {
                                                        e.currentTarget.style.borderColor = '#e8f5e9';
                                                        e.currentTarget.style.boxShadow = '0 1px 3px rgba(0,0,0,0.05)';
                                                        e.currentTarget.style.transform = 'translateY(0)';
                                                    }}
                                                >
                                                    <div style={{ 
                                                      display: 'flex', 
                                                      flexDirection: 'column',
                                                      gap: '8px'
                                                    }}>
                                                        <div style={{ 
                                                          display: 'flex', 
                                                          alignItems: 'center', 
                                                          gap: '8px', 
                                                          fontSize: '13px', 
                                                          fontWeight: '600', 
                                                          color: '#1B1F3B',
                                                          flexWrap: 'wrap'
                                                        }}>
                                                            <span style={{ 
                                                              backgroundColor: '#e3f2fd', 
                                                              padding: '4px 8px', 
                                                              borderRadius: '4px',
                                                              color: '#0076B5'
                                                            }}>
                                                              {sourceName}
                                                            </span>
                                                            <span style={{ 
                                                              color: '#2D6A4F', 
                                                              fontSize: '11px',
                                                              fontWeight: '700',
                                                              display: 'flex',
                                                              alignItems: 'center',
                                                              gap: '4px'
                                                            }}>
                                                              ── {edge.label} ──▶
                                                            </span>
                                                            <span style={{ 
                                                              backgroundColor: '#e3f2fd', 
                                                              padding: '4px 8px', 
                                                              borderRadius: '4px',
                                                              color: '#0076B5'
                                                            }}>
                                                              {targetName}
                                                            </span>
                                                        </div>
                                                        <div style={{ 
                                                          fontSize: '11px', 
                                                          color: '#999',
                                                          fontStyle: 'italic'
                                                        }}>
                                                          Click to view details
                                                        </div>
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>
          )}

        </div>

        {/* Search Results Section */}


        {/* Graph Visualization with Filter */}
        <div style={{ marginBottom: "30px" }}>
          <h3>Space Graph</h3>
          <div style={{ 
            display: "flex", 
            gap: isLeftPanelCollapsed ? "0" : "12px", 
            alignItems: "flex-start",
            marginLeft: isLeftPanelCollapsed ? "30px" : "0",
            transition: "all 0.3s ease"
          }}>
            {/* Left side: Filter and Legend (Collapsible) */}
            {!isLeftPanelCollapsed && (
              <div style={{ width: "240px", display: "flex", flexDirection: "column", gap: "12px" }}>
                <InstanceTypeFilter
                  instanceTypes={instanceTypes}
                  selectedTypes={selectedInstanceTypes}
                  onToggleType={(typeId) => {
                    const newSet = new Set(selectedInstanceTypes);
                    if (newSet.has(typeId)) {
                      newSet.delete(typeId);
                    } else {
                      newSet.add(typeId);
                    }
                    setSelectedInstanceTypes(newSet);
                  }}
                  onSelectAll={() => {
                    setSelectedInstanceTypes(new Set(instanceTypes.map(t => t.group_id)));
                  }}
                  onDeselectAll={() => {
                    setSelectedInstanceTypes(new Set());
                  }}
                />
                
                <InstanceTypeLegend
                  visibleTypes={instanceTypes}
                  isVisible={showLegend}
                  onToggle={() => setShowLegend(!showLegend)}
                />
              </div>
            )}

            {/* Graph Container with both toggle buttons */}
            <div
              style={{
                flex: 1,
                position: "relative"
              }}
            >
              {/* Left Panel Toggle Button */}
              <button
                onClick={() => setIsLeftPanelCollapsed(!isLeftPanelCollapsed)}
                style={{
                  position: "absolute",
                  left: isLeftPanelCollapsed ? "-24px" : "-2px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  zIndex: 1000,
                  backgroundColor: "var(--color-accent)",
                  color: "var(--color-white)",
                  border: "1px solid var(--color-accent-dark, #5a0ca8)",
                  borderRadius: "4px",
                  padding: "4px 3px",
                  cursor: "pointer",
                  fontSize: "12px",
                  lineHeight: 1,
                  boxShadow: "0 1px 3px rgba(0,0,0,0.15)",
                  transition: "all 0.3s ease",
                  width: "20px",
                  height: "40px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  opacity: 0.85
                }}
                onMouseEnter={(e) => e.currentTarget.style.opacity = "1"}
                onMouseLeave={(e) => e.currentTarget.style.opacity = "0.85"}
                title={isLeftPanelCollapsed ? t("graph.showFilters") : t("graph.hideFilters")}
              >
                {isLeftPanelCollapsed ? "►" : "◄"}
              </button>

              {/* Right Panel Toggle Button */}
              <button
                onClick={() => setIsRightPanelCollapsed(!isRightPanelCollapsed)}
                style={{
                  position: "absolute",
                  right: isRightPanelCollapsed ? "-24px" : "-2px",
                  top: "50%",
                  transform: "translateY(-50%)",
                  zIndex: 1000,
                  backgroundColor: "var(--color-accent)",
                  color: "var(--color-white)",
                  border: "1px solid var(--color-accent-dark, #5a0ca8)",
                  borderRadius: "4px",
                  padding: "4px 3px",
                  cursor: "pointer",
                  fontSize: "12px",
                  lineHeight: 1,
                  boxShadow: "0 1px 3px rgba(0,0,0,0.15)",
                  transition: "all 0.3s ease",
                  width: "20px",
                  height: "40px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  opacity: 0.85
                }}
                onMouseEnter={(e) => e.currentTarget.style.opacity = "1"}
                onMouseLeave={(e) => e.currentTarget.style.opacity = "0.85"}
                title={isRightPanelCollapsed ? t("graph.showSidebar") : t("graph.hideSidebar")}
              >
                {isRightPanelCollapsed ? "◄" : "►"}
              </button>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "10px" }}>
            <h3 style={{ margin: 0 }}>{t("graph.spaceGraphTitle")}</h3>
            <button
              onClick={handleFullscreenToggle}
              style={{
                padding: "8px 16px",
                background: "var(--color-gray-200)",
                color: "var(--color-text)",
                border: "none",
                borderRadius: "6px",
                cursor: "pointer",
                fontSize: "14px",
                fontWeight: "500",
                display: "flex",
                alignItems: "center",
                gap: "8px",
                transition: "all 0.2s",
              }}
              onMouseEnter={(e) => e.currentTarget.style.background = "var(--color-gray-200)"}
              onMouseLeave={(e) => e.currentTarget.style.background = "var(--color-gray-300)"}
            >
              <span>⤢</span> {t("graph.fullscreen")}
            </button>
          </div>
              {/* Actual Graph with border and styling */}
              <div
                ref={graphContainerRef}
                className={isGraphFullscreen ? "graph-container-fullscreen" : ""}
                style={{
                  width: "100%",
                  height: isGraphFullscreen ? "100vh" : "600px",
                  border: "1px solid #ddd",
                  borderRadius: "8px",
                  overflow: "hidden",
                  background: "#f8f9fa",
                  position: "relative",
                }}
              >
            {isGraphFullscreen && (
              <button
                className="fullscreen-exit-btn"
                onClick={handleFullscreenToggle}
              >
                <span>⨯</span> {t("graph.exitFullscreen")}
              </button>
            )}
                <SpaceGraph
                  nodes={nodesWithColors}
                  edges={edges}
                  loading={graphLoading}
                  error={graphError}
                  onNodeClick={handleNodeClick}
                  onEdgeClick={handleEdgeClick}
                  selectedInstanceTypes={selectedInstanceTypes}
                  isFullscreen={isGraphFullscreen}
                />
              </div>
            </div>
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
                    <h5 style={{ margin: 0 }}>{t("spaceAnalytics.location")} ({t("common.optional")}):</h5>
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
                          {t("spaceAnalytics.country")}:
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
                          <option value="">{t("spaceAnalytics.selectCountry")}</option>
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
                            {t("spaceAnalytics.city")}:
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
                            <option value="">{loadingCities ? t("spaceAnalytics.loadingCities") : t("spaceAnalytics.selectCity")}</option>
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
                            {t("spaceAnalytics.district")}:
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
                            <option value="">{loadingDistricts ? t("spaceAnalytics.loadingDistricts") : t("spaceAnalytics.selectDistrict")}</option>
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
                            {t("spaceAnalytics.street")}:
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
                            <option value="">{loadingStreets ? t("spaceAnalytics.loadingStreets") : t("spaceAnalytics.selectStreet")}</option>
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

      {/* Right sidebar (Collapsible) */}
      {!isRightPanelCollapsed && (
        <div
          style={{
            width: "20%",
            display: "flex",
            flexDirection: "column",
            gap: "16px",
            transition: "all 0.3s ease"
          }}
        >
          <ActivityStream spaceId={id} dense onNodeClick={handleActivityNodeClick} />

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
                <>
                  {showAllCollaborators ? (
                    <ul style={{ listStyleType: "none", padding: 0, margin: 0 }}>
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
                            color: "var(--color-accent)",
                            textDecoration: "underline",
                          }}
                          onClick={() => navigate(`/profile/${collaborator}`)}
                        >
                          {collaborator}
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                      {topCollaborators.length > 0 ? (
                        topCollaborators.slice(0, 5).map((collaborator, index) => (
                          <div
                            key={collaborator.id || index}
                            style={{
                              padding: "10px",
                              borderRadius: "6px",
                              border: "1px solid var(--color-gray-200)",
                              cursor: "pointer",
                              transition: "transform 0.2s ease, box-shadow 0.2s ease",
                            }}
                            onMouseEnter={(e) => {
                              e.currentTarget.style.transform = "translateY(-2px)";
                              e.currentTarget.style.boxShadow = "0 2px 8px rgba(0, 0, 0, 0.1)";
                            }}
                            onMouseLeave={(e) => {
                              e.currentTarget.style.transform = "translateY(0)";
                              e.currentTarget.style.boxShadow = "none";
                            }}
                            onClick={() => navigate(`/profile/${collaborator.username}`)}
                          >
                            <div style={{
                              display: "flex",
                              alignItems: "center",
                              gap: "8px",
                            }}>
                              <div style={{
                                minWidth: "20px",
                                height: "20px",
                                borderRadius: "50%",
                                background: index < 3 ? "var(--color-danger)" : "var(--color-gray-400)",
                                color: "var(--color-white)",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                fontSize: "11px",
                                fontWeight: "bold",
                              }}>
                                {index + 1}
                              </div>
                              <div style={{
                                fontSize: "14px",
                                fontWeight: "600",
                                color: "var(--color-primary-text)",
                                flex: 1,
                              }}>
                                {collaborator.username}
                              </div>
                            </div>
                          </div>
                        ))
                      ) : (
                        <div style={{ 
                          fontSize: "13px", 
                          color: "var(--color-secondary-text)",
                          fontStyle: "italic",
                          padding: "8px"
                        }}>
                          {t("space.noContributionData")}
                        </div>
                      )}
                    </div>
                  )}
                  
                  {topCollaborators.length > 0 && (
                    <button
                      onClick={() => setShowAllCollaborators(!showAllCollaborators)}
                      style={{
                        width: "100%",
                        marginTop: "10px",
                        padding: "8px",
                        background: "var(--color-bg)",
                        border: "1px solid var(--color-gray-200)",
                        borderRadius: "4px",
                        color: "#0076B5",
                        fontSize: "13px",
                        fontWeight: "500",
                        cursor: "pointer",
                        transition: "background-color 0.2s ease",
                      }}
                      onMouseEnter={(e) => e.target.style.background = "var(--color-white)"} 
                      onMouseLeave={(e) => e.target.style.background = "var(--color-gray-200)"}
                    >
                      {showAllCollaborators ? t("space.showTopContributors") : t("space.seeAllCollaborators")}
                    </button>
                  )}
                </>
              ) : (
                <p style={{ margin: 0, fontSize: "13px", color: "var(--color-text)", fontStyle: "italic" }}>
                  {t("space.noCollaboratorsYet")}
                </p>
              )}
            </div>
          )}
        </div>

        {/* Add discussions component */}
        <SpaceDiscussions spaceId={id} isCollaborator={isCollaborator} isArchived={space.is_archived} />
        </div>
      )}

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
        spaceTitle={space.title || "Space"}
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
      {/* AI Summary Modal */}
      {showAiSummary && (
        <div 
          className="modal-backdrop" 
          onClick={() => setShowAiSummary(false)}
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000
          }}
        >
          <div 
            className="ai-summary-modal"
            onClick={(e) => e.stopPropagation()}
            style={{
              backgroundColor: 'white',
              borderRadius: '12px',
              padding: '30px',
              maxWidth: '700px',
              width: '90%',
              maxHeight: '80vh',
              overflowY: 'auto',
              boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)'
            }}
          >
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between', 
              alignItems: 'center',
              marginBottom: '20px'
            }}>
              <h2 style={{ 
                margin: 0, 
                fontSize: '24px',
                color: '#1B1F3B',
                display: 'flex',
                alignItems: 'center',
                gap: '10px'
              }}>
                <span>✨</span>
                AI Summary
              </h2>
              <button
                onClick={() => setShowAiSummary(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#666',
                  padding: '0',
                  width: '30px',
                  height: '30px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                ×
              </button>
            </div>

            {loadingAiSummary && (
              <div style={{
                textAlign: 'center',
                padding: '40px 20px',
                color: '#666'
              }}>
                <div style={{
                  width: '40px',
                  height: '40px',
                  border: '4px solid #f3f3f3',
                  borderTop: '4px solid #2D6A4F',
                  borderRadius: '50%',
                  animation: 'spin 1s linear infinite',
                  margin: '0 auto 20px'
                }}></div>
                <p style={{ margin: 0, fontSize: '16px' }}>
                  Generating AI summary...
                </p>
                <style>
                  {`
                    @keyframes spin {
                      0% { transform: rotate(0deg); }
                      100% { transform: rotate(360deg); }
                    }
                  `}
                </style>
              </div>
            )}

            {aiSummaryError && !loadingAiSummary && (
              <div style={{
                padding: '20px',
                backgroundColor: '#fee',
                borderRadius: '8px',
                color: '#c33',
                marginBottom: '20px'
              }}>
                <p style={{ margin: 0, fontWeight: '600' }}>Error</p>
                <p style={{ margin: '8px 0 0 0' }}>{aiSummaryError}</p>
              </div>
            )}

            {aiSummary && !loadingAiSummary && !aiSummaryError && (
              <div>
                <div style={{
                  backgroundColor: '#f8f9fa',
                  padding: '15px',
                  borderRadius: '8px',
                  marginBottom: '20px',
                  display: 'flex',
                  gap: '20px',
                  flexWrap: 'wrap'
                }}>
                  <div style={{ flex: '1', minWidth: '120px' }}>
                    <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>
                      Nodes
                    </div>
                    <div style={{ fontSize: '20px', fontWeight: '600', color: '#2D6A4F' }}>
                      {aiSummary.metadata?.node_count || 0}
                    </div>
                  </div>
                  <div style={{ flex: '1', minWidth: '120px' }}>
                    <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>
                      Edges
                    </div>
                    <div style={{ fontSize: '20px', fontWeight: '600', color: '#2D6A4F' }}>
                      {aiSummary.metadata?.edge_count || 0}
                    </div>
                  </div>
                  <div style={{ flex: '1', minWidth: '120px' }}>
                    <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>
                      Discussions
                    </div>
                    <div style={{ fontSize: '20px', fontWeight: '600', color: '#2D6A4F' }}>
                      {aiSummary.metadata?.discussion_count || 0}
                    </div>
                  </div>
                  <div style={{ flex: '1', minWidth: '120px' }}>
                    <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px' }}>
                      Collaborators
                    </div>
                    <div style={{ fontSize: '20px', fontWeight: '600', color: '#2D6A4F' }}>
                      {aiSummary.metadata?.collaborator_count || 0}
                    </div>
                  </div>
                </div>

                <div 
                  className="ai-summary-content"
                  style={{
                    fontSize: '15px',
                    lineHeight: '1.7',
                    color: '#333'
                  }}
                  dangerouslySetInnerHTML={{ 
                    __html: marked.parse(aiSummary.summary || '') 
                  }}
                />
                
                <style>
                  {`
                    .ai-summary-content h3 {
                      color: #1B1F3B;
                      font-size: 18px;
                      font-weight: 600;
                      margin: 20px 0 10px 0;
                      border-bottom: 2px solid #2D6A4F;
                      padding-bottom: 5px;
                    }
                    
                    .ai-summary-content h3:first-child {
                      margin-top: 0;
                    }
                    
                    .ai-summary-content strong {
                      color: #2D6A4F;
                      font-weight: 600;
                    }
                    
                    .ai-summary-content p {
                      margin: 12px 0;
                    }
                    
                    .ai-summary-content ul {
                      margin: 10px 0;
                      padding-left: 25px;
                    }
                    
                    .ai-summary-content li {
                      margin: 6px 0;
                    }
                    
                    .ai-summary-content code {
                      background-color: #f5f5f5;
                      padding: 2px 6px;
                      border-radius: 3px;
                      font-family: monospace;
                      font-size: 14px;
                    }
                  `}
                </style>
              </div>
            )}

            <div style={{ marginTop: '30px', textAlign: 'right' }}>
              <button
                onClick={() => setShowAiSummary(false)}
                style={{
                  padding: '10px 24px',
                  backgroundColor: '#2D6A4F',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontSize: '14px',
                  fontWeight: '600'
                }}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SpaceDetails;
