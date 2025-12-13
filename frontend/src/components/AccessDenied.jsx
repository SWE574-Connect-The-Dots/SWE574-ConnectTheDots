import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "../contexts/TranslationContext";

const AccessDenied = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();

  useEffect(() => {
    const modal = document.getElementById("accessDeniedModal");
    if (modal) {
      modal.style.display = "flex";
    }
  }, []);

  const handleOkClick = () => {
    navigate("/");
  };

  return (
    <div
      id="accessDeniedModal"
      style={{
        display: "flex",
        position: "fixed",
        top: 0,
        left: 0,
        width: "100%",
        height: "100%",
        backgroundColor: "rgba(0, 0, 0, 0.5)",
        justifyContent: "center",
        alignItems: "center",
        zIndex: 9999,
      }}
    >
      <div
        style={{
          backgroundColor: "var(--color-white)",
          borderRadius: "12px",
          padding: "40px",
          maxWidth: "500px",
          width: "90%",
          boxShadow: "0 8px 32px rgba(0, 0, 0, 0.2)",
          textAlign: "center",
        }}
      >
        <div
          style={{
            fontSize: "48px",
            marginBottom: "20px",
          }}
        >
          🔒
        </div>
        <h2
          style={{
            color: "var(--color-text)",
            marginBottom: "15px",
            fontSize: "24px",
            fontWeight: "600",
          }}
        >
          {t("errors.accessDenied")}
        </h2>
        <p
          style={{
            color: "var(--color-gray-400)",
            marginBottom: "30px",
            fontSize: "16px",
            lineHeight: "1.6",
          }}
        >
          {t("errors.accessDeniedMessage")}
        </p>
        <button
          onClick={handleOkClick}
          style={{
            backgroundColor: "var(--color-accent)",
            color: "var(--color-white)",
            border: "none",
            borderRadius: "8px",
            padding: "12px 40px",
            fontSize: "16px",
            fontWeight: "600",
            cursor: "pointer",
            transition: "background-color 0.2s",
          }}
          onMouseOver={(e) => {
            e.currentTarget.style.backgroundColor = "var(--color-accent-hover)";
          }}
          onMouseOut={(e) => {
            e.currentTarget.style.backgroundColor = "var(--color-accent)";
          }}
        >
          {t("common.ok")}
        </button>
      </div>
    </div>
  );
};

export default AccessDenied;

