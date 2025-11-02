import { useState } from "react";
import PropTypes from "prop-types";
import { useTranslation } from "../contexts/TranslationContext";
import useClickOutside from "../hooks/useClickOutside";

const ReportModal = ({ contentId, contentType, contentTitle, onClose }) => {
  const { t } = useTranslation();
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState(null);

  const commonReasons = [
    t("report.inappropriateContent"),
    t("report.misinformation"),
    t("report.spam"),
    t("report.harassment"),
    t("report.other"),
  ];

  const specificReasons = {
    Space: [t("report.duplicateSpace"), t("report.misleadingTitle")],
    Node: [t("report.inaccurateInfo"), t("report.duplicateNode"), t("report.unverifiedSource")],
    Discussion: [t("report.offTopic"), t("report.offensiveLanguage")],
    Profile: [t("report.fakeAccount"), t("report.impersonation")],
  };

  console.log("contentType:", contentType);
  console.log("specificReasons for this type:", specificReasons[contentType]);

  const reportReasons = [
    ...commonReasons,
    ...(specificReasons[contentType] || []),
  ];

  console.log("All reportReasons:", reportReasons);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!reason) {
      setError(t("report.selectReason"));
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const reportData = {
        contentId,
        contentType,
        reason: reason,
      };
      console.log("Report data:", reportData);

      await new Promise((resolve) => setTimeout(resolve, 500));

      setSuccess(true);
      setTimeout(() => {
        onClose();
      }, 2000);
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
        style={{ maxWidth: "500px" }}
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
                  {reportReasons.map((r) => (
                    <option key={r} value={r}>
                      {r}
                    </option>
                  ))}
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
