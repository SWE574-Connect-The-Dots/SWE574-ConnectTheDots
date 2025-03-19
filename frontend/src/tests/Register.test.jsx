import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import Register from "../pages/Register";
import axios from "axios";
import { BrowserRouter } from "react-router-dom";
import { vi } from "vitest";
import "@testing-library/jest-dom";

vi.mock("axios");

describe("Register Component", () => {
  it("renders the registration form", () => {
    render(
      <BrowserRouter>
        <Register />
      </BrowserRouter>
    );

    expect(screen.getByPlaceholderText("Username")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Email")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Profession")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Date of Birth")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /register/i })).toBeInTheDocument();
  });

  it("registers a user successfully", async () => {
    axios.post.mockResolvedValue({ data: { message: "User registered successfully!" } });

    render(
      <BrowserRouter>
        <Register />
      </BrowserRouter>
    );

    // Fill the form
    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "newuser" } });
    fireEvent.change(screen.getByPlaceholderText("Email"), { target: { value: "newuser@example.com" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "securepassword" } });
    fireEvent.change(screen.getByPlaceholderText("Profession"), { target: { value: "Software Engineer" } });
    fireEvent.change(screen.getByPlaceholderText("Date of Birth"), { target: { value: "2000-01-01" } });

    fireEvent.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() => expect(screen.getByText("User registered successfully!")).toBeInTheDocument());
  });
  
  it("shows an error when the username is already taken", async () => {
    axios.post.mockRejectedValue({ response: { data: { username: ["Username is already taken."] } } });

    render(
      <BrowserRouter>
        <Register />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "existinguser" } });
    fireEvent.change(screen.getByPlaceholderText("Email"), { target: { value: "existing@example.com" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "securepassword" } });
    fireEvent.change(screen.getByPlaceholderText("Profession"), { target: { value: "Software Engineer" } });
    fireEvent.change(screen.getByPlaceholderText("Date of Birth"), { target: { value: "2000-01-01" } });

    fireEvent.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() => expect(screen.getByText("Username is already taken.")).toBeInTheDocument());
  });

  it("shows an error when the user is under 18", async () => {
    axios.post.mockRejectedValue({ response: { data: { dob: ["You must be at least 18 years old to register."] } } });

    render(
      <BrowserRouter>
        <Register />
      </BrowserRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Username"), { target: { value: "younguser" } });
    fireEvent.change(screen.getByPlaceholderText("Email"), { target: { value: "young@example.com" } });
    fireEvent.change(screen.getByPlaceholderText("Password"), { target: { value: "securepassword" } });
    fireEvent.change(screen.getByPlaceholderText("Profession"), { target: { value: "Student" } });
    fireEvent.change(screen.getByPlaceholderText("Date of Birth"), { target: { value: "2010-01-01" } });

    fireEvent.click(screen.getByRole("button", { name: /register/i }));

    await waitFor(() => expect(screen.getByText("You must be at least 18 years old to register.")).toBeInTheDocument());
  });
});
