import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";

import CreateChild from "../src/components/create-child/CreateChild.jsx";

// --- mocks ---
const navigateMock = vi.fn();
const authFetchMock = vi.fn();

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
      authFetch: authFetchMock,
    }),
  };
});

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

function renderComponent() {
  return render(
    <MemoryRouter>
      <CreateChild />
    </MemoryRouter>
  );
}

describe("CreateChild", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the create child form", () => {
    renderComponent();

    expect(
      screen.getByRole("heading", { name: /create new child/i })
    ).toBeInTheDocument();

    expect(screen.getByPlaceholderText(/child's name/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/child's age/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /create child/i })
    ).toBeInTheDocument();
  });

  it("shows error when fields are empty and does not call authFetch", async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.click(screen.getByRole("button", { name: /create child/i }));

    expect(await screen.findByText(/all fields are required\./i)).toBeInTheDocument();
    expect(authFetchMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it("submits form, calls POST, and navigates to /profile on success", async () => {
    const user = userEvent.setup();
    authFetchMock.mockResolvedValueOnce(okJson({ id: 1 }));

    renderComponent();

    await user.type(screen.getByPlaceholderText(/child's name/i), "  John  ");
    await user.type(screen.getByPlaceholderText(/child's age/i), "10");

    await user.click(screen.getByRole("button", { name: /create child/i }));

    await waitFor(() => {
      expect(authFetchMock).toHaveBeenCalledTimes(1);
    });

    const [url, options] = authFetchMock.mock.calls[0];

    expect(String(url)).toMatch(/\/parents\/me\/children$/);
    expect(options).toMatchObject({ method: "POST" });

    const bodyObj = JSON.parse(options.body);
    expect(bodyObj).toEqual({ name: "John", age: 10 });

    expect(navigateMock).toHaveBeenCalledWith("/profile");
  });

  it("shows backend error message when response is not ok", async () => {
    const user = userEvent.setup();
    authFetchMock.mockResolvedValueOnce(
      failJson({ message: "Age must be between 7 and 24" })
    );

    renderComponent();

    await user.type(screen.getByPlaceholderText(/child's name/i), "John");
    await user.type(screen.getByPlaceholderText(/child's age/i), "3");
    await user.click(screen.getByRole("button", { name: /create child/i }));

    expect(await screen.findByText(/age must be between 7 and 24/i)).toBeInTheDocument();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it("shows generic failure message when response is not ok and has no message", async () => {
    const user = userEvent.setup();
    authFetchMock.mockResolvedValueOnce(failJson({}));

    renderComponent();

    await user.type(screen.getByPlaceholderText(/child's name/i), "John");
    await user.type(screen.getByPlaceholderText(/child's age/i), "10");
    await user.click(screen.getByRole("button", { name: /create child/i }));

    expect(await screen.findByText(/failed to create child\./i)).toBeInTheDocument();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it("shows fallback error message on unexpected error", async () => {
    const user = userEvent.setup();
    authFetchMock.mockRejectedValueOnce(new Error("Network down"));

    renderComponent();

    await user.type(screen.getByPlaceholderText(/child's name/i), "John");
    await user.type(screen.getByPlaceholderText(/child's age/i), "10");
    await user.click(screen.getByRole("button", { name: /create child/i }));

    expect(await screen.findByText(/something went wrong\./i)).toBeInTheDocument();
    expect(navigateMock).not.toHaveBeenCalled();
  });
});
