import { useState } from "react";
import PropTypes from "prop-types";
import api from "../axiosConfig";
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
  const [label, setLabel] = useState(edge.label || "");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(false);

  const handleUpdate = async () => {
    setLoading(true);
    setError(null);
    try {
      await api.put(
        `/spaces/${spaceId}/edges/${edge.id}/update/`,
        { label },
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
        <style>{/* Reuse NodeDetailModal styles */}</style>
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
            <h4>Edit Edge Label</h4>
            <input
              type="text"
              value={label}
              onChange={(e) => setLabel(e.target.value)}
              style={{ width: "100%", marginBottom: 10 }}
              disabled={loading}
            />
            <button
              onClick={handleUpdate}
              className="save-button"
              disabled={loading}
            >
              Update Label
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
