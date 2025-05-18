import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { API_ENDPOINTS } from "../constants/config";

const Header = ({ isAuthenticated, currentUser, setIsAuthenticated }) => {
  const [searchQuery, setSearchQuery] = useState("");
  const navigate = useNavigate();

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("is_staff");
    localStorage.removeItem("is_superuser");
    setIsAuthenticated(false);
    navigate("/login");
  };

  return (
    <header className="header">
      <div className="logo-container">
        <Link to="/">
          <div className="app-name">Connect the Dots</div>
        </Link>
      </div>

      {isAuthenticated && (
        <>
          <div className="navigation">
            <Link to="/" className="nav-item">
              Discover
            </Link>
            {currentUser && (
              <Link
                to={`/profile/${currentUser.username}`}
                className="nav-item"
              >
                Profile ({currentUser.username})
              </Link>
            )}
          </div>

          <div className="search-container">
            <form onSubmit={handleSearch}>
              <input
                type="text"
                placeholder="Search..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              {searchQuery && (
                <button
                  type="button"
                  className="clear-button"
                  onClick={() => setSearchQuery("")}
                >
                  ×
                </button>
              )}
            </form>
          </div>

          <Link to="/create-space" className="create-space-button">
            Create Space
          </Link>

          <button onClick={handleLogout} className="logout-button">
            Logout
          </button>
        </>
      )}

      {!isAuthenticated && (
        <div className="auth-buttons">
          <Link to="/login" className="auth-button login">
            Login
          </Link>
          <Link to="/register" className="auth-button register">
            Register
          </Link>
        </div>
      )}
    </header>
  );
};

export default Header;
