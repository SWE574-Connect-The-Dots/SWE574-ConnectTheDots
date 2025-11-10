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
  const stats = {
    totalUsers: 156,
    totalSpaces: 87,
    totalGraphNodes: 435,
    activeDiscussions: 23,
  };

  const [topSpaces, setTopSpaces] = useState([]);
  const [loading, setLoading] = useState(true);

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
        setLoading(false);
      }
    };

    fetchTopSpaces();
  }, []);

  return (
    <div>
      <style>{spinnerStyles}</style>
      <h2>Dashboard</h2>
      <p>Welcome to the Connect The Dots admin dashboard.</p>

      <div style={{ marginTop: "30px" }}>
        <h3>Platform Overview</h3>

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
              backgroundColor: "white",
              padding: "20px",
              borderRadius: "8px",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            <h3 style={{ margin: "0 0 10px 0", color: "var(--color-accent)" }}>
              Total Users
            </h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", margin: "0" }}>
              {stats.totalUsers}
            </p>
          </div>

          <div
            style={{
              backgroundColor: "white",
              padding: "20px",
              borderRadius: "8px",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            <h3 style={{ margin: "0 0 10px 0", color: "var(--color-success)" }}>
              Total Spaces
            </h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", margin: "0" }}>
              {stats.totalSpaces}
            </p>
          </div>

          <div
            style={{
              backgroundColor: "white",
              padding: "20px",
              borderRadius: "8px",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            <h3 style={{ margin: "0 0 10px 0", color: "var(--color-danger)" }}>
              Graph Nodes
            </h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", margin: "0" }}>
              {stats.totalGraphNodes}
            </p>
          </div>

          <div
            style={{
              backgroundColor: "white",
              padding: "20px",
              borderRadius: "8px",
              boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
            }}
          >
            <h3 style={{ margin: "0 0 10px 0", color: "var(--color-warning)" }}>
              Active Discussions
            </h3>
            <p style={{ fontSize: "28px", fontWeight: "bold", margin: "0" }}>
              {stats.activeDiscussions}
            </p>
          </div>
        </div>
      </div>

      <div style={{ marginTop: "50px" }}>
        <h3>Top Contributed Spaces</h3>

        
        {loading ? (
          <p style={{ color: "#666" }}>Loading top spaces...</p>
        ) : topSpaces.length === 0 ? (
          <p style={{ color: "#666" }}>No spaces found.</p>
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
                  backgroundColor: "white",
                  padding: "20px",
                  borderRadius: "8px",
                  boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                  cursor: "pointer",
                  transition: "transform 0.2s, box-shadow 0.2s",
                  display: "flex",
                  flexDirection: "column",
                  gap: "15px",
                  minWidth: "280px",
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
                  backgroundColor: index === 0 ? "#ffd700" : index === 1 ? "#c0c0c0" : index === 2 ? "#cd7f32" : "#007bff",
                  color: index < 3 ? "#000" : "#fff",
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
                  color: "#3498db", 
                  fontSize: "18px",
                  fontWeight: "600",
                  paddingRight: "45px" // Make room for badge
                }}>
                  {space.title}
                </h4>
                
                <p style={{
                  margin: "0",
                  fontSize: "14px",
                  color: "#666",
                  lineHeight: "1.4",
                  height: "40px",
                  overflow: "hidden"
                }}>
                  {space.description || "No description available"}
                </p>
                
                <div style={{ 
                  display: "flex", 
                  gap: "20px",
                  alignItems: "center"
                }}>
                  <div>
                    <span style={{ 
                      fontWeight: "bold", 
                      color: "#2ecc71",
                      fontSize: "16px"
                    }}>
                      {space.contributor_count || 0}
                    </span>
                    <span style={{ 
                      color: "#666", 
                      marginLeft: "5px",
                      fontSize: "14px"
                    }}>
                      contributors
                    </span>
                  </div>
                  
                  <div>
                    <span style={{ 
                      fontWeight: "bold", 
                      color: "#e74c3c",
                      fontSize: "16px"
                    }}>
                      {space.node_count || 0}
                    </span>
                    <span style={{ 
                      color: "#666", 
                      marginLeft: "5px",
                      fontSize: "14px"
                    }}>
                      nodes
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
