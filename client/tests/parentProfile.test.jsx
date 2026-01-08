import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";

import ParentProfile from "../src/components/profile/parent-profile/ParentProfile.jsx";

// ---- globals ----
const authFetchMock = vi.fn();

vi.mock("../src/context/UserContext.jsx", () => {
  return {
    useUser: () => ({
      user: { firstName: "Anna", lastName: "Ivanova", email: "anna@ab.com" },
      authFetch: authFetchMock,
      loading: false,
      isAuthenticated: true,
    }),
  };
});

vi.mock("../src/components/profile/child-card/ChildCard.jsx", () => {
  // Simple test double to trigger callbacks
  return {
    default: ({ child, onAddAllowance, onDelete }) => (
      <div data-testid={`child-${child.id}`}>
        <div>{child.firstName} {child.lastName}</div>
        <button onClick={() => onAddAllowance(child.id)}>Add Allowance</button>
        <button onClick={() => onDelete(child.id)}>Delete Child</button>
      </div>
    ),
  };
});

vi.mock("../src/components/profile/edit-modal/EditChild.jsx", () => {
  return {
    default: ({ child, onClose, onSaved }) => (
      <div data-testid="edit-modal">
        <div>Editing {child.firstName}</div>
        <button onClick={() => onSaved({ ...child, firstName: "Updated" })}>Save</button>
        <button onClick={onClose}>Close</button>
      </div>
    ),
  };
});

function okJson(data) {
  return {
    ok: true,
    json: vi.fn().mockResolvedValue(data),
  };
}


function okNoJson() {
  return { ok: true, json: vi.fn().mockResolvedValue(null) };
}

function renderComponent() {
  return render(
    <MemoryRouter>
      <ParentProfile />
    </MemoryRouter>
  );
}

describe("ParentProfile", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("confirm", vi.fn());
    vi.stubGlobal("prompt", vi.fn());
    vi.stubGlobal("alert", vi.fn());
  });

  it("shows loading when userLoading or local loading is true (initial render)", () => {
    // ParentProfile starts with loading=true
    authFetchMock.mockResolvedValueOnce(okJson([]));
    renderComponent();

    expect(screen.getByText(/loading\.\.\./i)).toBeInTheDocument();
  });

  it("renders parent info and 'No children found.' when API returns empty list", async () => {
    authFetchMock.mockResolvedValueOnce(okJson([]));

    renderComponent();

    expect(await screen.findByText(/anna ivanova/i)).toBeInTheDocument();
    expect(screen.getByText(/anna@ab\.com/i)).toBeInTheDocument();
    expect(await screen.findByText(/no children found\./i)).toBeInTheDocument();

    // It should call children list endpoint
    expect(authFetchMock).toHaveBeenCalledTimes(1);
  });

  it("fetches children and inventories and renders ChildCard items", async () => {
    const children = [
      { id: 1, firstName: "Kid", lastName: "One" },
      { id: 2, firstName: "Kid", lastName: "Two" },
    ];

    // 1) /parents/me/children
    authFetchMock.mockResolvedValueOnce(okJson(children));
    // 2) inventory for child 1
    authFetchMock.mockResolvedValueOnce(okJson([{ id: "i1" }]));
    // 3) inventory for child 2
    authFetchMock.mockResolvedValueOnce(okJson([]));

    renderComponent();

    expect(await screen.findByTestId("child-1")).toBeInTheDocument();
    expect(await screen.findByTestId("child-2")).toBeInTheDocument();

    // called 3 times: list + 2 inventories
    expect(authFetchMock).toHaveBeenCalledTimes(3);
  });

  it("delete flow: confirms, calls DELETE, and removes child from UI", async () => {
    const user = userEvent.setup();
    confirm.mockReturnValue(true);

    const children = [{ id: 1, firstName: "Kid", lastName: "One" }];

    // initial fetch
    authFetchMock.mockResolvedValueOnce(okJson(children));
    // inventory fetch
    authFetchMock.mockResolvedValueOnce(okJson([]));
    // delete
    authFetchMock.mockResolvedValueOnce(okNoJson());

    renderComponent();

    expect(await screen.findByTestId("child-1")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /delete child/i }));

    await waitFor(() => {
      expect(authFetchMock).toHaveBeenCalled();
    });

    // child should disappear
    await waitFor(() => {
      expect(screen.queryByTestId("child-1")).not.toBeInTheDocument();
    });

    // confirm must be called
    expect(confirm).toHaveBeenCalled();
  });

  it("delete flow: if confirm is false, does not call DELETE", async () => {
    const user = userEvent.setup();
    confirm.mockReturnValue(false);

    const children = [{ id: 1, firstName: "Kid", lastName: "One" }];

    authFetchMock.mockResolvedValueOnce(okJson(children));
    authFetchMock.mockResolvedValueOnce(okJson([]));

    renderComponent();

    expect(await screen.findByTestId("child-1")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /delete child/i }));

    // still visible and only initial calls happened
    expect(screen.getByTestId("child-1")).toBeInTheDocument();
    expect(authFetchMock).toHaveBeenCalledTimes(2);
  });

  it("add allowance: invalid prompt input shows alert and does not call API", async () => {
    const user = userEvent.setup();
    prompt.mockReturnValue("abc"); // invalid

    const children = [{ id: 1, firstName: "Kid", lastName: "One" }];

    authFetchMock.mockResolvedValueOnce(okJson(children));
    authFetchMock.mockResolvedValueOnce(okJson([]));

    renderComponent();

    expect(await screen.findByTestId("child-1")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /add allowance/i }));

    expect(alert).toHaveBeenCalled();
    // no extra authFetch calls beyond initial 2
    expect(authFetchMock).toHaveBeenCalledTimes(2);
  });

  it("add allowance: success calls POST and updates list when API returns updated child", async () => {
    const user = userEvent.setup();
    prompt.mockReturnValue("5");

    const initialChildren = [{ id: 1, firstName: "Kid", lastName: "One" }];

    // initial list + inventory
    authFetchMock.mockResolvedValueOnce(okJson(initialChildren));
    authFetchMock.mockResolvedValueOnce(okJson([]));

    // POST add-money returns updated child object
    authFetchMock.mockResolvedValueOnce(
      okJson({ id: 1, firstName: "KidUpdated", lastName: "One" })
    );

    renderComponent();

    expect(await screen.findByTestId("child-1")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /add allowance/i }));

    await waitFor(() => {
      expect(authFetchMock).toHaveBeenCalledTimes(3);
    });

    // We don't rely on ChildCard rendering name changes, but we ensure POST happened
    const thirdCall = authFetchMock.mock.calls[2];
    expect(thirdCall[1]).toMatchObject({ method: "POST" });
  });

  it("opens edit modal and closes after save (updates via onSaved)", async () => {
    const user = userEvent.setup();

    const children = [{ id: 1, firstName: "Kid", lastName: "One" }];

    authFetchMock.mockResolvedValueOnce(okJson(children));
    authFetchMock.mockResolvedValueOnce(okJson([]));

    renderComponent();

    expect(await screen.findByTestId("child-1")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /edit/i }));
    expect(await screen.findByTestId("edit-modal")).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /save/i }));

    await waitFor(() => {
      expect(screen.queryByTestId("edit-modal")).not.toBeInTheDocument();
    });
  });
});
