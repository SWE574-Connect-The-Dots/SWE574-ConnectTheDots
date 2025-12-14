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
  const [modalData, setModalData] = useState(null);
  const [sortOrder, setSortOrder] = useState('desc');
  const [dateSortOrder, setDateSortOrder] = useState('desc');
  const [activeSort, setActiveSort] = useState('reportCount');

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
    const discussionId = reportGroup.content_object?.id;
    if (!spaceId || !discussionId) {
        console.error("Missing spaceId or discussionId for deletion", reportGroup);
        return;
    };
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

  const getLabel = (group) => {
    const obj = group.content_object;
    if (obj) {
      if (group.content_type === 'discussion') {
        return (obj.text?.substring(0, 50) || '') + '...';
      }
      return obj.label || obj.title || obj.user?.username || 'N/A';
    }
    return 'N/A';
  }

  const itemTypes = [
    "All",
    ...new Set(reports.map((report) => report.content_type)),
  ];

  const filteredAndSortedReports = reports
    .filter((report) => {
      const typeMatch = typeFilter === "All" || report.content_type === typeFilter;
      
      const searchMatch = searchTerm === "" || 
        report.label?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        report.reporter_username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        report.content_type?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        report.status?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        String(report.id)?.includes(searchTerm);
      
      return typeMatch && searchMatch;
    })
    .sort((a, b) => {
      if (activeSort === 'date') {
        const dateA = new Date(a.created_at);
        const dateB = new Date(b.created_at);
        if (dateSortOrder === 'asc') {
          return dateA - dateB;
        } else {
          return dateB - dateA;
        }
      } else {
        if (sortOrder === 'asc') {
          return a.entity_report_count - b.entity_report_count;
        } else {
          return b.entity_report_count - a.entity_report_count;
        }
      }
    });

  const handleSortToggle = () => {
    setActiveSort('reportCount');
    setSortOrder(sortOrder === 'desc' ? 'asc' : 'desc');
  };

  const handleDateSortToggle = () => {
    setActiveSort('date');
    setDateSortOrder(dateSortOrder === 'desc' ? 'asc' : 'desc');
  };

  const ReportDetailModal = ({ data, onClose }) => {
    if (!data) return null;

    const renderContentDetails = () => {
      const { content_type, content_object } = data;
      if (!content_object) return <p>Content data is not available.</p>;

      switch (content_type) {
        case "discussion":
          return (
            <div>
              <h4>Discussion Text:</h4>
              <p style={{ whiteSpace: "pre-wrap" }}>{content_object.text}</p>
            </div>
          );
        case "space":
          return (
            <div>
              <h4>Space: {content_object.title}</h4>
              <p>{content_object.description}</p>
            </div>
          );
        case "node":
          return (
            <div>
              <h4>Node: {content_object.label}</h4>
              <p>Wikidata ID: {content_object.wikidata_id}</p>
            </div>
          );
        case "profile":
          return (
            <div>
              <h4>Profile: {content_object.user?.username}</h4>
              <p>Bio: {content_object.bio}</p>
            </div>
          );
        default:
          return <p>Unknown content type.</p>;
      }
    };

    return (
      <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
        <div style={{ backgroundColor: 'var(--color-white)', padding: '2rem', borderRadius: '8px', width: '80%', maxWidth: '800px', color: 'var(--color-text)' }}>
          <h3>Report Details</h3>
          {renderContentDetails()}
          <h4 style={{marginTop: '1rem'}}>Individual Reports ({data.reports.length})</h4>
          <table style={{width: '100%', marginTop: '0.5rem'}}>
            <thead>
              <tr>
                <th style={{textAlign: 'left'}}>Reporter</th>
                <th style={{textAlign: 'left'}}>Reason</th>
                <th style={{textAlign: 'left'}}>Date</th>
              </tr>
            </thead>
            <tbody>
              {data.reports.map(report => (
                <tr key={report.id}>
                  <td>{report.reporter_username}</td>
                  <td>{report.reason}</td>
                  <td>{new Date(report.created_at).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <button onClick={onClose} style={{marginTop: '1.5rem'}}>Close</button>
        </div>
      </div>
    );
  };

  return (
    <div>
      <ReportDetailModal data={modalData} onClose={() => setModalData(null)} />
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
              <th style={{ padding: "15px", textAlign: "left", position: "relative" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                  <span>Date</span>
                  <button
                    onClick={handleDateSortToggle}
                    style={{
                      background: "none",
                      border: "none",
                      cursor: "pointer",
                      padding: "4px",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      fontSize: "12px",
                      color: activeSort === 'date' ? "var(--color-accent)" : "var(--color-text-secondary)",
                    }}
                    title={dateSortOrder === 'desc' ? 'Sorted: Newest first (click to reverse)' : 'Sorted: Oldest first (click to reverse)'}
                  >
                    {dateSortOrder === 'desc' ? '▼' : dateSortOrder === 'asc' ? '▲' : '⇅'}
                  </button>
                </div>
              </th>
              <th style={{ padding: "15px", textAlign: "center", position: "relative" }}>
                <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: "8px" }}>
                  <span>Report Count</span>
                  <button
                    onClick={handleSortToggle}
                    style={{
                      background: "none",
                      border: "none",
                      cursor: "pointer",
                      padding: "4px",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      fontSize: "12px",
                      color: activeSort === 'reportCount' ? "var(--color-accent)" : "var(--color-text-secondary)",
                    }}
                    title={sortOrder === 'desc' ? 'Sorted: Highest first (click to reverse)' : 'Sorted: Lowest first (click to reverse)'}
                  >
                    {sortOrder === 'desc' ? '▼' : sortOrder === 'asc' ? '▲' : '⇅'}
                  </button>
                </div>
              </th>
              <th style={{ padding: "15px", textAlign: "center" }}>Status</th>
              <th style={{ padding: "15px", textAlign: "center", width: '200px' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading && filteredAndSortedReports.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ padding: "20px", textAlign: "center" }}>
                  Loading...
                </td>
              </tr>
            ) : filteredAndSortedReports.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ padding: "20px", textAlign: "center" }}>
                  No reports found matching your filters
                </td>
              </tr>
            ) : (
              filteredAndSortedReports.map((report, index) => (
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
                    <div style={{ display: "flex", gap: "5px", justifyContent: 'center' }}>
                      <button
                        onClick={() => setModalData(report._group)}
                        style={{
                          backgroundColor: "var(--color-accent)",
                          border: "none",
                          padding: "5px 10px",
                          borderRadius: "4px",
                          fontSize: "12px",
                          color: "white",
                          cursor: "pointer",
                        }}
                      >
                        See Details
                      </button>
                      {getActionButtons(report._group)}
                    </div>
                  </td>
                </tr>
              ))
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
             Showing {filteredAndSortedReports.length} of {reports.length} reports
           </span>
         </div>
       </div>
    </div>
  );
}
