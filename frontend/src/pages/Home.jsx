import { useNavigate } from "react-router-dom";
import React, { useState, useEffect } from "react";
import api from "../axiosConfig";
import "../ConnectTheDots.css";

export default function Home({ setIsAuthenticated, currentUser }) {
  const navigate = useNavigate();
  const [searchValue, setSearchValue] = useState("");

  const [activeTab, setActiveTab] = useState(() => {
    return localStorage.getItem("activeTab") || "trending";
  });

  const [spaces, setSpaces] = useState([]);
  const [loadingSpaces, setLoadingSpaces] = useState({});
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [spaceToDelete, setSpaceToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");

  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsAuthenticated(false);
    navigate("/login");
  };

  const handleOnCreateSpace = () => {
    navigate("/create-space");
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && searchValue.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchValue.trim())}`);
    }
  };

  const handleSearchButtonClick = () => {
    if (searchValue.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchValue.trim())}`);
    }
  };

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
        err.response?.data?.detail ||
          "Failed to delete space. Please try again."
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
          Trending
        </div>
        <div
          className={`tab ${activeTab === "new" ? "active" : ""}`}
          onClick={() => handleTabChange("new")}
        >
          New
        </div>
      </div>
      {/* Space Cards */}
      <div className="spaces-container">
        {spaces.map((space) => (
          <div
            key={space.id}
            className="space-card"
            style={{ position: "relative" }}
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
                  onClick={() => navigateToSpace(space)}
                >
                  {space.title}
                </h2>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <div className="contributors">
                  Contributors: {space.collaborators?.length || 0}
                </div>
                {canDeleteSpace(space) && (
                  <button
                    className="delete-button"
                    title="Delete"
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
                    Delete
                  </button>
                )}
              </div>
            </div>
            <div className="space-content">
              <p>{space.description}</p>
              <div className="activities">{/* Placeholder for now */}</div>
            </div>
            <div className="space-footer">
              <div className="tags">
                {space.tags.map((tag) => (
                  <div key={tag.id} className="tag">
                    {tag.name}
                  </div>
                ))}
              </div>
              <button
                className={`${
                  space.collaborators?.includes(
                    localStorage.getItem("username")
                  )
                    ? "leave-button"
                    : "join-button"
                }`}
                onClick={(e) => {
                  e.stopPropagation();
                  handleJoinLeaveSpace(
                    space.id,
                    space.collaborators?.includes(
                      localStorage.getItem("username")
                    )
                  );
                }}
                disabled={loadingSpaces[space.id]}
              >
                {loadingSpaces[space.id]
                  ? "Processing..."
                  : space.collaborators?.includes(
                      localStorage.getItem("username")
                    )
                  ? "LEAVE"
                  : "JOIN"}
              </button>
            </div>
          </div>
        ))}
      </div>
      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="modal-backdrop">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Confirm Delete</h3>
            </div>
            <div className="modal-body">
              <p>
                Are you sure you want to delete the space "
                {spaceToDelete?.title}"? This action cannot be undone.
              </p>
              {deleteError && <div style={{ color: "#BD4902" }}>{deleteError}</div>}
            </div>
            <div className="modal-footer">
              <button
                onClick={() => setShowDeleteModal(false)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmDelete}
                style={{ background: "#BD4902", color: "#FFFFFF" }}
                disabled={deleting}
              >
                {deleting ? "Deleting..." : "Delete"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
