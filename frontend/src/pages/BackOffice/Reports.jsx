import React, { useState, useEffect } from "react";
import reportsMockData from "../../data/reportsMock.json";

export default function Reports() {
  const [reports, setReports] = useState([]);
  const [sortField, setSortField] = useState("reportCount");
  const [sortDirection, setSortDirection] = useState("desc");
  const [searchTerm, setSearchTerm] = useState("");
  const [typeFilter, setTypeFilter] = useState("All");

  useEffect(() => {
    const normalizedReports = reportsMockData.map((report) => ({
      ...report,
      itemType: report.itemType === "Discussion" ? "Comment" : report.itemType,
    }));
    setReports(normalizedReports);
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

  const itemTypes = [
    "All",
    ...new Set(reports.map((report) => report.itemType)),
  ];

  const handleDelete = (reportId) => {
    setReports(reports.filter((report) => report.id !== reportId));
  };

  const filteredReports = reports
    .filter(
      (report) =>
        (typeFilter === "All" || report.itemType === typeFilter) &&
        (report.itemName.toLowerCase().includes(searchTerm.toLowerCase()) ||
          report.itemType.toLowerCase().includes(searchTerm.toLowerCase()) ||
          report.itemId.toLowerCase().includes(searchTerm.toLowerCase()))
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

  const getActionButtons = (report) => {
    const buttons = [];

    if (report.itemType === "Comment" || report.itemType === "Node") {
      buttons.push(
        <button
          key="delete"
          onClick={() => handleDelete(report.id)}
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
          Delete
        </button>
      );
    } else if (report.itemType === "Profile" || report.itemType === "Space") {
      buttons.push(
        <button
          key="archive"
          onClick={() => handleDelete(report.id)}
          style={{
            backgroundColor: "var(--color-danger)",
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
      );
    } else {
      buttons.push(
        <button
          key="dismiss"
          onClick={() => handleDelete(report.id)}
          style={{
            backgroundColor: "#e74c3c",
            color: "white",
            border: "none",
            padding: "5px 10px",
            borderRadius: "4px",
            cursor: "pointer",
            fontSize: "12px",
          }}
        >
          Dismiss
        </button>
      );
    }

    return buttons;
  };

  return (
    <div>
      <h2>Reports</h2>
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
            {itemTypes.map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
        </div>
        <div>
          <input
            type="text"
            placeholder="Search reports..."
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
                onClick={() => handleSort("itemType")}
              >
                Type {getSortIndicator("itemType")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("itemName")}
              >
                Item Name {getSortIndicator("itemName")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("itemId")}
              >
                Item ID {getSortIndicator("itemId")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "left",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("dateReported")}
              >
                Date {getSortIndicator("dateReported")}
              </th>
              <th
                style={{
                  padding: "15px",
                  textAlign: "center",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("reportCount")}
              >
                Report Count {getSortIndicator("reportCount")}
              </th>
              <th style={{ padding: "15px", textAlign: "center" }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredReports.map((report, index) => (
              <tr
                key={report.id + "-" + report.itemId}
                style={{
                  borderBottom: "1px solid #f1f1f1",
                  backgroundColor: index % 2 === 0 ? "#fff" : "#f9f9f9",
                }}
              >
                <td style={{ padding: "12px 15px" }}>{report.itemType}</td>
                <td style={{ padding: "12px 15px" }}>{report.itemName}</td>
                <td style={{ padding: "12px 15px" }}>{report.itemId}</td>
                <td style={{ padding: "12px 15px" }}>{report.dateReported}</td>
                <td style={{ padding: "12px 15px", textAlign: "center" }}>
                  <span>{report.reportCount}</span>
                </td>
                <td style={{ padding: "12px 15px", textAlign: "center" }}>
                  {getActionButtons(report)}
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
            Showing {filteredReports.length} of {reports.length} reports
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
              border: "none",
              padding: "5px 10px",
              margin: "0 5px",
              borderRadius: "4px",
              cursor: "pointer",
              color: "black",
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
