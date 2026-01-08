import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";

import ChildLogin from "../src/components/login-page/child-login/ChildLogin.jsx";

// ---------- mocks ----------
const navigateMock = vi.fn();
const loginMock = vi.fn();
const childApiSetupPatternMock = vi.fn();
const childApiLoginMock = vi.fn();

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
        childApi: {
            setupPattern: (...args) => childApiSetupPatternMock(...args),
            login: (...args) => childApiLoginMock(...args),
        },
        ApiError,
    };
});

function renderComponent() {
    return render(
        <MemoryRouter>
            <ChildLogin />
        </MemoryRouter>
    );
}

describe("ChildLogin", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
        sessionStorage.clear();
        vi.stubGlobal("alert", vi.fn());
    });

    it("renders the child pattern setup form by default", () => {
        renderComponent();

        expect(screen.getByRole("heading", { name: /child pattern setup/i })).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/enter child's name/i)).toBeInTheDocument();
        expect(screen.getByPlaceholderText(/enter parent email/i)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /set pattern/i })).toBeInTheDocument();
        expect(screen.getByText(/switch to login/i)).toBeInTheDocument();
    });

    it("shows logout message when logoutReason is expired", () => {
        sessionStorage.setItem("logoutReason", "expired");

        renderComponent();

        expect(
            screen.getByText(/your session has expired\. please log in again\./i)
        ).toBeInTheDocument();

        expect(sessionStorage.getItem("logoutReason")).toBeNull();
    });

    it("shows logout message when logoutReason is server-rejected", () => {
        sessionStorage.setItem("logoutReason", "server-rejected");

        renderComponent();

        expect(
            screen.getByText(/your session is no longer valid\. please log in again\./i)
        ).toBeInTheDocument();
    });

    it("toggles between setup and login modes", async () => {
        const user = userEvent.setup();
        renderComponent();

        expect(screen.getByRole("heading", { name: /child pattern setup/i })).toBeInTheDocument();

        await user.click(screen.getByText(/switch to login/i));

        expect(screen.getByRole("heading", { name: /child login/i })).toBeInTheDocument();
        expect(screen.getByText(/first-time setup/i)).toBeInTheDocument();
    });

    it("validates empty fields", async () => {
        const user = userEvent.setup();
        renderComponent();

        await user.click(screen.getByRole("button", { name: /set pattern/i }));

        expect(await screen.findByText(/child name is required/i)).toBeInTheDocument();
        expect(await screen.findByText(/parent email is required/i)).toBeInTheDocument();
        expect(childApiSetupPatternMock).not.toHaveBeenCalled();
    });

    it("validates invalid email format", async () => {
        const user = userEvent.setup();
        renderComponent();

        await user.type(screen.getByPlaceholderText(/enter child's name/i), "Tommy");
        await user.type(screen.getByPlaceholderText(/enter parent email/i), "not-an-email");

        await user.click(screen.getByRole("button", { name: /set pattern/i }));

        expect(await screen.findByText(/invalid email format/i)).toBeInTheDocument();
        expect(childApiSetupPatternMock).not.toHaveBeenCalled();
    });

    it("validates pattern has at least 3 dots", async () => {
        const user = userEvent.setup();
        renderComponent();

        await user.type(screen.getByPlaceholderText(/enter child's name/i), "Tommy");
        await user.type(screen.getByPlaceholderText(/enter parent email/i), "parent@test.com");

        await user.click(screen.getByRole("button", { name: /set pattern/i }));

        expect(await screen.findByText(/connect at least 3 dots/i)).toBeInTheDocument();
        expect(childApiSetupPatternMock).not.toHaveBeenCalled();
    });

    it("allows selecting pattern dots by clicking", async () => {
        const user = userEvent.setup();
        renderComponent();

        const dots = screen.getAllByLabelText(/dot \d/i);

        await user.click(dots[0]); // Dot 1
        await user.click(dots[1]); // Dot 2
        await user.click(dots[2]); // Dot 3

        expect(screen.getByText(/selected: 1 → 2 → 3/i)).toBeInTheDocument();
    });

    it("clears the pattern when clear button is clicked", async () => {
        const user = userEvent.setup();
        renderComponent();

        const dots = screen.getAllByLabelText(/dot \d/i);

        await user.click(dots[0]);
        await user.click(dots[1]);
        await user.click(dots[2]);

        expect(screen.getByText(/selected: 1 → 2 → 3/i)).toBeInTheDocument();

        await user.click(screen.getByText(/clear/i));

        expect(screen.getByText(/selected: -/i)).toBeInTheDocument();
    });

    it("submits pattern setup successfully and navigates to home", async () => {
        const user = userEvent.setup();
        const responseData = {
            token: "child-token",
            tokenType: "Bearer",
            childName: "Tommy"
        };

        childApiSetupPatternMock.mockResolvedValueOnce(responseData);
        renderComponent();

        await user.type(screen.getByPlaceholderText(/enter child's name/i), "Tommy");
        await user.type(screen.getByPlaceholderText(/enter parent email/i), "parent@test.com");

        const dots = screen.getAllByLabelText(/dot \d/i);
        await user.click(dots[0]);
        await user.click(dots[1]);
        await user.click(dots[2]);

        await user.click(screen.getByRole("button", { name: /set pattern/i }));

        await waitFor(() => {
            expect(childApiSetupPatternMock).toHaveBeenCalledTimes(1);
        });

        expect(childApiSetupPatternMock).toHaveBeenCalledWith({
            childName: "Tommy",
            parentEmail: "parent@test.com",
            pattern: "1,2,3",
        });

        expect(loginMock).toHaveBeenCalledWith({
            ...responseData,
            firstName: "Tommy",
            email: "parent@test.com",
            token: "child-token",
            tokenType: "Bearer",
            role: "child",
        });

        expect(global.alert).toHaveBeenCalledWith("Pattern set successfully!");
        expect(navigateMock).toHaveBeenCalledWith("/");
    });

    it("submits child login successfully", async () => {
        const user = userEvent.setup();
        const responseData = {
            token: "child-login-token",
            tokenType: "Bearer",
            firstName: "Tommy"
        };

        childApiLoginMock.mockResolvedValueOnce(responseData);
        renderComponent();

        // Switch to login mode
        await user.click(screen.getByText(/switch to login/i));

        await user.type(screen.getByPlaceholderText(/enter child's name/i), "Tommy");
        await user.type(screen.getByPlaceholderText(/enter parent email/i), "parent@test.com");

        const dots = screen.getAllByLabelText(/dot \d/i);
        await user.click(dots[0]);
        await user.click(dots[4]);
        await user.click(dots[8]);

        await user.click(screen.getByRole("button", { name: /^login$/i }));

        await waitFor(() => {
            expect(childApiLoginMock).toHaveBeenCalledTimes(1);
        });

        expect(childApiLoginMock).toHaveBeenCalledWith({
            childName: "Tommy",
            parentEmail: "parent@test.com",
            pattern: "1,5,9",
        });

        expect(loginMock).toHaveBeenCalled();
        expect(global.alert).toHaveBeenCalledWith("Logged in successfully!");
        expect(navigateMock).toHaveBeenCalledWith("/");
    });

    it("shows API error message on ApiError", async () => {
        const user = userEvent.setup();
        const { ApiError } = await import("../src/services/api");

        childApiSetupPatternMock.mockRejectedValueOnce(new ApiError("Pattern already exists"));
        renderComponent();

        await user.type(screen.getByPlaceholderText(/enter child's name/i), "Tommy");
        await user.type(screen.getByPlaceholderText(/enter parent email/i), "parent@test.com");

        const dots = screen.getAllByLabelText(/dot \d/i);
        await user.click(dots[0]);
        await user.click(dots[1]);
        await user.click(dots[2]);

        await user.click(screen.getByRole("button", { name: /set pattern/i }));

        expect(await screen.findByText(/pattern already exists/i)).toBeInTheDocument();
        expect(navigateMock).not.toHaveBeenCalled();
    });

    it("shows generic error message on unexpected error", async () => {
        const user = userEvent.setup();

        childApiLoginMock.mockRejectedValueOnce(new Error("Network error"));
        renderComponent();

        await user.click(screen.getByText(/switch to login/i));

        await user.type(screen.getByPlaceholderText(/enter child's name/i), "Tommy");
        await user.type(screen.getByPlaceholderText(/enter parent email/i), "parent@test.com");

        const dots = screen.getAllByLabelText(/dot \d/i);
        await user.click(dots[0]);
        await user.click(dots[1]);
        await user.click(dots[2]);

        await user.click(screen.getByRole("button", { name: /^login$/i }));

        expect(
            await screen.findByText(/an unexpected error occurred\. please try again\./i)
        ).toBeInTheDocument();
    });

    it("prevents duplicate dot selection", async () => {
        const user = userEvent.setup();
        renderComponent();

        const dots = screen.getAllByLabelText(/dot \d/i);

        await user.click(dots[0]);
        await user.click(dots[1]);
        await user.click(dots[0]); // Try to click same dot again

        // Should still only show 1 → 2, not 1 → 2 → 1
        await waitFor(() => {
            const selectedText = screen.getByText(/selected:/i).textContent;
            expect(selectedText).toMatch(/1.*2/);
            expect(selectedText).not.toMatch(/1.*2.*1/);
        });
    });
});