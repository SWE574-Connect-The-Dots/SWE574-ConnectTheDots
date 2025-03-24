import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { vi } from 'vitest';
import axios from 'axios';
import { BrowserRouter } from 'react-router-dom';
import CreateSpace from '../pages/CreateSpace';

vi.mock('axios');

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
    axios.post.mockClear();
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
    expect(screen.getByLabelText(/Tags \(comma separated\):/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Create Space/i })).toBeInTheDocument();
  });

  test('successfully creates space and navigates to detail page', async () => {
    axios.post.mockResolvedValue({
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
    fireEvent.change(screen.getByLabelText(/Tags/i), { target: { value: 'tag1, tag2' } });

    fireEvent.click(screen.getByRole('button', { name: /Create Space/i }));

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        'http://localhost:8000/api/spaces/',
        {
          title: 'New Space',
          description: 'A test description',
          tags: ['tag1', 'tag2']
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': expect.stringContaining('Bearer')
          }
        }
      );

      expect(mockedNavigate).toHaveBeenCalledWith('/spaces/1');
    });
  });

  test('displays error message if API call fails', async () => {
    axios.post.mockRejectedValue({
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
    axios.post.mockResolvedValue({
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
