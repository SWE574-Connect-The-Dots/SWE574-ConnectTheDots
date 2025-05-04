import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import api from '../axiosConfig';
import { BrowserRouter } from 'react-router-dom';
import CreateSpace from '../pages/CreateSpace';

const mockedNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockedNavigate,
  };
});

describe('CreateSpace Component', () => {
  beforeEach(() => {
    api.post.mockReset();
    api.get.mockReset();
    mockedNavigate.mockClear();
  });

  test('renders form fields correctly', () => {
    render(
      <BrowserRouter>
        <CreateSpace />
      </BrowserRouter>
    );

    expect(screen.getByLabelText(/Title:/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Description:/i)).toBeInTheDocument();
    expect(screen.getByText(/Tags:/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Create Space/i })).toBeInTheDocument();
  });

  test('successfully creates space and navigates to detail page', async () => {
    api.post.mockResolvedValue({
      data: {
        id: 1,
        title: 'New Space',
        description: 'A test description',
      }
    });

    render(
      <BrowserRouter>
        <CreateSpace />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByLabelText(/Title:/i), { target: { value: 'New Space' } });
    fireEvent.change(screen.getByLabelText(/Description:/i), { target: { value: 'A test description' } });
    
    fireEvent.click(screen.getByRole('button', { name: /Create Space/i }));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith(
        '/spaces/',
        {
          title: 'New Space',
          description: 'A test description',
          tags: []
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer fake-jwt-token'
          }
        }
      );

      expect(mockedNavigate).toHaveBeenCalledWith('/spaces/1');
    });
  });

  test('displays error message if API call fails', async () => {
    api.post.mockRejectedValue({
      response: {
        data: { message: 'Creation failed' }
      }
    });

    render(
      <BrowserRouter>
        <CreateSpace />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByLabelText(/Title:/i), { target: { value: 'Failing Space' } });
    fireEvent.change(screen.getByLabelText(/Description:/i), { target: { value: 'Failing description' } });

    fireEvent.click(screen.getByRole('button', { name: /Create Space/i }));

    await waitFor(() => {
      expect(screen.getByText('Creation failed')).toBeInTheDocument();
      expect(mockedNavigate).not.toHaveBeenCalled();
    });
  });

  test('handles loading state correctly', async () => {
    api.post.mockResolvedValue({
      data: { id: 1 }
    });

    render(
      <BrowserRouter>
        <CreateSpace />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByLabelText(/Title:/i), { target: { value: 'Loading Space' } });
    fireEvent.change(screen.getByLabelText(/Description:/i), { target: { value: 'Loading description' } });

    const button = screen.getByRole('button', { name: /Create Space/i });
    fireEvent.click(button);

    expect(button).toBeDisabled();
    expect(button).toHaveTextContent(/Creating.../i);

    await waitFor(() => expect(button).not.toBeDisabled());
  });
});
