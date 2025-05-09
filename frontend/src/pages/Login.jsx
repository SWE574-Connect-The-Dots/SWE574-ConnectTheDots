import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../axiosConfig';

function Login({ setIsAuthenticated }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      const response = await api.post('/login/', {
        username,
        password
      }, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('username', username);
      setIsAuthenticated(true);
      navigate('/');
    } catch (error) {
      setMessage('Login failed. Please check your credentials.');
      console.error('Login error:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Login</h2>
      <input type="text" name="username" placeholder="Username" value={username} onChange={(e)=>setUsername(e.target.value)} required />
      <input type="password" name="password" placeholder="Password" value={password} onChange={(e)=>setPassword(e.target.value)} required />
      <button type="submit">Login</button>
      <p>{message}</p>
    </form>
  );
};

export default Login;
