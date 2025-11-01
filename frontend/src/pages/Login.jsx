import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "../contexts/TranslationContext";
import api from "../axiosConfig";
import { API_ENDPOINTS } from "../constants/config";

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
        // fallback: clear admin flags if profile fetch fails
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
    <form onSubmit={handleSubmit}>
      <h2>{t("auth.login")}</h2>
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
      <button type="submit">{t("auth.login")}</button>
      <p>{message}</p>
    </form>
  );
}

export default Login;
