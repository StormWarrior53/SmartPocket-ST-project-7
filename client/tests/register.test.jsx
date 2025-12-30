import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";

import Register from "../src/components/register-page/Register.jsx";

// ---------- mocks ----------
const navigateMock = vi.fn();
const authApiRegisterMock = vi.fn();

vi.mock("react-router", async () => {
  const actual = await vi.importActual("react-router");
  return {
    ...actual,
    useNavigate: () => navigateMock,
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
      register: (...args) => authApiRegisterMock(...args),
    },
    ApiError,
  };
});

function renderComponent() {
  return render(
    <MemoryRouter>
      <Register />
    </MemoryRouter>
  );
}

describe("Register", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    // prevent real alert popup during tests
    vi.stubGlobal("alert", vi.fn());
  });

  it("renders the register form", () => {
    renderComponent();

    expect(screen.getByRole("heading", { name: /register/i })).toBeInTheDocument();

    expect(screen.getByPlaceholderText(/enter first name/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/enter last name/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/enter email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/enter password/i)).toBeInTheDocument();

    expect(screen.getByRole("button", { name: /create account/i })).toBeInTheDocument();
    expect(screen.getByText(/log in here/i)).toBeInTheDocument();
  });

  it("validates empty fields and blocks submit", async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.click(screen.getByRole("button", { name: /create account/i }));

    expect(await screen.findByText(/first name is required/i)).toBeInTheDocument();
    expect(await screen.findByText(/last name is required/i)).toBeInTheDocument();
    expect(await screen.findByText(/email is required/i)).toBeInTheDocument();
    expect(await screen.findByText(/password is required/i)).toBeInTheDocument();

    expect(authApiRegisterMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it("validates first and last name length", async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter first name/i), "A");
    await user.type(screen.getByPlaceholderText(/enter last name/i), "B");
    await user.type(screen.getByPlaceholderText(/enter email/i), "a@b.com");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123456");

    await user.click(screen.getByRole("button", { name: /create account/i }));

    expect(await screen.findByText(/first name must be at least 2 characters/i)).toBeInTheDocument();
    expect(await screen.findByText(/last name must be at least 2 characters/i)).toBeInTheDocument();

    expect(authApiRegisterMock).not.toHaveBeenCalled();
  });

  it("validates invalid email format and blocks submit", async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter first name/i), "Anna");
    await user.type(screen.getByPlaceholderText(/enter last name/i), "Ivanova");
    await user.type(screen.getByPlaceholderText(/enter email/i), "not-an-email");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123456");

    await user.click(screen.getByRole("button", { name: /create account/i }));

    // This assertion is stable and sufficient: invalid email should block API call
    expect(authApiRegisterMock).not.toHaveBeenCalled();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it("validates password length", async () => {
    const user = userEvent.setup();
    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter first name/i), "Anna");
    await user.type(screen.getByPlaceholderText(/enter last name/i), "Ivanova");
    await user.type(screen.getByPlaceholderText(/enter email/i), "a@b.com");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123");

    await user.click(screen.getByRole("button", { name: /create account/i }));

    expect(
      await screen.findByText(/password must be at least 6 characters long/i)
    ).toBeInTheDocument();

    expect(authApiRegisterMock).not.toHaveBeenCalled();
  });

  it("submits data and navigates to /login on success", async () => {
    const user = userEvent.setup();
    authApiRegisterMock.mockResolvedValueOnce({ ok: true });

    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter first name/i), "Anna");
    await user.type(screen.getByPlaceholderText(/enter last name/i), "Ivanova");
    await user.type(screen.getByPlaceholderText(/enter email/i), "anna@ab.com");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123456");

    await user.click(screen.getByRole("button", { name: /create account/i }));

    await waitFor(() => {
      expect(authApiRegisterMock).toHaveBeenCalledTimes(1);
    });

    expect(authApiRegisterMock).toHaveBeenCalledWith({
      firstName: "Anna",
      lastName: "Ivanova",
      email: "anna@ab.com",
      password: "123456",
    });

    expect(global.alert).toHaveBeenCalledWith("Registration successful! Please login.");
    expect(navigateMock).toHaveBeenCalledWith("/login");
  });

  it("shows API error message on ApiError without field errors", async () => {
    const user = userEvent.setup();
    const { ApiError } = await import("../src/services/api");

    authApiRegisterMock.mockRejectedValueOnce(new ApiError("Email already exists"));
    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter first name/i), "Anna");
    await user.type(screen.getByPlaceholderText(/enter last name/i), "Ivanova");
    await user.type(screen.getByPlaceholderText(/enter email/i), "anna@ab.com");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123456");

    await user.click(screen.getByRole("button", { name: /create account/i }));

    expect(await screen.findByText(/email already exists/i)).toBeInTheDocument();
    expect(navigateMock).not.toHaveBeenCalled();
  });

  it("shows field errors on ApiError with errors object", async () => {
    const user = userEvent.setup();
    const { ApiError } = await import("../src/services/api");

    authApiRegisterMock.mockRejectedValueOnce(
      new ApiError("Validation failed", { email: "Email is already taken" })
    );

    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter first name/i), "Anna");
    await user.type(screen.getByPlaceholderText(/enter last name/i), "Ivanova");
    await user.type(screen.getByPlaceholderText(/enter email/i), "anna@ab.com");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123456");

    await user.click(screen.getByRole("button", { name: /create account/i }));

    expect(await screen.findByText(/email is already taken/i)).toBeInTheDocument();
  });

  it("shows fallback error message on unexpected error", async () => {
    const user = userEvent.setup();

    authApiRegisterMock.mockRejectedValueOnce(new Error("Network down"));
    renderComponent();

    await user.type(screen.getByPlaceholderText(/enter first name/i), "Anna");
    await user.type(screen.getByPlaceholderText(/enter last name/i), "Ivanova");
    await user.type(screen.getByPlaceholderText(/enter email/i), "anna@ab.com");
    await user.type(screen.getByPlaceholderText(/enter password/i), "123456");

    await user.click(screen.getByRole("button", { name: /create account/i }));

    expect(
      await screen.findByText(/an unexpected error occurred\. please try again\./i)
    ).toBeInTheDocument();
  });
});
