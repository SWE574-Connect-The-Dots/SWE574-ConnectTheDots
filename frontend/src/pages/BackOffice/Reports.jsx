import React, { useState, useEffect } from "react";
import api from "../../axiosConfig";
import { API_ENDPOINTS } from "../../constants/config";
import { useTranslation } from "../../contexts/TranslationContext";

export default function Reports() {
  const { t } = useTranslation();
  const [reports, setReports] = useState([]);
  const [sortField, setSortField] = useState("reportCount");
  const [sortDirection, setSortDirection] = useState("desc");
  const [searchTerm, setSearchTerm] = useState("");
  const [typeFilter, setTypeFilter] = useState("All");
  const [loading, setLoading] = useState(false);
  const [backendReasons, setBackendReasons] = useState(null);
  const [backendError, setBackendError] = useState(null);

  const fetchReports = async () => {
    try {
      setLoading(true);
      setBackendError(null);
      const [reasonsRes, reportsRes] = await Promise.all([
        api.get(API_ENDPOINTS.REPORTS_REASONS),
        api.get(API_ENDPOINTS.REPORTS),
      ]);
      setBackendReasons(reasonsRes.data);
      setReports(
        Array.isArray(reportsRes.data)
          ? reportsRes.data
          : reportsRes.data?.results || []
      );
    } catch (e) {
      setBackendError(
        e?.response?.data || e?.message || "Failed to load reports"
      );
      console.error("Back-office reports load error:", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let isMounted = true;
    const loadData = async () => {
      try {
        await fetchReports();
      } catch (e) {
        if (!isMounted) return;
        console.error("Failed to load reports:", e);
      }
    };
    loadData();
    return () => {
      isMounted = false;
    };
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

  const handleDismiss = async (reportId) => {

    try {
      setLoading(true);
      await api.post(`${API_ENDPOINTS.REPORTS}${reportId}/dismiss/`);
      await fetchReports();
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteDiscussion = async (report) => {

    try {
      setLoading(true);
      // endpoint for delete discussion hopefully /spaces/{space_id}/discussions/{discussion_id}/delete/
      await api.delete(`/spaces/${report.space}/discussions/${report.content_id}/delete/`);
      await fetchReports();
    } catch (error) {
      console.error("Error deleting discussion:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleArchive = async (report) => {
    try {
      setLoading(true);
      await api.post(API_ENDPOINTS.ARCHIVE_CREATE, {
        content_type: report.content_type,
        content_id: report.content_id,
        reason: report.reason || "Archived from reports"
      });
      await fetchReports();
    } catch (error) {
      console.error("Error archiving item:", error);
    } finally {
      setLoading(false);
    }
  };

  const getActionButtons = (report) => {
    const buttons = [];

    if (report.content_type === "discussion") {
      buttons.push(
        <>
          <button
            key="dismiss"
            onClick={() => handleDismiss(report.id)}
            disabled={loading}
            style={{
              backgroundColor: "var(--color-danger-light)",
              border: "none",
              padding: "5px 10px",
              borderRadius: "4px",
              fontSize: "12px",
              marginRight: "5px",
              cursor: loading ? "not-allowed" : "pointer",
              opacity: loading ? 0.7 : 1,
            }}
          >
            {t("report.dismiss")}
          </button>
          <button
            key="delete"
            onClick={() => handleDeleteDiscussion(report)}
            disabled={loading}
            style={{
              backgroundColor: "var(--color-danger-dark)",
              border: "none",
              padding: "5px 10px",
              borderRadius: "4px",
              fontSize: "12px",
              cursor: loading ? "not-allowed" : "pointer",
              opacity: loading ? 0.7 : 1,
            }}
          >
            {t("common.delete")}
          </button>
        </>
      );
    } else {
      buttons.push(
        <>
          <button
            key="dismiss"
            onClick={() => handleDismiss(report.id)}
            style={{
              backgroundColor: "var(--color-danger-light)",
              border: "none",
              padding: "5px 10px",
              borderRadius: "4px",
              fontSize: "12px",
              marginRight: "5px",
            }}
          >
            {t("report.dismiss")}

          </button>
          <button
            key="archive"
            onClick={() => handleArchive(report)}
            disabled={loading}
            style={{
              backgroundColor: "var(--color-danger)",
              border: "none",
              padding: "5px 10px",
              borderRadius: "4px",
              fontSize: "12px",
              cursor: loading ? "not-allowed" : "pointer",
              opacity: loading ? 0.7 : 1,
            }}
          >
            {t("report.archive")}
          </button>
        </>
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
                onClick={() => handleSort("reporter")}
              >
                Reporter {getSortIndicator("reporter")}
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
              <th
                style={{
                  padding: "15px",
                  textAlign: "center",
                  cursor: "pointer",
                }}
                onClick={() => handleSort("status")}
              >
                Status {getSortIndicator("status")}
              </th>
              <th style={{ padding: "15px", textAlign: "center" }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {reports &&
              reports.map((report, index) => (
                <tr
                  key={report.id + "-" + report.itemId}
                  style={{
                    borderBottom: "1px solid #f1f1f1",
                    backgroundColor: index % 2 === 0 ? "#fff" : "#f9f9f9",
                  }}
                >
                  <td style={{ padding: "12px 15px" }}>{report.id}</td>
                  <td style={{ padding: "12px 15px" }}>
                    {report.content_type}
                  </td>
                  <td style={{ padding: "12px 15px" }}>
                    {report.reporter_username}
                  </td>
                  <td style={{ padding: "12px 15px" }}>
                    {new Date(report.updated_at).toLocaleDateString()}
                  </td>
                  <td style={{ padding: "12px 15px", textAlign: "center" }}>
                    <span>{report.entity_report_count}</span>
                  </td>
                  <td style={{ padding: "12px 15px", textAlign: "center" }}>
                    <span>{report.status}</span>
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
            Showing {reports.length} of {reports.length} reports
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
