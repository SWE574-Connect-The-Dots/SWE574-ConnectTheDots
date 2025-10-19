import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import userMockData from "../../data/userListMock.json";

export default function Users() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [sortField, setSortField] = useState("username");
  const [sortDirection, setSortDirection] = useState("asc");
  const [searchTerm, setSearchTerm] = useState("");
  const [isAdmin, setIsAdmin] = useState(true);
  const [editingUserId, setEditingUserId] = useState(null);

  const availableRoles = ["User", "Moderator", "Admin"];

  useEffect(() => {
    function handleClickOutside(event) {
      if (editingUserId && !event.target.closest(".role-dropdown-container")) {
        setEditingUserId(null);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [editingUserId]);

  useEffect(() => {
    setUsers(userMockData);
    const isAdminUser = localStorage.getItem("is_superuser") === "true";
    setIsAdmin(isAdminUser || true);
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
      user.role.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleSort = (field) => {
    if (sortField === field) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    } else {
      setSortField(field);
      setSortDirection("asc");
    }
  };

  const handleRoleChange = (username, newRole) => {
    setUsers(
      users.map((user) =>
        user.username === username ? { ...user, role: newRole } : user
      )
    );
    setEditingUserId(null);
  };

  const toggleRoleEdit = (username) => {
    if (isAdmin) {
      setEditingUserId(editingUserId === username ? null : username);
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
      <h2>Users</h2>
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
            placeholder="Search users..."
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
                Username {getSortIndicator("username")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("email")}
              >
                Email {getSortIndicator("email")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("createdDate")}
              >
                Created Date {getSortIndicator("createdDate")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("role")}
              >
                Role {getSortIndicator("role")}
              </th>
              <th style={{ padding: "15px", textAlign: "center" }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.map((user, index) => (
              <tr
                key={user.username}
                style={{
                  borderBottom: "1px solid #f1f1f1",
                  backgroundColor: index % 2 === 0 ? "#fff" : "#f9f9f9",
                }}
              >
                <td style={{ padding: "12px 15px" }}>{user.username}</td>
                <td style={{ padding: "12px 15px" }}>{user.email}</td>
                <td style={{ padding: "12px 15px" }}>{user.createdDate}</td>
                <td style={{ padding: "12px 15px" }}>
                  {isAdmin && user.role !== "Admin" ? (
                    <div
                      className="role-dropdown-container"
                      style={{ display: "inline-block", position: "relative" }}
                    >
                      <span
                        onClick={() => toggleRoleEdit(user.username)}
                        style={{
                          ...getRoleBadgeStyle(user.role),
                          cursor: "pointer",
                          display: "inline-block",
                        }}
                        title="Click to change role"
                      >
                        {user.role} ▼
                      </span>
                      {editingUserId === user.username && (
                        <div
                          style={{
                            position: "absolute",
                            bottom:
                              index >= filteredUsers.length - 3
                                ? "100%"
                                : "auto",
                            top:
                              index >= filteredUsers.length - 3
                                ? "auto"
                                : "100%",
                            left: 0,
                            zIndex: 999,
                            backgroundColor: "white",
                            border: "1px solid #ddd",
                            borderRadius: "4px",
                            boxShadow: "0 2px 5px rgba(0,0,0,0.2)",
                            minWidth: "120px",
                          }}
                        >
                          {availableRoles
                            .filter((role) => role !== "Admin")
                            .map((role) => (
                              <div
                                key={role}
                                onClick={() =>
                                  handleRoleChange(user.username, role)
                                }
                                style={{
                                  padding: "8px 12px",
                                  cursor: "pointer",
                                  borderBottom: "1px solid #eee",
                                  backgroundColor:
                                    role === user.role
                                      ? "#f1f1f1"
                                      : "transparent",
                                  fontWeight:
                                    role === user.role ? "bold" : "normal",
                                  zIndex: 1,
                                }}
                              >
                                {role}
                              </div>
                            ))}
                        </div>
                      )}
                    </div>
                  ) : (
                    <span style={getRoleBadgeStyle(user.role)}>
                      {user.role}
                    </span>
                  )}
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
                    Visit Profile
                  </button>
                  {user.role !== "Admin" ? (
                    <button
                      style={{
                        backgroundColor: "var(--color-danger-dark)",
                        color: "white",
                        border: "none",
                        padding: "5px 10px",
                        borderRadius: "4px",
                        cursor: "pointer",
                        fontSize: "12px",
                      }}
                    >
                      Archive
                    </button>
                  ) : (
                    <></>
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
            Showing {filteredUsers.length} of {users.length} users
          </span>
        </div>
        <div>
          <button
            style={{
              border: "1px solid #ddd",
              padding: "5px 10px",
              margin: "0 5px",
              borderRadius: "4px",
              cursor: "pointer",
              color: "black",
            }}
            disabled
          >
            Previous
          </button>
          <button
            style={{
              color: "black",
              border: "none",
              padding: "5px 10px",
              margin: "0 5px",
              borderRadius: "4px",
              cursor: "pointer",
            }}
          >
            1
          </button>
          <button
            style={{
              border: "1px solid #ddd",
              padding: "5px 10px",
              margin: "0 5px",
              borderRadius: "4px",
              cursor: "pointer",
              color: "black",
            }}
            disabled
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
