import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import api from "../../axiosConfig";

// Add spinner animation styles
const spinnerStyles = `
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
`;

export default function BackOffice() {
  const navigate = useNavigate();
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalSpaces: 0,
    totalGraphNodes: 0,
    activeDiscussions: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [hasAccess, setHasAccess] = useState(true);

  // Fetch dashboard statistics from API
  useEffect(() => {
    const fetchDashboardStats = async () => {
      try {
        setLoading(true);
        const response = await api.get('/dashboard/stats/');
        setStats(response.data);
        setError(null);
      } catch (err) {
        console.error('Error fetching dashboard stats:', err);
        console.error('Error response:', err.response?.data);
        console.error('Error status:', err.response?.status);
        if (err.response?.status === 403) {
          setError(null);
          setHasAccess(false);
        } else {
          let errorMessage = 'Failed to load dashboard statistics';
          if (err.response?.status === 404) {
            errorMessage = 'Dashboard endpoint not found';
          } else if (err.response?.data?.error) {
            errorMessage = err.response.data.error;
          }
          setError(errorMessage);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

  const [topSpaces, setTopSpaces] = useState([]);
  const [topSpacesLoading, setTopSpacesLoading] = useState(true);

  useEffect(() => {
    const fetchTopSpaces = async () => {
      try {
        const token = localStorage.getItem("token");
        const headers = {
          Authorization: `Bearer ${token}`,
        };

        // Fetch top contributed spaces from backend
        const response = await api.get("/spaces/top-scored/?limit=10", { headers });
        setTopSpaces(response.data);
      } catch (error) {
        console.error("Error fetching top spaces:", error);
        setTopSpaces([]);
      } finally {
        setTopSpacesLoading(false);
      }
    };

    fetchTopSpaces();
  }, []);

  return (
    <div>
      <style>{spinnerStyles}</style>
      <h2>Dashboard</h2>
      <p>Welcome to the Connect The Dots admin dashboard.</p>

      {hasAccess && (
        <div style={{ marginTop: "30px" }}>
          <h3>Platform Overview</h3>
          
          {error && (
            <div style={{
              backgroundColor: "#fee2e2",
              border: "1px solid #fca5a5",
              color: "var(--color-danger-light)",
              padding: "10px",
              borderRadius: "6px",
              marginBottom: "20px"
            }}>
              {error}
            </div>
          )}

          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
              gap: "20px",
              marginTop: "20px",
            }}
          >
          <div
            style={{
              backgroundColor: "var(--color-white)",
              padding: "20px",
              borderRadius: "8px",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            <h3 style={{ margin: "0 0 10px 0", color: "var(--color-accent)" }}>
              Total Users
            </h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", margin: "0" }}>
              {loading ? "..." : stats.totalUsers}
            </p>
          </div>

          <div
            style={{
              backgroundColor: "var(--color-white)",
              padding: "20px",
              borderRadius: "8px",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            <h3 style={{ margin: "0 0 10px 0", color: "var(--color-success)" }}>
              Total Spaces
            </h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", margin: "0" }}>
              {loading ? "..." : stats.totalSpaces}
            </p>
          </div>

          <div
            style={{
              backgroundColor: "var(--color-white)",
              padding: "20px",
              borderRadius: "8px",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            <h3 style={{ margin: "0 0 10px 0", color: "var(--color-danger)" }}>
              Graph Nodes
            </h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", margin: "0" }}>
              {loading ? "..." : stats.totalGraphNodes}
            </p>
          </div>

          <div
            style={{
              backgroundColor: "var(--color-white)",
              padding: "20px",
              borderRadius: "8px",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            <h3 style={{ margin: "0 0 10px 0", color: "var(--color-warning)" }}>
              Active Discussions
            </h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", margin: "0" }}>
              {loading ? "..." : stats.activeDiscussions}
            </p>
          </div>
        </div>
        </div>
      )}

      <div style={{ marginTop: "50px" }}>
        <h3>Top Contributed Spaces</h3>

        
        {topSpacesLoading ? (
          <p style={{ color: "var(--color-text-secondary)" }}>Loading top spaces...</p>
        ) : topSpaces.length === 0 ? (
          <p style={{ color: "var(--color-text-secondary)" }}>No spaces found.</p>
        ) : (
          <div
            style={{
              display: "flex",
              gap: "20px",
              marginTop: "20px",
              overflowX: "auto",
              paddingBottom: "10px"
            }}
          >
            {topSpaces.map((space, index) => (
              <div
                key={space.id}
                onClick={() => navigate(`/spaces/${space.id}`)}
                style={{
                  backgroundColor: "var(--color-white)",
                  padding: "20px",
                  borderRadius: "8px",
                  boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                  cursor: "pointer",
                  transition: "transform 0.2s, box-shadow 0.2s",
                  display: "flex",
                  flexDirection: "column",
                  gap: "15px",
                  width: "280px",
                  minWidth: "280px",
                  maxWidth: "280px",
                  flexShrink: 0,
                  position: "relative"
                }}
                onMouseOver={(e) => {
                  e.currentTarget.style.transform = "translateY(-5px)";
                  e.currentTarget.style.boxShadow = "0 6px 12px rgba(0,0,0,0.15)";
                }}
                onMouseOut={(e) => {
                  e.currentTarget.style.transform = "translateY(0)";
                  e.currentTarget.style.boxShadow = "0 2px 4px rgba(0,0,0,0.1)";
                }}
              >
                {/* Ranking Badge */}
                <div style={{
                  position: "absolute",
                  top: "15px",
                  right: "15px",
                  backgroundColor: index === 0 ? "var(--color-warning)" : index === 1 ? "var(--color-border-2)" : index === 2 ? "var(--color-danger)" : "var(--color-accent)",
                  color: "var(--color-white)",
                  width: "28px",
                  height: "28px",
                  borderRadius: "50%",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "12px",
                  fontWeight: "bold",
                  boxShadow: "0 2px 4px rgba(0,0,0,0.3)",
                  zIndex: 10
                }}>
                  #{index + 1}
                </div>

                <h4 style={{ 
                  margin: "0", 
                  color: "var(--color-text)",
                  fontSize: "18px",
                  fontWeight: "600",
                  paddingRight: "45px",
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                  whiteSpace: "nowrap"
                }}>
                  {space.title}
                </h4>
                
                <p style={{
                  margin: "0",
                  fontSize: "14px",
                  color: "var(--color-gray-400)",
                  lineHeight: "1.4",
                  height: "40px",
                  overflow: "hidden",
                  textOverflow: "ellipsis",
                  display: "-webkit-box",
                  WebkitLineClamp: 2,
                  WebkitBoxOrient: "vertical"
                }}>
                  {space.description || "No description available"}
                </p>
                
                <div style={{ 
                  display: "grid", 
                  gridTemplateColumns: "1fr 1fr",
                  gap: "15px",
                  marginBottom: "15px"
                }}>
                  <div style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "8px"
                  }}>
                    <span style={{ 
                      fontWeight: "bold", 
                      color: "var(--color-success)",
                      fontSize: "16px"
                    }}>
                      {space.collaborator_count || 0}
                    </span>
                    <span style={{ 
                      color: "var(--color-text-secondary)",
                      fontSize: "13px"
                    }}>
                      collaborators
                    </span>
                  </div>
                  
                  <div style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "8px"
                  }}>
                    <span style={{ 
                      fontWeight: "bold", 
                      color: "var(--color-purple)",
                      fontSize: "16px"
                    }}>
                      {space.node_count || 0}
                    </span>
                    <span style={{ 
                      color: "var(--color-text-secondary)",
                      fontSize: "13px"
                    }}>
                      nodes
                    </span>
                  </div>

                  <div style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "8px"
                  }}>
                    <span style={{ 
                      fontWeight: "bold", 
                      color: "var(--color-border-1)",
                      fontSize: "16px"
                    }}>
                      {space.edge_count || 0}
                    </span>
                    <span style={{ 
                      color: "var(--color-text-secondary)",
                      fontSize: "13px"
                    }}>
                      edges
                    </span>
                  </div>

                  <div style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "8px"
                  }}>
                    <span style={{ 
                      fontWeight: "bold", 
                      color: "var(--color-teal-dark)",
                      fontSize: "16px"
                    }}>
                      {space.discussion_count || 0}
                    </span>
                    <span style={{ 
                      color: "var(--color-text-secondary)",
                      fontSize: "13px"
                    }}>
                      discussions
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
