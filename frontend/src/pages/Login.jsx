import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useTranslation } from "../contexts/TranslationContext";
import api from "../axiosConfig";
import { API_ENDPOINTS } from "../constants/config";
import logo from "../assets/logo.png";

function Login({ setIsAuthenticated }) {
  const { t } = useTranslation();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    try {
      const response = await api.post(
        "/login/",
        {
          username,
          password,
        },
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      localStorage.setItem("token", response.data.token);
      localStorage.setItem("username", username);

      // Fetch user profile to get is_staff and is_superuser
      try {
        const profileRes = await api.get(API_ENDPOINTS.PROFILE_ME, {
          headers: {
            Authorization: `Bearer ${response.data.token}`,
          },
        });
        const user = profileRes.data.user;
        const is_staff = user.is_staff ?? profileRes.data.is_staff ?? false;
        const is_superuser =
          user.is_superuser ?? profileRes.data.is_superuser ?? false;
        localStorage.setItem("is_staff", String(is_staff));
        localStorage.setItem("is_superuser", String(is_superuser));
      } catch (profileErr) {
        localStorage.setItem("is_staff", "false");
        localStorage.setItem("is_superuser", "false");
      }

      setIsAuthenticated(true);
      navigate("/");
    } catch (error) {
      setMessage(t("auth.loginFailed"));
      console.error("Login error:", error);
    }
  };

  return (
    <div className="login-container" style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      background: 'linear-gradient(290deg, #E3F2FD 0%, var(--color-bg) 50%,var(--color-accent) 100%)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      overflow: 'auto'
    }}>
      <form onSubmit={handleSubmit} style={{ 
        maxWidth: '20%', 
        margin: '2rem auto',
        padding: '2rem',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: 'var(--color-white)',
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)'
      }}>
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '1rem',
          paddingBottom: '1.5rem',
          borderBottom: '1px solid rgba(0, 0, 0, 0.1)',
          marginBottom: '0.5rem'
        }}>
          <img 
            src={logo} 
            alt="Logo" 
            style={{
              width: '200px',
              height: 'auto',
              display: 'block'
            }}
          />
          
          <h2 style={{ 
            textAlign: 'center',
            margin: '0',
            fontSize: '1.75rem',
            fontWeight: '500'
          }}>
            {t("auth.login")}
          </h2>
        </div>
      
      <input
        type="text"
        name="username"
        placeholder={t("auth.username")}
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        required
      />
      <input
        type="password"
        name="password"
        placeholder={t("auth.password")}
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
      />
      
      <button 
        type="submit"
        style={{
          padding: '0.75rem',
          fontSize: '1rem',
          marginTop: '0.5rem'
        }}
      >
        {t("auth.login")}
      </button>
      
      {message && (
        <p style={{ 
          textAlign: 'center',
          margin: '0',
          marginTop: '-0.5rem'
        }}>
          {message}
        </p>
      )}
      
      <div style={{
        textAlign: 'center',
        paddingTop: '1rem',
        borderTop: '1px solid rgba(0, 0, 0, 0.1)'
      }}>
        <p style={{ margin: '0', fontSize: '0.95rem' }}>
          Don't have an account?{' '}
          <Link 
            to="/register" 
            style={{
              color: 'var(--color-primary, #007bff)',
              textDecoration: 'none',
              fontWeight: '500'
            }}
          >
            Register here
          </Link>
        </p>
      </div>
    </form>
    </div>
  );
}

export default Login;
