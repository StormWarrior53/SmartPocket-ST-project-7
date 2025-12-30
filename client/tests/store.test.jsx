import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";

import Store from "../src/components/store/Store.jsx";

// ---------- shared mocks (mutable) ----------
let userMock = null;
let loadingMock = false;
const authFetchMock = vi.fn();
const navigateMock = vi.fn();

vi.mock("react-router", async () => {
  const actual = await vi.importActual("react-router");
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock("../src/context/UserContext.jsx", () => {
  return {
    useUser: () => ({
      user: userMock,
      loading: loadingMock,
      authFetch: authFetchMock,
    }),
  };
});

// Mock ItemCard so we can trigger buyItem easily
vi.mock("../src/components/store/item/ItemCard.jsx", () => {
  return {
    default: ({ item, buyItem, owned }) => {
      return (
        <div data-testid={`item-${item.id}`}>
          <div>{item.name}</div>
          {owned ? <div>Owned</div> : null}
          <button onClick={() => buyItem(item)}>Buy</button>
        </div>
      );
    },
  };
});

// ---------- helpers ----------
function okJson(data) {
  return {
    ok: true,
    json: vi.fn().mockResolvedValue(data),
  };
}

function failJson(data) {
  return {
    ok: false,
    json: vi.fn().mockResolvedValue(data),
  };
}

function okNoJson() {
  return { ok: true, json: vi.fn().mockResolvedValue({}) };
}

function renderStore() {
  return render(
    <MemoryRouter>
      <Store />
    </MemoryRouter>
  );
}

describe("Store", () => {
  const alertSpy = vi.spyOn(window, "alert").mockImplementation(() => {});
  const confirmSpy = vi.spyOn(window, "confirm").mockImplementation(() => true);

  beforeEach(() => {
    vi.clearAllMocks();
    userMock = null;
    loadingMock = false;

    // mock global fetch for /api/store
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("renders store title and loads store items via fetch()", async () => {
    fetch.mockResolvedValueOnce(
      okJson([
        { id: 1, name: "Sticker", price: 10, emoji: "⭐" },
        { id: 2, name: "Toy", price: 20, emoji: "🧸" },
      ])
    );

    userMock = { role: "parent" };
    authFetchMock.mockResolvedValue(okNoJson());

    renderStore();

    expect(
      screen.getByRole("heading", { name: /kids finance store/i })
    ).toBeInTheDocument();

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(1);
    });

    expect(String(fetch.mock.calls[0][0])).toMatch(/\/api\/store$/);

    expect(await screen.findByTestId("item-1")).toBeInTheDocument();
    expect(screen.getByTestId("item-2")).toBeInTheDocument();
  });

  it("for child user, loads balance + inventory via authFetch and shows balance", async () => {
    fetch.mockResolvedValueOnce(
      okJson([{ id: 1, name: "Sticker", price: 10, emoji: "⭐" }])
    );

    userMock = { role: "child" };

    authFetchMock
      // GET /children/me
      .mockResolvedValueOnce(okJson({ pocketMoney: 123 }))
      // GET /children/me/inventory
      .mockResolvedValueOnce(okJson([{ storeItemId: 1 }]));

    renderStore();

    expect(await screen.findByText(/your balance:\s*123/i)).toBeInTheDocument();

    // owned item should be marked
    expect(await screen.findByText("Owned")).toBeInTheDocument();

    // verify authFetch endpoints
    const calls = authFetchMock.mock.calls.map((c) => String(c[0]));
    expect(calls.some((u) => u.endsWith("/api/children/me"))).toBe(true);
    expect(calls.some((u) => u.endsWith("/api/children/me/inventory"))).toBe(
      true
    );
  });

  it("blocks purchase when user is not child", async () => {
    fetch.mockResolvedValueOnce(
      okJson([{ id: 1, name: "Sticker", price: 10, emoji: "⭐" }])
    );

    userMock = { role: "parent" };
    authFetchMock.mockResolvedValue(okNoJson());

    renderStore();

    await screen.findByTestId("item-1");

    await userEvent.click(screen.getByRole("button", { name: /buy/i }));

    expect(alertSpy).toHaveBeenCalled();
    expect(String(alertSpy.mock.calls[0][0]).toLowerCase()).toContain(
      "only child"
    );
    expect(authFetchMock).not.toHaveBeenCalledWith(
      expect.stringContaining("/purchase"),
      expect.anything()
    );
  });

  it("blocks purchase when item is already owned", async () => {
    fetch.mockResolvedValueOnce(
      okJson([{ id: 1, name: "Sticker", price: 10, emoji: "⭐" }])
    );

    userMock = { role: "child" };

    authFetchMock
      .mockResolvedValueOnce(okJson({ pocketMoney: 50 })) // me
      .mockResolvedValueOnce(okJson([{ storeItemId: 1 }])); // inventory owns item

    renderStore();

    await screen.findByTestId("item-1");

    await userEvent.click(screen.getByRole("button", { name: /buy/i }));

    expect(alertSpy).toHaveBeenCalled();
    expect(String(alertSpy.mock.calls[0][0]).toLowerCase()).toContain(
      "already own"
    );
    expect(confirmSpy).not.toHaveBeenCalled();
  });

  it("purchase success: calls purchase endpoint, refreshes balance, and shows purchased list", async () => {
    fetch.mockResolvedValueOnce(
      okJson([{ id: 1, name: "Sticker", price: 10, emoji: "⭐" }])
    );

    userMock = { role: "child" };

    authFetchMock
      // initial load
      .mockResolvedValueOnce(okJson({ pocketMoney: 100 })) // GET /children/me
      .mockResolvedValueOnce(okJson([])) // GET /children/me/inventory
      // purchase
      .mockResolvedValueOnce(okJson({ storeItemId: 1 }))
      // refresh balance after purchase
      .mockResolvedValueOnce(okJson({ pocketMoney: 90 }));

    renderStore();

    await screen.findByTestId("item-1");

    // click Buy -> confirm -> POST purchase
    await userEvent.click(screen.getByRole("button", { name: /buy/i }));

    await waitFor(() => {
      const urls = authFetchMock.mock.calls.map((c) => String(c[0]));
      expect(urls.some((u) => u.endsWith("/api/children/me/inventory/purchase"))).toBe(true);
    });

    // verify purchase payload
    const purchaseCall = authFetchMock.mock.calls.find((c) =>
      String(c[0]).endsWith("/api/children/me/inventory/purchase")
    );
    expect(purchaseCall[1]).toMatchObject({ method: "POST" });

    const purchaseBody = JSON.parse(purchaseCall[1].body);
    expect(purchaseBody).toEqual({ storeItemId: 1, quantity: 1 });

    // Purchased list appears
    expect(
      await screen.findByText(/your purchased items/i)
    ).toBeInTheDocument();
    expect(screen.getByText(/⭐\s*sticker/i)).toBeInTheDocument();

    // refresh balance happened (second GET /children/me)
    const meCalls = authFetchMock.mock.calls.filter((c) =>
      String(c[0]).endsWith("/api/children/me")
    );
    expect(meCalls.length).toBeGreaterThanOrEqual(2);
  });

  it("purchase failure shows alert with message from response body", async () => {
    fetch.mockResolvedValueOnce(
      okJson([{ id: 1, name: "Sticker", price: 10, emoji: "⭐" }])
    );

    userMock = { role: "child" };

    authFetchMock
      // initial load
      .mockResolvedValueOnce(okJson({ pocketMoney: 10 }))
      .mockResolvedValueOnce(okJson([]))
      // purchase fail
      .mockResolvedValueOnce(failJson({ message: "Not enough coins" }));

    renderStore();

    await screen.findByTestId("item-1");

    await userEvent.click(screen.getByRole("button", { name: /buy/i }));

    await waitFor(() => {
      expect(alertSpy).toHaveBeenCalled();
    });

    expect(String(alertSpy.mock.calls.at(-1)[0]).toLowerCase()).toContain(
      "not enough"
    );
  });
});
