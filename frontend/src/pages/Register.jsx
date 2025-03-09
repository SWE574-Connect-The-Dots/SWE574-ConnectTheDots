import { useState } from 'react';
import axios from 'axios';

const Register = () => {
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    profession: '',
    dob: '',
  });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setMessage('');
    setError('');
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post('http://localhost:8000/api/register/', form);
      setMessage(res.data.message || 'Registration successful!');
    } catch (error) {
      if (error.response) {
        const errors = error.response.data;
        if (errors.username) setError(errors.username[0]);
        else if (errors.email) setError(errors.email[0]);
        else if (errors.dob) setError(errors.dob[0]);
        else setError('Registration failed.');
      }
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input name="username" placeholder="Username" onChange={handleChange} required />
      <input type="email" name="email" placeholder="Email" onChange={handleChange} required />
      <input type="password" name="password" placeholder="Password" onChange={handleChange} required />
      <input name="profession" placeholder="Profession" onChange={handleChange} required />
      <input type="date" name="dob" onChange={handleChange} required />
      <button type="submit">Register</button>
      
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {message && <p style={{ color: 'green' }}>{message}</p>}
    </form>
  );
};

export default Register;
