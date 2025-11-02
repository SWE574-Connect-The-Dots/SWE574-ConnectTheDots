import { useState, useEffect } from 'react';
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
    <form onSubmit={handleSubmit}>
      <input name="username" placeholder={t('auth.username')} onChange={handleChange} required />
      <input type="email" name="email" placeholder={t('auth.email')} onChange={handleChange} required />
      <input type="password" name="password" placeholder={t('auth.password')} onChange={handleChange} required />
      <input name="profession" placeholder={t('profile.profession')} onChange={handleChange} required />
      <input type="date" name="dob" placeholder={t('profile.dateOfBirth')} onChange={handleChange} required />

      {/* Manual country/city selection only if permission denied */}
      {permissionDenied && (
        <div>
          <label htmlFor="country">Country:</label>
          <select
            id="country"
            name="country"
            value={form.country}
            onChange={handleChange}
            required
          >
            <option value="">-- Choose Country --</option>
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

          {form.country && (
            <>
              <label htmlFor="city">City:</label>
              <select
                id="city"
                name="city"
                value={form.city}
                onChange={handleChange}
                required
              >
                <option value="">-- Choose City --</option>
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
            </>
          )}
        </div>
      )}

      <button type="submit">{t('auth.register')}</button>
      
      {error && <p style={{ color: 'var(--color-danger)' }}>{error}</p>}
      {message && <p style={{ color: 'var(--color-success)' }}>{message}</p>}
    </form>
  );
};

export default Register;
