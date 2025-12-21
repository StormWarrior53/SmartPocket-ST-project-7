// tests/childProfile.test.jsx
import React from "react";
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";

import ChildProfile from "../src/components/profile/child-profile/ChildProfile.jsx";

// ---- Mock useUser() ----
let mockIsAuthenticated = true;
let mockLoading = false;

vi.mock("../src/context/UserContext.jsx", () => {
  return {
    useUser: () => ({
      user: { email: "parent@test.com" },
      isAuthenticated: mockIsAuthenticated,
      loading: mockLoading,
      authFetch: mockAuthFetch,
    }),
  };
});

// authFetch mock (mutable)
let mockAuthFetch = vi.fn();

function mockResJson(data, ok = true, status = 200) {
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

describe("ChildProfile", () => {
  beforeEach(() => {
    mockIsAuthenticated = true;
    mockLoading = false;

    mockAuthFetch = vi.fn(async (url) => {
      const u = String(url);

      if (u.includes("/children/me/inventory")) {
        return mockResJson([
          { id: 1, name: "Pencil", price: 10 },
          { id: 2, name: "Notebook", price: 20 },
        ]);
      }

      if (u.includes("/children/me")) {
        return mockResJson({
          name: "TestKid",
          age: 10,
          xp: 42,
          pocketMoney: 15,
          allowanceMoney: 1000,
          parentEmail: "parent@test.com",
        });
      }

      return mockResJson({}, true);
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it("shows loading when context loading=true", () => {
    mockLoading = true;
    render(<ChildProfile />);

    expect(screen.getByText(/Loading\.\.\./i)).toBeInTheDocument();
  });

  it("shows message when not authenticated", () => {
    mockIsAuthenticated = false;
    render(<ChildProfile />);

    expect(
      screen.getByText(/You must be logged in to view your profile\./i)
    ).toBeInTheDocument();
  });

  it("renders profile data + inventory on success", async () => {
    render(<ChildProfile />);

    // while loading, header shows "…" (we don't need to assert it строго)
    // wait until profile name appears
    expect(await screen.findByRole("heading", { name: "TestKid" })).toBeInTheDocument();

    // profile info
    expect(screen.getByText(/Age:\s*10/i)).toBeInTheDocument();
    expect(screen.getByText(/Parent:\s*parent@test\.com/i)).toBeInTheDocument();

    // stat cards
    expect(screen.getByText("XP")).toBeInTheDocument();
    expect(screen.getByText("Pocket Money")).toBeInTheDocument();
    expect(screen.getByText("Allowance Money")).toBeInTheDocument();

    // inventory title
    expect(screen.getByRole("heading", { name: /My Inventory/i })).toBeInTheDocument();

    // inventory items
    expect(await screen.findByText("Pencil")).toBeInTheDocument();
    expect(screen.getByText("Notebook")).toBeInTheDocument();

    // called both endpoints
    await waitFor(() => {
      const calls = mockAuthFetch.mock.calls.map(([u]) => String(u));
      expect(calls.some((u) => u.includes("/children/me"))).toBe(true);
      expect(calls.some((u) => u.includes("/children/me/inventory"))).toBe(true);
    });
  });

  it("shows error state when inventory fetch fails", async () => {
    mockAuthFetch = vi.fn(async (url) => {
      const u = String(url);

      if (u.includes("/children/me/inventory")) {
        return mockResJson({ message: "Failed to load inventory" }, false, 500);
      }

      if (u.includes("/children/me")) {
        return mockResJson({
          name: "TestKid",
          age: 10,
          xp: 42,
          pocketMoney: 15,
          allowanceMoney: 1000,
          parentEmail: "parent@test.com",
        });
      }

      return mockResJson({}, true);
    });

    render(<ChildProfile />);

    // profile still renders
    expect(await screen.findByRole("heading", { name: "TestKid" })).toBeInTheDocument();

    // inventory error shown
    expect(await screen.findByText(/Failed to load inventory/i)).toBeInTheDocument();
  });

  it("shows error state when profile fetch fails but still shows Parent from context", async () => {
    mockAuthFetch = vi.fn(async (url) => {
      const u = String(url);

      if (u.includes("/children/me/inventory")) {
        return mockResJson([], true);
      }

      if (u.includes("/children/me")) {
        return mockResJson({ message: "Failed to load child info" }, false, 500);
      }

      return mockResJson({}, true);
    });

    render(<ChildProfile />);

    // should show parent fallback from context even if child info failed
    expect(await screen.findByText(/Parent:\s*parent@test\.com/i)).toBeInTheDocument();

    // error shown somewhere (inventory part uses same error state too)
    expect(await screen.findByText(/Failed to load child info/i)).toBeInTheDocument();
  });
});
