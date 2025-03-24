import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import axios from 'axios';
import { BrowserRouter, MemoryRouter, Route, Routes } from 'react-router-dom';
import SpaceDetail from '../pages/SpaceDetails';

vi.mock('axios');

describe('SpaceDetail Component', () => {
  const mockSpaceData = {
    title: 'Test Space',
    description: 'This is a test space description.'
  };

  beforeEach(() => {
    axios.get.mockClear();
  });

  test('renders correctly with navigation state', () => {
    render(
      <MemoryRouter initialEntries={[{ pathname: '/spaces/1', state: mockSpaceData }]}>
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetail />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText('Test Space')).toBeInTheDocument();
    expect(screen.getByText('This is a test space description.')).toBeInTheDocument();
    expect(axios.get).not.toHaveBeenCalled();
  });

  test('fetches space details from API if no state provided', async () => {
    axios.get.mockResolvedValue({
      data: mockSpaceData
    });

    render(
      <BrowserRouter>
        <SpaceDetail />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(axios.get).toHaveBeenCalledWith('http://localhost:8000/api/spaces/undefined/', {
        headers: {
          Authorization: expect.stringContaining('Bearer')
        }
      });

      expect(screen.getByText('Test Space')).toBeInTheDocument();
      expect(screen.getByText('This is a test space description.')).toBeInTheDocument();
    });
  });

  test('handles API errors gracefully', async () => {
    axios.get.mockRejectedValue(new Error('API Error'));

    // Suppress console.error for this test
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});

    render(
      <BrowserRouter>
        <SpaceDetail />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(axios.get).toHaveBeenCalled();
      expect(consoleError).toHaveBeenCalledWith(expect.any(Error));
    });

    consoleError.mockRestore();
  });
});
