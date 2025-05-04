import { useState, useCallback } from 'react';
import api from '../axiosConfig';

const useWikidataSearch = () => {
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const search = useCallback(async (query) => {
    if (!query.trim()) {
      setSearchResults([]);
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const response = await api.get(
        `/spaces/wikidata-search/?q=${encodeURIComponent(query)}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      setSearchResults(response.data);
    } catch (err) {
      setError(err.message || 'Failed to search Wikidata');
      console.error("Error searching Wikidata:", err);
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchProperties = useCallback(async (entityId) => {
    try {
      setLoading(true);
      setError(null);

      const response = await api.get(
        `/spaces/wikidata-entity-properties/${encodeURIComponent(entityId)}/`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      return response.data;
    } catch (err) {
      setError(err.message || 'Failed to fetch entity properties');
      console.error("Error fetching entity properties:", err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    searchResults,
    loading,
    error,
    search,
    fetchProperties,
  };
};

export default useWikidataSearch; 