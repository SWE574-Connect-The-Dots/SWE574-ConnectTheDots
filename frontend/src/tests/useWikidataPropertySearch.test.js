import { renderHook, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import useWikidataPropertySearch from '../hooks/useWikidataPropertySearch';
import api from '../axiosConfig';

vi.mock('../axiosConfig');

describe('useWikidataPropertySearch Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('token', 'fake-jwt-token');
  });

  afterEach(() => {
    localStorage.removeItem('token');
  });

  test('initializes with empty state', () => {
    const { result } = renderHook(() => useWikidataPropertySearch());
    
    expect(result.current.searchResults).toEqual([]);
    expect(result.current.loading).toBe(false);
    expect(result.current.error).toBeNull();
    expect(typeof result.current.search).toBe('function');
    expect(typeof result.current.clearSearch).toBe('function');
  });

  test('searches for properties successfully', async () => {
    const mockData = [
      { id: 'P50', label: 'author', description: 'creator of a work', url: 'http://wikidata.org/entity/P50' },
      { id: 'P123', label: 'publisher', description: 'organization', url: 'http://wikidata.org/entity/P123' }
    ];

    api.get.mockResolvedValueOnce({ data: mockData });

    const { result } = renderHook(() => useWikidataPropertySearch());
    
    result.current.search('author');
    
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
      expect(result.current.searchResults).toEqual(mockData);
    });
    
    expect(result.current.error).toBeNull();
    expect(api.get).toHaveBeenCalledWith(
      '/spaces/wikidata-property-search',
      expect.objectContaining({
        params: { q: 'author' },
        headers: { Authorization: 'Bearer fake-jwt-token' }
      })
    );
  });

  test('handles empty query', async () => {
    const { result } = renderHook(() => useWikidataPropertySearch());
    
    result.current.search('');
    
    await waitFor(() => {
      expect(result.current.searchResults).toEqual([]);
      expect(result.current.loading).toBe(false);
    });
    
    expect(api.get).not.toHaveBeenCalled();
  });

  test('handles search error', async () => {
    api.get.mockRejectedValueOnce(new Error('Network error'));

    const { result } = renderHook(() => useWikidataPropertySearch());
    
    result.current.search('author');
    
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });
    
    expect(result.current.error).toBe('Failed to fetch properties from Wikidata.');
    expect(result.current.searchResults).toEqual([]);
  });

  test('clears search results', async () => {
    const mockData = [
      { id: 'P50', label: 'author', description: 'creator of a work', url: 'http://wikidata.org/entity/P50' }
    ];

    api.get.mockResolvedValueOnce({ data: mockData });

    const { result } = renderHook(() => useWikidataPropertySearch());
    
    result.current.search('author');
    
    await waitFor(() => {
      expect(result.current.searchResults).toEqual(mockData);
    });
    
    result.current.clearSearch();
    
    await waitFor(() => {
      expect(result.current.searchResults).toEqual([]);
    });
  });

  test('handles null or undefined query gracefully', async () => {
    const { result } = renderHook(() => useWikidataPropertySearch());
    
    result.current.search(null);
    
    await waitFor(() => {
      expect(result.current.searchResults).toEqual([]);
    });
    
    expect(api.get).not.toHaveBeenCalled();
  });

  test('subsequent searches replace previous results', async () => {
    const mockData1 = [
      { id: 'P50', label: 'author', description: 'creator', url: 'http://wikidata.org/entity/P50' }
    ];
    const mockData2 = [
      { id: 'P123', label: 'publisher', description: 'organization', url: 'http://wikidata.org/entity/P123' }
    ];

    api.get.mockResolvedValueOnce({ data: mockData1 });

    const { result } = renderHook(() => useWikidataPropertySearch());
    
    result.current.search('author');
    
    await waitFor(() => {
      expect(result.current.searchResults).toEqual(mockData1);
    });
    
    api.get.mockResolvedValueOnce({ data: mockData2 });
    
    result.current.search('publisher');
    
    await waitFor(() => {
      expect(result.current.searchResults).toEqual(mockData2);
    });
  });
});

