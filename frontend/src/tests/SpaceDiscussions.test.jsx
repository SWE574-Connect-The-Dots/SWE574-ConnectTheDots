import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import SpaceDiscussions from "../components/SpaceDiscussions";
import api from "../axiosConfig";
import { API_ENDPOINTS } from "../constants/config";

// Mock the API and localStorage
vi.mock("../constants/config", () => ({
  API_ENDPOINTS: {
    SPACES: "/spaces",
    NODES: (id) => `/spaces/${id}/nodes/`,
    EDGES: (id) => `/spaces/${id}/edges/`,
    SNAPSHOTS: (id) => `/spaces/${id}/snapshots/`,
    DISCUSSIONS: (id) => `/spaces/${id}/discussions/`,
    ADD_DISCUSSION: (id) => `/spaces/${id}/discussions/add/`,
    WIKIDATA_SEARCH: "/spaces/wikidata-search/",
    WIKIDATA_PROPERTIES: (entityId) =>
      `/spaces/wikidata-entity-properties/${entityId}/`,
    SEARCH: "/search/",
  },
}));

describe("SpaceDiscussions Component", () => {
  const mockDiscussions = [
    {
      id: 1,
      text: "Test discussion 1",
      username: "user1",
      created_at: "2023-05-20T12:00:00Z",
    },
    {
      id: 2,
      text: "Test discussion 2",
      username: "user2",
      created_at: "2023-05-20T13:00:00Z",
    },
  ];

  beforeEach(() => {
    // Mock localStorage
    localStorage.setItem("token", "fake-jwt-token");
    localStorage.setItem("username", "testuser");

    // Reset mocks
    api.get.mockReset();

    // Setup default API responses
    api.get.mockResolvedValue({ data: mockDiscussions });
    api.post.mockResolvedValue({
      data: {
        id: 3,
        text: "New comment",
        username: "user1",
        created_at: "2023-05-20T14:00:00Z",
      },
    });
  });

  test("renders discussions for all users", async () => {
    render(<SpaceDiscussions spaceId="1" isCollaborator={false} />);

    // Wait for discussions to load
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        API_ENDPOINTS.DISCUSSIONS("1"),
        expect.any(Object)
      );
    });

    // Check if discussions are displayed
    expect(screen.getByText("Test discussion 1")).toBeInTheDocument();
    expect(screen.getByText("Test discussion 2")).toBeInTheDocument();
    expect(screen.getByText("user1")).toBeInTheDocument();
    expect(screen.getByText("user2")).toBeInTheDocument();
  });

  test("shows comment form for collaborators", () => {
    render(<SpaceDiscussions spaceId="1" isCollaborator={true} />);

    // Check if comment form is displayed
    expect(screen.getByPlaceholderText("Add a comment...")).toBeInTheDocument();
    expect(screen.getByText("Post Comment")).toBeInTheDocument();
  });

  test("hides comment form for non-collaborators", () => {
    render(<SpaceDiscussions spaceId="1" isCollaborator={false} />);

    // Check if comment form is not displayed
    expect(
      screen.queryByPlaceholderText("Add a comment...")
    ).not.toBeInTheDocument();
    expect(screen.queryByText("Post Comment")).not.toBeInTheDocument();

    // Check if the message for non-collaborators is displayed
    expect(
      screen.getByText("Join as a collaborator to participate in discussions")
    ).toBeInTheDocument();
  });

  test("submits new comment for collaborators", async () => {
    render(<SpaceDiscussions spaceId="1" isCollaborator={true} />);

    // Fill and submit the comment form
    const textarea = screen.getByPlaceholderText("Add a comment...");
    fireEvent.change(textarea, { target: { value: "New comment" } });

    const submitButton = screen.getByText("Post Comment");
    fireEvent.click(submitButton);

    // Check if API was called correctly
    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith(
        API_ENDPOINTS.ADD_DISCUSSION("1"),
        { text: "New comment" },
        expect.any(Object)
      );
    });

    // Check if discussions are refreshed after posting
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledTimes(2); // Initial load + refresh after posting
    });
  });
});
