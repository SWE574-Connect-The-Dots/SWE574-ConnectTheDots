import { useState, useEffect } from "react";
import PropTypes from "prop-types";
import useWikidataPropertySearch from "../hooks/useWikidataPropertySearch";
import "./PropertySearch.css";

const PropertySearch = ({ onSelect, initialLabel }) => {
  const [searchTerm, setSearchTerm] = useState(initialLabel || "");
  const [selectedProperty, setSelectedProperty] = useState({
    id: null,
    label: initialLabel || ""
  });
  const { searchResults, loading, search, clearSearch } =
    useWikidataPropertySearch();

  useEffect(() => {
    if (selectedProperty) {
      onSelect(selectedProperty);
    }
  }, [selectedProperty, onSelect]);

  useEffect(() => {
    const debounceTimer = setTimeout(() => {
      if (searchTerm.length > 2) {
        search(searchTerm);
      } else {
        clearSearch();
      }
    }, 500);

    return () => clearTimeout(debounceTimer);
  }, [searchTerm, search, clearSearch]);

  const handleSearch = (e) => {
    const query = e.target.value;
    setSearchTerm(query);
    setSelectedProperty({ id: null, label: query });
  };

  const handleSelectProperty = (property) => {
    setSearchTerm(property.label);
    setSelectedProperty(property);
    clearSearch();
  };

  return (
    <div className="property-search-container">
      <input
        type="text"
        value={searchTerm}
        onChange={handleSearch}
        placeholder="Search for a property or enter a custom label..."
        className="property-search-input"
      />
      {loading && <div className="property-search-loading">Searching...</div>}
      {searchResults.length > 0 && (
        <ul className="property-search-results">
          {searchResults.map((prop) => (
            <li
              key={prop.id}
              onClick={() => handleSelectProperty(prop)}
              className="property-search-item"
            >
              <strong>{prop.label}</strong> ({prop.id})
              <p>{prop.description}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

PropertySearch.propTypes = {
  onSelect: PropTypes.func.isRequired,
  initialLabel: PropTypes.string,
};

export default PropertySearch;
