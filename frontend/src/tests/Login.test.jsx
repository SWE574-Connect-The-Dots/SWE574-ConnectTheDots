import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import Login from "../pages/Login";
import axios from "axios";
import { BrowserRouter } from "react-router-dom";
import { vi } from "vitest";
import "@testing-library/jest-dom";

vi.mock("axios");

describe("Login Component", () => {
  let setIsAuthenticatedMock;

  beforeEach(() => {
    setIsAuthenticatedMock = vi.fn();
  });

  it("renders the login form", () => {
    render(
      <BrowserRouter>
        <Login setIsAuthenticated={setIsAuthenticatedMock} />
      </BrowserRouter>
    );

    expect(screen.getByPlaceholderText("Username")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /login/i })).toBeInTheDocument();
  });

  it("logs in a user successfully", async () => {
    const fakeToken = "fake-jwt-token";
    axios.post.mockResolvedValue({ data: { token: fakeToken } });

    render(
      <BrowserRouter>
        <Login setIsAuthenticated={setIsAuthenticatedMock} />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "testuser" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "testpassword" } });

    fireEvent.click(screen.getByRole("button", { name: /login/i })); // Fix here

    await waitFor(() => {
      expect(localStorage.getItem("token")).toBe(fakeToken);
      expect(setIsAuthenticatedMock).toHaveBeenCalledWith(true);
    });
  });

  it("shows error message for incorrect credentials", async () => {
    axios.post.mockRejectedValue({ response: { data: { message: "Login failed." } } });

    render(
      <BrowserRouter>
        <Login setIsAuthenticated={setIsAuthenticatedMock} />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "wronguser" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "wrongpassword" } });

    fireEvent.click(screen.getByRole("button", { name: /login/i }));

    await waitFor(() => expect(screen.getByText("Login failed.")).toBeInTheDocument());
  });
});
