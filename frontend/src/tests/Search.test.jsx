import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import Search from '../pages/Search';
import api from '../axiosConfig';
import { vi } from 'vitest';
import { API_ENDPOINTS } from '../constants/config';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => ({
      search: '?q=test',
      pathname: '/search'
    })
  };
});

Object.defineProperty(window, 'history', {
  writable: true,
  value: {
    replaceState: vi.fn()
  }
});

describe('Search Component', () => {
  beforeEach(() => {
    api.get.mockReset();
    api.get.mockResolvedValue({ 
      data: { spaces: [], users: [] } 
    });
    window.history.replaceState.mockReset();
  });

  test('renders search component with input field and buttons', () => {
    render(
      <BrowserRouter>
        <Search />
      </BrowserRouter>
    );

    expect(screen.getByPlaceholderText('Search spaces or users...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /search/i })).toBeInTheDocument();
    
    expect(screen.getByText('Spaces (0)')).toBeInTheDocument();
    expect(screen.getByText('Users (0)')).toBeInTheDocument();
  });

  test('loads search results on initial render with query parameter', async () => {
    const mockSpaces = [
      { 
        id: 1, 
        title: 'Test Space', 
        description: 'Test Description',
        creator_username: 'testuser',
        created_at: '2023-01-01T00:00:00Z',
        tags: [{ id: 1, name: 'test-tag' }]
      }
    ];
    
    const mockUsers = [
      { id: 1, username: 'testuser', profession: 'Developer' }
    ];

    api.get.mockResolvedValueOnce({ 
      data: { 
        spaces: mockSpaces, 
        users: mockUsers 
      } 
    });

    render(
      <BrowserRouter>
        <Search />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(expect.any(String), expect.objectContaining({
        params: { q: 'test' }
      }));
    });

    await waitFor(() => {
      const titleElement = screen.getByText(/Test Space/i);
      expect(titleElement).toBeInTheDocument();
    });
    expect(screen.getByText(/Test Description/i)).toBeInTheDocument();
    
    fireEvent.click(screen.getByRole('button', { name: /users/i }));
    expect(screen.getByText(/testuser/i)).toBeInTheDocument();
    expect(screen.getByText(/Profession: Developer/i)).toBeInTheDocument();
  });

  test('performs a search when Search button is clicked', async () => {
    api.get.mockResolvedValueOnce({ 
      data: { spaces: [], users: [] } 
    });
    
    const mockResults = { 
      spaces: [{ 
        id: 2, 
        title: 'React Space', 
        description: 'Learning React',
        creator_username: 'reactdev',
        created_at: '2023-01-01T00:00:00Z',
        tags: []
      }], 
      users: [] 
    };
    api.get.mockResolvedValueOnce({ data: mockResults });

    const { container } = render(
      <BrowserRouter>
        <Search />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledTimes(1);
    });

    const searchInput = screen.getByPlaceholderText('Search spaces or users...');
    fireEvent.change(searchInput, { target: { value: 'react' } });
    fireEvent.click(screen.getByRole('button', { name: /search/i }));

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledTimes(2);
      expect(api.get).toHaveBeenLastCalledWith(API_ENDPOINTS.SEARCH, expect.objectContaining({
        params: { q: 'react' }
      }));
    });

    await waitFor(() => {
      expect(container.textContent).toContain('React Space');
      expect(container.textContent).toContain('Learning React');
    });
  });

  test('performs a search when Enter key is pressed', async () => {
    api.get.mockResolvedValueOnce({ 
      data: { spaces: [], users: [] } 
    });

    const mockResults = { 
      spaces: [], 
      users: [{ id: 3, username: 'djangodev', profession: 'Backend Developer' }] 
    };
    api.get.mockResolvedValueOnce({ data: mockResults });

    const { container } = render(
      <BrowserRouter>
        <Search />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledTimes(1);
    });

    const searchInput = screen.getByPlaceholderText('Search spaces or users...');
    fireEvent.change(searchInput, { target: { value: 'django' } });
    fireEvent.keyDown(searchInput, { key: 'Enter', code: 'Enter' });

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledTimes(2);
      expect(api.get).toHaveBeenLastCalledWith(API_ENDPOINTS.SEARCH, expect.objectContaining({
        params: { q: 'django' }
      }));
    });

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /users/i })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: /users/i }));

    await waitFor(() => {
      expect(container.textContent).toContain('djangodev');
      expect(container.textContent).toContain('Profession: Backend Developer');
    });
  });

  test('shows "No spaces found" message when no space results', async () => {
    render(
      <BrowserRouter>
        <Search />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/No spaces found matching your search/i)).toBeInTheDocument();
    });
  });

  test('clears search input when X button is clicked', async () => {
    render(
      <BrowserRouter>
        <Search />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText('Search spaces or users...');
    fireEvent.change(searchInput, { target: { value: 'test query' } });
    
    const clearButton = screen.getByRole('button', { name: '×' });
    fireEvent.click(clearButton);
    
    expect(searchInput.value).toBe('');
  });

  test('switches between Spaces and Users tabs', async () => {
    const mockData = { 
      spaces: [{ 
        id: 1, 
        title: 'Test Space', 
        description: 'Test Description',
        creator_username: 'user1',
        created_at: '2023-01-01T00:00:00Z',
        tags: []
      }], 
      users: [{ id: 1, username: 'testuser', profession: 'Developer' }] 
    };
    
    api.get.mockResolvedValueOnce({ data: mockData });

    render(
      <BrowserRouter>
        <Search />
      </BrowserRouter>
    );

    await waitFor(() => {
      const titleElement = screen.getByText(/Test Space/i);
      expect(titleElement).toBeInTheDocument();
    });

    expect(screen.getByText(/Test Space/i)).toBeInTheDocument();
    expect(screen.queryByText(/testuser/i)).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: /users/i }));
    
    expect(screen.queryByText(/Test Space/i)).not.toBeInTheDocument();
    expect(screen.getByText(/testuser/i)).toBeInTheDocument();
    expect(screen.getByText(/Profession: Developer/i)).toBeInTheDocument();
  });
}); 