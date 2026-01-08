import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";

import ParentLogin from "../src/components/login-page/parent-login/ParentLogin.jsx";

// ---------- mocks ----------
const navigateMock = vi.fn();
const loginMock = vi.fn();
const authApiLoginMock = vi.fn();

vi.mock("react-router", async () => {
  const actual = await vi.importActual("react-router");
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock("../src/context/UserContext", () => {
  return {
    useUser: () => ({ login: loginMock }),
  };
});

vi.mock("../src/services/api", () => {
  class ApiError extends Error {
    constructor(message, errors) {
      super(message);
      this.name = "ApiError";
      this.errors = errors;
    }
  }

  return {
    authApi: {
      login: (...args) => authApiLoginMock(...args),
    },
    ApiError,
  };
});

function renderComponent() {
  return render(
    <MemoryRouter>
      <ParentLogin />
    </MemoryRouter>
  );
}

describe("ParentLogin", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    sessionStorage.clear();
  });

  it("renders the parent login form", () => {
    renderComponent();

    expect(screen.getByRole("heading", { name: /parent login/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/enter email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/enter password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /login/i })).toBeInTheDocument();
    expect(screen.getByText(/register here/i)).toBeInTheDocument();
  });

  it("shows logout message when logoutReason is expired", () => {
    sessionStorage.setItem("logoutReason", "expired");

    renderComponent();

    expect(
      screen.getByText(/your session has expired\. please log in again\./i)
    ).toBeInTheDocument();

    expect(sessionStorage.getItem("logoutReason")).toBeNull();
  });

  it("validates empty fields and does not call API", async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.click(screen.getByRole("button", { name: /login/i }));

    expect(await screen.findByText(/email is required/i)).toBeInTheDocument();
    expect(await screen.findByText(/password is required/i)).toBeInTheDocument();
    expect(authApiLoginMock).not.toHaveBeenCalled();
  });

  it("validates invalid email format and blocks submit", async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter email/i), "not-an-email");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123456");
    await user.click(screen.getByRole("button", { name: /login/i }));

    expect(authApiLoginMock).not.toHaveBeenCalled();
    expect(loginMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it("validates password length", async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter email/i), "a@b.com");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123");
    await user.click(screen.getByRole("button", { name: /login/i }));

    expect(
      await screen.findByText(/password must be at least 6 characters long/i)
    ).toBeInTheDocument();
    expect(authApiLoginMock).not.toHaveBeenCalled();
  });

  it("submits credentials, calls login(), and navigates to home on success", async () => {
    const user = userEvent.setup();
    const userData = { token: "t1", user: { id: 1, role: "PARENT" } };

    authApiLoginMock.mockResolvedValueOnce(userData);
    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter email/i), "a@b.com");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123456");
    await user.click(screen.getByRole("button", { name: /login/i }));

    await waitFor(() => {
      expect(authApiLoginMock).toHaveBeenCalledTimes(1);
    });

    expect(loginMock).toHaveBeenCalledWith(userData);
    expect(navigateMock).toHaveBeenCalledWith("/");
  });

  it("shows API error message on ApiError", async () => {
    const user = userEvent.setup();
    const { ApiError } = await import("../src/services/api");

    authApiLoginMock.mockRejectedValueOnce(new ApiError("Invalid credentials"));
    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter email/i), "a@b.com");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123456");
    await user.click(screen.getByRole("button", { name: /login/i }));

    expect(await screen.findByText(/invalid credentials/i)).toBeInTheDocument();
  });
});
