import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import api from "../../axiosConfig";
import { API_ENDPOINTS } from "../../constants/config";
import { useTranslation } from "../../contexts/TranslationContext";

export default function Users() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [sortField, setSortField] = useState("username");
  const [sortDirection, setSortDirection] = useState("asc");
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get(API_ENDPOINTS.USERS);
      const usersData = response.data.users || [];
      
      const transformedUsers = usersData.map(user => ({
        id: user.id,
        username: user.username,
        email: user.email,
        createdDate: new Date(user.created_at).toLocaleDateString(),
        role: user.user_type_display,
        user_type: user.user_type,
        profession: user.profession || "N/A"
      }));
      
      setUsers(transformedUsers);
    } catch (err) {
      setError(err?.response?.data?.error || err?.message || "Failed to load users");
      console.error("Failed to load users:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const sortedUsers = [...users].sort((a, b) => {
    if (a[sortField] < b[sortField]) {
      return sortDirection === "asc" ? -1 : 1;
    }
    if (a[sortField] > b[sortField]) {
      return sortDirection === "asc" ? 1 : -1;
    }
    return 0;
  });

  const filteredUsers = sortedUsers.filter(
    (user) =>
      user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.role.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.profession.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortDirection("asc");
    }
  };

  const handleArchiveUser = async (userId, username) => {
    if (!window.confirm(`Are you sure you want to archive user "${username}"?`)) {
      return;
    }
    
    try {
      setLoading(true);
      await api.post(API_ENDPOINTS.ARCHIVE_CREATE, {
        content_type: "profile",
        content_id: userId,
        reason: "Archived from users panel"
      });
      
      await fetchUsers();
    } catch (err) {
      console.error("Failed to archive user:", err);
    } finally {
      setLoading(false);
    }
  };

  const getRoleBadgeStyle = (role) => {
    switch (role) {
      case "Admin":
        return {
          backgroundColor: "var(--color-danger)",
          color: "white",
          padding: "3px 8px",
          borderRadius: "12px",
          fontSize: "12px",
          fontWeight: "bold",
        };
      case "Moderator":
        return {
          backgroundColor: "var(--color-warning)",
          color: "white",
          padding: "3px 8px",
          borderRadius: "12px",
          fontSize: "12px",
          fontWeight: "bold",
        };
      default:
        return {
          backgroundColor: "var(--color-accent)",
          color: "white",
          padding: "3px 8px",
          borderRadius: "12px",
          fontSize: "12px",
          fontWeight: "bold",
        };
    }
  };

  const getSortIndicator = (field) => {
    if (sortField === field) {
      return sortDirection === "asc" ? " ▲" : " ▼";
    }
    return "";
  };

  return (
    <div>
      <h2>{t("backoffice.users")}</h2>
      <div
        style={{
          marginBottom: "20px",
          display: "flex",
          justifyContent: "end",
        }}
      >
        <div>
          <input
            type="text"
            placeholder={t("backoffice.searchUsers")}
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

      <div style={{ overflowX: "auto", position: "relative" }}>
        <table
          style={{
            width: "100%",
            borderCollapse: "collapse",
            backgroundColor: "white",
            boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
            position: "relative",
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
                onClick={() => handleSort("username")}
              >
                {t("backoffice.username")} {getSortIndicator("username")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("email")}
              >
                {t("backoffice.email")} {getSortIndicator("email")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("profession")}
              >
                {t("backoffice.profession")} {getSortIndicator("profession")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("createdDate")}
              >
                {t("backoffice.createdDate")} {getSortIndicator("createdDate")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("role")}
              >
                {t("backoffice.role")} {getSortIndicator("role")}
              </th>
              <th style={{ padding: "15px", textAlign: "center" }}>{t("backoffice.actions")}</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.map((user, index) => (
              <tr
                key={user.id}
                style={{
                  borderBottom: "1px solid #f1f1f1",
                  backgroundColor: index % 2 === 0 ? "#fff" : "#f9f9f9",
                }}
              >
                <td style={{ padding: "12px 15px" }}>{user.username}</td>
                <td style={{ padding: "12px 15px" }}>{user.email}</td>
                <td style={{ padding: "12px 15px" }}>{user.profession}</td>
                <td style={{ padding: "12px 15px" }}>{user.createdDate}</td>
                <td style={{ padding: "12px 15px" }}>
                  <span style={getRoleBadgeStyle(user.role)}>
                    {user.role}
                  </span>
                </td>
                <td style={{ padding: "12px 15px", textAlign: "center" }}>
                  <button
                    onClick={() => navigate(`/profile/${user.username}`)}
                    style={{
                      backgroundColor: "var(--color-accent)",
                      color: "white",
                      border: "none",
                      padding: "5px 10px",
                      borderRadius: "4px",
                      marginRight: "5px",
                      cursor: "pointer",
                      fontSize: "12px",
                    }}
                  >
                    {t("backoffice.visitProfile")}
                  </button>
                  {user.role !== "Admin" && (
                    <button
                      onClick={() => handleArchiveUser(user.id, user.username)}
                      disabled={loading}
                      style={{
                        backgroundColor: "var(--color-danger-dark)",
                        color: "white",
                        border: "none",
                        padding: "5px 10px",
                        borderRadius: "4px",
                        cursor: loading ? "not-allowed" : "pointer",
                        fontSize: "12px",
                        opacity: loading ? 0.6 : 1,
                      }}
                    >
                      {t("backoffice.archiveUser")}
                    </button>
                  )}
                </td>
              </tr>
            ))}
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
            {t("backoffice.showing")} {filteredUsers.length} {t("backoffice.of")} {users.length} {t("backoffice.usersCount")}
          </span>
        </div>
      </div>
    </div>
  );
}
