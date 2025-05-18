import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import "@testing-library/jest-dom";
import Header from "../components/Header";
import { vi } from "vitest";

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: () => ({ pathname: "/" }),
  };
});

describe("Header Search Functionality", () => {
  const mockCurrentUser = { username: "testuser" };
  const mockSetIsAuthenticated = vi.fn();

  beforeEach(() => {
    mockNavigate.mockReset();
  });

  test("navigates to search page when search button is clicked", () => {
    render(
      <BrowserRouter>
        <Header
          isAuthenticated={true}
          currentUser={mockCurrentUser}
          setIsAuthenticated={mockSetIsAuthenticated}
        />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText("Search...");

    fireEvent.change(searchInput, { target: { value: "test query" } });
    fireEvent.submit(searchInput.closest("form"));
    expect(mockNavigate).toHaveBeenCalledWith("/search?q=test%20query");
  });

  test("navigates to search page when Enter key is pressed", () => {
    render(
      <BrowserRouter>
        <Header
          isAuthenticated={true}
          currentUser={mockCurrentUser}
          setIsAuthenticated={mockSetIsAuthenticated}
        />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText("Search...");

    fireEvent.change(searchInput, { target: { value: "react tutorial" } });
    fireEvent.submit(searchInput.closest("form"));

    expect(mockNavigate).toHaveBeenCalledWith("/search?q=react%20tutorial");
  });

  test("does not navigate when search input is empty", () => {
    render(
      <BrowserRouter>
        <Header
          isAuthenticated={true}
          currentUser={mockCurrentUser}
          setIsAuthenticated={mockSetIsAuthenticated}
        />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText("Search...");

    fireEvent.change(searchInput, { target: { value: "" } });
    fireEvent.submit(searchInput.closest("form"));

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  test("clears search input when × button is clicked", () => {
    render(
      <BrowserRouter>
        <Header
          isAuthenticated={true}
          currentUser={mockCurrentUser}
          setIsAuthenticated={mockSetIsAuthenticated}
        />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText("Search...");

    fireEvent.change(searchInput, { target: { value: "test query" } });

    const clearButton = screen.getByText("×");
    fireEvent.click(clearButton);

    expect(searchInput.value).toBe("");
  });

  test("trims whitespace when searching", () => {
    render(
      <BrowserRouter>
        <Header
          isAuthenticated={true}
          currentUser={mockCurrentUser}
          setIsAuthenticated={mockSetIsAuthenticated}
        />
      </BrowserRouter>
    );

    const searchInput = screen.getByPlaceholderText("Search...");

    fireEvent.change(searchInput, { target: { value: "  django   " } });
    fireEvent.submit(searchInput.closest("form"));

    expect(mockNavigate).toHaveBeenCalledWith("/search?q=django");
  });
});
