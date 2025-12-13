import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from '../contexts/TranslationContext';
import api from '../axiosConfig';
import logo from '../assets/logo.png';

const Register = () => {
  const { t } = useTranslation();
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    profession: '',
    dob: '',
    latitude: null,
    longitude: null,
    country: '',
    city: '',
    location_name: '',
  });

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [permissionDenied, setPermissionDenied] = useState(false);
  const [countries, setCountries] = useState([]);
  const [cities, setCities] = useState([]);

  //  Try to get user's geolocation on mount
  useEffect(() => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const lat = position.coords.latitude;
          const lon = position.coords.longitude;

          try {
            const res = await fetch(
              `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`
            );
            const data = await res.json();
            setForm((prev) => ({
              ...prev,
              latitude: lat,
              longitude: lon,
              location_name: data.display_name || '',
            }));
          } catch {
            setForm((prev) => ({ ...prev, latitude: lat, longitude: lon }));
          }
        },
        () => {
          // Denied → fallback to manual
          setPermissionDenied(true);
          fetchCountries();
        }
      );
    } else {
      setPermissionDenied(true);
      fetchCountries();
    }
  }, []);

  //  Fetch all countries
  const fetchCountries = async () => {
    try {
      const response = await fetch('https://countriesnow.space/api/v0.1/countries/positions');
      const data = await response.json();
      setCountries(data.data || []);
    } catch (err) {
      console.error('Failed to fetch countries', err);
    }
  };

  //  Fetch cities for selected country
  const fetchCities = async (countryName) => {
    try {
      const response = await fetch('https://countriesnow.space/api/v0.1/countries/cities', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ country: countryName }),
      });
      const data = await response.json();
      setCities(data.data || []);
    } catch (err) {
      console.error('Failed to fetch cities', err);
    }
  };

  // Handle input change
  const handleChange = (e) => {
    setMessage('');
    setError('');
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));

    // when a new country is selected, load its cities
    if (name === 'country') {
      setForm((prev) => ({ ...prev, city: '', location_name: '' }));
      fetchCities(value);
    }
  };

  // Submit registration
  const handleSubmit = async (e) => {
    e.preventDefault();

    // combine city + country for backend location_name
    const payload = {
      ...form,
      location_name: form.city && form.country ? `${form.city}, ${form.country}` : form.location_name,
    };

    try {
      const res = await api.post('/register/', payload);
      setMessage(res.data.message || t('auth.registrationSuccess'));
    } catch (error) {
      if (error.response) {
        const errors = error.response.data;
        if (errors.username) setError(errors.username[0]);
        else if (errors.email) setError(errors.email[0]);
        else if (errors.dob) setError(errors.dob[0]);
        else setError(t('auth.registrationFailed'));
      } else {
        setError(t('auth.networkError'));
      }
    }
  };

  return (
    <div className="register-container" style={{
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
            {t('auth.register')}
          </h2>
        </div>
      
      <input 
        name="username" 
        placeholder={t('auth.username')} 
        onChange={handleChange} 
        required 
      />
      
      <input 
        type="email" 
        name="email" 
        placeholder={t('auth.email')} 
        onChange={handleChange} 
        required
      />
      
      <input 
        type="password" 
        name="password" 
        placeholder={t('auth.password')} 
        onChange={handleChange} 
        required
      />
      
      <input 
        name="profession" 
        placeholder={t('profile.profession')} 
        onChange={handleChange} 
        required
      />
      
      <input 
        type="date" 
        name="dob" 
        placeholder={t('profile.dateOfBirth')} 
        onChange={handleChange} 
        required
      />

      {permissionDenied && (
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '0.5rem'
        }}>
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            gap: '0.5rem'
          }}>
            <select
              id="country"
              name="country"
              value={form.country}
              onChange={handleChange}
              required
              style={{
                padding: '0.75rem',
                fontSize: '1rem',
                backgroundColor: 'var(--color-white)',
                border: '1px solid var(--color-gray-200)',
                borderRadius: '8px',
                color: 'var(--color-gray-400)'
              }}
            >
              <option value="">Select Country</option>
              {countries.length > 0 ? (
                countries.map((country) => (
                  <option key={country.name} value={country.name}>
                    {country.name}
                  </option>
                ))
              ) : (
                <option disabled>Loading countries...</option>
              )}
            </select>
          </div>

          {form.country && (
            <div style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '0.75rem'
            }}>
              <select
                id="city"
                name="city"
                value={form.city}
                onChange={handleChange}
                required
                style={{
                  padding: '0.75rem',
                  fontSize: '1rem',
                  backgroundColor: 'var(--color-white)',
                  border: '1px solid var(--color-gray-200)',
                  borderRadius: '8px',
                  color: 'var(--color-gray-400)'
                }}
              >
                <option value="">Select City</option>
                {cities.length > 0 ? (
                  cities.map((city) => (
                    <option key={city} value={city}>
                      {city}
                    </option>
                  ))
                ) : (
                  <option disabled>Loading cities...</option>
                )}
              </select>
            </div>
          )}
        </div>
      )}

      <button 
        type="submit"
        style={{
          padding: '0.75rem',
          fontSize: '1rem',
          marginTop: '0.5rem'
        }}
      >
        {t('auth.register')}
      </button>
      
      {error && (
        <p style={{ 
          color: 'var(--color-danger)',
          textAlign: 'center',
          margin: '0',
          marginTop: '-0.5rem'
        }}>
          {error}
        </p>
      )}
      
      {message && (
        <p style={{ 
          color: 'var(--color-success)',
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
          Already have an account?{' '}
          <Link 
            to="/login" 
            style={{
              color: 'var(--color-primary, #007bff)',
              textDecoration: 'none',
              fontWeight: '500'
            }}
          >
            Login here
          </Link>
        </p>
      </div>
    </form>
    </div>
  );
};

export default Register;
