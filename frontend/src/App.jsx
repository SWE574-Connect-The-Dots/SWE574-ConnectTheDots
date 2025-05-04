import { BrowserRouter as Router, Route, Routes, Navigate, Link } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Login from './pages/Login';
import Register from './pages/Register';
import Home from './pages/Home';
import CreateSpace from './pages/CreateSpace';
import SpaceDetail from './pages/SpaceDetails';
import Search from './pages/Search';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));

  useEffect(() => {
    const handleStorageChange = () => {
      setIsAuthenticated(!!localStorage.getItem('token'));
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  return (
    <Router>
      <Routes>
        <Route path="/" element={isAuthenticated ? <Home setIsAuthenticated={setIsAuthenticated} /> : <Navigate to="/login" />} />
        <Route path="/login" element={!isAuthenticated ? <Login setIsAuthenticated={setIsAuthenticated} /> : <Home setIsAuthenticated={setIsAuthenticated} />} />
        <Route path="/register" element={!isAuthenticated ? <Register /> : <Navigate to="/" />} />
        <Route path="/create-space" element={isAuthenticated ? <CreateSpace/> : <Navigate to="/login" />} />
        <Route path="/spaces/:id" element={isAuthenticated ? <SpaceDetail /> : <Navigate to="/login" />} />
        <Route path="/search" element={isAuthenticated ? <Search /> : <Navigate to="/login" />} />
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
            <Link to="/logout" onClick={() => {
              localStorage.removeItem('token');
              setIsAuthenticated(false);
              window.location.href = '/login';
            }}>Logout</Link>
          </>
        )}
      </nav>
    </Router>
  );
}

export default App;
