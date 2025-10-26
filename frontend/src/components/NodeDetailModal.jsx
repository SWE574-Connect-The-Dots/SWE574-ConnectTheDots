import { useState, useEffect, useMemo, useRef } from "react";
import PropTypes from "prop-types";
import api from "../axiosConfig";
import useWikidataSearch from "../hooks/useWikidataSearch";
import "./NodeDetailModal.css";

const propertySelectionStyles = `
.property-selection-container {
  border: 1px solid #ddd;
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
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
}

.property-selection-item:hover {
  background-color: #f8f9fa;
}

.property-selection-item.selected {
  background-color: #e6f4ff;
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
  color: #1a73e8;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.entity-link:hover {
  text-decoration: underline;
  color: #0f62fe;
}

.selection-help-text {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 8px;
}

.property-label {
  font-weight: 600;
  color: #444;
}
`;

const getPropertyLabelWithId = (prop) => {
  const label =
    prop.property_label ||
    (prop.display && prop.display.includes(":")
      ? prop.display.split(":")[0].trim()
      : null);

  const propId = prop.property || prop.property_id;

  if (!label) {
    return propId || "Unknown Property";
  }

  if (label.includes(propId)) {
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

    return prop.value ? String(prop.value) : "No value available";
  };

  return (
    <div className="property-selection-container">
      <div className="property-selection-list" ref={scrollContainerRef}>
        {properties.map((prop) => (
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
  const [addEdgeLabel, setAddEdgeLabel] = useState("");
  const [addEdgeError, setAddEdgeError] = useState(null);
  const [addEdgeLoading, setAddEdgeLoading] = useState(false);
  const [isCurrentNodeSource, setIsCurrentNodeSource] = useState(true);
  const [propertySearch, setPropertySearch] = useState("");

  const { fetchProperties } = useWikidataSearch();

  const filteredAndSortedProperties = useMemo(() => {
    if (!availableProperties) return [];
    return availableProperties
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

        if (node.data?.wikidata_id) {
          const properties = await fetchProperties(node.data.wikidata_id);
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
  }, [node, spaceId, fetchProperties]);

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
    if (!addEdgeTarget || !addEdgeLabel.trim()) {
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
          label: addEdgeLabel.trim(),
        },
        {
          headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        }
      );
      setAddEdgeTarget("");
      setAddEdgeLabel("");
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

  return (
    <div className="modal-backdrop">
      <div className="modal-content">
        {/* Inject CSS for property selection */}
        <style>{propertySelectionStyles}</style>

        <div className="modal-header">
          <h2>Node Details</h2>
          <button onClick={onClose} className="close-button">
            ×
          </button>
        </div>

        {loading ? (
          <div className="loading">Loading node details...</div>
        ) : error ? (
          <div className="error">{error}</div>
        ) : (
          <div className="modal-body">
            <div className="node-info-section">
              <h3>{node.data.label}</h3>
              {node.data.wikidata_id && (
                <p className="wikidata-id">
                  Wikidata ID:{" "}
                  <a
                    href={`https://www.wikidata.org/wiki/${node.data.wikidata_id}`}
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    {node.data.wikidata_id}
                  </a>
                </p>
              )}
            </div>

            <div className="properties-section">
              <h4>Node Properties</h4>
              {nodeProperties.length > 0 ? (
                <div className="current-properties">
                  <ul>
                    {nodeProperties.map((prop) => (
                      <li
                        key={prop.statement_id}
                        className="property-item"
                      >
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
                <input
                  id="add-edge-label"
                  type="text"
                  value={addEdgeLabel}
                  onChange={(e) => setAddEdgeLabel(e.target.value)}
                  style={{ width: "100%" }}
                  disabled={addEdgeLoading}
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
                    ? `${node.data.label} → ${
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
                      } → ${node.data.label}`}
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
};

export default NodeDetailModal;
