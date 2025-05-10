import {
  BrowserRouter as Router,
  Route,
  Routes,
  Navigate,
  Link,
} from "react-router-dom";
import { useState, useEffect } from "react";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import CreateSpace from "./pages/CreateSpace";
import SpaceDetail from "./pages/SpaceDetails";
import Search from "./pages/Search";
import Profile from "./pages/Profile";
import api from "./axiosConfig";
import { API_ENDPOINTS } from "./constants/config";

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(
    !!localStorage.getItem("token")
  );
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    const handleStorageChange = () => {
      setIsAuthenticated(!!localStorage.getItem("token"));
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  useEffect(() => {
    const fetchCurrentUser = async () => {
      if (isAuthenticated) {
        try {
          const token = localStorage.getItem("token");
          if (!token) {
            console.error("No token found");
            setIsAuthenticated(false);
            return;
          }

          const response = await api.get(API_ENDPOINTS.PROFILE_ME, {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });

          setCurrentUser(response.data.user);
        } catch (error) {
          console.error("Error fetching current user:", error);
          if (error.message.includes("401") || error.message.includes("403")) {
            localStorage.removeItem("token");
            setIsAuthenticated(false);
          }
        }
      }
    };

    fetchCurrentUser();
  }, [isAuthenticated]);

  return (
    <Router>
      <Routes>
        <Route
          path="/"
          element={
            isAuthenticated ? (
              <Home
                setIsAuthenticated={setIsAuthenticated}
                currentUser={currentUser}
              />
            ) : (
              <Navigate to="/login" />
            )
          }
        />
        <Route
          path="/login"
          element={
            !isAuthenticated ? (
              <Login setIsAuthenticated={setIsAuthenticated} />
            ) : (
              <Home
                setIsAuthenticated={setIsAuthenticated}
                currentUser={currentUser}
              />
            )
          }
        />
        <Route
          path="/register"
          element={!isAuthenticated ? <Register /> : <Navigate to="/" />}
        />
        <Route
          path="/create-space"
          element={isAuthenticated ? <CreateSpace /> : <Navigate to="/login" />}
        />
        <Route
          path="/spaces/:id"
          element={isAuthenticated ? <SpaceDetail /> : <Navigate to="/login" />}
        />
        <Route
          path="/search"
          element={isAuthenticated ? <Search /> : <Navigate to="/login" />}
        />
        <Route
          path="/profile/:username"
          element={isAuthenticated ? <Profile /> : <Navigate to="/login" />}
        />
      </Routes>

      <nav>
        {!isAuthenticated ? (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        ) : (
          <>
            <Link to="/">Home</Link>
            <Link to="/search">Search</Link>
            {currentUser && (
              <Link to={`/profile/${currentUser.username}`}>Profile</Link>
            )}
            <Link
              to="/logout"
              onClick={() => {
                localStorage.removeItem("token");
                setIsAuthenticated(false);
                window.location.href = "/login";
              }}
            >
              Logout
            </Link>
          </>
        )}
      </nav>
    </Router>
  );
}

export default App;
