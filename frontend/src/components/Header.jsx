import React, { useState, useRef, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "../contexts/TranslationContext";
import "./Header.css";

const Header = ({ isAuthenticated, currentUser, setIsAuthenticated }) => {
  const { t, changeLanguage, currentLanguage } = useTranslation();
  const [searchQuery, setSearchQuery] = useState("");
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const navigate = useNavigate();
  const dropdownRef = useRef(null);

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

  const toggleDropdown = () => {
    setDropdownOpen(!dropdownOpen);
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [dropdownRef]);

  return (
    <header className="header">
      <div className="logo-container">
        <Link to="/">
          <div className="app-name">{t("app.name")}</div>
        </Link>
      </div>

      {isAuthenticated && (
        <>
          <div className="navigation">
            <Link to="/" className="nav-item">
              {t("navigation.discover")}
            </Link>
            <Link to="/map" className="nav-item">
              Map
            </Link>
          </div>

          <div className="search-container">
            <form onSubmit={handleSearch}>
              <input
                type="text"
                placeholder={t("common.search")}
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
            {t("navigation.createSpace")}
          </Link>
          
          <div className="language-switcher">
            <button
              onClick={() => changeLanguage('en')}
              className={currentLanguage === 'en' ? 'active' : ''}
            >
              EN
            </button>
            <button
              onClick={() => changeLanguage('tr')}
              className={currentLanguage === 'tr' ? 'active' : ''}
            >
              TR
            </button>
          </div>

          {currentUser && (
            <div className="nav-item profile-dropdown" ref={dropdownRef}>
              <div onClick={toggleDropdown} className="profile-toggle">
                {currentUser.username} ▼
              </div>
              {dropdownOpen && (
                <div className="dropdown-menu">
                  <Link
                    to={`/profile/${currentUser.username}`}
                    onClick={() => setDropdownOpen(false)}
                  >
                    {t("navigation.profile")}
                  </Link>
                  {(currentUser.is_staff || currentUser.is_superuser) && (
                    <Link
                      to="/backoffice"
                      onClick={() => setDropdownOpen(false)}
                    >
                      {t("navigation.adminDashboard")}
                    </Link>
                  )}
                  <button onClick={handleLogout}>{t("auth.logout")}</button>
                </div>
              )}
            </div>
          )}
        </>
      )}

      {!isAuthenticated && (
        <>
          <div className="language-switcher">
            <button
              onClick={() => changeLanguage('en')}
              className={currentLanguage === 'en' ? 'active' : ''}
            >
              EN
            </button>
            <button
              onClick={() => changeLanguage('tr')}
              className={currentLanguage === 'tr' ? 'active' : ''}
            >
              TR
            </button>
          </div>
          <div className="auth-buttons">
            <Link to="/login" className="auth-button login">
              {t("auth.login")}
            </Link>
            <Link to="/register" className="auth-button register">
              {t("auth.register")}
            </Link>
          </div>
        </>
      )}
    </header>
  );
};

export default Header;
