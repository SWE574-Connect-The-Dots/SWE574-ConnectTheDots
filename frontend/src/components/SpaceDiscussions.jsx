import { useState, useEffect } from "react";
import api from "../axiosConfig";
import { API_ENDPOINTS } from "../constants/config";

const SpaceDiscussions = ({ spaceId, isCollaborator }) => {
  const [discussions, setDiscussions] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [isDiscussionsOpen, setIsDiscussionsOpen] = useState(true);
  const currentUsername = localStorage.getItem("username");
  const isLoggedIn = !!localStorage.getItem("token");

  useEffect(() => {
    // Initial fetch of discussions
    fetchDiscussions();
  }, [spaceId]);

  const fetchDiscussions = async (showLoading = true) => {
    if (showLoading) {
      setLoading(true);
    }

    try {
      const response = await api.get(API_ENDPOINTS.DISCUSSIONS(spaceId), {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      setDiscussions(response.data);
      setError("");
    } catch (err) {
      console.error("Error fetching discussions:", err);
      setError("Failed to load discussions");
    } finally {
      if (showLoading) {
        setLoading(false);
      }
    }
  };

  const handleSubmitComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    try {
      await api.post(
        API_ENDPOINTS.ADD_DISCUSSION(spaceId),
        { text: newComment },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      // Clear the input field immediately
      setNewComment("");

      // Fetch all discussions to ensure we have the most up-to-date list
      await fetchDiscussions(false);
    } catch (err) {
      console.error("Error adding comment:", err);
      setError("Failed to add comment");
    }
  };

  const handleReact = async (discussionId, value) => {
    try {
      const response = await api.post(
        API_ENDPOINTS.DISCUSSION_REACT(spaceId, discussionId),
        { value }
      );
      const updated = response.data.discussion;
      setDiscussions((prev) =>
        prev.map((d) => (d.id === updated.id ? updated : d))
      );
    } catch (err) {
      console.error("Error reacting to comment:", err);
      setError("Failed to update reaction");
    }
  };

  return (
    <div
      style={{
        border: "1px solid var(--color-gray-300)",
        borderRadius: "4px",
        marginTop: "20px",
        overflow: "hidden",
      }}
    >
      <div
        style={{
          backgroundColor: "var(--color-panel-bg)",
          padding: "10px",
          cursor: "pointer",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
        onClick={() => setIsDiscussionsOpen(!isDiscussionsOpen)}
      >
        <strong>Discussions</strong>
        <span>{isDiscussionsOpen ? "▲" : "▼"}</span>
      </div>

      {isDiscussionsOpen && (
        <div style={{ padding: "10px" }}>
          {/* Comment form - only visible to collaborators */}
          {isCollaborator && (
            <form onSubmit={handleSubmitComment}>
              <textarea
                value={newComment}
                onChange={(e) => setNewComment(e.target.value)}
                placeholder="Add a comment..."
                style={{
                  width: "100%",
                  padding: "8px",
                  borderRadius: "4px",
                  border: "1px solid var(--color-gray-300)",
                  marginBottom: "10px",
                  minHeight: "60px",
                  resize: "vertical",
                }}
              />
              <button
                type="submit"
                style={{
                  backgroundColor: "var(--color-accent)",
                  color: "var(--color-white)",
                  border: "none",
                  padding: "8px 16px",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
                disabled={!newComment.trim()}
              >
                Post Comment
              </button>
            </form>
          )}

          {/* Non-collaborator message */}
          {!isCollaborator && currentUsername && (
            <div
              style={{
                marginBottom: "15px",
                fontStyle: "italic",
                color: "var(--color-text-secondary)",
              }}
            >
              Join as a collaborator to participate in discussions
            </div>
          )}

          {/* Error message */}
          {error && (
            <div style={{ color: "var(--color-danger)", margin: "10px 0" }}>{error}</div>
          )}

          {/* Comments list - visible to everyone */}
          <div style={{ marginTop: "20px" }}>
            {loading ? (
              <p>Loading discussions...</p>
            ) : discussions.length > 0 ? (
              <div
                style={{
                  maxHeight: "750px",
                  overflowY: "auto",
                  borderRadius: "4px",
                  padding: "5px",
                }}
              >
                <ul style={{ listStyleType: "none", padding: 0, margin: 0 }}>
                  {discussions.map((discussion) => (
                    <li
                      key={discussion.id}
                      style={{
                        padding: "12px",
                        marginBottom: "10px",
                        borderRadius: "8px",
                        backgroundColor:
                          discussion.username === currentUsername
                            ? "var(--color-item-own-bg)"
                            : "var(--color-item-bg)",
                        border: "1px solid var(--color-gray-200)",
                        boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
                      }}
                    >
                      <div
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          marginBottom: "5px",
                        }}
                      >
                        <strong>{discussion.username}</strong>
                      </div>
                      <div>
                        <small style={{ color: "var(--color-text-secondary)" }}>
                          {new Date(discussion.created_at).toLocaleString()}
                        </small>
                      </div>
                      <div
                        style={{
                          whiteSpace: "pre-wrap",
                          marginTop: "8px",
                          lineHeight: "1.4",
                        }}
                      >
                        {discussion.text}
                      </div>
                      <div
                        style={{
                          display: "flex",
                          gap: "8px",
                          alignItems: "center",
                          marginTop: "10px",
                        }}
                        aria-label="Comment reactions"
                      >
                        <button
                          type="button"
                          onClick={() => handleReact(discussion.id, "up")}
                          disabled={!isLoggedIn}
                          aria-pressed={discussion.user_reaction === "up"}
                          aria-label="Thumbs up"
                          title={isLoggedIn ? "Thumbs up" : "Login to react"}
                          style={{
                            display: "inline-flex",
                            alignItems: "center",
                            gap: "6px",
                            padding: "6px 10px",
                            borderRadius: "6px",
                            border:
                              discussion.user_reaction === "up"
                                ? "2px solid var(--color-accent)"
                                : "1px solid var(--color-gray-300)",
                            background: "var(--color-white)",
                            color: "var(--color-text)",
                            cursor: isLoggedIn ? "pointer" : "not-allowed",
                            fontWeight:
                              discussion.user_reaction === "up" ? 600 : 500,
                          }}
                        >
                          <span aria-hidden="true">👍</span>
                          <span>{discussion.upvotes ?? 0}</span>
                        </button>
                        <button
                          type="button"
                          onClick={() => handleReact(discussion.id, "down")}
                          disabled={!isLoggedIn}
                          aria-pressed={discussion.user_reaction === "down"}
                          aria-label="Thumbs down"
                          title={isLoggedIn ? "Thumbs down" : "Login to react"}
                          style={{
                            display: "inline-flex",
                            alignItems: "center",
                            gap: "6px",
                            padding: "6px 10px",
                            borderRadius: "6px",
                            border:
                              discussion.user_reaction === "down"
                                ? "2px solid var(--color-accent)"
                                : "1px solid var(--color-gray-300)",
                            background: "var(--color-white)",
                            color: "var(--color-text)",
                            cursor: isLoggedIn ? "pointer" : "not-allowed",
                            fontWeight:
                              discussion.user_reaction === "down" ? 600 : 500,
                          }}
                        >
                          <span aria-hidden="true">👎</span>
                          <span>{discussion.downvotes ?? 0}</span>
                        </button>
                      </div>
                    </li>
                  ))}
                </ul>
              </div>
            ) : (
              <p>No discussions yet</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default SpaceDiscussions;
