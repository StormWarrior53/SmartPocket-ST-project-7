
import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { render, screen, waitFor, fireEvent, within } from "@testing-library/react";

import BudgetGame from "../src/components/budget-game/BudgetGame.jsx";
import { UserProvider } from "../src/context/UserContext.jsx";


const navigateMock = vi.fn();

vi.mock("react-router", async () => {
  const actual = await vi.importActual("react-router");
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

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

function renderBudgetGame() {
  return render(
    <UserProvider>
      <BudgetGame />
    </UserProvider>
  );
}


describe("BudgetGame", () => {
  let fetchMock;

  beforeEach(() => {
    localStorage.clear();
    navigateMock.mockClear();

    
    localStorage.setItem(
      "user",
      JSON.stringify({
        token: "test-token",
        tokenType: "Bearer",
        expiresAt: Date.now() + 60 * 60 * 1000,
      })
    );

    
    vi.spyOn(Math, "random").mockReturnValue(0.9);

    // Default fetch mock (we override per test with allowanceMoney)
    fetchMock = vi.fn(async (url, options = {}) => {
      const method = (options.method || "GET").toUpperCase();

      // GET child data
      if (String(url).includes("/children/me") && method === "GET") {
        // Default allowance; tests can override by changing this mock implementation
        return mockJsonResponse({ allowanceMoney: 1000 });
      }

      // PATCH endpoints
      if (method === "PATCH") {
        return mockJsonResponse({ ok: true });
      }

      return mockJsonResponse({ ok: true });
    });

    vi.stubGlobal("fetch", fetchMock);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("loads budget from /children/me", async () => {
    renderBudgetGame();

    // Wait until budget is shown
    await waitFor(() => {
      expect(screen.getByText("Monthly Budget Game")).toBeInTheDocument();
    });

    // Should show default allowance 1000 -> "€1000"
    expect(screen.getByText("€1000")).toBeInTheDocument();

    // Verify GET called
    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalled();
    });

    const called = fetchMock.mock.calls.some(([url, opts]) => {
      const method = (opts?.method || "GET").toUpperCase();
      return String(url).includes("/children/me") && method === "GET";
    });

    expect(called).toBe(true);
  });

  it("blocks game when allowanceMoney < 500", async () => {
    // Override GET response for this test
    fetchMock.mockImplementationOnce(async () =>
      mockJsonResponse({ allowanceMoney: 400 })
    );

    renderBudgetGame();

    // Block message appears
    expect(
      await screen.findByText(/Your starting budget is below/i)
    ).toBeInTheDocument();

    // Back button exists
    expect(
      screen.getByRole("button", { name: /Back to Games/i })
    ).toBeInTheDocument();
  });

  it("WIN: with enough savings PATCH-es allowance + xp + pocket", async () => {
    // allowance 1000 (default)
    renderBudgetGame();

    // Wait UI ready and click Done
    const doneBtn = await screen.findByRole("button", { name: /^Done$/i });
    fireEvent.click(doneBtn);

    const modal = await screen.findByTestId("modal");
    expect(modal).toBeInTheDocument();

    // IMPORTANT: match ONLY the badge "You Win" (not the sentence "You win!")
    expect(within(modal).getByText((t) => t.trim() === "You Win")).toBeInTheDocument();

    // Collect PATCH calls
    const patchCalls = fetchMock.mock.calls.filter(([, opts]) => {
      const method = (opts?.method || "GET").toUpperCase();
      return method === "PATCH";
    });

    // Should PATCH 3 times: allowance + xp + pocket
    expect(patchCalls.length).toBe(3);

    const urls = patchCalls.map(([u]) => String(u));

    expect(urls.some((u) => u.includes("/children/me/allowance"))).toBe(true);
    expect(urls.some((u) => u.includes("/children/me/xp"))).toBe(true);
    expect(urls.some((u) => u.includes("/children/me/pocket"))).toBe(true);

    // Check bodies
    const allowanceCall = patchCalls.find(([u]) =>
      String(u).includes("/children/me/allowance")
    );
    const xpCall = patchCalls.find(([u]) => String(u).includes("/children/me/xp"));
    const pocketCall = patchCalls.find(([u]) =>
      String(u).includes("/children/me/pocket")
    );

    const allowanceBody = JSON.parse(allowanceCall[1].body);
    const xpBody = JSON.parse(xpCall[1].body);
    const pocketBody = JSON.parse(pocketCall[1].body);

    // Default minimum expenses: food 125 + elec 20 + water 15 + gas 15 = 175
    // Savings = 1000 - 175 = 825 -> XP = 83
    expect(allowanceBody).toEqual({ amount: 825 });
    expect(xpBody).toEqual({ xp: 83 });
    expect(pocketBody).toEqual({ amount: 25 });
  });

  it("LOSE: with low savings PATCH-es only allowance (no XP, no pocket)", async () => {
    renderBudgetGame();

    // Make savings low by setting Miscellaneous = 800 (expenses 175 + 800 = 975, savings 25)
    const miscInputs = await screen.findAllByLabelText(/Miscellaneous/i);
    const miscInput = miscInputs.find(input => input.type === 'number');
    fireEvent.change(miscInput, { target: { value: "800" } });

    const doneBtn = screen.getByRole("button", { name: /^Done$/i });
    fireEvent.click(doneBtn);

    const modal = await screen.findByTestId("modal");
    expect(modal).toBeInTheDocument();

    // IMPORTANT: match ONLY the badge "You Lose"
    expect(within(modal).getByText((t) => t.trim() === "You Lose")).toBeInTheDocument();

    const patchCalls = fetchMock.mock.calls.filter(([, opts]) => {
      const method = (opts?.method || "GET").toUpperCase();
      return method === "PATCH";
    });

    // Only allowance should be patched
    expect(patchCalls.length).toBe(1);
    expect(String(patchCalls[0][0]).includes("/children/me/allowance")).toBe(true);

    const allowanceBody = JSON.parse(patchCalls[0][1].body);
    expect(allowanceBody).toEqual({ amount: 25 });
  });
});
