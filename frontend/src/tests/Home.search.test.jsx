import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import Home from '../pages/Home';
import api from '../axiosConfig';
import { vi } from 'vitest';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => ({ pathname: '/' })
  };
});

describe('Home Search Functionality', () => {
  beforeEach(() => {
    mockNavigate.mockReset();
    api.get.mockReset();
    api.get.mockResolvedValue({ data: [] });
  });

  test('navigates to search page when search button is clicked', () => {
    render(
      <BrowserRouter>
        <Home setIsAuthenticated={vi.fn()} />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText('Search spaces or users...');
    const searchButton = screen.getByRole('button', { name: /search/i });

    fireEvent.change(searchInput, { target: { value: 'test query' } });
    fireEvent.click(searchButton);
    expect(mockNavigate).toHaveBeenCalledWith('/search?q=test%20query');
  });

  test('navigates to search page when Enter key is pressed', () => {
    render(
      <BrowserRouter>
        <Home setIsAuthenticated={vi.fn()} />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText('Search spaces or users...');

    fireEvent.change(searchInput, { target: { value: 'react tutorial' } });
    fireEvent.keyDown(searchInput, { key: 'Enter', code: 'Enter' });

    expect(mockNavigate).toHaveBeenCalledWith('/search?q=react%20tutorial');
  });

  test('does not navigate when search input is empty', () => {
    render(
      <BrowserRouter>
        <Home setIsAuthenticated={vi.fn()} />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText('Search spaces or users...');
    const searchButton = screen.getByRole('button', { name: /search/i });

    fireEvent.change(searchInput, { target: { value: '' } });
    fireEvent.click(searchButton);

    expect(mockNavigate).not.toHaveBeenCalled();
    
    fireEvent.keyDown(searchInput, { key: 'Enter', code: 'Enter' });
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  test('clears search input when X button is clicked', () => {
    render(
      <BrowserRouter>
        <Home setIsAuthenticated={vi.fn()} />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText('Search spaces or users...');
    
    fireEvent.change(searchInput, { target: { value: 'test query' } });
    
    const clearButton = screen.getByRole('button', { name: '×' });
    fireEvent.click(clearButton);
    
    expect(searchInput.value).toBe('');
  });

  test('trims whitespace when searching', () => {
    render(
      <BrowserRouter>
        <Home setIsAuthenticated={vi.fn()} />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText('Search spaces or users...');
    const searchButton = screen.getByRole('button', { name: /search/i });

    fireEvent.change(searchInput, { target: { value: '  django   ' } });
    
    fireEvent.click(searchButton);

    expect(mockNavigate).toHaveBeenCalledWith('/search?q=django');
  });
}); 