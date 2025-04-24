import { useNavigate, useLocation } from "react-router-dom";
import React, { useState, useEffect } from "react";
import api from '../axiosConfig';
import "../ConnectTheDots.css";
import AppLogo from "../assets/AppLogo.svg";

export default function Home({ setIsAuthenticated }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchValue, setSearchValue] = useState("");

  const [activeTab, setActiveTab] = useState(() => {
    return localStorage.getItem("activeTab") || "trending";
  });
  
  const [spaces, setSpaces] = useState([]);
  
  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsAuthenticated(false);
    navigate("/login");
  };
  
  const handleOnCreateSpace = () => {
    navigate("/create-space");
  };
  
  useEffect(() => {
    localStorage.setItem("activeTab", activeTab);
  }, [activeTab]);
  
  useEffect(() => {
    fetchSpaces();
  }, [activeTab]);
  
  const fetchSpaces = () => {
    api
      .get(`/spaces/${activeTab}/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      })
      .then((res) => {
        setSpaces(res.data);
      })
      .catch((err) => console.error(err));
  };
  
  const handleTabChange = (tab) => {
    setActiveTab(tab);
  };
  
  const navigateToSpace = (space) => {
    navigate(`/spaces/${space.id}`, { 
      state: { 
        ...space,
      }
    });
  };
  
  return (
    <div className="connect-dots-container">
      <header className="header">
        <div className="logo-container">
          <img src={AppLogo} alt="Connect-The-Dots Logo" className="logo" />
          <div className="app-name">Connect-The-Dots</div>
        </div>
        <nav className="navigation">
          <div className="nav-item">Discover</div>
          <div className="nav-item">My Spaces</div>
          <div className="nav-item">Profile</div>
        </nav>
        <div className="search-container">
          <input
            type="text"
            placeholder="Search..."
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
          />
          {searchValue && (
            <button className="clear-button" onClick={() => setSearchValue("")}>
              ×
            </button>
          )}
        </div>
        <button className="create-space-button" onClick={handleOnCreateSpace}>
          Create Space
        </button>
      </header>
      {/* Tabs */}
      <div className="tabs-container">
        <div
          className={`tab ${activeTab === "trending" ? "active" : ""}`}
          onClick={() => handleTabChange("trending")}
        >
          Trending
        </div>
        <div
          className={`tab ${activeTab === "new" ? "active" : ""}`}
          onClick={() => handleTabChange("new")}
        >
          New
        </div>
      </div>
      {/* Space Cards */}
      <div className="spaces-container">
        {spaces.map((space) => (
          <div key={space.id} className="space-card">
            <div className="space-header">
              <h2
                className="space-title"
                style={{ cursor: "pointer" }}
                onClick={() => navigateToSpace(space)}
              >
                {space.title}
              </h2>
              <div className="contributors">
                Contributors: {space.collaborators?.length || 0}
              </div>
            </div>
            <div className="space-content">
              <p>{space.description}</p>
              <div className="activities">
                {/* Placeholder for now */}
              </div>
            </div>
            <div className="space-footer">
              <div className="tags">
                {space.tags.map((tag) => (
                  <div key={tag.id} className="tag">
                    {tag.name}
                  </div>
                ))}
              </div>
              <button className="join-button">JOIN</button>
            </div>
          </div>
        ))}
      </div>
      <div>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </div>
  );
}