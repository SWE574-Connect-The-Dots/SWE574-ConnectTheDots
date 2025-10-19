import React, { useState, useEffect } from "react";
import ArchivedItems from "../../data/archived.json";

export default function Archive() {
  const [archivedItems, setArchivedItems] = useState([]);
  const [sortField, setSortField] = useState("archivedDate");
  const [sortDirection, setSortDirection] = useState("desc");
  const [searchTerm, setSearchTerm] = useState("");
  const [typeFilter, setTypeFilter] = useState("All");

  useEffect(() => {
    setArchivedItems(ArchivedItems);
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
    switch (type) {
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
      (item) =>
        (typeFilter === "All" || item.type === typeFilter) &&
        (item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          item.archivedBy.toLowerCase().includes(searchTerm.toLowerCase()) ||
          item.reason.toLowerCase().includes(searchTerm.toLowerCase()))
    )
    .sort((a, b) => {
      if (a[sortField] < b[sortField]) {
        return sortDirection === "asc" ? -1 : 1;
      }
      if (a[sortField] > b[sortField]) {
        return sortDirection === "asc" ? 1 : -1;
      }
      return 0;
    });

  const handleRestore = (id) => {
    console.log(`Restoring item with ID: ${id}`);
    setArchivedItems(archivedItems.filter((item) => item.id !== id));
  };

  return (
    <div>
      <h2>Archive</h2>
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
                onClick={() => handleSort("type")}
              >
                Type {getSortIndicator("type")}
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
                onClick={() => handleSort("archivedBy")}
              >
                Archived By {getSortIndicator("archivedBy")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("reason")}
              >
                Reason {getSortIndicator("reason")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("archivedDate")}
              >
                Date Archived {getSortIndicator("archivedDate")}
              </th>
              <th style={{ padding: "15px", textAlign: "center" }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredItems.map((item, index) => (
              <tr
                key={item.id}
                style={{
                  borderBottom: "1px solid #f1f1f1",
                  backgroundColor: index % 2 === 0 ? "#fff" : "#f9f9f9",
                }}
              >
                <td style={{ padding: "12px 15px" }}>
                  <span style={getTypeBadgeStyle(item.type)}>{item.type}</span>
                </td>
                <td style={{ padding: "12px 15px" }}>{item.name}</td>
                <td style={{ padding: "12px 15px" }}>{item.archivedBy}</td>
                <td style={{ padding: "12px 15px" }}>{item.reason}</td>
                <td style={{ padding: "12px 15px" }}>{item.archivedDate}</td>
                <td style={{ padding: "12px 15px", textAlign: "center" }}>
                  <button
                    onClick={() => handleRestore(item.id)}
                    style={{
                      backgroundColor: "var(--color-success)",
                      color: "white",
                      border: "none",
                      padding: "5px 10px",
                      borderRadius: "4px",
                      cursor: "pointer",
                      fontSize: "12px",
                    }}
                  >
                    Restore
                  </button>
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
            Showing {filteredItems.length} of {archivedItems.length} archived
            items
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
