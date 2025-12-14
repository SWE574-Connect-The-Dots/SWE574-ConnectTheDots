import { useNavigate } from "react-router-dom";
import React, { useState, useEffect } from "react";
import { useTranslation } from "../contexts/TranslationContext";
import api from "../axiosConfig";
import "../ConnectTheDots.css";
import ActivityStream from "../components/ActivityStream";

export default function Home({ currentUser }) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState(() => {
    return localStorage.getItem("activeTab") || "trending";
  });

  const [spaces, setSpaces] = useState([]);
  const [loadingSpaces, setLoadingSpaces] = useState({});
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [spaceToDelete, setSpaceToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");

  useEffect(() => {
    localStorage.setItem("activeTab", activeTab);
  }, [activeTab]);

  useEffect(() => {
    fetchSpaces();
  }, [activeTab]);

  const fetchSpaces = () => {
    api
      .get(`/spaces/${activeTab}/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      })
      .then((res) => {
        setSpaces(res.data);
      })
      .catch((err) => console.error(err));
  };

  const handleJoinLeaveSpace = async (spaceId, isCollaborator) => {
    setLoadingSpaces((prev) => ({ ...prev, [spaceId]: true }));
    try {
      const endpoint = isCollaborator ? "leave" : "join";
      await api.post(
        `/spaces/${spaceId}/${endpoint}/`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      const response = await api.get(`/spaces/${spaceId}/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      setSpaces((prevSpaces) =>
        prevSpaces.map((space) => {
          if (space.id === spaceId) {
            return {
              ...space,
              ...response.data,
            };
          }
          return space;
        })
      );
    } catch (error) {
      console.error("Error joining/leaving space:", error);
    } finally {
      setLoadingSpaces((prev) => ({ ...prev, [spaceId]: false }));
    }
  };

  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };

  const navigateToSpace = (space) => {
    navigate(`/spaces/${space.id}`, {
      state: {
        ...space,
      },
    });
  };

  const canDeleteSpace = (space) => {
    if (!currentUser) return false;
    return (
      space.creator_username === currentUser.username ||
      currentUser.is_staff ||
      currentUser.is_superuser
    );
  };

  const handleDeleteClick = (space) => {
    setSpaceToDelete(space);
    setShowDeleteModal(true);
    setDeleteError("");
  };

  const handleConfirmDelete = async () => {
    if (!spaceToDelete) return;
    setDeleting(true);
    setDeleteError("");
    try {
      await api.delete(`/spaces/${spaceToDelete.id}/`);
      setSpaces((prev) => prev.filter((s) => s.id !== spaceToDelete.id));
      setShowDeleteModal(false);
      setSpaceToDelete(null);
    } catch (err) {
      setDeleteError(
        err.response?.data?.detail || t("errors.failedToDeleteSpace")
      );
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="connect-dots-container">
      {/* Tabs */}
      <div className="tabs-container">
        <div
          className={`tab ${activeTab === "trending" ? "active" : ""}`}
          onClick={() => handleTabChange("trending")}
        >
          {t("space.trending")}
        </div>
        <div
          className={`tab ${activeTab === "new" ? "active" : ""}`}
          onClick={() => handleTabChange("new")}
        >
          {t("space.new")}
        </div>
      </div>
      <div style={{ 
        display: "flex", 
        gap: "20px", 
        padding: "20px",
        maxWidth: "100%",
        boxSizing: "border-box"
      }}>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div className="spaces-container">
        {spaces.map((space) => (
          <div
            key={space.id}
            className="space-card"
            style={{ position: "relative" }}
            onClick={() => navigateToSpace(space)}
          >
            <div
              className="space-header"
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <h2
                  className="space-title"
                  style={{ cursor: "pointer", margin: 0 }}
                >
                  {space.title}
                </h2>
                {space.is_archived && (
                  <span
                    style={{
                      backgroundColor: "var(--color-gray-400)",
                      color: "var(--color-white)",
                      padding: "2px 8px",
                      borderRadius: "4px",
                      fontSize: "12px",
                      fontWeight: "600",
                    }}
                  >
                    {t("space.archived")}
                  </span>
                )}
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <div className="contributors">
                  {t("space.contributors")}: {space.collaborators?.length || 0}
                </div>
                {canDeleteSpace(space) && (
                  <button
                    className="delete-button"
                    title={t("common.delete")}
                    style={{
                      background: "#e53935",
                      color: "white",
                      border: "none",
                      borderRadius: 4,
                      fontWeight: 600,
                      padding: "6px 16px",
                      marginLeft: 8,
                      cursor: "pointer",
                      transition: "background 0.2s",
                    }}
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteClick(space);
                    }}
                    disabled={deleting && spaceToDelete?.id === space.id}
                    onMouseOver={(e) =>
                      (e.currentTarget.style.background = "#b71c1c")
                    }
                    onMouseOut={(e) =>
                      (e.currentTarget.style.background = "#e53935")
                    }
                  >
                    {t("common.delete")}
                  </button>
                )}
              </div>
            </div>
            <div className="space-content">
              <p>{space.description}</p>
              <div className="activities" />
            </div>
            <div className="space-footer" style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              gap: "16px",
              minHeight: "40px"
            }}>
              <div className="tags" style={{
                display: "flex",
                flexWrap: "nowrap",
                gap: "8px",
                flex: 1,
                minWidth: 0,
                overflowX: "auto",
                overflowY: "hidden",
                scrollbarWidth: "thin",
                scrollbarColor: "#ccc transparent"
              }}>
                {space.tags.map((tag) => (
                  <div key={tag.id} className="tag" style={{
                    flexShrink: 0
                  }}>
                    {tag.name}
                  </div>
                ))}
              </div>
              <button
                className={`${
                  space.collaborators?.includes(localStorage.getItem("username"))
                    ? "leave-button"
                    : "join-button"
                }`}
                onClick={(e) => {
                  e.stopPropagation();
                  if (!space.is_archived) {
                    handleJoinLeaveSpace(
                      space.id,
                      space.collaborators?.includes(
                        localStorage.getItem("username")
                      )
                    );
                  }
                }}
                disabled={loadingSpaces[space.id] || space.is_archived}
                style={{
                  flexShrink: 0,
                  ...(space.is_archived ? { opacity: 0.5, cursor: "not-allowed" } : {})
                }}
              >
                {loadingSpaces[space.id]
                  ? t("common.processing")
                  : space.collaborators?.includes(
                      localStorage.getItem("username")
                    )
                  ? t("space.leave")
                  : t("space.join")}
              </button>
            </div>
          </div>
        ))}
          </div>
        </div>
        {/* Activity Stream Sidebar */}
        <div style={{ 
          flexShrink: 0,
          display: "flex",
          flexDirection: "column",
          gap: "16px"
        }}>
          <ActivityStream />
        </div>
      </div>
      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="modal-backdrop">
          <div className="modal-content">
            <div className="modal-header">
              <h3>{t("space.confirmDelete")}</h3>
            </div>
            <div className="modal-body">
              <p>
                {t("space.confirmDeleteMessage", {
                  title: spaceToDelete?.title,
                })}
              </p>
              {deleteError && (
                <div style={{ color: "#BD4902" }}>{deleteError}</div>
              )}
            </div>
            <div className="modal-footer">
              <button
                onClick={() => setShowDeleteModal(false)}
                disabled={deleting}
              >
                {t("common.cancel")}
              </button>
              <button
                onClick={handleConfirmDelete}
                style={{ background: "#BD4902", color: "#FFFFFF" }}
                disabled={deleting}
              >
                {deleting ? t("space.deleting") : t("common.delete")}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
