import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function Login({ setIsAuthenticated }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    try {
      const res = await axios.post('http://localhost:8000/api/login/', {
        username,
        password
      });
      console.log("Full Response:", res.data);

      if (res.data && res.data.token) {
        localStorage.setItem('token', res.data.token);
        setIsAuthenticated(true);
        navigate('/');
      } else {
        setMessage('Login successful, but no token received.');
      }
      
    } catch (error) {
      setMessage('Login failed.');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Login</h2>
      <input type="text" placeholder="Username" value={username} onChange={(e)=>setUsername(e.target.value)} required />
      <input type="password" placeholder="Password" value={password} onChange={(e)=>setPassword(e.target.value)} required />
      <button type="submit">Login</button>
      <p>{message}</p>
    </form>
  );
};

export default Login;
