import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import api from '../axiosConfig';
import { BrowserRouter } from 'react-router-dom';
import Home from '../pages/Home';

// Mock the api module
vi.mock('../axiosConfig', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}));

// Mock localStorage
const localStorageMock = (() => {
  let store = {
    'token': 'fake-jwt-token',
    'username': 'testuser',
    'activeTab': 'trending'
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

describe('Home Component - Join Space Functionality', () => {
  const mockSetIsAuthenticated = vi.fn();
  
  const mockSpaces = [
    {
      id: 1,
      title: 'Test Space 1',
      description: 'This is test space 1',
      tags: [{ id: 1, name: 'Tag1' }],
      collaborators: ['otheruser'] // current user is not a collaborator
    },
    {
      id: 2,
      title: 'Test Space 2',
      description: 'This is test space 2',
      tags: [{ id: 2, name: 'Tag2' }],
      collaborators: ['testuser', 'otheruser'] // current user is a collaborator
    }
  ];

  beforeEach(() => {
    api.get.mockReset();
    api.post.mockReset();
    
    // Mock API response for fetching spaces
    api.get.mockResolvedValue({ data: mockSpaces });
    
    // Mock successful join/leave responses
    api.post.mockImplementation((url) => {
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
      } else {
        return Promise.resolve({ data: {} });
      }
    });
  });

  test('renders join button for spaces user is not part of', async () => {
    render(
      <BrowserRouter>
        <Home setIsAuthenticated={mockSetIsAuthenticated} />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/spaces/trending/', expect.any(Object));
    });

    // Find the first space (where user is not a collaborator)
    const joinButton = screen.getAllByText('JOIN')[0];
    expect(joinButton).toBeInTheDocument();
  });

  test('renders leave button for spaces user is part of', async () => {
    render(
      <BrowserRouter>
        <Home setIsAuthenticated={mockSetIsAuthenticated} />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/spaces/trending/', expect.any(Object));
    });

    // Find the second space (where user is a collaborator)
    const leaveButton = screen.getAllByText('LEAVE')[0];
    expect(leaveButton).toBeInTheDocument();
  });

  test('handles joining a space successfully', async () => {
    render(
      <BrowserRouter>
        <Home setIsAuthenticated={mockSetIsAuthenticated} />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/spaces/trending/', expect.any(Object));
    });

    // Mock the response for getting space details after joining
    api.get.mockImplementation((url) => {
      if (url.includes('/spaces/1')) {
        return Promise.resolve({
          data: {
            ...mockSpaces[0],
            collaborators: [...mockSpaces[0].collaborators, 'testuser']
          }
        });
      }
      return Promise.resolve({ data: mockSpaces });
    });

    // Find and click the join button
    const joinButton = screen.getAllByText('JOIN')[0];
    fireEvent.click(joinButton);

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith(
        '/spaces/1/join/',
        {},
        expect.any(Object)
      );
      expect(api.get).toHaveBeenCalledWith('/spaces/1/', expect.any(Object));
    });
  });

  test('handles leaving a space successfully', async () => {
    render(
      <BrowserRouter>
        <Home setIsAuthenticated={mockSetIsAuthenticated} />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/spaces/trending/', expect.any(Object));
    });

    // Mock the response for getting space details after leaving
    api.get.mockImplementation((url) => {
      if (url.includes('/spaces/2')) {
        return Promise.resolve({
          data: {
            ...mockSpaces[1],
            collaborators: mockSpaces[1].collaborators.filter(name => name !== 'testuser')
          }
        });
      }
      return Promise.resolve({ data: mockSpaces });
    });

    // Find and click the leave button
    const leaveButton = screen.getAllByText('LEAVE')[0];
    fireEvent.click(leaveButton);

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith(
        '/spaces/2/leave/',
        {},
        expect.any(Object)
      );
      expect(api.get).toHaveBeenCalledWith('/spaces/2/', expect.any(Object));
    });
  });
}); 