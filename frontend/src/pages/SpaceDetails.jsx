import { useLocation, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";

const SpaceDetails = () => {
  const location = useLocation();
  const { id } = useParams();
  const [space, setSpace] = useState({
    title: location.state?.title || "",
    description: location.state?.description || "",
    tags: location.state?.tags || [],
  });
  const [snapshots, setSnapshots] = useState([]);
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedEntity, setSelectedEntity] = useState(null);
  const [entityProperties, setEntityProperties] = useState([]);
  const [selectedProperties, setSelectedProperties] = useState([]);
  const [edgeLabel, setEdgeLabel] = useState("");
  const [sourceNodeId, setSourceNodeId] = useState("");
  const [existingNodes, setExistingNodes] = useState([]);

  useEffect(() => {
    if (!location.state) {
      axios
        .get(`http://localhost:8000/api/spaces/${id}/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        })
        .then((res) => {
          setSpace({
            title: res.data.title,
            description: res.data.description,
            tags: res.data.tags || [],
          });
        })
        .catch((err) => {
          console.error(err);
        });
    }

    axios
      .get(`http://localhost:8000/api/spaces/${id}/snapshots/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      })
      .then((res) => setSnapshots(res.data))
      .catch((err) => console.error("Failed to fetch snapshots", err));

    axios
      .get(`http://localhost:8000/api/spaces/${id}/nodes/`, {
        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
      })
      .then((res) => setExistingNodes(res.data))
      .catch((err) => console.error("Failed to fetch nodes", err));
  }, [id, location.state]);

  const searchWikidata = async () => {
    if (!query.trim()) return;
    try {
      const response = await axios.get(
        `http://localhost:8000/api/spaces/wikidata-search/?q=${encodeURIComponent(
          query
        )}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      setSearchResults(response.data);
    } catch (err) {
      console.error("Error searching Wikidata:", err);
    }
  };

  const fetchProperties = async (entityId) => {
    try {
      const response = await axios.get(
        `http://localhost:8000/api/spaces/wikidata-entity-properties/${encodeURIComponent(
          entityId
        )}/`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      setEntityProperties(response.data);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div>
      <h2>{space.title}</h2>
      <p>{space.description}</p>
      <ul className="tags-list">
        {space.tags.map((tag) => (
          <li key={tag} className="tag-item">
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
      {existingNodes.length === 0 && (
        <p>This space has no nodes yet. Start by adding one from Wikidata!</p>
      )}
      <ul className="nodes-list">
        {existingNodes.map((node) => (
          <li key={node.id} className="node-item">
            <strong>{node.id}</strong>
            <strong>{node.label}</strong>
          </li>
        ))}
      </ul>
      <h3>Revert Graph to Previous State</h3>
      {snapshots.length > 0 ? (
        <select
          onChange={(e) => {
            const snapshotId = e.target.value;
            if (!snapshotId) return;

            axios
              .post(
                `http://localhost:8000/api/spaces/${id}/snapshots/revert/`,
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
      <h3>Add Node from Wikidata</h3>
      <input
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Search Wikidata..."
      />
      <button onClick={searchWikidata}>Search</button>
      <ul>
        {searchResults.map((entity) => (
          <li key={entity.id}>
            {entity.label} ({entity.description})
            <button
              onClick={() => {
                setSelectedEntity(entity);
                fetchProperties(entity.id);
              }}
            >
              Select
            </button>
          </li>
        ))}
      </ul>
      {selectedEntity && (
        <>
          <h4>Selected Entity: {selectedEntity.label}</h4>
          <div>
            <h5>Select Properties</h5>
            {entityProperties.map((prop) => (
              <div key={prop.property}>
                <input
                  type="checkbox"
                  value={prop.property}
                  onChange={(e) => {
                    if (e.target.checked) {
                      setSelectedProperties([
                        ...selectedProperties,
                        prop.property,
                      ]);
                    } else {
                      setSelectedProperties(
                        selectedProperties.filter((p) => p !== prop.property)
                      );
                    }
                  }}
                />
                {prop.property}: {prop.value}
              </div>
            ))}
          </div>

          <div>
            <label>Connect To Node:</label>
            <select
              value={sourceNodeId}
              onChange={(e) => setSourceNodeId(e.target.value)}
            >
              <option value="">Select a source node</option>
              {existingNodes.map((node) => (
                <option key={node.id} value={node.id}>
                  {node.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label>Edge Label:</label>
            <input
              type="text"
              value={edgeLabel}
              onChange={(e) => setEdgeLabel(e.target.value)}
              placeholder="e.g., related_to"
            />
          </div>

          <button
            disabled={!selectedEntity}
            onClick={() => {
              axios
                .post(
                  `http://localhost:8000/api/spaces/${id}/add-node/`,
                  {
                    source_node_id: sourceNodeId,
                    wikidata_entity: selectedEntity,
                    selected_properties: selectedProperties,
                    edge_label: edgeLabel,
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
                    await axios.post(
                      `http://localhost:8000/api/spaces/${id}/snapshots/create/`,
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
        </>
      )}
    </div>
  );
};

export default SpaceDetails;
