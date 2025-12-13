import React, { useState, useEffect } from "react";
import { useNavigate, Routes, Route, useLocation } from "react-router-dom";
import { useTranslation } from "../contexts/TranslationContext";
import Users from "./BackOffice/Users";
import Analytics from "./BackOffice/Analytics";
import Reports from "./BackOffice/Reports";
import Overview from "./BackOffice/Overview";
import Archive from "./BackOffice/Archive";

const BackOffice = ({ currentUser }) => {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState("dashboard");
  const navigate = useNavigate();
  const location = useLocation();
  const isAdmin = currentUser?.user_type == 1 || currentUser?.user_type == 2;

  useEffect(() => {
    const path = location.pathname;
    if (path === "/backoffice" || path === "/backoffice/") {
      setActiveTab("dashboard");
    } else if (path.includes("/users")) {
      if (!isAdmin) {
        navigate("/backoffice");
        setActiveTab("dashboard");
      } else {
        setActiveTab("users");
      }
    } else if (path.includes("/analytics")) {
      if (!isAdmin) {
        navigate("/backoffice");
        setActiveTab("dashboard");
      } else {
        setActiveTab("analytics");
      }
    } else if (path.includes("/reports")) {
      setActiveTab("reports");
    } else if (path.includes("/archive")) {
      setActiveTab("archive");
    }
  }, [location.pathname, isAdmin, navigate]);

  const handleTabClick = (tab) => {
    setActiveTab(tab);
    navigate(`/backoffice${tab === "dashboard" ? "" : `/${tab}`}`);
  };

  return (
    <div
      className="backoffice-container"
      style={{ display: "flex", minHeight: "calc(100vh - 60px)" }}
    >
      <div
        className="backoffice-sidebar"
        style={{
          width: "250px",
          color: "var(--color-white)",
          padding: "20px 0",
        }}
      >
        <nav>
          <ul style={{ listStyleType: "none", padding: 0 }}>
            <li>
              <div
                onClick={() => handleTabClick("dashboard")}
                className={activeTab === "dashboard" ? "active" : ""}
                style={{
                  display: "block",
                  padding: "15px 20px",
                  borderRight:
                    activeTab === "dashboard" ? "5px solid var(--color-black)" : "",
                  fontWeight: activeTab === "dashboard" ? "800" : "400",
                  textAlign: "left",
                  color: "var(--color-black)",
                  cursor: "pointer",
                  fontSize: "16px",
                }}
              >
                {t("backoffice.dashboard")}
              </div>
            </li>
            {isAdmin && (
              <li>
                <div
                  onClick={() => handleTabClick("users")}
                  className={activeTab === "users" ? "active" : ""}
                  style={{
                    display: "block",
                    padding: "15px 20px",
                    borderRight: activeTab === "users" ? "5px solid var(--color-black)" : "",
                    fontWeight: activeTab === "users" ? "800" : "400",
                    textAlign: "left",
                    color: "var(--color-black)",
                    cursor: "pointer",
                    fontSize: "16px",
                  }}
                >
                  {t("backoffice.userManagement")}
                </div>
              </li>
            )}
            {isAdmin && (
              <li>
                <div
                  onClick={() => handleTabClick("analytics")}
                  className={activeTab === "analytics" ? "active" : ""}
                  style={{
                    display: "block",
                    padding: "15px 20px",
                    borderRight:
                      activeTab === "analytics" ? "5px solid var(--color-black)" : "",
                    fontWeight: activeTab === "analytics" ? "800" : "400",
                    textAlign: "left",
                    color: "var(--color-black)",
                    cursor: "pointer",
                    fontSize: "16px",
                  }}
                >
                  {t("backoffice.analytics")}
                </div>
              </li>
            )}
            <li>
              <div
                onClick={() => handleTabClick("reports")}
                className={activeTab === "reports" ? "active" : ""}
                style={{
                  display: "block",
                  padding: "15px 20px",
                  borderRight: activeTab === "reports" ? "5px solid var(--color-black)" : "",
                  fontWeight: activeTab === "reports" ? "800" : "400",
                  textAlign: "left",
                  color: "var(--color-black)",
                  cursor: "pointer",
                  fontSize: "16px",
                }}
              >
                {t("backoffice.reports")}
              </div>
            </li>
            <li>
              <div
                onClick={() => handleTabClick("archive")}
                className={activeTab === "archive" ? "active" : ""}
                style={{
                  display: "block",
                  padding: "15px 20px",
                  borderRight: activeTab === "archive" ? "5px solid var(--color-black)" : "",
                  fontWeight: activeTab === "archive" ? "800" : "400",
                  textAlign: "left",
                  color: "var(--color-black)",
                  cursor: "pointer",
                  fontSize: "16px",
                }}
              >
                {t("backoffice.archive")}
              </div>
            </li>
          </ul>
        </nav>
      </div>

      <div
        className="backoffice-content"
        style={{
          flex: 1,
          padding: "30px",
          backgroundColor: "var(--color-bg)",
          overflowY: "auto",
        }}
      >
        <Routes>
          <Route path="/" element={<Overview />} />
          {isAdmin && <Route path="/users" element={<Users />} />}
          {isAdmin && <Route path="/analytics" element={<Analytics />} />}
          <Route path="/reports" element={<Reports />} />
          <Route path="/archive" element={<Archive />} />
        </Routes>
      </div>
    </div>
  );
};

export default BackOffice;
