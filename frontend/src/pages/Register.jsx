import { useState } from 'react';
import { useTranslation } from '../contexts/TranslationContext';
import api from '../axiosConfig';

const Register = () => {
  const { t } = useTranslation();
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
      const res = await api.post('/register/', form);
      setMessage(res.data.message || t('auth.registrationSuccess'));
    } catch (error) {
      if (error.response) {
        const errors = error.response.data;
        if (errors.username) setError(errors.username[0]);
        else if (errors.email) setError(errors.email[0]);
        else if (errors.dob) setError(errors.dob[0]);
        else setError(t('auth.registrationFailed'));
      }
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input name="username" placeholder={t('auth.username')} onChange={handleChange} required />
      <input type="email" name="email" placeholder={t('auth.email')} onChange={handleChange} required />
      <input type="password" name="password" placeholder={t('auth.password')} onChange={handleChange} required />
      <input name="profession" placeholder={t('profile.profession')} onChange={handleChange} required />
      <input type="date" name="dob" placeholder={t('profile.dateOfBirth')} onChange={handleChange} required />
      <button type="submit">{t('auth.register')}</button>
      
      {error && <p style={{ color: 'var(--color-danger)' }}>{error}</p>}
      {message && <p style={{ color: 'var(--color-success)' }}>{message}</p>}
    </form>
  );
};

export default Register;
