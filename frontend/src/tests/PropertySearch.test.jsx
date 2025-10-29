import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import '@testing-library/jest-dom';
import PropertySearch from '../components/PropertySearch';
import * as useWikidataPropertySearchModule from '../hooks/useWikidataPropertySearch';

describe('PropertySearch Component', () => {
  const mockOnSelect = vi.fn();
  const mockSearch = vi.fn();
  const mockClearSearch = vi.fn();

  beforeEach(() => {
    mockOnSelect.mockClear();
    mockSearch.mockClear();
    mockClearSearch.mockClear();
    
    vi.spyOn(useWikidataPropertySearchModule, 'default').mockReturnValue({
      searchResults: [],
      loading: false,
      error: null,
      search: mockSearch,
      clearSearch: mockClearSearch,
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  test('renders with initial label', () => {
    render(<PropertySearch onSelect={mockOnSelect} initialLabel="author of" />);
    
    const input = screen.getByPlaceholderText(/search for a property or enter a custom label/i);
    expect(input).toBeInTheDocument();
    expect(input.value).toBe('author of');
  });

  test('renders with empty initial label', () => {
    render(<PropertySearch onSelect={mockOnSelect} initialLabel="" />);
    
    const input = screen.getByPlaceholderText(/search for a property or enter a custom label/i);
    expect(input.value).toBe('');
  });

  test('calls onSelect with initial value on mount', () => {
    render(<PropertySearch onSelect={mockOnSelect} initialLabel="test" />);
    
    expect(mockOnSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        id: null,
        label: 'test'
      })
    );
  });

  test('triggers search when typing more than 2 characters', async () => {
    render(<PropertySearch onSelect={mockOnSelect} initialLabel="" />);
    
    const input = screen.getByPlaceholderText(/search for a property or enter a custom label/i);
    fireEvent.change(input, { target: { value: 'aut' } });
    
    expect(mockSearch).toHaveBeenCalledWith('aut');
  });

  test('does not trigger search with 2 or fewer characters', () => {
    render(<PropertySearch onSelect={mockOnSelect} initialLabel="" />);
    
    const input = screen.getByPlaceholderText(/search for a property or enter a custom label/i);
    fireEvent.change(input, { target: { value: 'au' } });
    
    expect(mockSearch).not.toHaveBeenCalled();
    expect(mockClearSearch).toHaveBeenCalled();
  });

  test('updates onSelect with custom label when typing', () => {
    render(<PropertySearch onSelect={mockOnSelect} initialLabel="" />);
    
    const input = screen.getByPlaceholderText(/search for a property or enter a custom label/i);
    fireEvent.change(input, { target: { value: 'my custom label' } });
    
    expect(mockOnSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        id: null,
        label: 'my custom label'
      })
    );
  });

  test('displays search results when available', () => {
    const mockResults = [
      { id: 'P50', label: 'author', description: 'creator of a work' },
      { id: 'P123', label: 'publisher', description: 'organization' }
    ];

    vi.spyOn(useWikidataPropertySearchModule, 'default').mockReturnValue({
      searchResults: mockResults,
      loading: false,
      error: null,
      search: mockSearch,
      clearSearch: mockClearSearch,
    });

    render(<PropertySearch onSelect={mockOnSelect} initialLabel="" />);
    
    expect(screen.getByText('author')).toBeInTheDocument();
    expect(screen.getByText('(P50)')).toBeInTheDocument();
    expect(screen.getByText('publisher')).toBeInTheDocument();
    expect(screen.getByText('(P123)')).toBeInTheDocument();
  });

  test('selects property from dropdown and clears search', () => {
    const mockResults = [
      { id: 'P50', label: 'author', description: 'creator of a work' }
    ];

    vi.spyOn(useWikidataPropertySearchModule, 'default').mockReturnValue({
      searchResults: mockResults,
      loading: false,
      error: null,
      search: mockSearch,
      clearSearch: mockClearSearch,
    });

    render(<PropertySearch onSelect={mockOnSelect} initialLabel="" />);
    
    const propertyItem = screen.getByText('author');
    fireEvent.click(propertyItem);
    
    expect(mockClearSearch).toHaveBeenCalled();
    expect(mockOnSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        id: 'P50',
        label: 'author'
      })
    );
  });

  test('shows loading state', () => {
    vi.spyOn(useWikidataPropertySearchModule, 'default').mockReturnValue({
      searchResults: [],
      loading: true,
      error: null,
      search: mockSearch,
      clearSearch: mockClearSearch,
    });

    render(<PropertySearch onSelect={mockOnSelect} initialLabel="" />);
    
    expect(screen.getByText(/searching/i)).toBeInTheDocument();
  });

  test('handles property selection correctly', async () => {
    const mockResults = [
      { id: 'P31', label: 'instance of', description: 'that class of which this subject is a particular example' }
    ];

    vi.spyOn(useWikidataPropertySearchModule, 'default').mockReturnValue({
      searchResults: mockResults,
      loading: false,
      error: null,
      search: mockSearch,
      clearSearch: mockClearSearch,
    });

    render(<PropertySearch onSelect={mockOnSelect} initialLabel="" />);
    
    const input = screen.getByPlaceholderText(/search for a property or enter a custom label/i);
    fireEvent.change(input, { target: { value: 'instance' } });
    
    await waitFor(() => {
      expect(screen.getByText('instance of')).toBeInTheDocument();
    });
    
    fireEvent.click(screen.getByText('instance of'));
    
    expect(input.value).toBe('instance of');
    expect(mockOnSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        id: 'P31',
        label: 'instance of'
      })
    );
  });
});

