import { useState, useEffect, useRef } from "react";
import PropTypes from "prop-types";
import { useTranslation } from "../contexts/TranslationContext";
import useWikidataPropertySearch from "../hooks/useWikidataPropertySearch";
import useClickOutside from "../hooks/useClickOutside";
import "./PropertySearch.css";

const PropertySearch = ({ onSelect, initialLabel }) => {
  const { t } = useTranslation();
  const [searchTerm, setSearchTerm] = useState(initialLabel || "");
  const [selectedProperty, setSelectedProperty] = useState({
    id: null,
    label: initialLabel || ""
  });
  const { searchResults, loading, search, clearSearch } =
    useWikidataPropertySearch();
  const isSelectingEdgeRef = useRef(false);
  const hasInteractedRef = useRef(false);
  const containerRef = useClickOutside(() => {
    if (searchResults.length > 0) {
      clearSearch();
    }
  });

  useEffect(() => {
    if (selectedProperty) {
      onSelect(selectedProperty);
    }
  }, [selectedProperty, onSelect]);

  useEffect(() => {
    if (isSelectingEdgeRef.current) {
      isSelectingEdgeRef.current = false;
      return;
    }

    if (!hasInteractedRef.current) {
      return;
    }

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
    hasInteractedRef.current = true;
    setSearchTerm(query);
    setSelectedProperty({ id: null, label: query });
  };

  const handleSelectProperty = (property) => {
    isSelectingEdgeRef.current = true;
    setSearchTerm(property.label);
    setSelectedProperty(property);
    clearSearch();
  };

  return (
    <div ref={containerRef} className="property-search-container">
      <input
        type="text"
        value={searchTerm}
        onChange={handleSearch}
        placeholder={t("property.searchPlaceholder")}
        className="property-search-input"
      />
      {loading && <div className="property-search-loading">{t("property.searching")}</div>}
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
