import { useLocation, useParams, useNavigate } from "react-router-dom";
import { useEffect, useState, useCallback } from "react";
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

.entity-indicator {
  font-size: 0.85em;
  color: #888;
  font-style: italic;
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

const nodeTypes = {
  circular: CircularNode,
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
  });
  const [isCollaborator, setIsCollaborator] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isCollaboratorsOpen, setIsCollaboratorsOpen] = useState(false);
  const [snapshots, setSnapshots] = useState([]);
  const [query, setQuery] = useState("");
  const [selectedEntity, setSelectedEntity] = useState(null);
  const [entityProperties, setEntityProperties] = useState([]);
  const [selectedProperties, setSelectedProperties] = useState([]);
  const [edgeLabel, setEdgeLabel] = useState("");
  const [existingNodes, setExistingNodes] = useState([]);
  const [relatedNodeId, setRelatedNodeId] = useState("");
  const [isNewNodeSource, setIsNewNodeSource] = useState(false);
  const [selectedNode, setSelectedNode] = useState(null);
  const [selectedEdge, setSelectedEdge] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");

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
        setExistingNodes(nodesResponse.data);
        fetchGraphData();
      } catch (error) {
        console.error("Error fetching space data:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [id, fetchGraphData]);

  const handleSearch = async () => {
    await search(query);
  };

  const handleEntitySelection = async (e) => {
    const entityId = e.target.value;
    if (!entityId) {
      setSelectedEntity(null);
      setEntityProperties([]);
      return;
    }

    const entity = searchResults.find((entity) => entity.id === entityId);
    setSelectedEntity(entity);
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
      fetchGraphData();
    } catch (err) {
      console.error("Failed to refresh data after node update:", err);
    }
  }, [id, fetchGraphData]);

  const handleEdgeClick = useCallback((event, edge) => {
    setSelectedEdge(edge);
  }, []);

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

    const renderPropertyValue = (prop) => {
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
              />
              <label
                htmlFor={`prop-${prop.property}`}
                className="property-selection-label"
                onClick={(e) => e.stopPropagation()}
              >
                <span className="property-label">
                  {prop.property_label || prop.property}:
                </span>{" "}
                {renderPropertyValue(prop)}
              </label>
            </div>
          ))}
        </div>
      </div>
    );
  };

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
                  background: "#e53935",
                  color: "white",
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
                  (e.currentTarget.style.background = "#b71c1c")
                }
                onMouseOut={(e) =>
                  (e.currentTarget.style.background = "#e53935")
                }
              >
                Delete
              </button>
            )}
          </div>
        </div>
        <p>{space.description}</p>
        <ul className="tags-list">
          {space.tags.map((tag) => (
            <li key={tag.id || tag.name} className="tag-item">
              <strong>{tag.name}</strong>
              {tag.wikidata_label && (
                <p className="tag-label">{tag.wikidata_label}</p>
              )}
              {tag.wikidata_id && (
                <span className="tag-id">ID: {tag.wikidata_id}</span>
              )}
            </li>
          ))}
        </ul>

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
                  <PropertySelectionList
                    properties={entityProperties}
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
                      backgroundColor: isNewNodeSource ? "#4CAF50" : "#f44336",
                      color: "white",
                      border: "none",
                      borderRadius: "4px",
                      cursor: "pointer",
                    }}
                  >
                    {isNewNodeSource ? "New → Existing" : "Existing → New"}
                  </button>
                </div>
                <div
                  style={{ marginTop: "10px", color: "#666", fontSize: "14px" }}
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
                  <input
                    type="text"
                    value={edgeLabel}
                    onChange={(e) => setEdgeLabel(e.target.value)}
                    placeholder="e.g., related_to"
                    style={{ width: "100%", maxWidth: "500px" }}
                  />
                </div>
                <button
                  style={{ marginTop: "20px" }}
                  disabled={!selectedEntity}
                  onClick={() => {
                    api
                      .post(
                        `/spaces/${id}/add-node/`,
                        {
                          related_node_id: relatedNodeId,
                          wikidata_entity: selectedEntity,
                          selected_properties: selectedProperties,
                          edge_label: edgeLabel,
                          is_new_node_source: isNewNodeSource,
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
                          console.log("Snapshot created after node addition.");
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
                  <div style={{ color: "red" }}>{deleteError}</div>
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
                  style={{ background: "#e53935", color: "white" }}
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
            border: "1px solid #ddd",
            borderRadius: "4px",
            overflow: "hidden",
          }}
        >
          <div
            style={{
              backgroundColor: "#f1f1f1",
              padding: "10px",
              cursor: "pointer",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
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
                            ? "1px solid #eee"
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
    </div>
  );
};

export default SpaceDetails;
