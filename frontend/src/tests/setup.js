import { vi } from 'vitest';
import '@testing-library/jest-dom';

if (!window.env) {
  window.env = {};
}
window.env.REACT_APP_API_URL = 'http://localhost:8000';

Object.defineProperty(window, 'localStorage', {
  value: {
    getItem: vi.fn(() => 'fake-jwt-token'),
    setItem: vi.fn(),
    removeItem: vi.fn()
  },
  writable: true
});

delete window.location;
window.location = {
  ...window.location,
  href: '',
  pathname: '/',
  search: '',
  hash: '',
  replace: vi.fn(),
  reload: vi.fn()
};

vi.mock('../axiosConfig', () => {
  const mockAxios = {
    get: vi.fn().mockResolvedValue({ data: {} }),
    post: vi.fn().mockResolvedValue({ data: {} }),
    put: vi.fn().mockResolvedValue({ data: {} }),
    delete: vi.fn().mockResolvedValue({ data: {} }),
    interceptors: {
      request: {
        use: vi.fn()
      },
      response: {
        use: vi.fn()
      }
    }
  };
  return {
    default: mockAxios
  };
}); 