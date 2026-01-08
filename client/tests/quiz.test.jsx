import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router";

import Quiz from "../src/components/quiz-page/Quiz.jsx";

// ---------- mocks ----------
const authFetchMock = vi.fn();
const quizApiGetQuizByExerciseIdMock = vi.fn();
const quizApiSubmitQuizMock = vi.fn();

vi.mock("../src/context/UserContext", () => {
    return {
        useUser: () => ({ authFetch: authFetchMock }),
    };
});

vi.mock("../src/services/api", () => {
    return {
        quizApi: {
            getQuizByExerciseId: (...args) => quizApiGetQuizByExerciseIdMock(...args),
            submitQuiz: (...args) => quizApiSubmitQuizMock(...args),
        },
    };
});

const mockQuiz = {
    id: 1,
    title: "Financial Basics Quiz",
    passPercent: 70,
    questions: [
        {
            id: 101,
            text: "What is a budget?",
            choices: [
                { id: 1001, text: "A plan for spending money" },
                { id: 1002, text: "A type of bank account" },
                { id: 1003, text: "A credit card" },
            ],
        },
        {
            id: 102,
            text: "What does saving mean?",
            choices: [
                { id: 2001, text: "Spending all your money" },
                { id: 2002, text: "Keeping money for future use" },
                { id: 2003, text: "Borrowing money" },
            ],
        },
    ],
};

function renderQuiz(exerciseId = "123") {
    return render(
        <MemoryRouter initialEntries={[`/quiz/${exerciseId}`]}>
            <Routes>
                <Route path="/quiz/:id" element={<Quiz />} />
                <Route path="/roadmap/:id" element={<div>Roadmap Page</div>} />
            </Routes>
        </MemoryRouter>
    );
}

