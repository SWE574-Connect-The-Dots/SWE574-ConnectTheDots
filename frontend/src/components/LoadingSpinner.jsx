import { useTranslation } from "../contexts/TranslationContext";

const LoadingSpinner = () => {
  const { t } = useTranslation();

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "60vh",
        gap: "20px",
      }}
    >
      <div
        style={{
          width: "50px",
          height: "50px",
          border: "5px solid var(--color-gray-200)",
          borderTop: "5px solid var(--color-accent)",
          borderRadius: "50%",
          animation: "spin 1s linear infinite",
        }}
      />
      <p style={{ color: "var(--color-text-secondary)", fontSize: "16px" }}>
        {t("common.loading")}
      </p>
      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}
      </style>
    </div>
  );
};

export default LoadingSpinner;

