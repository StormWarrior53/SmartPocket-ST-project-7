// tests/leaderboard.test.jsx
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { render, screen, waitFor, within } from "@testing-library/react";
import { MemoryRouter } from "react-router";

import Leaderboard from "../src/components/leaderboard-page/Leaderboard.jsx";

// ---- mock useUser ----
let mockAuthFetch;
let mockUserLoading = false;
let mockCurrentUser = { id: 999, name: "Me" };

vi.mock("../src/context/UserContext.jsx", () => ({
  useUser: () => ({
    authFetch: mockAuthFetch,
    loading: mockUserLoading,
    user: mockCurrentUser,
  }),
}));

function mockJsonResponse(data, ok = true, status = 200) {
  return Promise.resolve({
    ok,
    status,
    headers: {
      get: (name) =>
        name?.toLowerCase() === "content-type" ? "application/json" : "",
    },
    json: async () => data,
    text: async () => JSON.stringify(data),
  });
}

function renderLeaderboard() {
  return render(
    <MemoryRouter>
      <Leaderboard />
    </MemoryRouter>
  );
}

describe("Leaderboard", () => {
  beforeEach(() => {
    mockUserLoading = false;
    mockCurrentUser = { id: 999, name: "Me" };

    mockAuthFetch = vi.fn(async (url) => {
      const u = String(url);

      if (u.includes("/children/leaderboard")) {
        return mockJsonResponse([
          { id: 1, name: "Alex", xp: 50 },
          { id: 2, name: "Mimi", xp: 120 },
          { id: 3, name: "Zero", xp: 0 },
        ]);
      }

      return mockJsonResponse({ ok: true });
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("shows loading first, then renders table with users", async () => {
    renderLeaderboard();

  
    await waitFor(() => {
      expect(screen.getByText(/Top 100 XP Leaders/i)).toBeInTheDocument();
    });

    // users appear
    expect(screen.getByText("Alex")).toBeInTheDocument();
    expect(screen.getByText("Mimi")).toBeInTheDocument();
    expect(screen.getByText("Zero")).toBeInTheDocument();

    // headers exist
    expect(screen.getByText("#")).toBeInTheDocument();
    expect(screen.getByText("User")).toBeInTheDocument();
    expect(screen.getByText("XP")).toBeInTheDocument();

    // fetch called
    expect(mockAuthFetch).toHaveBeenCalled();
    const calledLeaderboard = mockAuthFetch.mock.calls.some(([url]) =>
      String(url).includes("/children/leaderboard")
    );
    expect(calledLeaderboard).toBe(true);
  });

  it("sorts users by xp desc (Mimi should be #1)", async () => {
    renderLeaderboard();

    await screen.findByText(/Top 100 XP Leaders/i);

   
    const table = screen.getByRole("table");
    const tbody = table.querySelector("tbody");
    expect(tbody).toBeTruthy();

    const rows = within(tbody).getAllByRole("row");
    expect(rows.length).toBeGreaterThan(0);


    expect(within(rows[0]).getByText("Mimi")).toBeInTheDocument();
    expect(within(rows[0]).getByText("120")).toBeInTheDocument();
  });

  it("renders error message when API returns non-ok", async () => {
    mockAuthFetch = vi.fn(async (url) => {
      if (String(url).includes("/children/leaderboard")) {
        return mockJsonResponse({ message: "Failed to fetch children." }, false, 500);
      }
      return mockJsonResponse({ ok: true });
    });

    renderLeaderboard();

    await waitFor(() => {
      expect(screen.getByText(/Failed to fetch children/i)).toBeInTheDocument();
    });
  });

  it("renders generic error when fetch throws", async () => {
    mockAuthFetch = vi.fn(async (url) => {
      if (String(url).includes("/children/leaderboard")) {
        throw new Error("Network down");
      }
      return mockJsonResponse({ ok: true });
    });

    renderLeaderboard();

    await waitFor(() => {
      expect(
        screen.getByText(/Something went wrong while fetching children/i)
      ).toBeInTheDocument();
    });
  });

  it("highlights current user row when ids match", async () => {
    mockCurrentUser = { id: 2, name: "Me" }; // Mimi has id 2

    renderLeaderboard();

    await screen.findByText(/Top 100 XP Leaders/i);

    // Mimi row should have highlight classes
    const mimiCell = screen.getByText("Mimi");
    const row = mimiCell.closest("tr");
    expect(row).toBeTruthy();
    
    expect(row.className).toMatch(/bg-blue-100\/70/);
    expect(row.className).toMatch(/ring-1/);
  });
});