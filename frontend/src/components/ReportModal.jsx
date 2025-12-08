import { useEffect, useMemo, useState } from "react";
import PropTypes from "prop-types";
import { useTranslation } from "../contexts/TranslationContext";
import api from "../axiosConfig";
import { API_ENDPOINTS } from "../constants/config";
import useClickOutside from "../hooks/useClickOutside";

const ReportModal = ({ contentId, contentType, contentTitle, onClose }) => {
  const { t } = useTranslation();
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState(null);
  const [reasonsPayload, setReasonsPayload] = useState(null);
  const [reasonsLoading, setReasonsLoading] = useState(true);

  useEffect(() => {
    const fetchReasons = async () => {
      try {
        setReasonsLoading(true);
        const res = await api.get(API_ENDPOINTS.REPORTS_REASONS);
        setReasonsPayload(res.data);
      } catch (e) {
        console.error("Failed to load report reasons", e);
        setError(t("report.submitFailed"));
      } finally {
        setReasonsLoading(false);
      }
    };
    fetchReasons();
  }, [t]);

  const options = useMemo(() => {
    if (!reasonsPayload) return [];
    const key = String(contentType).toLowerCase();
    const entries = reasonsPayload?.reasons?.[key] || [];
    return entries.map(({ code, label }) => ({ code, label }));
  }, [reasonsPayload, contentType]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!reason) {
      setError(t("report.selectReason"));
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const payload = {
        content_type: String(contentType).toLowerCase(),
        content_id: contentId,
        reason: reason,
      };

      await api.post(API_ENDPOINTS.REPORTS, payload, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      setSuccess(true);
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      console.error("Error submitting report:", err);
      setError(t("report.submitFailed"));
    } finally {
      setLoading(false);
    }
  };

  const modalRef = useClickOutside(onClose);

  return (
    <div className="modal-backdrop" onClick={(e) => e.stopPropagation()}>
      <div
        className="modal-content"
        ref={modalRef}
        style={{ maxWidth: "500px", width: "100%" }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <h2>{t("report.reportTitle")} {contentType}</h2>
          <button onClick={onClose} className="close-button">
            ×
          </button>
        </div>

        <div className="modal-body">
          {success ? (
            <div className="success-message">
              <p>{t("report.thankYou")}</p>
            </div>
          ) : (
            <form onSubmit={handleSubmit}>
              <div>
                <p style={{ color: "var(--color-text-secondary)" }}>
                  <strong>{contentType}:</strong> {contentTitle}
                </p>
              </div>

              <div style={{ marginBottom: "20px" }}>
                <label
                  htmlFor="report-reason"
                  style={{
                    display: "block",
                    marginBottom: "10px",
                    fontWeight: "bold",
                  }}
                >
                  {t("report.reasonLabel")}
                </label>
                <select
                  id="report-reason"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  style={{
                    width: "100%",
                    padding: "10px",
                    border: "1px solid var(--color-border)",
                    borderRadius: "4px",
                    backgroundColor: "var(--color-bg)",
                    color: "var(--color-text)",
                  }}
                >
                  <option value="">{t("report.selectReasonPlaceholder")}</option>
                  {reasonsLoading ? (
                    <option disabled>{t("common.loading")}</option>
                  ) : (
                    options.map((opt) => (
                      <option key={opt.code} value={opt.code}>
                        {opt.label}
                      </option>
                    ))
                  )}
                </select>
              </div>

              {error && (
                <div
                  style={{
                    color: "var(--color-danger)",
                    marginBottom: "15px",
                    fontSize: "0.9rem",
                  }}
                >
                  {error}
                </div>
              )}

              <div
                style={{
                  display: "flex",
                  justifyContent: "flex-end",
                  gap: "10px",
                }}
              >
                <button
                  type="button"
                  onClick={onClose}
                  style={{
                    padding: "8px 16px",
                    border: "1px solid var(--color-gray-300)",
                    borderRadius: "4px",
                    backgroundColor: "var(--color-gray-100)",
                    color: "var(--color-text)",
                    cursor: "pointer",
                  }}
                >
                  {t("common.cancel")}
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  style={{
                    padding: "8px 16px",
                    border: "none",
                    borderRadius: "4px",
                    backgroundColor: "var(--color-danger)",
                    color: "white",
                    cursor: loading ? "not-allowed" : "pointer",
                    opacity: loading ? 0.7 : 1,
                  }}
                >
                  {loading ? t("report.submitting") : t("report.submitButton")}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

ReportModal.propTypes = {
  contentId: PropTypes.oneOfType([PropTypes.string, PropTypes.number])
    .isRequired,
  contentType: PropTypes.oneOf(["space", "node", "discussion", "profile"])
    .isRequired,
  contentTitle: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default ReportModal;
