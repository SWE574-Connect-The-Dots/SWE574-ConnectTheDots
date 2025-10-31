import { useState, useMemo, useEffect } from "react";
import PropTypes from "prop-types";
import { useTranslation } from "../contexts/TranslationContext";
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
  const { t } = useTranslation();
  const wikidataPropertyId = edge.data?.wikidata_property_id || edge.wikidata_property_id;
  const originalLabel = edge.data?.original_label || edge.label;
  
  const [edgeProperty, setEdgeProperty] = useState({ 
    id: wikidataPropertyId || null, 
    label: originalLabel || "" 
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
      setError(t("errors.general"));
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
      setError(t("errors.general"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-content">
        <div className="modal-header">
          <h2>{t("graph.edgeDetails")}</h2>
          <button onClick={onClose} className="close-button">
            ×
          </button>
        </div>
        <div className="modal-body">
          <div className="node-info-section">
            <h3>{t("graph.connectedNodes")}</h3>
            <p>
              <strong>{t("graph.source")}:</strong>{" "}
              {sourceNode?.data?.label || sourceNode?.label} (ID:{" "}
              {sourceNode?.id})
            </p>
            <p>
              <strong>{t("graph.target")}:</strong>{" "}
              {targetNode?.data?.label || targetNode?.label} (ID:{" "}
              {targetNode?.id})
            </p>
            {wikidataPropertyId && (
              <div style={{
                marginTop: 12,
                padding: '8px 12px',
                background: 'var(--color-wikidata-bg)',
                border: '2px solid var(--color-wikidata-border)',
                borderRadius: 6,
                display: 'inline-block'
              }}>
                <strong style={{ color: 'var(--color-wikidata)' }}>🔗 {t("graph.wikidataProperty")}:</strong>{" "}
                <span style={{ 
                  background: 'var(--color-wikidata)', 
                  color: 'var(--color-white)', 
                  padding: '2px 8px', 
                  borderRadius: 4,
                  marginLeft: 6,
                  fontWeight: 600
                }}>
                  {wikidataPropertyId}
                </span>
              </div>
            )}
          </div>
          <div className="properties-section">
            <h4>{t("graph.editEdgeLabelDirection")}</h4>
            <PropertySearch 
              onSelect={setEdgeProperty}
              initialLabel={edgeProperty.label}
            />
            <div style={{ marginBottom: 10, marginTop: 10 }}>
              <label>{t("graph.direction")}:</label>
              <button
                type="button"
                onClick={() => setIsSourceToTarget((v) => !v)}
                style={{
                  marginLeft: 10,
                  padding: "5px 10px",
                  backgroundColor: isSourceToTarget ? "var(--color-success)" : "var(--color-danger-light)",
                  color: "var(--color-white)",
                  border: "none",
                  borderRadius: "4px",
                  cursor: duplicateExists ? "not-allowed" : "pointer",
                }}
                disabled={loading || duplicateExists}
                title={
                  duplicateExists
                    ? t("graph.edgeExistsInDirection")
                    : t("graph.toggleDirection")
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
                <span style={{ color: "var(--color-danger-light)", marginLeft: 8 }}>
                  {t("graph.edgeExistsInDirection")}
                </span>
              )}
            </div>
            <button
              onClick={handleUpdate}
              className="save-button"
              disabled={loading || duplicateExists}
            >
              {t("graph.updateLabelDirection")}
            </button>
          </div>
          <div className="danger-zone">
            <h4>{t("graph.dangerZone")}</h4>
            <p className="warning-text">
              {t("graph.deleteEdgeWarning")}
            </p>
            <button
              onClick={handleDelete}
              className={`delete-button ${confirmDelete ? "confirm" : ""}`}
              disabled={loading}
            >
              {confirmDelete
                ? t("graph.confirmDeletion")
                : t("graph.deleteEdge")}
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
