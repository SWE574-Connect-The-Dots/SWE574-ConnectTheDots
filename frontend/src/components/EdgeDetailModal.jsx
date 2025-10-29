import { useState, useMemo, useEffect } from "react";
import PropTypes from "prop-types";
import api from "../axiosConfig";
import PropertySearch from "./PropertySearch";
import "./NodeDetailModal.css";

const EdgeDetailModal = ({
  edge,
  sourceNode,
  targetNode,
  onClose,
  onEdgeUpdate,
  onEdgeDelete,
  spaceId,
}) => {
  const [edgeProperty, setEdgeProperty] = useState({ 
    id: edge.wikidata_property_id || null, 
    label: edge.label || "" 
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [isSourceToTarget, setIsSourceToTarget] = useState(true);
  const [allEdges, setAllEdges] = useState([]);

  useEffect(() => {
    const fetchEdges = async () => {
      try {
        const res = await api.get(`/spaces/${spaceId}/edges/`, {
          headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
        });
        setAllEdges(res.data);
      } catch (err) {}
    };
    fetchEdges();
  }, [spaceId]);

  const duplicateExists = useMemo(() => {
    if (!sourceNode || !targetNode) return false;
    const src = isSourceToTarget ? sourceNode.id : targetNode.id;
    const tgt = isSourceToTarget ? targetNode.id : sourceNode.id;
    return allEdges.some(
      (e) =>
        String(e.source) === String(src) &&
        String(e.target) === String(tgt) &&
        String(e.id) !== String(edge.id)
    );
  }, [allEdges, sourceNode, targetNode, isSourceToTarget, edge.id]);

  const handleUpdate = async () => {
    setLoading(true);
    setError(null);
    try {
      const srcId = isSourceToTarget ? sourceNode.id : targetNode.id;
      const tgtId = isSourceToTarget ? targetNode.id : sourceNode.id;
      await api.put(
        `/spaces/${spaceId}/edges/${edge.id}/update/`,
        { 
          label: edgeProperty.label, 
          source_id: srcId, 
          target_id: tgtId,
          wikidata_property_id: edgeProperty.id
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      onEdgeUpdate();
      onClose();
    } catch (err) {
      setError("Failed to update edge");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!confirmDelete) {
      setConfirmDelete(true);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await api.delete(`/spaces/${spaceId}/edges/${edge.id}/delete/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      onEdgeDelete();
      onClose();
    } catch (err) {
      setError("Failed to delete edge");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-content">
        <div className="modal-header">
          <h2>Edge Details</h2>
          <button onClick={onClose} className="close-button">
            ×
          </button>
        </div>
        <div className="modal-body">
          <div className="node-info-section">
            <h3>Connected Nodes</h3>
            <p>
              <strong>Source:</strong>{" "}
              {sourceNode?.data?.label || sourceNode?.label} (ID:{" "}
              {sourceNode?.id})
            </p>
            <p>
              <strong>Target:</strong>{" "}
              {targetNode?.data?.label || targetNode?.label} (ID:{" "}
              {targetNode?.id})
            </p>
          </div>
          <div className="properties-section">
            <h4>Edit Edge Label & Direction</h4>
            <PropertySearch 
              onSelect={setEdgeProperty}
              initialLabel={edgeProperty.label}
            />
            <div style={{ marginBottom: 10, marginTop: 10 }}>
              <label>Direction:</label>
              <button
                type="button"
                onClick={() => setIsSourceToTarget((v) => !v)}
                style={{
                  marginLeft: 10,
                  padding: "5px 10px",
                  backgroundColor: isSourceToTarget ? "#4CAF50" : "#f44336",
                  color: "white",
                  border: "none",
                  borderRadius: "4px",
                  cursor: duplicateExists ? "not-allowed" : "pointer",
                }}
                disabled={loading || duplicateExists}
                title={
                  duplicateExists
                    ? "Edge in this direction already exists"
                    : "Toggle direction"
                }
              >
                {isSourceToTarget
                  ? `${sourceNode?.data?.label || sourceNode?.label} → ${
                      targetNode?.data?.label || targetNode?.label
                    }`
                  : `${targetNode?.data?.label || targetNode?.label} → ${
                      sourceNode?.data?.label || sourceNode?.label
                    }`}
              </button>
              {duplicateExists && (
                <span style={{ color: "#d83025", marginLeft: 8 }}>
                  Edge in this direction already exists
                </span>
              )}
            </div>
            <button
              onClick={handleUpdate}
              className="save-button"
              disabled={loading || duplicateExists}
            >
              Update Label & Direction
            </button>
          </div>
          <div className="danger-zone">
            <h4>Danger Zone</h4>
            <p className="warning-text">
              Deleting this edge will remove the connection between these nodes.
              This action cannot be undone.
            </p>
            <button
              onClick={handleDelete}
              className={`delete-button ${confirmDelete ? "confirm" : ""}`}
              disabled={loading}
            >
              {confirmDelete
                ? "Click again to confirm deletion"
                : "Delete Edge"}
            </button>
          </div>
          {error && <div className="error">{error}</div>}
        </div>
      </div>
    </div>
  );
};

EdgeDetailModal.propTypes = {
  edge: PropTypes.object.isRequired,
  sourceNode: PropTypes.object,
  targetNode: PropTypes.object,
  onClose: PropTypes.func.isRequired,
  onEdgeUpdate: PropTypes.func.isRequired,
  onEdgeDelete: PropTypes.func.isRequired,
  spaceId: PropTypes.string.isRequired,
};

export default EdgeDetailModal;
