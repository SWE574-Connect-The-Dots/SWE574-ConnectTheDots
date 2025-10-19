import React, { useState, useEffect } from "react";
import analyticsData from "../../data/analyticsMock.json";

export default function Analytics() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [timeframe, setTimeframe] = useState("monthly");

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
          backgroundColor: "white",
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
          <h3 style={{ margin: 0, fontSize: "16px", color: "#555" }}>
            {title}
          </h3>
          {icon && <div>{icon}</div>}
        </div>
        <div style={{ display: "flex", alignItems: "baseline", gap: "10px" }}>
          <span style={{ fontSize: "28px", fontWeight: "600" }}>{value}</span>
          {change && (
            <span
              style={{
                color: changeDirection === "up" ? "#4CAF50" : "#F44336",
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
      {description && <p style={{ color: "#666", margin: 0 }}>{description}</p>}
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
          backgroundColor: "white",
          color: "black",
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
          gap: "20px",
          marginTop: "30px",
        }}
      >
        <StatCard
          title="Total Users"
          value={data.users.total.toLocaleString()}
          change={`${data.users.growth_rate}%`}
          changeDirection="up"
        />
        <StatCard
          title="New Users This Month"
          value={data.users.new_this_month.toLocaleString()}
        />
        <StatCard
          title="Total Spaces"
          value={data.spaces.total.toLocaleString()}
        />
        <StatCard
          title="New Spaces This Month"
          value={data.spaces.new_this_month.toLocaleString()}
        />
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
        <StatCard
          title="Total Nodes"
          value={data.nodes.total.toLocaleString()}
        />
        <StatCard
          title="New Nodes This Month"
          value={data.nodes.created_this_month.toLocaleString()}
        />
        <StatCard
          title="Total Edges"
          value={data.edges.total.toLocaleString()}
        />
        <StatCard
          title="New Edges This Month"
          value={data.edges.created_this_month.toLocaleString()}
        />
      </div>

      <SectionHeader
        title="Popular Tags"
        description="Most frequently used tags across all spaces"
      />

      <div
        style={{
          backgroundColor: "white",
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
                backgroundColor: "#f0f0f0",
                borderRadius: "16px",
                padding: "6px 12px",
                fontSize: "14px",
                color: "#333",
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
          backgroundColor: "white",
          borderRadius: "8px",
          padding: "20px",
          boxShadow: "0 2px 4px rgba(0,0,0,0.05)",
          marginBottom: "30px",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            height: "300px",
          }}
        >
          <div style={{ textAlign: "center", color: "#666" }}>
            <p>Graph visualization would be displayed here</p>
          </div>
        </div>
      </div>
    </div>
  );
}
