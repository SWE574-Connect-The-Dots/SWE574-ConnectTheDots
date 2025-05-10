import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import { describe, it, expect, beforeEach, vi, afterEach } from "vitest";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import Profile from "../pages/Profile";
import api from "../axiosConfig";
import { formatDate } from "../utils/dateUtils";

vi.mock("../axiosConfig");

vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => vi.fn(),
  };
});

describe("Profile Component", () => {
  const mockUser = {
    user: {
      id: 1,
      username: "testuser",
      email: "test@example.com",
    },
    profession: "Software Engineer",
    bio: "This is a test bio",
    dob: "1990-07-15",
    created_at: "2025-05-08T12:58:27.408592Z",
    updated_at: "2025-05-10T09:30:15.123456Z",
    joined_spaces: [
      {
        id: 1,
        title: "Test Space",
        description: "A space for testing",
      },
      {
        id: 2,
        title: "Another Space",
        description: "Another testing space",
      },
    ],
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("displays loading state initially", () => {
    api.get.mockImplementation(
      () => new Promise((resolve) => setTimeout(resolve, 1000))
    );

    render(
      <MemoryRouter initialEntries={["/profile/testuser"]}>
        <Routes>
          <Route path="/profile/:username" element={<Profile />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText("Loading...")).toBeDefined();
  });

  it("displays user profile data when loaded successfully", async () => {
    api.get.mockResolvedValue({ data: mockUser });

    render(
      <MemoryRouter initialEntries={["/profile/testuser"]}>
        <Routes>
          <Route path="/profile/:username" element={<Profile />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(
        screen.getByText(`${mockUser.user.username}'s Profile`)
      ).toBeDefined();
    });

    expect(
      screen.getByText(`Profession: ${mockUser.profession}`)
    ).toBeDefined();
    expect(screen.getByText(`Bio: ${mockUser.bio}`)).toBeDefined();

    const formattedDOB = formatDate(mockUser.dob);
    const formattedJoinDate = formatDate(mockUser.created_at);

    expect(screen.getByText(`Date of Birth: ${formattedDOB}`)).toBeDefined();
    expect(screen.getByText(`Joined: ${formattedJoinDate}`)).toBeDefined();
  });

  it("displays error message when API call fails", async () => {
    api.get.mockRejectedValue(new Error("Failed to load profile data"));

    render(
      <MemoryRouter initialEntries={["/profile/testuser"]}>
        <Routes>
          <Route path="/profile/:username" element={<Profile />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText("Failed to load profile data")).toBeDefined();
    });
  });

  it("handles missing profile fields gracefully", async () => {
    const minimalUser = {
      user: {
        id: 1,
        username: "minimaluser",
        email: "minimal@example.com",
      },
      joined_spaces: [],
    };

    api.get.mockResolvedValue({ data: minimalUser });

    render(
      <MemoryRouter initialEntries={["/profile/minimaluser"]}>
        <Routes>
          <Route path="/profile/:username" element={<Profile />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(
        screen.getByText(`${minimalUser.user.username}'s Profile`)
      ).toBeDefined();
    });

    expect(screen.queryByText(/Profession:/)).toBeNull();
    expect(screen.queryByText(/Bio:/)).toBeNull();
    expect(screen.queryByText(/Date of Birth:/)).toBeNull();

    expect(screen.getByText("No spaces joined yet")).toBeDefined();
  });

  it("displays joined spaces correctly", async () => {
    api.get.mockResolvedValue({ data: mockUser });

    render(
      <MemoryRouter initialEntries={["/profile/testuser"]}>
        <Routes>
          <Route path="/profile/:username" element={<Profile />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(
        screen.getByText(`${mockUser.user.username}'s Profile`)
      ).toBeDefined();
    });

    expect(screen.getByText("Joined Spaces")).toBeDefined();

    mockUser.joined_spaces.forEach((space) => {
      expect(screen.getByText(space.title)).toBeDefined();
      expect(screen.getByText(space.description)).toBeDefined();
    });
  });

  it("uses formatDate utility for consistent date formatting", async () => {
    api.get.mockResolvedValue({ data: mockUser });

    render(
      <MemoryRouter initialEntries={["/profile/testuser"]}>
        <Routes>
          <Route path="/profile/:username" element={<Profile />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(
        screen.getByText(`${mockUser.user.username}'s Profile`)
      ).toBeDefined();
    });

    const formattedDOB = formatDate(mockUser.dob);
    const formattedJoinDate = formatDate(mockUser.created_at);

    expect(screen.getByText(`Date of Birth: ${formattedDOB}`)).toBeDefined();
    expect(screen.getByText(`Joined: ${formattedJoinDate}`)).toBeDefined();

    expect(formattedDOB).toMatch(/^\d{2}\.\d{2}\.\d{4}$/);
    expect(formattedJoinDate).toMatch(/^\d{2}\.\d{2}\.\d{4}$/);
  });
});