describe("Quiz", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        vi.stubGlobal("confirm", vi.fn(() => true));
    });

    it("shows loading state initially", () => {
        quizApiGetQuizByExerciseIdMock.mockImplementation(() => new Promise(() => { })); // Never resolves

        renderQuiz();

        expect(screen.getByText(/loading quiz\.\.\./i)).toBeInTheDocument();
    });

    it("fetches and displays quiz data", async () => {
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);

        renderQuiz("123");

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        expect(quizApiGetQuizByExerciseIdMock).toHaveBeenCalledWith("123");
        expect(screen.getByText(/what is a budget\?/i)).toBeInTheDocument();
        expect(screen.getByText(/what does saving mean\?/i)).toBeInTheDocument();
        expect(screen.getByText(/a plan for spending money/i)).toBeInTheDocument();
    });

    it("shows error state when quiz fetch fails", async () => {
        quizApiGetQuizByExerciseIdMock.mockRejectedValueOnce(new Error("Network error"));

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByRole("heading", { name: /error/i })).toBeInTheDocument();
        });

        expect(screen.getByText(/network error/i)).toBeInTheDocument();
        expect(screen.getByRole("link", { name: /back/i })).toBeInTheDocument();
    });

    it("shows 'No Quiz Found' message when quiz is null", async () => {
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(null);

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/no quiz found/i)).toBeInTheDocument();
        });

        expect(screen.getByText(/this lecture does not have a quiz yet/i)).toBeInTheDocument();
    });

    it("allows selecting answers", async () => {
        const user = userEvent.setup();
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        const firstAnswer = screen.getByLabelText(/a plan for spending money/i);
        await user.click(firstAnswer);

        expect(firstAnswer).toBeChecked();
    });

    it("allows changing selected answer", async () => {
        const user = userEvent.setup();
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        const firstAnswer = screen.getByLabelText(/a plan for spending money/i);
        const secondAnswer = screen.getByLabelText(/a type of bank account/i);

        await user.click(firstAnswer);
        expect(firstAnswer).toBeChecked();

        await user.click(secondAnswer);
        expect(secondAnswer).toBeChecked();
        expect(firstAnswer).not.toBeChecked();
    });

    it("submits quiz successfully and shows result", async () => {
        const user = userEvent.setup();
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);
        quizApiSubmitQuizMock.mockResolvedValueOnce({
            correct: 2,
            total: 2,
            percent: 100,
            message: "Perfect score!",
            awarded: 50,
            passed: true,
        });
        authFetchMock.mockResolvedValueOnce({ ok: true });

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        // Select answers
        await user.click(screen.getByLabelText(/a plan for spending money/i));
        await user.click(screen.getByLabelText(/keeping money for future use/i));

        // Submit quiz
        await user.click(screen.getByRole("button", { name: /submit quiz/i }));

        await waitFor(() => {
            expect(quizApiSubmitQuizMock).toHaveBeenCalledWith(1, {
                101: 1001,
                102: 2002,
            });
        });

        expect(screen.getByText(/result/i)).toBeInTheDocument();
        expect(screen.getByText(/score: 2 \/ 2 \(100%\)/i)).toBeInTheDocument();
        expect(screen.getByText(/perfect score!/i)).toBeInTheDocument();
        expect(screen.getByText(/pocketmoney awarded:/i)).toBeInTheDocument();
        expect(screen.getByText(/50/i)).toBeInTheDocument();
    });

    it("disables inputs after submission", async () => {
        const user = userEvent.setup();
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);
        quizApiSubmitQuizMock.mockResolvedValueOnce({
            correct: 1,
            total: 2,
            percent: 50,
            message: "Try again!",
            awarded: 0,
            passed: false,
        });

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        await user.click(screen.getByLabelText(/a plan for spending money/i));
        await user.click(screen.getByRole("button", { name: /submit quiz/i }));

        await waitFor(() => {
            expect(screen.getByText(/result/i)).toBeInTheDocument();
        });

        // All radio buttons should be disabled
        const radios = screen.getAllByRole("radio");
        radios.forEach(radio => {
            expect(radio).toBeDisabled();
        });

        // Submit button should be disabled
        expect(screen.getByRole("button", { name: /submit quiz/i })).toBeDisabled();
    });

    it("shows loading state while submitting", async () => {
        const user = userEvent.setup();
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);
        quizApiSubmitQuizMock.mockImplementation(() => new Promise(() => { })); // Never resolves

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        await user.click(screen.getByLabelText(/a plan for spending money/i));
        await user.click(screen.getByRole("button", { name: /submit quiz/i }));

        expect(screen.getByRole("button", { name: /submitting\.\.\./i })).toBeInTheDocument();
    });

    it("shows error when quiz submission fails", async () => {
        const user = userEvent.setup();
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);
        quizApiSubmitQuizMock.mockRejectedValueOnce(new Error("Submission failed"));

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        await user.click(screen.getByLabelText(/a plan for spending money/i));
        await user.click(screen.getByRole("button", { name: /submit quiz/i }));

        await waitFor(() => {
            expect(screen.getByText(/submission failed/i)).toBeInTheDocument();
        });
    });

    it("does not submit if user cancels confirmation", async () => {
        const user = userEvent.setup();
        global.confirm = vi.fn(() => false); // User cancels

        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        await user.click(screen.getByLabelText(/a plan for spending money/i));
        await user.click(screen.getByRole("button", { name: /submit quiz/i }));

        expect(global.confirm).toHaveBeenCalled();
        expect(quizApiSubmitQuizMock).not.toHaveBeenCalled();
    });

    it("allocates pocket money when quiz is passed with reward", async () => {
        const user = userEvent.setup();
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);
        quizApiSubmitQuizMock.mockResolvedValueOnce({
            correct: 2,
            total: 2,
            percent: 100,
            message: "Great job!",
            awarded: 75,
            passed: true,
        });
        authFetchMock.mockResolvedValueOnce({ ok: true });

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        await user.click(screen.getByLabelText(/a plan for spending money/i));
        await user.click(screen.getByRole("button", { name: /submit quiz/i }));

        await waitFor(() => {
            expect(authFetchMock).toHaveBeenCalledWith(
                expect.stringContaining("/children/me/pocket-money"),
                expect.objectContaining({
                    method: "POST",
                    body: JSON.stringify({ amount: 75 }),
                })
            );
        });
    });

    it("does not allocate pocket money when quiz is failed", async () => {
        const user = userEvent.setup();
        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);
        quizApiSubmitQuizMock.mockResolvedValueOnce({
            correct: 1,
            total: 2,
            percent: 50,
            message: "Keep trying!",
            awarded: 0,
            passed: false,
        });

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        await user.click(screen.getByLabelText(/a plan for spending money/i));
        await user.click(screen.getByRole("button", { name: /submit quiz/i }));

        await waitFor(() => {
            expect(screen.getByText(/result/i)).toBeInTheDocument();
        });

        expect(authFetchMock).not.toHaveBeenCalled();
    });

    it("handles pocket money allocation failure gracefully", async () => {
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => { });
        const user = userEvent.setup();

        quizApiGetQuizByExerciseIdMock.mockResolvedValueOnce(mockQuiz);
        quizApiSubmitQuizMock.mockResolvedValueOnce({
            correct: 2,
            total: 2,
            percent: 100,
            message: "Perfect!",
            awarded: 50,
            passed: true,
        });
        authFetchMock.mockResolvedValueOnce({ ok: false, status: 500 });

        renderQuiz();

        await waitFor(() => {
            expect(screen.getByText(/financial basics quiz/i)).toBeInTheDocument();
        });

        await user.click(screen.getByLabelText(/a plan for spending money/i));
        await user.click(screen.getByRole("button", { name: /submit quiz/i }));

        await waitFor(() => {
            expect(screen.getByText(/result/i)).toBeInTheDocument();
        });

        // Should still show results even if pocket money allocation fails
        expect(screen.getByText(/perfect!/i)).toBeInTheDocument();
        expect(consoleErrorSpy).toHaveBeenCalled();

        consoleErrorSpy.mockRestore();
    });
});