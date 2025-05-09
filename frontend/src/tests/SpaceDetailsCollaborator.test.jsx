// Mock ResizeObserver
class ResizeObserverMock {
  observe() {}
  unobserve() {}
  disconnect() {}
}

global.ResizeObserver = ResizeObserverMock;

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import api from '../axiosConfig';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import SpaceDetails from '../pages/SpaceDetails';
import { API_ENDPOINTS } from '../constants/config';

// Mock API_ENDPOINTS
vi.mock('../constants/config', () => ({
  API_ENDPOINTS: {
    SPACES: '/spaces',
    NODES: (id) => `/spaces/${id}/nodes/`,
    EDGES: (id) => `/spaces/${id}/edges/`,
    SNAPSHOTS: (id) => `/spaces/${id}/snapshots/`,
  }
}));

// Mock the api module
vi.mock('../axiosConfig', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}));

// Mock the hooks
vi.mock('../hooks/useGraphData', () => ({
  default: () => ({
    nodes: [],
    edges: [],
    loading: false,
    error: null,
    fetchGraphData: vi.fn()
  })
}));

vi.mock('../hooks/useWikidataSearch', () => ({
  default: () => ({
    searchResults: [],
    loading: false,
    error: null,
    search: vi.fn(),
    fetchProperties: vi.fn()
  })
}));

