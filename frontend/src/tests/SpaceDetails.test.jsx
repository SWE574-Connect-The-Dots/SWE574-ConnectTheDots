import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { vi } from "vitest";
import api from "../axiosConfig";
import { BrowserRouter, MemoryRouter, Route, Routes } from "react-router-dom";
import SpaceDetail from "../pages/SpaceDetails";
import { API_ENDPOINTS } from "../constants/config";

// Mock API_ENDPOINTS
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

describe("SpaceDetail Component", () => {
  const mockSpaceData = {
    title: "Test Space",
    description: "This is a test space description.",
    collaborators: ["testuser"],
  };

  beforeEach(() => {
    api.get.mockReset();

    // Set up localStorage with the test user
    localStorage.setItem("token", "fake-jwt-token");
    localStorage.setItem("username", "testuser");

    // Mock all API endpoints
    api.get.mockImplementation((url) => {
      console.log("Mock API called with URL:", url);
      if (url === API_ENDPOINTS.SNAPSHOTS("1")) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.NODES("1")) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.SPACES + "/1/") {
        return Promise.resolve({ data: mockSpaceData });
      } else if (url === API_ENDPOINTS.DISCUSSIONS("1")) {
        return Promise.resolve({
          data: [
            {
              id: 1,
              text: "Test discussion",
              username: "testuser",
              created_at: new Date().toISOString(),
            },
          ],
        });
      }
      return Promise.resolve({ data: {} });
    });
  });

  afterEach(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
  });

  test("renders correctly with navigation state", async () => {
    const { container } = render(
      <MemoryRouter
        initialEntries={[{ pathname: "/spaces/1", state: mockSpaceData }]}
      >
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetail />} />
        </Routes>
      </MemoryRouter>
    );

    // First verify that the initial state is rendered
    expect(screen.getByText(mockSpaceData.title)).toBeInTheDocument();
    expect(screen.getByText(mockSpaceData.description)).toBeInTheDocument();

    // Then wait for the API calls to complete
    await waitFor(
      () => {
        const calls = api.get.mock.calls;
        console.log(
          "All API calls:",
          calls.map((call) => call[0])
        );

        // Log each call for debugging
        calls.forEach((call, index) => {
          console.log(`Call ${index + 1}:`, call[0]);
        });

        // Verify that all required API calls were made
        const hasSpaceCall = calls.some(
          (call) => call[0] === API_ENDPOINTS.SPACES + "/1/"
        );
        const hasSnapshotsCall = calls.some(
          (call) => call[0] === API_ENDPOINTS.SNAPSHOTS("1")
        );
        const hasNodesCall = calls.some(
          (call) => call[0] === API_ENDPOINTS.NODES("1")
        );

        console.log("Has space call:", hasSpaceCall);
        console.log("Has snapshots call:", hasSnapshotsCall);
        console.log("Has nodes call:", hasNodesCall);

        expect(hasSpaceCall).toBe(true);
        expect(hasSnapshotsCall).toBe(true);
        expect(hasNodesCall).toBe(true);
      },
      { timeout: 5000 }
    );

    // Verify that the content is still rendered after API calls
    expect(screen.getByText(mockSpaceData.title)).toBeInTheDocument();
    expect(screen.getByText(mockSpaceData.description)).toBeInTheDocument();
  });

  test("fetches space details from API if no state provided", async () => {
    // Mock the API response for space details
    api.get.mockImplementation((url) => {
      console.log("Mock API called with URL:", url);
      if (url === API_ENDPOINTS.SNAPSHOTS("1")) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.NODES("1")) {
        return Promise.resolve({ data: [] });
      } else if (url === API_ENDPOINTS.SPACES + "/1/") {
        return Promise.resolve({ data: mockSpaceData });
      } else if (url === API_ENDPOINTS.DISCUSSIONS("1")) {
        return Promise.resolve({
          data: [
            {
              id: 1,
              text: "Test discussion",
              username: "testuser",
              created_at: new Date().toISOString(),
            },
          ],
        });
      }
      return Promise.resolve({ data: {} });
    });

    render(
      <MemoryRouter initialEntries={["/spaces/1"]}>
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetail />} />
        </Routes>
      </MemoryRouter>
    );

    // Wait for the API calls to complete and content to be rendered
    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(API_ENDPOINTS.SPACES + "/1/", {
        headers: {
          Authorization: "Bearer fake-jwt-token",
        },
      });
    });

    // Now verify that the content is rendered
    expect(screen.getByText(mockSpaceData.title)).toBeInTheDocument();
    expect(screen.getByText(mockSpaceData.description)).toBeInTheDocument();
  });

  test("handles API errors gracefully", async () => {
    const mockError = new Error("API Error");
    api.get.mockRejectedValue(mockError);

    const consoleError = vi
      .spyOn(console, "error")
      .mockImplementation(() => {});

    render(
      <BrowserRouter>
        <SpaceDetail />
      </BrowserRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalled();
      expect(consoleError).toHaveBeenCalledWith(
        "Error fetching space data:",
        mockError
      );
    });

    consoleError.mockRestore();
  });

  test("renders SpaceDiscussions component", async () => {
    render(
      <MemoryRouter initialEntries={["/spaces/1"]}>
        <Routes>
          <Route path="/spaces/:id" element={<SpaceDetail />} />
        </Routes>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith(
        API_ENDPOINTS.SPACES + "/1/",
        expect.any(Object)
      );
    });

    // Check if the SpaceDiscussions component is rendered
    expect(screen.getByText("Discussions")).toBeInTheDocument();
  });
});
