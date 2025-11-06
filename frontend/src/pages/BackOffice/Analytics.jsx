import React, { useState, useEffect } from "react";
import analyticsData from "../../data/analyticsMock.json";

export default function Analytics() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [timeframe, setTimeframe] = useState("monthly");
  const [activeTab, setActiveTab] = useState("users");
  const [selectedPanel, setSelectedPanel] = useState("panel-1");

  useEffect(() => {
    setData(analyticsData);
    setLoading(false);
  }, []);

  if (loading) {
    return <div>Loading analytics data...</div>;
  }

  const StatCard = ({ title, value, change, changeDirection, icon }) => {
    return (
      <div
        style={{
          backgroundColor: "var(--color-white)",
          borderRadius: "8px",
          padding: "20px",
          boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
          display: "flex",
          flexDirection: "column",
          height: "100%",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: "15px",
          }}
        >
          <h3
            style={{
              margin: 0,
              fontSize: "16px",
              color: "var(--color-gray-400)",
            }}
          >
            {title}
          </h3>
          {icon && <div>{icon}</div>}
        </div>
        <div style={{ display: "flex", alignItems: "baseline", gap: "10px" }}>
          <span style={{ fontSize: "28px", fontWeight: "600" }}>{value}</span>
          {change && (
            <span
              style={{
                color:
                  changeDirection === "up"
                    ? "var(--color-success)"
                    : "var(--color-danger-dark)",
                fontSize: "14px",
                fontWeight: "500",
              }}
            >
              {changeDirection === "up" ? "↑" : "↓"} {change}
            </span>
          )}
        </div>
      </div>
    );
  };

  const SectionHeader = ({ title, description }) => (
    <div style={{ marginBottom: "20px", marginTop: "20px" }}>
      <h2 style={{ fontSize: "20px", margin: "0 0 5px 0" }}>{title}</h2>
      {description && (
        <p style={{ color: "var(--color-gray-400)", margin: 0 }}>
          {description}
        </p>
      )}
    </div>
  );

  const TimeframeSelector = () => (
    <div
      style={{
        marginBottom: "20px",
        display: "flex",
        justifyContent: "flex-end",
      }}
    >
      <select
        value={timeframe}
        onChange={(e) => setTimeframe(e.target.value)}
        style={{
          padding: "8px 12px",
          borderRadius: "4px",
          border: "1px solid #ddd",
          backgroundColor: "var(--color-white)",
          color: "var(--color-black)",
        }}
      >
        <option value="daily">Daily</option>
        <option value="weekly">Weekly</option>
        <option value="monthly">Monthly</option>
        <option value="yearly">Yearly</option>
      </select>
    </div>
  );

  return (
    <div>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "20px",
        }}
      >
        <h2>Analytics Dashboard</h2>
        <TimeframeSelector />
      </div>

      <SectionHeader
        title="Overall Platform Metrics"
        description="Key metrics showing the growth of the platform"
      />

      <div
        style={{
          display: "flex",
          flexTemplateColumns: "repeat(auto-fill, minmax(250px, 1fr))",
          gap: "16px",
          marginTop: "20px",
        }}
      > 
        <div
          style={{
            backgroundColor: "var(--color-white)",
            borderRadius: "8px",
            padding: "12px",
            boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
            width: "220px", 
            height: "140px",
            alignItems: "center",
            display: "flex",
            justifyContent: "center",
          }}
        >
          <iframe
            src="http://localhost:3001/d-solo/d5c878d6-2b00-4fce-90e6-4240c4c09d53/connectthedots-back-office-numerics-analytics?orgId=1&from=1759816786925&to=1762408786925&timezone=browser&refresh=5m&theme=light&panelId=panel-1&__feature.dashboardSceneSolo=true"
            width="100%"
            height="120px"
            frameBorder="0"
            style={{
              borderRadius: "8px",
              border: "none",
            }}
          ></iframe>
        </div>
        <div
          style={{
            backgroundColor: "var(--color-white)",
            borderRadius: "8px",
            padding: "12px",
            boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
            width: "220px", 
            height: "140px",
            alignItems: "center",
            display: "flex",
            justifyContent: "center",
          }}
        >
          <iframe
            src="http://localhost:3001/d-solo/d5c878d6-2b00-4fce-90e6-4240c4c09d53/connectthedots-back-office-numerics-analytics?orgId=1&from=1759816786925&to=1762408786925&timezone=browser&refresh=5m&theme=light&panelId=panel-2&__feature.dashboardSceneSolo=true"
            width="100%"
            height="120px"
            frameBorder="0"
            style={{
              borderRadius: "8px",
              border: "none",
            }}
          ></iframe>
        </div>
        <div
          style={{
            backgroundColor: "var(--color-white)",
            borderRadius: "8px",
            padding: "12px",
            boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
            width: "220px", 
            height: "140px",
            alignItems: "center",
            display: "flex",
            justifyContent: "center",
          }}
        >
          <iframe
            src="http://localhost:3001/d-solo/d5c878d6-2b00-4fce-90e6-4240c4c09d53/connectthedots-back-office-numerics-analytics?orgId=1&from=1759816786925&to=1762408786925&timezone=browser&refresh=5m&theme=light&panelId=panel-3&__feature.dashboardSceneSolo=true"
            width="100%"
            height="120px"
            frameBorder="0"
            style={{
              borderRadius: "8px",
              border: "none",
            }}
          ></iframe>
        </div>
        <div
          style={{
            backgroundColor: "var(--color-white)",
            borderRadius: "8px",
            padding: "12px",
            boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
            width: "220px", 
            height: "140px",
            alignItems: "center",
            display: "flex",
            justifyContent: "center",
          }}
        >
          <iframe
            src="http://localhost:3001/d-solo/d5c878d6-2b00-4fce-90e6-4240c4c09d53/connectthedots-back-office-numerics-analytics?orgId=1&from=1759816786925&to=1762408786925&timezone=browser&refresh=5m&theme=light&panelId=panel-4&__feature.dashboardSceneSolo=true"
            width="100%"
            height="120px"
            frameBorder="0"
            style={{
              borderRadius: "8px",
              border: "none",
            }}
          ></iframe>
        </div>
      </div>

      <SectionHeader
        title="Content Statistics"
        description="Statistics about nodes, edges and connections in the system"
      />

      <div
        style={{
          display: "flex",
          gap: "20px",
          marginTop: "30px",
        }}
      >
        <div
          style={{
            backgroundColor: "var(--color-white)",
            borderRadius: "8px",
            padding: "12px",
            boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
            width: "220px", 
            height: "140px",
            alignItems: "center",
            display: "flex",
            justifyContent: "center",
          }}
        >
          <iframe
            src="http://localhost:3001/d-solo/d5c878d6-2b00-4fce-90e6-4240c4c09d53/connectthedots-back-office-numerics-analytics?orgId=1&from=1759816786925&to=1762408786925&timezone=browser&refresh=5m&theme=light&panelId=panel-5&__feature.dashboardSceneSolo=true"
            width="100%"
            height="120px"
            frameBorder="0"
            style={{
              borderRadius: "8px",
              border: "none",
            }}
          ></iframe>
        </div>
        <div
          style={{
            backgroundColor: "var(--color-white)",
            borderRadius: "8px",
            padding: "12px",
            boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
            width: "220px", 
            height: "140px",
            alignItems: "center",
            display: "flex",
            justifyContent: "center",
          }}
        >
          <iframe
            src="http://localhost:3001/d-solo/d5c878d6-2b00-4fce-90e6-4240c4c09d53/connectthedots-back-office-numerics-analytics?orgId=1&from=1759816786925&to=1762408786925&timezone=browser&refresh=5m&theme=light&panelId=panel-6&__feature.dashboardSceneSolo=true" 
            width="100%"
            height="120px"
            frameBorder="0"
            style={{
              borderRadius: "8px",
              border: "none",
            }}
          ></iframe>
        </div>
        <div
          style={{
            backgroundColor: "var(--color-white)",
            borderRadius: "8px",
            padding: "12px",
            boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
            width: "220px", 
            height: "140px",
            alignItems: "center",
            display: "flex",
            justifyContent: "center",
          }}
        >
          <iframe
            src="http://localhost:3001/d-solo/d5c878d6-2b00-4fce-90e6-4240c4c09d53/connectthedots-back-office-numerics-analytics?orgId=1&from=1759816786925&to=1762408786925&timezone=browser&refresh=5m&theme=light&panelId=panel-7&__feature.dashboardSceneSolo=true"
            width="100%"
            height="120px"
            frameBorder="0"
            style={{
              borderRadius: "8px",
              border: "none",
            }}
          ></iframe>
        </div>
        <div
          style={{
            backgroundColor: "var(--color-white)",
            borderRadius: "8px",
            padding: "12px",
            boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
            width: "220px", 
            height: "140px",
            alignItems: "center",
            display: "flex",
            justifyContent: "center",
          }}
        >
          <iframe
            src="http://localhost:3001/d-solo/d5c878d6-2b00-4fce-90e6-4240c4c09d53/connectthedots-back-office-numerics-analytics?orgId=1&from=1759816786925&to=1762408786925&timezone=browser&refresh=5m&theme=light&panelId=panel-8&__feature.dashboardSceneSolo=true" 
            width="100%"
            height="120px"
            frameBorder="0"
            style={{
              borderRadius: "8px",
              border: "none",
            }}
          ></iframe>
        </div>
      </div>

      <SectionHeader
        title="Popular Tags"
        description="Most frequently used tags across all spaces"
      />

      <div
        style={{
          backgroundColor: "var(--color-white)",
          borderRadius: "8px",
          padding: "20px",
          boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
          marginTop: "30px",
        }}
      >
        <div style={{ display: "flex", flexWrap: "wrap", gap: "10px" }}>
          {data.spaces.popular_tags.map((tag, index) => (
            <div
              key={index}
              style={{
                backgroundColor: "var(--color-gray-200)",
                borderRadius: "16px",
                padding: "6px 12px",
                fontSize: "14px",
                color: "var(--color-black)",
              }}
            >
              {tag}
            </div>
          ))}
        </div>
      </div>

      <SectionHeader
        title="Activity Trends"
        description="Trends showing user activity over time"
      />

      <div
        style={{
          backgroundColor: "var(--color-white)",
          borderRadius: "8px",
          padding: "20px",
          boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
          marginBottom: "30px",
        }}
      >
        <div
          style={{
            display: "flex",
            borderBottom: "1px solid var(--color-gray-200)",
            marginBottom: "20px",
          }}
        >
          <div
            onClick={() => setActiveTab("users")}
            style={{
              padding: "10px 20px",
              border: "none",
              borderBottom:
                activeTab === "users" ? "2px solid var(--color-black)" : "none",
              cursor: "pointer",
              fontWeight: activeTab === "users" ? "bold" : "normal",
            }}
          >
            Users
          </div>
          <div
            onClick={() => setActiveTab("nodes")}
            style={{
              padding: "10px 20px",
              border: "none",
              borderBottom:
                activeTab === "nodes" ? "2px solid var(--color-black)" : "none",
              cursor: "pointer",
              fontWeight: activeTab === "nodes" ? "bold" : "normal",
            }}
          >
            Nodes
          </div>
          <div
            onClick={() => setActiveTab("spaces")}
            style={{
              padding: "10px 20px",
              border: "none",
              borderBottom:
                activeTab === "spaces"
                  ? "2px solid var(--color-black)"
                  : "none",
              cursor: "pointer",
              fontWeight: activeTab === "spaces" ? "bold" : "normal",
            }}
          >
            Spaces
          </div>
        </div>

        <div>
          {activeTab === "users" && (
            <div>
              <div style={{ textAlign: "center", marginBottom: "10px" }}>
                <button
                  onClick={() => setSelectedPanel(1)}
                  style={{
                    padding: "6px 12px",
                    margin: "0 5px",
                    borderRadius: "6px",
                    border: "1px solid var(--color-gray-300)",
                    backgroundColor: selectedPanel === 1 ? "var(--color-black)" : "white",
                    color: selectedPanel === 1 ? "white" : "black",
                    cursor: "pointer",
                  }}
                >
                  Daily
                </button>
                <button
                  onClick={() => setSelectedPanel(2)}
                  style={{
                    padding: "6px 12px",
                    margin: "0 5px",
                    borderRadius: "6px",
                    border: "1px solid var(--color-gray-300)",
                    backgroundColor: selectedPanel === 2 ? "var(--color-black)" : "white",
                    color: selectedPanel === 2 ? "white" : "black",
                    cursor: "pointer",
                  }}
                >
                  Weekly
                </button>
                <button
                  onClick={() => setSelectedPanel(3)}
                  style={{
                    padding: "6px 12px",
                    margin: "0 5px",
                    borderRadius: "6px",
                    border: "1px solid var(--color-gray-300)",
                    backgroundColor: selectedPanel === 3 ? "var(--color-black)" : "white",
                    color: selectedPanel === 3 ? "white" : "black",
                    cursor: "pointer",
                  }}
                >
                  Monthly
                </button>
              </div>

              <iframe
                key={selectedPanel} // re-render when panel changes //change ip before pr
                src={`http://localhost:3001/d-solo/17bba15f-5cf5-4e4f-b26f-4c6130594eb5/connectthedots-user-growth-analytics?orgId=1&refresh=5m&theme=light&panelId=${selectedPanel}&__feature.dashboardSceneSolo=true`}
                width="100%"
                height="400"
                frameBorder="0"
                style={{ borderRadius: "8px" }}
              ></iframe>
            </div>
          )}
          {activeTab === "nodes" && (
            <div>
              <div style={{ textAlign: "center", marginBottom: "10px" }}>
                <button
                  onClick={() => setSelectedPanel(1)}
                  style={{
                    padding: "6px 12px",
                    margin: "0 5px",
                    borderRadius: "6px",
                    border: "1px solid var(--color-gray-300)",
                    backgroundColor: selectedPanel === 1 ? "var(--color-black)" : "white",
                    color: selectedPanel === 1 ? "white" : "black",
                    cursor: "pointer",
                  }}
                >
                  Daily
                </button>
                <button
                  onClick={() => setSelectedPanel(2)}
                  style={{
                    padding: "6px 12px",
                    margin: "0 5px",
                    borderRadius: "6px",
                    border: "1px solid var(--color-gray-300)",
                    backgroundColor: selectedPanel === 2 ? "var(--color-black)" : "white",
                    color: selectedPanel === 2 ? "white" : "black",
                    cursor: "pointer",
                  }}
                >
                  Weekly
                </button>
                <button
                  onClick={() => setSelectedPanel(3)}
                  style={{
                    padding: "6px 12px",
                    margin: "0 5px",
                    borderRadius: "6px",
                    border: "1px solid var(--color-gray-300)",
                    backgroundColor: selectedPanel === 3 ? "var(--color-black)" : "white",
                    color: selectedPanel === 3 ? "white" : "black",
                    cursor: "pointer",
                  }}
                >
                  Monthly
                </button>
              </div>

              <iframe
                key={selectedPanel} // re-render when panel changes //change ip before pr
                src={`http://localhost:3001/d-solo/82544b7e-ff84-4bef-8e0c-7f86efca450f/connectthedots-node-analytics?orgId=1&refresh=5m&theme=light&panelId=${selectedPanel}&__feature.dashboardSceneSolo=true`}
                width="100%"
                height="400"
                frameBorder="0"
                style={{ borderRadius: "8px" }}
              ></iframe>
            </div>
          )}
          {activeTab === "spaces" && (
            <div>
              <div style={{ textAlign: "center", marginBottom: "10px" }}>
                <button
                  onClick={() => setSelectedPanel(1)}
                  style={{
                    padding: "6px 12px",
                    margin: "0 5px",
                    borderRadius: "6px",
                    border: "1px solid var(--color-gray-300)",
                    backgroundColor: selectedPanel === 1 ? "var(--color-black)" : "white",
                    color: selectedPanel === 1 ? "white" : "black",
                    cursor: "pointer",
                  }}
                >
                  Daily
                </button>
                <button
                  onClick={() => setSelectedPanel(2)}
                  style={{
                    padding: "6px 12px",
                    margin: "0 5px",
                    borderRadius: "6px",
                    border: "1px solid var(--color-gray-300)",
                    backgroundColor: selectedPanel === 2 ? "var(--color-black)" : "white",
                    color: selectedPanel === 2 ? "white" : "black",
                    cursor: "pointer",
                  }}
                >
                  Weekly
                </button>
                <button
                  onClick={() => setSelectedPanel(3)}
                  style={{
                    padding: "6px 12px",
                    margin: "0 5px",
                    borderRadius: "6px",
                    border: "1px solid var(--color-gray-300)",
                    backgroundColor: selectedPanel === 3 ? "var(--color-black)" : "white",
                    color: selectedPanel === 3 ? "white" : "black",
                    cursor: "pointer",
                  }}
                >
                  Monthly
                </button>
              </div>

              <iframe
                key={selectedPanel} // re-render when panel changes //change ip before pr
                src={`http://localhost:3001/d-solo/05f712d2-fe25-452b-95e2-b7168d3c6745/connectthedots-space-analytics?orgId=1&refresh=5m&theme=light&panelId=${selectedPanel}&__feature.dashboardSceneSolo=true`}
                width="100%"
                height="400"
                frameBorder="0"
                style={{ borderRadius: "8px" }}
              ></iframe>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
