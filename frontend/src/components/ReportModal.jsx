import { useState } from "react";
import PropTypes from "prop-types";
import useClickOutside from "../hooks/useClickOutside";

const ReportModal = ({ contentId, contentType, contentTitle, onClose }) => {
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState(null);

  const commonReasons = [
    "Inappropriate content",
    "Misinformation",
    "Spam",
    "Harassment",
    "Other",
  ];

  const specificReasons = {
    Space: ["Duplicate space", "Misleading title or description"],
    Node: ["Inaccurate information", "Duplicate node", "Unverified source"],
    Discussion: ["Off-topic", "Offensive language"],
    Profile: ["Fake account", "Impersonation"],
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
      setError("Please select a reason for your report");
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
      setError("Failed to submit report. Please try again later.");
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
          <h2>Report {contentType}</h2>
          <button onClick={onClose} className="close-button">
            ×
          </button>
        </div>

        <div className="modal-body">
          {success ? (
            <div className="success-message">
              <p>Thank you for your report. We will review it shortly.</p>
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
                  Reason for report:
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
                  <option value="">-- Select a reason --</option>
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
                  Cancel
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
                  {loading ? "Submitting..." : "Submit Report"}
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