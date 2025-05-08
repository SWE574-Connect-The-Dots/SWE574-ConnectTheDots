import { useLocation, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import ReactFlow, { Controls, Background } from "reactflow";
import "reactflow/dist/style.css";
import api from "../axiosConfig";
import CircularNode from "../components/CircularNode";
import useGraphData from "../hooks/useGraphData";
import useWikidataSearch from "../hooks/useWikidataSearch";
import {
  API_ENDPOINTS
} from "../constants/config";

const nodeTypes = {
  circular: CircularNode,
};

const SpaceDetails = () => {
  const location = useLocation();
  const { id } = useParams();
  const [space, setSpace] = useState({
    title: location.state?.title || "",
    description: location.state?.description || "",
    tags: location.state?.tags || [],
    collaborators: location.state?.collaborators || [],
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
        });
        
        const username = localStorage.getItem("username");
        const isUserCollaborator = spaceResponse.data.collaborators.includes(username);
        setIsCollaborator(isUserCollaborator);
        
        const snapshotsResponse = await api.get(API_ENDPOINTS.SNAPSHOTS(id), {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        setSnapshots(snapshotsResponse.data);
        
        const nodesResponse = await api.get(API_ENDPOINTS.NODES(id), {
          headers: { 
            Authorization: `Bearer ${localStorage.getItem("token")}` 
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

  const handlePropertySelection = (e) => {
    const selectedOptions = Array.from(
      e.target.selectedOptions,
      (option) => option.value
    );
    setSelectedProperties(selectedOptions);
  };
  
  const handleJoinLeaveSpace = async () => {
    try {
      const endpoint = isCollaborator ? 'leave' : 'join';
      await api.post(`/spaces/${id}/${endpoint}/`, {}, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        }
      });
      
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
      });
      
    } catch (error) {
      console.error('Error joining/leaving space:', error);
    }
  };

  return (
    <div style={{ width: "100%", margin: "0 auto", padding: "20px", display: "flex" }}>
      <div style={{ flex: 1, marginRight: "20px" }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <h2>{space.title}</h2>
          <button
            className={isCollaborator ? "leave-button" : "join-button"}
            onClick={handleJoinLeaveSpace}
            data-testid={isCollaborator ? "leave-space-button" : "header-join-space-button"}
          >
            {isCollaborator ? "LEAVE SPACE" : "JOIN SPACE"}
          </button>
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
                          Authorization: `Bearer ${localStorage.getItem("token")}`,
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
                  <h5>Select Properties (hold Ctrl/Cmd to select multiple)</h5>
                  <select
                    multiple
                    value={selectedProperties}
                    onChange={handlePropertySelection}
                    style={{ width: "100%", maxWidth: "500px", height: "200px" }}
                  >
                    {entityProperties.map((prop) => (
                      <option key={prop.property} value={prop.property}>
                        {prop.property}: {prop.value}
                      </option>
                    ))}
                  </select>
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
                <div style={{ marginTop: "10px", color: "#666", fontSize: "14px" }}>
                  {isNewNodeSource
                    ? `"${selectedEntity?.label || "New Node"}" → "${
                        existingNodes.find((n) => n.id === parseInt(relatedNodeId))
                          ?.label || "Selected Node"
                      }"`
                    : `"${
                        existingNodes.find((n) => n.id === parseInt(relatedNodeId))
                          ?.label || "Selected Node"
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
                            Authorization: `Bearer ${localStorage.getItem("token")}`,
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
            <p>Join this space as a collaborator to add nodes and modify the graph.</p>
            <button
              className="join-button"
              onClick={handleJoinLeaveSpace}
              data-testid="bottom-join-space-button"
            >
              JOIN SPACE
            </button>
          </div>
        )}
      </div>
      
      {/* Collaborators sidebar */}
      <div style={{ width: "250px" }}>
        <div 
          style={{ 
            border: "1px solid #ddd", 
            borderRadius: "4px", 
            overflow: "hidden"
          }}
        >
          <div 
            style={{ 
              backgroundColor: "#f1f1f1", 
              padding: "10px", 
              cursor: "pointer",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center"
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
                        borderBottom: index < space.collaborators.length - 1 ? "1px solid #eee" : "none" 
                      }}
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
      </div>
    </div>
  );
};

export default SpaceDetails;
