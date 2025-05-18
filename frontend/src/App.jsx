import {
  BrowserRouter as Router,
  Route,
  Routes,
  Navigate,
} from "react-router-dom";
import { useState, useEffect } from "react";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import CreateSpace from "./pages/CreateSpace";
import SpaceDetail from "./pages/SpaceDetails";
import Search from "./pages/Search";
import Profile from "./pages/Profile";
import Header from "./components/Header";
import api from "./axiosConfig";
import { API_ENDPOINTS } from "./constants/config";
import "./ConnectTheDots.css";

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

          const user = response.data.user;
          const is_staff = user.is_staff ?? response.data.is_staff ?? false;
          const is_superuser =
            user.is_superuser ?? response.data.is_superuser ?? false;
          const userWithFlags = {
            ...user,
            is_staff,
            is_superuser,
          };
          setCurrentUser(userWithFlags);
          localStorage.setItem("is_staff", String(is_staff));
          localStorage.setItem("is_superuser", String(is_superuser));
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
      <div className="connect-dots-container">
        <Header
          isAuthenticated={isAuthenticated}
          currentUser={currentUser}
          setIsAuthenticated={setIsAuthenticated}
        />

        <main className="main-content">
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
                  <Navigate to="/" />
                )
              }
            />
            <Route
              path="/register"
              element={!isAuthenticated ? <Register /> : <Navigate to="/" />}
            />
            <Route
              path="/create-space"
              element={
                isAuthenticated ? <CreateSpace /> : <Navigate to="/login" />
              }
            />
            <Route
              path="/spaces/:id"
              element={
                isAuthenticated ? <SpaceDetail /> : <Navigate to="/login" />
              }
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
        </main>
      </div>
    </Router>
  );
}

export default App;
