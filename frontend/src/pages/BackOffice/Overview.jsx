import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import api from "../../axiosConfig";

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
        
        let errorMessage = 'Failed to load dashboard statistics';
        if (err.response?.status === 403) {
          errorMessage = 'Permission denied - Admin access required';
        } else if (err.response?.status === 404) {
          errorMessage = 'Dashboard endpoint not found';
        } else if (err.response?.data?.error) {
          errorMessage = err.response.data.error;
        }
        
        setError(errorMessage);
        // Keep default values on error
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

  const topSpaces = [
    {
      id: 1,
      title: "The Silent Room",
      contributors: 24,
      nodes: 87,
      description:
        "On a rainy November night in 2022, acclaimed crime novelist Emily Hart",
    },
    {
      id: 2,
      title: "World Literature",
      contributors: 18,
      nodes: 65,
      description:
        "Connecting authors, works, and literary movements across cultures",
    },
    {
      id: 3,
      title: "Philosophy Concepts",
      contributors: 15,
      nodes: 53,
      description:
        "Mapping relationships between philosophical ideas and thinkers",
    },
  ];

  return (
    <div>
      <h2>Dashboard</h2>
      <p>Welcome to the Connect The Dots admin dashboard.</p>

      <div style={{ marginTop: "30px" }}>
        <h3>Platform Overview</h3>
        
        {error && (
          <div style={{
            backgroundColor: "#fee2e2",
            border: "1px solid #fca5a5",
            color: "#dc2626",
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
              {loading ? "..." : stats.totalUsers}
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
              {loading ? "..." : stats.totalSpaces}
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
              {loading ? "..." : stats.totalGraphNodes}
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
              {loading ? "..." : stats.activeDiscussions}
            </p>
          </div>
        </div>
      </div>

      <div style={{ marginTop: "50px" }}>
        <h3>Top Contributed Spaces</h3>
        <div
          style={{
            display: "flex",
            gap: "20px",
            marginTop: "20px",
          }}
        >
          {topSpaces.map((space) => (
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
                gap: "10px",
                height: "100%",
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
              <h4 style={{ margin: "0", color: "#3498db", fontSize: "18px" }}>
                {space.title}
              </h4>
              <p
                style={{
                  margin: "0",
                  fontSize: "14px",
                  color: "#666",
                  flexGrow: 1,
                }}
              >
                {space.description}
              </p>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <div>
                  <span style={{ fontWeight: "bold", color: "#2ecc71" }}>
                    {space.contributors}
                  </span>
                  <span style={{ color: "#666", marginLeft: "5px" }}>
                    contributors
                  </span>
                </div>
                <div>
                  <span style={{ fontWeight: "bold", color: "#e74c3c" }}>
                    {space.nodes}
                  </span>
                  <span style={{ color: "#666", marginLeft: "5px" }}>
                    nodes
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
