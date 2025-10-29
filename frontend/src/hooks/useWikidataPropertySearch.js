import { useState, useCallback } from "react";
import api from "../axiosConfig";
import { API_ENDPOINTS } from "../constants/config";

const useWikidataPropertySearch = () => {
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const search = useCallback(async (query) => {
    if (!query) {
      setSearchResults([]);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = await api.get(API_ENDPOINTS.WIKIDATA_PROPERTY_SEARCH, {
        params: { q: query },
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      setSearchResults(response.data);
    } catch (err) {
      setError("Failed to fetch properties from Wikidata.");
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  const clearSearch = useCallback(() => {
    setSearchResults([]);
  }, []);

  return { searchResults, loading, error, search, clearSearch };
};

export default useWikidataPropertySearch;
