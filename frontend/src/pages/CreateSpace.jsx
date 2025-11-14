import React, { useState, useEffect } from "react";
import { useTranslation } from '../contexts/TranslationContext';
import api from "../axiosConfig";
import { useNavigate } from "react-router-dom";

const CreateSpace = () => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    country: "",
    city: "",
    district: "",
    street: "",
  });

  const [countries, setCountries] = useState([]);
  const [cities, setCities] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [streets, setStreets] = useState([]);

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [loadingCities, setLoadingCities] = useState(false);
  const [loadingDistricts, setLoadingDistricts] = useState(false);
  const [loadingStreets, setLoadingStreets] = useState(false);

  const [tagQuery, setTagQuery] = useState("");
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchResults, setSearchResults] = useState([]);
  const [selectedTags, setSelectedTags] = useState([]);
  const [showTagSearch, setShowTagSearch] = useState(false);
  const navigate = useNavigate();

  // Fetch countries on mount
  useEffect(() => {
    const fetchCountries = async () => {
      try {
        const res = await fetch("https://countriesnow.space/api/v0.1/countries/positions");
        const data = await res.json();
        setCountries(data.data || []);
      } catch (err) {
        console.error("Failed to fetch countries", err);
      }
    };
    fetchCountries();
  }, []);

  // Fetch cities when country changes
  useEffect(() => {
    if (formData.country) {
      const fetchCities = async () => {
        setLoadingCities(true);
        try {
          const res = await fetch("https://countriesnow.space/api/v0.1/countries/cities", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ country: formData.country }),
          });
          const data = await res.json();
          if (data.error === false) {
            setCities(data.data || []);
          } else {
            console.error("API Error:", data.msg);
            setCities([]);
          }
          setFormData((prev) => ({ ...prev, city: "", district: "", street: "" }));
          setDistricts([]);
          setStreets([]);
        } catch (err) {
          console.error("Failed to fetch cities", err);
          setCities([]);
        } finally {
          setLoadingCities(false);
        }
      };
      fetchCities();
    } else {
      setCities([]);
      setDistricts([]);
      setStreets([]);
    }
  }, [formData.country]);

  // Fetch districts when city changes using Nominatim API
  useEffect(() => {
    if (formData.city && formData.country) {
      const fetchDistricts = async () => {
        setLoadingDistricts(true);
        try {
          const query = `${formData.city}, ${formData.country}`;
          const res = await fetch(
            `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&limit=10&featuretype=settlement`
          );
          const data = await res.json();
          
          // Extract unique districts/suburbs from the results
          const districtSet = new Set();
          data.forEach(item => {
            if (item.address) {
              // Try different address components that might represent districts
              const district = item.address.suburb || 
                              item.address.district || 
                              item.address.neighbourhood || 
                              item.address.quarter ||
                              item.address.city_district;
              if (district && district !== formData.city) {
                districtSet.add(district);
              }
            }
          });
          
          const uniqueDistricts = Array.from(districtSet).slice(0, 20); // Limit to 20
          setDistricts(uniqueDistricts.length > 0 ? uniqueDistricts : [
            `${formData.city} Central`,
            `${formData.city} Downtown`,
            `${formData.city} Old Town`
          ]);
          
          setFormData((prev) => ({ ...prev, district: "", street: "" }));
          setStreets([]);
        } catch (err) {
          console.error("Failed to fetch districts", err);
          // Fallback to generic districts
          setDistricts([
            `${formData.city} Central`,
            `${formData.city} Downtown`,
            `${formData.city} Old Town`
          ]);
        } finally {
          setLoadingDistricts(false);
        }
      };
      fetchDistricts();
    } else {
      setDistricts([]);
      setStreets([]);
    }
  }, [formData.city, formData.country]);

  // Fetch streets when district changes using Nominatim API
  useEffect(() => {
    if (formData.district && formData.city && formData.country) {
      const fetchStreets = async () => {
        setLoadingStreets(true);
        try {
          const query = `${formData.district}, ${formData.city}, ${formData.country}`;
          const res = await fetch(
            `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&limit=20&featuretype=way&class=highway`
          );
          const data = await res.json();
          
          // Extract unique street names
          const streetSet = new Set();
          data.forEach(item => {
            if (item.address && item.address.road) {
              streetSet.add(item.address.road);
            }
            // Also check display_name for street patterns
            if (item.display_name) {
              const parts = item.display_name.split(',');
              if (parts.length > 0) {
                const streetPart = parts[0].trim();
                if (streetPart && (streetPart.includes('Street') || streetPart.includes('Avenue') || 
                    streetPart.includes('Road') || streetPart.includes('Boulevard') || 
                    streetPart.includes('Lane') || streetPart.includes('Way'))) {
                  streetSet.add(streetPart);
                }
              }
            }
          });
          
          const uniqueStreets = Array.from(streetSet).slice(0, 15); // Limit to 15
          setStreets(uniqueStreets.length > 0 ? uniqueStreets : [
            `${formData.district} Main Street`,
            `${formData.district} Central Avenue`,
            `${formData.district} Park Road`
          ]);
          
          setFormData((prev) => ({ ...prev, street: "" }));
        } catch (err) {
          console.error("Failed to fetch streets", err);
          // Fallback to generic streets
          setStreets([
            `${formData.district} Main Street`,
            `${formData.district} Central Avenue`,
            `${formData.district} Park Road`
          ]);
        } finally {
          setLoadingStreets(false);
        }
      };
      fetchStreets();
    } else {
      setStreets([]);
    }
  }, [formData.district, formData.city, formData.country]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  // Tag search logic (unchanged)
  const searchWikidata = async () => {
    if (!tagQuery.trim()) return;
    setSearchLoading(true);
    try {
      const response = await api.get(`/tags/search_wikidata/?query=${encodeURIComponent(tagQuery)}`, {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      setSearchResults(response.data);
    } catch (err) {
      setError(t('errors.failedToSearchWikidata'));
      console.error('Error searching Wikidata:', err);
    } finally {
      setSearchLoading(false);
    }
  };

  const handleTagSelect = (entity) => {
    if (!selectedTags.find((tag) => tag.wikidata_id === entity.id)) {
      setSelectedTags([
        ...selectedTags,
        {
          name: entity.label,
          wikidata_id: entity.id,
          wikidata_label: entity.label,
        },
      ]);
    }
    setTagQuery("");
    setSearchResults([]);
  };

  const handleTagRemove = (tagIndex) => {
    setSelectedTags(selectedTags.filter((_, index) => index !== tagIndex));
  };

  // 🧩 Handle form submit
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const tagResponses = await Promise.all(
        selectedTags.map((tag) =>
          api
            .post(
              "/tags/",
              {
                name: tag.name,
                wikidata_id: tag.wikidata_id,
                wikidata_label: tag.wikidata_label,
              },
              {
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${localStorage.getItem("token")}`,
                },
              }
            )
            .catch((err) => {
              if (err.response?.status === 409) {
                return { data: { name: tag.name } };
              }
              throw err;
            })
        )
      );

      const tagNames = tagResponses.map((res) => res.data.name);

      const response = await api.post(
        "/spaces/",
        {
          title: formData.title,
          description: formData.description,
          country: formData.country,
          city: formData.city,
          district: formData.district || null,
          street: formData.street || null,
          tags: tagNames,
        },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      if (window.refreshCurrentUser) {
        window.refreshCurrentUser();
      }

      navigate(`/spaces/${response.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || t('errors.failedToCreateSpace'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-space-container">
      <h2>{t('space.createNewSpace')}</h2>
      {error && <div className="error-message">{error}</div>}

      <form onSubmit={handleSubmit}>
        {/* Title */}
        <div className="form-group">
          <label htmlFor="title">{t('space.title')}:</label>
          <input
            type="text"
            id="title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            required
          />
        </div>

        {/* Description */}
        <div className="form-group">
          <label htmlFor="description">{t('space.description')}:</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            required
            rows="5"
          />
        </div>

        {/* Location dropdowns */}
        <div className="form-group">
          <label>Country:</label>
          <select name="country" value={formData.country} onChange={handleChange} required>
            <option value="">-- Select Country --</option>
            {countries.map((c) => (
              <option key={c.name} value={c.name}>
                {c.name}
              </option>
            ))}
          </select>
        </div>

        {formData.country && (
          <div className="form-group">
            <label>City:</label>
            <select name="city" value={formData.city} onChange={handleChange} required disabled={loadingCities}>
              <option value="">{loadingCities ? "Loading cities..." : "-- Select City --"}</option>
              {cities.map((city) => (
                <option key={city} value={city}>
                  {city}
                </option>
              ))}
            </select>
            {loadingCities && <small className="loading-text">Fetching cities for {formData.country}...</small>}
          </div>
        )}

        {formData.city && (
          <div className="form-group">
            <label>District (optional):</label>
            <select name="district" value={formData.district} onChange={handleChange} disabled={loadingDistricts}>
              <option value="">{loadingDistricts ? "Loading districts..." : "-- Select District --"}</option>
              {districts.map((d) => (
                <option key={d} value={d}>
                  {d}
                </option>
              ))}
            </select>
            {loadingDistricts && <small className="loading-text">Fetching districts for {formData.city}...</small>}
          </div>
        )}

        {formData.district && (
          <div className="form-group">
            <label>Street (optional):</label>
            <select name="street" value={formData.street} onChange={handleChange} disabled={loadingStreets}>
              <option value="">{loadingStreets ? "Loading streets..." : "-- Select Street --"}</option>
              {streets.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
            {loadingStreets && <small className="loading-text">Fetching streets for {formData.district}...</small>}
          </div>
        )}

        {/* Tags */}
        <div className="form-group">
          <label>{t('space.tags')}:</label>
          
          {/* Display selected tags */}
          <div className="selected-tags">
            {selectedTags.map((tag, index) => (
              <div key={index} className="selected-tag">
                <input
                  type="text"
                  value={tag.name}
                  onChange={(e) => {
                    const updated = [...selectedTags];
                    updated[index].name = e.target.value;
                    setSelectedTags(updated);
                  }}
                />
                <button type="button" onClick={() => handleTagRemove(index)}>
                  ×
                </button>
              </div>
            ))}
          </div>

          <button
            type="button"
            onClick={() => setShowTagSearch(!showTagSearch)}
            className="toggle-tag-search-btn"
          >
            {showTagSearch ? t('space.hideTagSearch') : t('space.addTagsFromWikidata')}
          </button>

          {showTagSearch && (
            <div className="tag-search-container">
              <div className="search-input-container">
                <input
                  type="text"
                  value={tagQuery}
                  onChange={(e) => setTagQuery(e.target.value)}
                  placeholder={t('space.searchTagsPlaceholder')}
                  className="tag-search-input"
                />
                <button 
                  type="button" 
                  onClick={searchWikidata}
                  disabled={searchLoading}
                  className="search-wikidata-btn"
                >
                  {searchLoading ? t('common.searching') : t('common.search')}
                </button>
              </div>
              
              {searchResults.length > 0 && (
                <div className="search-results">
                  <h4>{t('space.selectFromResults')}:</h4>
                  <ul className="wikidata-results-list">
                    {searchResults.map(entity => (
                      <li 
                        key={entity.id} 
                        onClick={() => handleTagSelect(entity)}
                        className="wikidata-result-item"
                      >
                        <strong>{entity.label}</strong>
                        {entity.description && <p className="entity-description">{entity.description}</p>}
                        <span className="entity-id">ID: {entity.id}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Submit */}
        <button type="submit" disabled={loading} className="create-space-btn">
          {loading ? t('space.creating') : t('space.createSpace')}
        </button>
      </form>
    </div>
  );
};

export default CreateSpace;
