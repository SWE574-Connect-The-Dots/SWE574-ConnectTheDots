import { useState, useEffect } from "react";
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

  const { fetchProperties } = useWikidataSearch();

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
            (prop) => prop.property_id
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

  const handlePropertySelection = (newSelectedProperties) => {
    setSelectedProperties(newSelectedProperties);
  };

  const handleSaveChanges = async () => {
    try {
      await api.put(
        `/spaces/${spaceId}/nodes/${node.id}/update-properties/`,
        {
          selected_properties: selectedProperties,
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

  const handleDeleteProperty = async (propertyId) => {
    try {
      await api.delete(
        `/spaces/${spaceId}/nodes/${node.id}/properties/${propertyId}/`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      setSelectedProperties(
        selectedProperties.filter((id) => id !== propertyId)
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

  const PropertySelectionList = ({
    properties,
    selectedProperties,
    onChange,
  }) => {
    const handleItemClick = (property, e) => {
      const container = e.currentTarget.parentNode;
      const scrollPos = container.scrollTop;

      e.preventDefault();
      e.stopPropagation();

      const newSelection = selectedProperties.includes(property)
        ? selectedProperties.filter((id) => id !== property)
        : [...selectedProperties, property];

      onChange(newSelection);

      setTimeout(() => {
        if (container) container.scrollTop = scrollPos;
      }, 0);
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
        <div className="property-selection-list">
          {properties.map((prop) => (
            <div
              key={prop.property}
              className={`property-selection-item ${
                selectedProperties.includes(prop.property) ? "selected" : ""
              }`}
              onClick={(e) => handleItemClick(prop.property, e)}
            >
              <input
                type="checkbox"
                id={`prop-${prop.property}`}
                checked={selectedProperties.includes(prop.property)}
                onChange={(e) => {
                  e.stopPropagation();
                  handleItemClick(prop.property, e);
                }}
                className="property-checkbox"
                onClick={(e) => e.stopPropagation()}
              />
              <label
                htmlFor={`prop-${prop.property}`}
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
                      <li key={prop.property_id} className="property-item">
                        <span className="property-content">
                          <span className="property-label">
                            {getPropertyLabelWithId(prop)}:
                          </span>{" "}
                          {renderPropertyValue(prop)}
                        </span>
                        <button
                          className="delete-property-button"
                          onClick={() => handleDeleteProperty(prop.property_id)}
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

            {availableProperties.length > 0 && (
              <div className="edit-properties-section">
                <h4>Edit Properties</h4>
                <p className="selection-help-text">
                  Select properties you want to include
                </p>
                <PropertySelectionList
                  properties={availableProperties}
                  selectedProperties={selectedProperties}
                  onChange={handlePropertySelection}
                />
                <button onClick={handleSaveChanges} className="save-button">
                  Save Changes
                </button>
              </div>
            )}

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
