import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "../contexts/TranslationContext";
import api from "../axiosConfig";
import { API_ENDPOINTS } from "../constants/config";
import { formatDate } from "../utils/dateUtils";
import ReportModal from "../components/ReportModal";
import "../ConnectTheDots.css";

const Profile = () => {
  const { t } = useTranslation();
  const [user, setUser] = useState(null);
  const [joinedSpaces, setJoinedSpaces] = useState([]);
  const [ownedSpaces, setOwnedSpaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editFormData, setEditFormData] = useState({
    bio: "",
    profession: "",
    country: "",
    city: "",
    location_name: "",
  });
  const [countries, setCountries] = useState([]);
  const [cities, setCities] = useState([]);
  const [selectedCountry, setSelectedCountry] = useState("");
  const [selectedCity, setSelectedCity] = useState("");
  const [isCurrentUser, setIsCurrentUser] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const { username } = useParams();
  const navigate = useNavigate();

  // Fetch countries list
  const fetchCountries = async () => {
    try {
      const response = await fetch('https://countriesnow.space/api/v0.1/countries');
      const data = await response.json();
      if (data.error === false) {
        setCountries(data.data.map(country => country.country));
      }
    } catch (error) {
      console.error('Failed to fetch countries:', error);
    }
  };

  // Fetch cities for selected country
  const fetchCities = async (country) => {
    try {
      const response = await fetch('https://countriesnow.space/api/v0.1/countries/cities', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ country: country }),
      });
      const data = await response.json();
      if (data.error === false) {
        setCities(data.data || []);
      }
    } catch (error) {
      console.error('Failed to fetch cities:', error);
      setCities([]);
    }
  };

  useEffect(() => {
    const fetchProfileData = async () => {
      try {
        const response = await api.get(API_ENDPOINTS.PROFILE(username));
        setUser(response.data);
        setJoinedSpaces(response.data.joined_spaces);
        setOwnedSpaces(response.data.owned_spaces || []);

        const currentUsername = localStorage.getItem("username");
        const isCurrentUserProfile = currentUsername === username;
        setIsCurrentUser(isCurrentUserProfile);

        setEditFormData({
          bio: response.data.bio || "",
          profession: response.data.profession || "",
          country: response.data.country || "",
          city: response.data.city || "",
          location_name: response.data.location_name || "",
        });

        setLoading(false);
      } catch (err) {
        setError(t("errors.failedToLoadProfile"));
        setLoading(false);
      }
    };

    fetchProfileData();
  }, [username]);

  const handleEditClick = async () => {
    setIsEditing(true);
    // Clear any previous errors when entering edit mode
    setError(null);
    
    // Fetch countries when starting to edit
    await fetchCountries();
    
    // Parse existing location to set country and city
    if (user?.location_name) {
      const locationParts = user.location_name.split(', ');
      if (locationParts.length >= 2) {
        const country = locationParts[locationParts.length - 1];
        const city = locationParts[0];
        setSelectedCountry(country);
        setSelectedCity(city);
        // Fetch cities for the existing country
        await fetchCities(country);
      }
    }
  };

  const handleCancelEdit = () => {
    setIsEditing(false);
    setEditFormData({
      bio: user.bio || "",
      profession: user.profession || "",
      country: user.country || "",
      city: user.city || "",
      location_name: user.location_name || "",
    });
    // Reset dropdown values
    setSelectedCountry("");
    setSelectedCity("");
    setCities([]);
    setCountries([]);
    // Clear any errors when cancelling
    setError(null);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setEditFormData({
      ...editFormData,
      [name]: value,
    });
  };

  const handleCountryChange = async (e) => {
    const country = e.target.value;
    setSelectedCountry(country);
    setSelectedCity(""); // Reset city when country changes
    setCities([]); // Clear cities
    
    if (country) {
      await fetchCities(country);
    }
    
    // Update location_name and location fields in form data
    setEditFormData({
      ...editFormData,
      country: country,
      city: selectedCity || "",
      location_name: country ? `${selectedCity ? selectedCity + ', ' : ''}${country}` : "",
    });
  };

  const handleCityChange = (e) => {
    const city = e.target.value;
    setSelectedCity(city);
    
    // Update location_name and location fields in form data
    const locationName = city && selectedCountry ? `${city}, ${selectedCountry}` : selectedCountry || "";
    setEditFormData({
      ...editFormData,
      country: selectedCountry || "",
      city: city,
      location_name: locationName,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (
      user.profession &&
      user.profession.trim() !== "" &&
      (!editFormData.profession || editFormData.profession.trim() === "")
    ) {
      setError(t("errors.professionCannotBeEmpty"));
      return;
    }

    try {
      const response = await api.put(
        API_ENDPOINTS.UPDATE_PROFILE,
        editFormData
      );
      setUser(response.data);
      setIsEditing(false);
      setError(null);
    } catch (err) {
      setError(t("errors.failedToUpdateProfile"));
    }
  };

  if (loading) {
    return <div className="loading">{t("common.loading")}</div>;
  }

  return (
    <div className="profile-container">
      {error && (
        <div className="error-message" data-testid="profile-error">
          <span>{error}</span>
        </div>
      )}
      <div className="profile-header">
        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <h1>{user?.user?.username}'s {t("profile.profile")}</h1>
          {!isCurrentUser && localStorage.getItem("token") && (
            <button
              onClick={() => setShowReportModal(true)}
              style={{
                background: "none",
                border: "none",
                color: "var(--color-text-secondary)",
                cursor: "pointer",
                fontSize: "0.8rem",
                padding: "2px 5px",
              }}
            >
              {t("common.report")} {t("profile.profile")}
            </button>
          )}
        </div>

        {isEditing ? (
          <form onSubmit={handleSubmit} className="profile-edit-form">
            <div className="form-group">
              <label htmlFor="profession">{t("profile.profession")}:</label>
              <input
                type="text"
                id="profession"
                name="profession"
                value={editFormData.profession}
                onChange={handleInputChange}
                maxLength={100}
                required={user.profession !== null && user.profession !== ""}
              />
              <small className="field-note">
                {t("profile.professionNote")}
              </small>
            </div>
            <div className="form-group">
              <label htmlFor="country">Country:</label>
              <select
                id="country"
                name="country"
                value={selectedCountry}
                onChange={handleCountryChange}
                required
              >
                <option value="">-- Select Country --</option>
                {countries.map((country) => (
                  <option key={country} value={country}>
                    {country}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="city">City:</label>
              <select
                id="city"
                name="city"
                value={selectedCity}
                onChange={handleCityChange}
                disabled={!selectedCountry}
                required={!!selectedCountry}
              >
                <option value="">-- Select City --</option>
                {cities.map((city) => (
                  <option key={city} value={city}>
                    {city}
                  </option>
                ))}
              </select>
              {!selectedCountry && (
                <small className="field-note">Please select a country first</small>
              )}
            </div>
            <div className="form-group">
              <label htmlFor="bio">{t("profile.bio")} (max 200 words):</label>
              <textarea
                id="bio"
                name="bio"
                value={editFormData.bio}
                onChange={handleInputChange}
                maxLength={200}
                rows={4}
              />
            </div>

            <div className="form-actions">
              <button type="submit" className="save-button">
                {t("common.save")}
              </button>
              <button
                type="button"
                onClick={handleCancelEdit}
                className="cancel-button"
              >
                {t("common.cancel")}
              </button>
            </div>
          </form>
        ) : (
          <>
            {user?.profession && (
              <p className="profession">{t("profile.profession")}: {user.profession}</p>
            )}
            {user?.location_name && (
              <p className="location">Location: {user.location_name}</p>
            )}
            <p className="bio">{t("profile.bio")}: {user?.bio || "-"}</p>
            {user?.dob && (
              <p className="dob">{t("profile.dateOfBirth")}: {formatDate(user.dob)}</p>
            )}
            {user?.created_at && (
              <p className="created-at">
                {t("profile.joinedDate")}: {formatDate(user.created_at)}
              </p>
            )}
            {isCurrentUser && (
              <button onClick={handleEditClick} className="edit-profile-button">
                {t("profile.editProfile")}
              </button>
            )}
          </>
        )}
      </div>

      <div className="profile-content">
        <div className="owned-spaces">
          <h2>{t("profile.ownedSpaces")}</h2>
          {ownedSpaces.length > 0 ? (
            <div className="spaces-grid">
              {ownedSpaces.map((space) => (
                <div
                  key={space.id}
                  className="space-card"
                  onClick={() => navigate(`/spaces/${space.id}`)}
                >
                  <h3>{space.title}</h3>
                  <p>{space.description}</p>
                </div>
              ))}
            </div>
          ) : (
            <p>{t("profile.noOwnedSpaces")}</p>
          )}
        </div>

        <div className="joined-spaces">
          <h2>{t("profile.joinedSpaces")}</h2>
          {joinedSpaces.length > 0 ? (
            <div className="spaces-grid">
              {joinedSpaces.map((space) => (
                <div
                  key={space.id}
                  className="space-card"
                  onClick={() => navigate(`/spaces/${space.id}`)}
                >
                  <h3>{space.title}</h3>
                  <p>{space.description}</p>
                </div>
              ))}
            </div>
          ) : (
            <p>{t("profile.noJoinedSpaces")}</p>
          )}
        </div>
      </div>
      {showReportModal && (
        <ReportModal
          contentId={user?.user?.id}
          contentType="profile"
          contentTitle={`${user?.user?.username}'s Profile`}
          onClose={() => setShowReportModal(false)}
        />
      )}
    </div>
  );
};

export default Profile;
