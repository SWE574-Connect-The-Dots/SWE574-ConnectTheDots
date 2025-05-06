import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import api from '../axiosConfig';
import { BrowserRouter, MemoryRouter, Route, Routes } from 'react-router-dom';
import SpaceDetail from '../pages/SpaceDetails';

describe('SpaceDetail Component', () => {
  const mockSpaceData = {
    title: 'Test Space',
    description: 'This is a test space description.'
  };

  beforeEach(() => {
    api.get.mockReset();
    api.get.mockResolvedValue({ data: mockSpaceData });
    
    api.get.mockImplementation((url) => {
      if (url.includes('/snapshots/')) {
        return Promise.resolve({ data: [] });
      } else if (url.includes('/nodes/')) {
        return Promise.resolve({ data: [] });
      } else {
        return Promise.resolve({ data: mockSpaceData });
      }
    });
    
    localStorage.setItem('token', 'fake-jwt-token');
  });

  afterEach(() => {
    localStorage.removeItem('token');
  });

  test('renders correctly with navigation state', async () => {
    render(
      <MemoryRouter initialEntries={[{ pathname: '/spaces/1', state: mockSpaceData }]}>
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetail />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Test Space')).toBeInTheDocument();
    expect(screen.getByText('This is a test space description.')).toBeInTheDocument();
    
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledTimes(2);
      const calls = api.get.mock.calls;
      const spaceDetailsCalled = calls.some(call => call[0] === '/spaces/1/');
      expect(spaceDetailsCalled).toBe(false);
    });
  });

  test('fetches space details from API if no state provided', async () => {
    render(
      <BrowserRouter>
        <SpaceDetail />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/spaces/undefined/', {
        headers: {
          Authorization: 'Bearer fake-jwt-token'
        }
      });

      expect(screen.getByText('Test Space')).toBeInTheDocument();
      expect(screen.getByText('This is a test space description.')).toBeInTheDocument();
    });
  });

  test('handles API errors gracefully', async () => {
    api.get.mockRejectedValue(new Error('API Error'));

    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});

    render(
      <BrowserRouter>
        <SpaceDetail />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalled();
      expect(consoleError).toHaveBeenCalledWith(expect.any(Error));
    });

    consoleError.mockRestore();
  });
});
