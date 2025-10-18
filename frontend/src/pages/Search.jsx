import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../axiosConfig";
import { API_ENDPOINTS } from "../constants/config";

const Search = () => {
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState({ spaces: [], users: [] });
  const [activeTab, setActiveTab] = useState("spaces");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const query = params.get("q");
    if (query) {
      setSearchQuery(query);
      handleSearch(query);
    }
  }, [location.search]);

  const handleSearch = async (query = searchQuery) => {
    if (!query.trim()) return;

    setLoading(true);
    try {
      const response = await api.get(API_ENDPOINTS.SEARCH, {
        params: { q: query },
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });

      setSearchResults(response.data);

      const url = new URL(window.location);
      url.searchParams.set("q", query);
      window.history.replaceState({}, "", url);
    } catch (error) {
      console.error("Search failed:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter") {
      handleSearch();
    }
  };

  const navigateToSpace = (spaceId) => {
    navigate(`/spaces/${spaceId}`);
  };

  const navigateToUserProfile = (username) => {
    navigate(`/profile/${username}`);
  };

  const isTagMatching = (tagName, query) => {
    return tagName.toLowerCase().includes(query.toLowerCase());
  };

  return (
    <div style={{ padding: "20px", maxWidth: "1000px", margin: "0 auto" }}>
      <h2>Search</h2>

      <div
        style={{ display: "flex", marginBottom: "20px", position: "relative" }}
      >
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Search spaces or users..."
          style={{
            flex: 1,
            padding: "10px",
            fontSize: "16px",
            borderRadius: "4px 0 0 4px",
            border: "1px solid #ccc",
            paddingRight: "30px",
          }}
        />
        {searchQuery && (
          <button
            onClick={() => setSearchQuery("")}
            style={{
              position: "absolute",
              right: "85px",
              top: "50%",
              transform: "translateY(-50%)",
              background: "transparent",
              border: "none",
              fontSize: "18px",
              cursor: "pointer",
              color: "#666",
              zIndex: 10,
            }}
          >
            ×
          </button>
        )}
        <button
          onClick={() => handleSearch()}
          style={{
            padding: "10px 20px",
            backgroundColor: "#0076B5",
            color: "#FFFFFF",
            border: "none",
            borderRadius: "0 4px 4px 0",
            cursor: "pointer",
          }}
        >
          Search
        </button>
      </div>

      {/* Tabs */}
      <div
        style={{
          display: "flex",
          borderBottom: "1px solid #ccc",
          marginBottom: "20px",
        }}
      >
        <button
          onClick={() => setActiveTab("spaces")}
          style={{
            padding: "10px 20px",
            backgroundColor: activeTab === "spaces" ? "#F5F5F5" : "transparent",
            border: "none",
            borderBottom: activeTab === "spaces" ? "2px solid #0076B5" : "none",
            color: activeTab === "spaces" ? "#1B1F3B" : "#4A5568",
            cursor: "pointer",
          }}
        >
          Spaces ({searchResults.spaces.length})
        </button>
        <button
          onClick={() => setActiveTab("users")}
          style={{
            padding: "10px 20px",
            backgroundColor: activeTab === "users" ? "#F5F5F5" : "transparent",
            border: "none",
            borderBottom: activeTab === "users" ? "2px solid #0076B5" : "none",
            color: activeTab === "users" ? "#1B1F3B" : "#4A5568",
            cursor: "pointer",
          }}
        >
          Users ({searchResults.users.length})
        </button>
      </div>

      {loading ? (
        <div>Loading...</div>
      ) : (
        <>
          {/* Spaces Tab */}
          {activeTab === "spaces" && (
            <div>
              {searchResults.spaces.length === 0 ? (
                <p>No spaces found matching your search.</p>
              ) : (
                <div>
                  {searchResults.spaces.map((space) => (
                    <div
                      key={space.id}
                      onClick={() => navigateToSpace(space.id)}
                      style={{
                        padding: "15px",
                        marginBottom: "10px",
                        border: "1px solid #68686B",
                        borderRadius: "4px",
                        cursor: "pointer",
                        transition: "background-color 0.2s",
                        backgroundColor: "#FFFFFF",
                      }}
                      onMouseEnter={(e) =>
                        (e.currentTarget.style.backgroundColor = "#F5F5F5")
                      }
                      onMouseLeave={(e) =>
                        (e.currentTarget.style.backgroundColor = "#FFFFFF")
                      }
                    >
                      <h3 style={{ margin: "0 0 10px 0", color: "#1B1F3B" }}>{space.title}</h3>
                      <p style={{ margin: "0 0 10px 0", color: "#4A5568" }}>
                        {space.description}
                      </p>
                      <div style={{ fontSize: "14px", color: "#4A5568" }}>
                        <span>Created by: {space.creator_username}</span>
                        <span style={{ marginLeft: "15px" }}>
                          Created:{" "}
                          {new Date(space.created_at).toLocaleDateString()}
                        </span>
                      </div>
                      {space.tags.length > 0 && (
                        <div style={{ marginTop: "10px" }}>
                          {space.tags.map((tag) => (
                            <span
                              key={tag.id}
                              style={{
                                display: "inline-block",
                                backgroundColor:
                                  searchQuery &&
                                  isTagMatching(tag.name, searchQuery)
                                    ? "#2D6A4F" // Highlight matching tags with success color
                                    : "#215D69",
                                color: "#FFFFFF",
                                padding: "3px 8px",
                                borderRadius: "12px",
                                fontSize: "12px",
                                marginRight: "5px",
                                marginBottom: "5px",
                              }}
                            >
                              {tag.name}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Users Tab */}
          {activeTab === "users" && (
            <div>
              {searchResults.users.length === 0 ? (
                <p>No users found matching your search.</p>
              ) : (
                <div>
                  {searchResults.users.map((user) => (
                    <div
                      key={user.id}
                      onClick={() => navigateToUserProfile(user.username)}
                      style={{
                        padding: "15px",
                        marginBottom: "10px",
                        border: "1px solid #68686B",
                        borderRadius: "4px",
                        backgroundColor: "#FFFFFF",
                        cursor: "pointer",
                        transition: "background-color 0.2s",
                      }}
                      onMouseEnter={(e) =>
                        (e.currentTarget.style.backgroundColor = "#F5F5F5")
                      }
                      onMouseLeave={(e) =>
                        (e.currentTarget.style.backgroundColor = "#FFFFFF")
                      }
                    >
                      <div
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "center",
                        }}
                      >
                        <h3 style={{ margin: "0 0 5px 0", color: "#1B1F3B" }}>{user.username}</h3>
                        <span
                          style={{
                            backgroundColor: "#0076B5",
                            color: "#FFFFFF",
                            padding: "3px 8px",
                            borderRadius: "4px",
                            fontSize: "12px",
                          }}
                        >
                          View Profile
                        </span>
                      </div>
                      {user.profession && (
                        <p style={{ margin: "5px 0 0 0", color: "#4A5568" }}>
                          Profession: {user.profession}
                        </p>
                      )}
                      {(user.first_name || user.last_name) && (
                        <p style={{ margin: "5px 0 0 0", color: "#4A5568" }}>
                          Name:{" "}
                          {[user.first_name, user.last_name]
                            .filter(Boolean)
                            .join(" ")}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Search;
