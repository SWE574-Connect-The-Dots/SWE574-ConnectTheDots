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
  const [loadingSpaces, setLoadingSpaces] = useState({});
  
  const handleLogout = () => {
    localStorage.removeItem("token");
    setIsAuthenticated(false);
    navigate("/login");
  };
  
  const handleOnCreateSpace = () => {
    navigate("/create-space");
  };
  
  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && searchValue.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchValue.trim())}`);
    }
  };
  
  const handleSearchButtonClick = () => {
    if (searchValue.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchValue.trim())}`);
    }
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
  
  const handleJoinLeaveSpace = async (spaceId, isCollaborator) => {
    setLoadingSpaces(prev => ({ ...prev, [spaceId]: true }));
    try {
      const endpoint = isCollaborator ? 'leave' : 'join';
      await api.post(`/spaces/${spaceId}/${endpoint}/`, {}, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        }
      });
      
      const response = await api.get(`/spaces/${spaceId}/`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        }
      });
      
      setSpaces(prevSpaces => prevSpaces.map(space => {
        if (space.id === spaceId) {
          return {
            ...space,
            ...response.data
          };
        }
        return space;
      }));
      
    } catch (error) {
      console.error('Error joining/leaving space:', error);
    } finally {
      setLoadingSpaces(prev => ({ ...prev, [spaceId]: false }));
    }
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
        <div className="search-container" style={{ 
          position: 'relative',
          display: 'flex',
          alignItems: 'center'
        }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <input
              type="text"
              placeholder="Search spaces or users..."
              value={searchValue}
              onChange={(e) => setSearchValue(e.target.value)}
              onKeyDown={handleKeyDown}
              style={{ 
                paddingRight: '30px',
                width: '100%',
                boxSizing: 'border-box'
              }}
            />
            {searchValue && (
              <button 
                className="clear-button" 
                onClick={() => setSearchValue("")}
                style={{
                  position: 'absolute',
                  right: '10px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'transparent',
                  border: 'none',
                  fontSize: '18px',
                  cursor: 'pointer',
                  color: '#666',
                  zIndex: 2
                }}
              >
                ×
              </button>
            )}
          </div>
          <button 
            className="search-button" 
            onClick={handleSearchButtonClick}
            style={{
              marginLeft: '5px',
              padding: '5px 10px',
              backgroundColor: '#1a73e8',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Search
          </button>
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
              <button 
                className={`${space.collaborators?.includes(localStorage.getItem('username')) ? 'leave-button' : 'join-button'}`}
                onClick={(e) => {
                  e.stopPropagation();
                  handleJoinLeaveSpace(
                    space.id, 
                    space.collaborators?.includes(localStorage.getItem('username'))
                  );
                }}
                disabled={loadingSpaces[space.id]}
              >
                {loadingSpaces[space.id] ? 'Processing...' : 
                 space.collaborators?.includes(localStorage.getItem('username')) ? 'LEAVE' : 'JOIN'}
              </button>
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