// Mock localStorage
const localStorageMock = (() => {
  let store = {
    'token': 'fake-jwt-token',
    'username': 'testuser'
  };
  return {
    getItem: key => store[key],
    setItem: (key, value) => {
      store[key] = value;
    },
    removeItem: key => {
      delete store[key];
    },
    clear: () => {
      store = {};
    }
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
});

describe('SpaceDetails Component - Collaboration Functionality', () => {
  const spaceId = '1';
  
  const mockSpaceDataAsCollaborator = {
    id: 1,
    title: 'Test Space',
    description: 'Test space description',
    tags: [{ id: 1, name: 'Tag1' }],
    collaborators: ['testuser', 'otheruser'] // User is a collaborator
  };
  
  const mockSpaceDataAsNonCollaborator = {
    id: 1,
    title: 'Test Space',
    description: 'Test space description',
    tags: [{ id: 1, name: 'Tag1' }],
    collaborators: ['otheruser'] // User is not a collaborator
  };

  beforeEach(() => {
    api.get.mockReset();
    api.post.mockReset();
    
    // Mock default responses
    api.get.mockImplementation((url) => {
      console.log('Mock API called with URL:', url);
      if (url === API_ENDPOINTS.SNAPSHOTS(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.NODES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.EDGES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.SPACES + `/${spaceId}/`) {
        return Promise.resolve({ data: mockSpaceDataAsNonCollaborator });
      }
      return Promise.resolve({ data: {} });
    });
    
    // Mock successful join/leave responses
    api.post.mockImplementation((url) => {
      console.log('Mock POST called with URL:', url);
      if (url.includes('join')) {
        return Promise.resolve({
          data: {
            message: 'Successfully joined the space'
          }
        });
      } else if (url.includes('leave')) {
        return Promise.resolve({
          data: {
            message: 'Successfully left the space'
          }
        });
      }
      return Promise.resolve({ data: {} });
    });
  });

  test('renders join button when user is not a collaborator', async () => {
    render(
      <MemoryRouter initialEntries={[`/spaces/${spaceId}`]}>
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetails />} />
        </Routes>
      </MemoryRouter>
    );

    // Wait for initial API calls
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        API_ENDPOINTS.SPACES + `/${spaceId}/`,
        expect.any(Object)
      );
    });

    // Should show both join buttons
    const headerJoinButton = screen.getByTestId('header-join-space-button');
    const bottomJoinButton = screen.getByTestId('bottom-join-space-button');
    expect(headerJoinButton).toBeInTheDocument();
    expect(bottomJoinButton).toBeInTheDocument();
    
    // Should show the non-collaborator info box
    const infoText = screen.getByText('Join this space as a collaborator to add nodes and modify the graph.');
    expect(infoText).toBeInTheDocument();
    
    // Should not show collaborator-only sections
    const addNodeHeading = screen.queryByText('Add Node from Wikidata');
    expect(addNodeHeading).not.toBeInTheDocument();
  });

  test('renders leave button when user is a collaborator', async () => {
    // Override to return user as collaborator
    api.get.mockImplementation((url) => {
      console.log('Mock API called with URL:', url);
      if (url === API_ENDPOINTS.SNAPSHOTS(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.NODES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.EDGES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.SPACES + `/${spaceId}/`) {
        return Promise.resolve({ data: mockSpaceDataAsCollaborator });
      }
      return Promise.resolve({ data: {} });
    });

    render(
      <MemoryRouter initialEntries={[`/spaces/${spaceId}`]}>
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetails />} />
        </Routes>
      </MemoryRouter>
    );

    // Wait for initial API calls
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        API_ENDPOINTS.SPACES + `/${spaceId}/`,
        expect.any(Object)
      );
    });

    // Should show the leave button
    const leaveButton = screen.getByText('LEAVE SPACE');
    expect(leaveButton).toBeInTheDocument();
    
    // Should show collaborator-only sections
    const addNodeHeading = screen.getByText('Add Node from Wikidata');
    expect(addNodeHeading).toBeInTheDocument();
    
    // Should not show the non-collaborator info
    const infoText = screen.queryByText('Join this space as a collaborator to add nodes and modify the graph.');
    expect(infoText).not.toBeInTheDocument();
  });

  test('handles joining a space successfully', async () => {
    render(
      <MemoryRouter initialEntries={[`/spaces/${spaceId}`]}>
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetails />} />
        </Routes>
      </MemoryRouter>
    );

    // Wait for initial API calls
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        API_ENDPOINTS.SPACES + `/${spaceId}/`,
        expect.any(Object)
      );
    });

    // Mock the API response after joining
    api.get.mockImplementation((url) => {
      console.log('Mock API called with URL:', url);
      if (url === API_ENDPOINTS.SNAPSHOTS(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.NODES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.EDGES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.SPACES + `/${spaceId}/`) {
        return Promise.resolve({ data: mockSpaceDataAsCollaborator });
      }
      return Promise.resolve({ data: {} });
    });

    // Find and click the header join button
    const joinButton = screen.getByTestId('header-join-space-button');
    fireEvent.click(joinButton);

    // Wait for join API call and subsequent space data fetch
    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith(
        `/spaces/${spaceId}/join/`,
        {},
        expect.any(Object)
      );
      expect(api.get).toHaveBeenCalledWith(
        API_ENDPOINTS.SPACES + `/${spaceId}/`,
        expect.any(Object)
      );
    });

    // Wait for UI to update and verify final state
    await waitFor(() => {
      // Verify the leave button is present
      const leaveButton = screen.getByTestId('leave-space-button');
      expect(leaveButton).toBeInTheDocument();
      
      // Verify collaborator-only sections are present
      const addNodeHeading = screen.getByText('Add Node from Wikidata');
      expect(addNodeHeading).toBeInTheDocument();
      
      // Verify the non-collaborator info is not present
      const infoText = screen.queryByText('Join this space as a collaborator to add nodes and modify the graph.');
      expect(infoText).not.toBeInTheDocument();
    });
  });

  test('handles leaving a space successfully', async () => {
    // Start as a collaborator
    api.get.mockImplementation((url) => {
      console.log('Mock API called with URL:', url);
      if (url === API_ENDPOINTS.SNAPSHOTS(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.NODES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.EDGES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.SPACES + `/${spaceId}/`) {
        return Promise.resolve({ data: mockSpaceDataAsCollaborator });
      }
      return Promise.resolve({ data: {} });
    });

    render(
      <MemoryRouter initialEntries={[`/spaces/${spaceId}`]}>
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetails />} />
        </Routes>
      </MemoryRouter>
    );

    // Wait for initial API calls and verify initial state
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        API_ENDPOINTS.SPACES + `/${spaceId}/`,
        expect.any(Object)
      );
    });

    // Verify initial state - should show leave button
    const initialLeaveButton = screen.getByTestId('leave-space-button');
    expect(initialLeaveButton).toBeInTheDocument();

    // Mock the API response after leaving
    api.get.mockImplementation((url) => {
      console.log('Mock API called with URL:', url);
      if (url === API_ENDPOINTS.SNAPSHOTS(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.NODES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.EDGES(spaceId)) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.SPACES + `/${spaceId}/`) {
        return Promise.resolve({ data: mockSpaceDataAsNonCollaborator });
      }
      return Promise.resolve({ data: {} });
    });

    // Click the leave button
    fireEvent.click(initialLeaveButton);

    // Wait for leave API call and subsequent space data fetch
    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith(
        `/spaces/${spaceId}/leave/`,
        {},
        expect.any(Object)
      );
      expect(api.get).toHaveBeenCalledWith(
        API_ENDPOINTS.SPACES + `/${spaceId}/`,
        expect.any(Object)
      );
    });

    // Wait for UI to update and verify final state
    await waitFor(() => {
      // Verify the join button is present
      const joinButton = screen.getByTestId('header-join-space-button');
      expect(joinButton).toBeInTheDocument();
      
      // Verify the non-collaborator info is present
      const infoText = screen.getByText('Join this space as a collaborator to add nodes and modify the graph.');
      expect(infoText).toBeInTheDocument();
      
      // Verify collaborator-only sections are not present
      const addNodeHeading = screen.queryByText('Add Node from Wikidata');
      expect(addNodeHeading).not.toBeInTheDocument();
    });
  });

  test('renders collaborators list in sidebar', async () => {
    api.get.mockImplementation((url) => {
      if (url.includes('/snapshots/')) {
        return Promise.resolve({ data: [] });
      } else if (url.includes('/nodes/')) {
        return Promise.resolve({ data: [] });
      } else if (url.includes('/edges/')) {
        return Promise.resolve({ data: [] });
      } else {
        return Promise.resolve({ data: mockSpaceDataAsCollaborator });
      }
    });

    render(
      <MemoryRouter initialEntries={[`/spaces/${spaceId}`]}>
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetails />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        expect.stringContaining(`/spaces/${spaceId}`),
        expect.any(Object)
      );
    });

    // Check that the collaborator sidebar is shown
    const collaboratorsHeading = screen.getByText('Collaborators (2)');
    expect(collaboratorsHeading).toBeInTheDocument();
    
    // Click to expand the collaborators list
    fireEvent.click(collaboratorsHeading);
    
    // Check that collaborators are listed
    expect(screen.getByText('testuser')).toBeInTheDocument();
    expect(screen.getByText('otheruser')).toBeInTheDocument();
  });
}); 