import { useState, useEffect } from "react";
import { quizApi } from "../../services/api.js";
import { useUser } from "../../context/UserContext.jsx";
import { useNavigate } from "react-router";
import QuizForm from "./quiz-form/QuizForm.jsx";
import QuizList from "./quiz-list/QuizList.jsx";
import AttemptsModal from "./attempts-modal/AttemptsModal.jsx";
import TestTradePredictionModal from "./TestTradePredictionModal.jsx";

export default function QuizAdmin() {
    const [quizzes, setQuizzes] = useState([]);
    const [exercises, setExercises] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [editingQuizId, setEditingQuizId] = useState(null);
    const [viewingAttemptsQuizId, setViewingAttemptsQuizId] = useState(null);
    const [attempts, setAttempts] = useState([]);
    const [testingTradePrediction, setTestingTradePrediction] = useState(false);

    const { user, isAuthenticated, loading: authLoading } = useUser();
    const navigate = useNavigate();

    useEffect(() => {
        if (authLoading) return;

        const isAdmin = user?.role === "admin"
            || (Array.isArray(user?.roles) && user.roles.includes("admin") && isAuthenticated === true)
            || user?.isAdmin === true;

        if (!isAdmin) {
            navigate("/", { replace: true });
        }
    }, [user, authLoading, navigate]);

    const [formData, setFormData] = useState({
        title: "",
        exerciseId: "",
        pocketMoneyPerQuestion: 5,
        passPercent: 70,
        questions: [
            {
                text: "",
                correctChoiceId: "a",
                orderIndex: 0,
                choices: [
                    { choiceId: "a", text: "", orderIndex: 0 },
                    { choiceId: "b", text: "", orderIndex: 1 },
                    { choiceId: "c", text: "", orderIndex: 2 }
                ]
            }
        ]
    });

    useEffect(() => {
        fetchQuizzes();
        fetchExercises();
    }, []);

    async function fetchQuizzes() {
        try {
            setLoading(true);
            const data = await quizApi.getAllQuizzes();
            setQuizzes(data);
            setError(null);
        } catch (err) {
            setError(err.message || "Failed to load quizzes");
        } finally {
            setLoading(false);
        }
    }

    async function fetchExercises() {
        try {
            const response = await fetch('http://localhost:8080/api/exercises');
            const data = await response.json();
            setExercises(data);
        } catch (err) {
            console.error("Failed to load exercises:", err);
        }
    }

    async function handleEdit(quiz) {
        setFormData({
            title: quiz.title,
            exerciseId: "",
            pocketMoneyPerQuestion: quiz.pocketMoneyPerQuestion,
            passPercent: quiz.passPercent,
            questions: quiz.questions.map(q => ({
                text: q.text,
                correctChoiceId: q.choices.find(c => c.id === q.correctChoiceId)?.id || "a",
                orderIndex: 0,
                choices: q.choices.map((c, idx) => ({
                    choiceId: c.id,
                    text: c.text,
                    orderIndex: idx
                }))
            }))
        });
        setEditingQuizId(quiz.id);
        setShowCreateForm(true);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    async function handleViewAttempts(quizId) {
        try {
            const data = await quizApi.getQuizAttempts(quizId);
            setAttempts(data);
            setViewingAttemptsQuizId(quizId);
        } catch (err) {
            setError(err.message || "Failed to load attempts");
        }
    }

    async function handleDelete(quizId, force = false) {
        if (!force && !confirm("Are you sure you want to delete this quiz?")) return;

        try {
            await quizApi.deleteQuiz(quizId, force);
            fetchQuizzes();
        } catch (err) {
            if (err.status === 409 && !force) {
                const confirmMessage = `${err.message}\n\nClick OK to permanently delete this quiz and all its attempt history.`;
                if (confirm(confirmMessage)) {
                    handleDelete(quizId, true);
                }
            } else {
                setError(err.message || "Failed to delete quiz");
            }
        }
    }

    async function handleCreateOrUpdate(form, isEdit) {
        try {
            if (isEdit && editingQuizId) {
                const { exerciseId, ...updateData } = form;
                await quizApi.updateQuiz(editingQuizId, updateData);
                setEditingQuizId(null);
            } else {
                await quizApi.createQuiz(form);
            }
            setShowCreateForm(false);
            await fetchQuizzes();
            resetForm();
        } catch (err) {
            setError(err.message || (isEdit ? "Failed to update quiz" : "Failed to create quiz"));
        }
    }

    function handleEditRequest(quiz) {
        handleEdit(quiz);
    }

    function handleCloseAttempts() {
        setViewingAttemptsQuizId(null);
        setAttempts([]);
    }

    function resetForm() {
        setFormData({
            title: "",
            exerciseId: "",
            pocketMoneyPerQuestion: 5,
            passPercent: 70,
            questions: [
                {
                    text: "",
                    correctChoiceId: "a",
                    orderIndex: 0,
                    choices: [
                        { choiceId: "a", text: "", orderIndex: 0 },
                        { choiceId: "b", text: "", orderIndex: 1 },
                        { choiceId: "c", text: "", orderIndex: 2 }
                    ]
                }
            ]
        });
        setEditingQuizId(null);
    }

    function handleCancelForm() {
        setShowCreateForm(false);
        resetForm();
    }


    if (loading) {
        return <div className="p-6">Loading...</div>;
    }

    return (
        <div className="p-6 max-w-6xl mx-auto">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">Admin Controls</h1>
                <div className="flex gap-3">
                    <button
                        onClick={() => setTestingTradePrediction(true)}
                        className="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700"
                    >
                        Test Trade Prediction Game
                    </button>
                    <button
                        onClick={() => {
                            if (showCreateForm) {
                                handleCancelForm();
                            } else {
                                setShowCreateForm(true);
                                setEditingQuizId(null);
                            }
                        }}
                        className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                    >
                        {showCreateForm ? "Cancel" : "Create New Quiz"}
                    </button>
                </div>
            </div>

            {error && (
                <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
                    {error}
                </div>
            )}

            {showCreateForm && (
                <QuizForm
                    exercises={exercises}
                    onSubmit={handleCreateOrUpdate}
                    onCancel={() => setShowCreateForm(false)}
                    editingQuiz={editingQuizId ? quizzes.find(q => q.id === editingQuizId) : null}
                />
            )}

            <QuizList
                quizzes={quizzes}
                onViewAttempts={handleViewAttempts}
                onEdit={handleEditRequest}
                onDelete={handleDelete}
            />

            {viewingAttemptsQuizId && (
                <AttemptsModal
                    attempts={attempts}
                    onClose={handleCloseAttempts}
                />
            )}

            {testingTradePrediction && (
                <TestTradePredictionModal
                    onClose={() => setTestingTradePrediction(false)}
                />
            )}
        </div>
    );
}
