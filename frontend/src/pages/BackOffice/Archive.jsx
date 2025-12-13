import React, { useState, useEffect } from "react";
import api from "../../axiosConfig";
import { API_ENDPOINTS } from "../../constants/config";
import { useTranslation } from "../../contexts/TranslationContext";

export default function Archive() {
  const [archivedItems, setArchivedItems] = useState([]);
  const [sortField, setSortField] = useState("archived_at");
  const [sortDirection, setSortDirection] = useState("desc");
  const [searchTerm, setSearchTerm] = useState("");
  const [typeFilter, setTypeFilter] = useState("All");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const { t } = useTranslation();
  

  const fetchArchivedItems = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get(API_ENDPOINTS.ARCHIVE);
      setArchivedItems(response.data || []);
    } catch (err) {
      setError(err?.response?.data?.error || err?.message || "Failed to load archived items");
      console.error("Failed to load archived items:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchArchivedItems();
  }, []);

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortDirection("desc");
    }
  };

  const getSortIndicator = (field) => {
    if (sortField === field) {
      return sortDirection === "asc" ? " ▲" : " ▼";
    }
    return "";
  };

  const getTypeBadgeStyle = (type) => {
    const normalizedType = type.charAt(0).toUpperCase() + type.slice(1).toLowerCase();
    switch (normalizedType) {
      case "Space":
        return {
          backgroundColor: "var(--color-accent)",
          color: "white",
          padding: "3px 8px",
          borderRadius: "12px",
          fontSize: "12px",
          fontWeight: "bold",
        };
      case "Profile":
        return {
          backgroundColor: "var(--color-purple-selected)",
          color: "white",
          padding: "3px 8px",
          borderRadius: "12px",
          fontSize: "12px",
          fontWeight: "bold",
        };
      case "Node":
        return {
          backgroundColor: "var(--color-purple)",
          color: "white",
          padding: "3px 8px",
          borderRadius: "12px",
          fontSize: "12px",
          fontWeight: "bold",
        };
      default:
        return {
          backgroundColor: "#95a5a6",
          color: "white",
          padding: "3px 8px",
          borderRadius: "12px",
          fontSize: "12px",
          fontWeight: "bold",
        };
    }
  };

  const filteredItems = archivedItems
    .filter(
      (item) => {
        const normalizedType = item.content_type.charAt(0).toUpperCase() + item.content_type.slice(1).toLowerCase();
        return (
          (typeFilter === "All" || normalizedType === typeFilter) &&
          (item.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            item.archived_by_username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            item.reason?.toLowerCase().includes(searchTerm.toLowerCase()))
        );
      }
    )
    .sort((a, b) => {
      const aVal = a[sortField] || "";
      const bVal = b[sortField] || "";
      if (aVal < bVal) {
        return sortDirection === "asc" ? -1 : 1;
      }
      if (aVal > bVal) {
        return sortDirection === "asc" ? 1 : -1;
      }
      return 0;
    });

  const handleRestore = async (id) => {
    try {
      setLoading(true);
      await api.post(API_ENDPOINTS.ARCHIVE_RESTORE(id));
      await fetchArchivedItems();
    } catch (err) {
      console.error("Failed to restore item:", err);
      setError(err?.response?.data?.error || err?.message || "Failed to restore item");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2>Archive</h2>
      {error && (
        <div
          style={{
            backgroundColor: "#fee",
            color: "#c00",
            padding: "10px",
            borderRadius: "4px",
            marginBottom: "20px",
          }}
        >
          {error}
        </div>
      )}
      <div
        style={{
          marginBottom: "20px",
          display: "flex",
          justifyContent: "space-between",
        }}
      >
        <div>
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            style={{
              padding: "8px 12px",
              border: "1px solid #ddd",
              borderRadius: "4px",
              marginRight: "10px",
            }}
          >
            <option value="All">All Types</option>
            <option value="Space">Spaces</option>
            <option value="Node">Nodes</option>
            <option value="Profile">Profiles</option>
          </select>
        </div>
        <div>
          <input
            type="text"
            placeholder="Search archive..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              padding: "8px 12px",
              border: "1px solid #ddd",
              borderRadius: "4px",
              width: "300px",
            }}
          />
        </div>
      </div>

      <div style={{ overflowX: "auto" }}>
        <table
          style={{
            width: "100%",
            borderCollapse: "collapse",
            backgroundColor: "white",
            boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
          }}
        >
          <thead>
            <tr style={{ borderBottom: "2px solid #f1f1f1" }}>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("content_type")}
              >
                Type {getSortIndicator("content_type")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("name")}
              >
                Name {getSortIndicator("name")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("archived_by_username")}
              >
                Archived By {getSortIndicator("archived_by_username")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("archived_at")}
              >
                Date Archived {getSortIndicator("archived_at")}
              </th>
              <th style={{ padding: "15px", textAlign: "center" }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading && filteredItems.length === 0 ? (
              <tr>
                <td colSpan="5" style={{ padding: "20px", textAlign: "center" }}>
                  Loading...
                </td>
              </tr>
            ) : filteredItems.length === 0 ? (
              <tr>
                <td colSpan="5" style={{ padding: "20px", textAlign: "center" }}>
                  No archived items found
                </td>
              </tr>
            ) : (
              filteredItems.map((item, index) => {
                const normalizedType = item.content_type.charAt(0).toUpperCase() + item.content_type.slice(1).toLowerCase();
                return (
                  <tr
                    key={item.id}
                    style={{
                      borderBottom: "1px solid #f1f1f1",
                      backgroundColor: index % 2 === 0 ? "#fff" : "#f9f9f9",
                    }}
                  >
                    <td style={{ padding: "12px 15px" }}>
                      <span style={getTypeBadgeStyle(item.content_type)}>{normalizedType}</span>
                    </td>
                    <td style={{ padding: "12px 15px" }}>{item.name || "N/A"}</td>
                    <td style={{ padding: "12px 15px" }}>{item.archived_by_username || "Unknown"}</td>
                    <td style={{ padding: "12px 15px" }}>
                      {new Date(item.archived_at).toLocaleDateString()}
                    </td>
                    <td style={{ padding: "12px 15px", textAlign: "center" }}>
                      <button
                        onClick={() => handleRestore(item.id)}
                        disabled={loading}
                        style={{
                          backgroundColor: "var(--color-success)",
                          color: "white",
                          border: "none",
                          padding: "5px 10px",
                          borderRadius: "4px",
                          cursor: loading ? "not-allowed" : "pointer",
                          opacity: loading ? 0.7 : 1,
                          fontSize: "12px",
                        }}
                      >
                        {t("backoffice.restore")}
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      <div
        style={{
          marginTop: "20px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div>
          <span>
            Showing {archivedItems.length} archived items
          </span>
        </div>
      </div>
    </div>
  );
}
