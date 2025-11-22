import React, { useState, useEffect, useCallback } from "react";
import api from "../../axiosConfig";
import { API_ENDPOINTS } from "../../constants/config";
import { useTranslation } from "../../contexts/TranslationContext";

export default function Reports() {
  const { t } = useTranslation();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(false);
  const [backendError, setBackendError] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [typeFilter, setTypeFilter] = useState("All");

  const fetchReports = useCallback(async () => {
    try {
      setLoading(true);
      setBackendError(null);
      const reportsRes = await api.get(API_ENDPOINTS.REPORTS_OPEN);
      const groupedData = reportsRes.data.results || reportsRes.data || [];
      
      const processedReports = groupedData.map(group => {
        const firstReport = group.reports[0];
        let label = 'N/A';
        const obj = group.content_object;
        if (obj) {
          if (group.content_type === 'discussion') {
            label = (obj.text?.substring(0, 50) || '') + '...';
          } else {
            label = obj.label || obj.title || obj.user?.username || 'N/A';
          }
        }

        return {
          // Data for the table row
          id: firstReport.id,
          content_type: group.content_type,
          label: label,
          reporter_username: firstReport.reporter_username,
          created_at: firstReport.created_at,
          entity_report_count: group.reports.length,
          status: firstReport.status,
          // Full group data needed for actions
          _group: group,
        };
      });

      setReports(processedReports);
    } catch (e) {
      setBackendError(e?.response?.data || e?.message || "Failed to load reports");
      console.error("Back-office reports load error:", e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchReports();
  }, [fetchReports]);

  const handleDismiss = async (reportGroup) => {
    const reportId = reportGroup.reports[0]?.id;
    if (!reportId) return;
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

  const handleDeleteDiscussion = async (reportGroup) => {
    const spaceId = reportGroup.content_object?.space;
    const discussionId = reportGroup.content_id;
    if (!spaceId || !discussionId) return;
    try {
      setLoading(true);
      await api.delete(`/spaces/${spaceId}/discussions/${discussionId}/delete/`);
      await fetchReports();
    } catch (error) {
      console.error("Error deleting discussion:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleArchive = async (reportGroup) => {
    try {
      setLoading(true);
      await api.post(API_ENDPOINTS.ARCHIVE_CREATE, {
        content_type: reportGroup.content_type,
        content_id: reportGroup.content_id,
        reason: "Archived from reports panel",
      });
      await fetchReports();
    } catch (error) {
      console.error("Error archiving item:", error);
    } finally {
      setLoading(false);
    }
  };

  const getActionButtons = (reportGroup) => {
    if (reportGroup.content_type === "discussion") {
      return (
        <>
          <button
            onClick={() => handleDismiss(reportGroup)}
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
            onClick={() => handleDeleteDiscussion(reportGroup)}
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
      return (
        <>
          <button
            onClick={() => handleDismiss(reportGroup)}
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
            onClick={() => handleArchive(reportGroup)}
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
  };

  const itemTypes = [
    "All",
    ...new Set(reports.map((report) => report.content_type)),
  ];

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
              <th style={{ padding: "15px", textAlign: "left" }}>Report ID</th>
              <th style={{ padding: "15px", textAlign: "left" }}>Type</th>
              <th style={{ padding: "15px", textAlign: "left" }}>Label</th>
              <th style={{ padding: "15px", textAlign: "left" }}>Reporter</th>
              <th style={{ padding: "15px", textAlign: "left" }}>Date</th>
              <th style={{ padding: "15px", textAlign: "center" }}>Report Count</th>
              <th style={{ padding: "15px", textAlign: "center" }}>Status</th>
              <th style={{ padding: "15px", textAlign: "center", width: '150px' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {reports.map((report, index) => (
                <tr
                  key={`${report.content_type}-${report._group.content_id}`}
                  style={{
                    borderBottom: "1px solid #f1f1f1",
                    backgroundColor: index % 2 === 0 ? "#fff" : "#f9f9f9",
                  }}
                >
                  <td style={{ padding: "12px 15px" }}>{report.id}</td>
                  <td style={{ padding: "12px 15px" }}>{report.content_type}</td>
                  <td style={{ padding: "12px 15px" }}>{report.label}</td>
                  <td style={{ padding: "12px 15px" }}>{report.reporter_username}</td>
                  <td style={{ padding: "12px 15px" }}>
                    {new Date(report.created_at).toLocaleDateString()}
                  </td>
                  <td style={{ padding: "12px 15px", textAlign: "center" }}>
                    <span>{report.entity_report_count}</span>
                  </td>
                  <td style={{ padding: "12px 15px", textAlign: "center" }}>
                    <span>{report.status}</span>
                  </td>
                  <td style={{ padding: "12px 15px", textAlign: "center" }}>
                    {getActionButtons(report._group)}
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
