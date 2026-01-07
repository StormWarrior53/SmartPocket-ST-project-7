import { useState, useEffect } from "react";
import { quizApi } from "../../services/api.js";
import { useUser } from "../../context/UserContext.jsx";
import { useNavigate } from "react-router";

export default function QuizAdmin() {
    const [quizzes, setQuizzes] = useState([]);
    const [exercises, setExercises] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [editingQuizId, setEditingQuizId] = useState(null);
    const [viewingAttemptsQuizId, setViewingAttemptsQuizId] = useState(null);
    const [attempts, setAttempts] = useState([]);

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

    async function handleSubmit(e) {
        e.preventDefault();
        try {
            if (editingQuizId) {
                const { exerciseId, ...updateData } = formData;
                await quizApi.updateQuiz(editingQuizId, updateData);
                setEditingQuizId(null);
            } else {
                await quizApi.createQuiz(formData);
            }
            setShowCreateForm(false);
            fetchQuizzes();
            resetForm();
        } catch (err) {
            setError(err.message || editingQuizId ? "Failed to update quiz" : "Failed to create quiz");
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

    function addQuestion() {
        setFormData({
            ...formData,
            questions: [
                ...formData.questions,
                {
                    text: "",
                    correctChoiceId: "a",
                    orderIndex: formData.questions.length,
                    choices: [
                        { choiceId: "a", text: "", orderIndex: 0 },
                        { choiceId: "b", text: "", orderIndex: 1 },
                        { choiceId: "c", text: "", orderIndex: 2 }
                    ]
                }
            ]
        });
    }

    function removeQuestion(index) {
        const newQuestions = formData.questions.filter((_, i) => i !== index);
        setFormData({ ...formData, questions: newQuestions });
    }

    function updateQuestion(index, field, value) {
        const newQuestions = [...formData.questions];
        newQuestions[index] = { ...newQuestions[index], [field]: value };
        setFormData({ ...formData, questions: newQuestions });
    }

    function updateChoice(qIndex, cIndex, field, value) {
        const newQuestions = [...formData.questions];
        newQuestions[qIndex].choices[cIndex] = {
            ...newQuestions[qIndex].choices[cIndex],
            [field]: value
        };
        setFormData({ ...formData, questions: newQuestions });
    }

    if (loading) {
        return <div className="p-6">Loading...</div>;
    }

    return (
        <div className="p-6 max-w-6xl mx-auto">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-3xl font-bold">Quiz Management</h1>
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

            {error && (
                <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
                    {error}
                </div>
            )}

            {showCreateForm && (
                <div className="mb-8 p-6 border rounded-lg bg-white shadow">
                    <h2 className="text-2xl font-bold mb-4">
                        {editingQuizId ? "Edit Quiz" : "Create New Quiz"}
                    </h2>
                    <form onSubmit={handleSubmit}>
                        <div className="mb-4">
                            <label className="block mb-2 font-semibold">Quiz Title</label>
                            <input
                                type="text"
                                value={formData.title}
                                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                                className="w-full p-2 border rounded"
                                required
                            />
                        </div>

                        {!editingQuizId && (
                            <div className="mb-4">
                                <label className="block mb-2 font-semibold">Exercise</label>
                                <select
                                    value={formData.exerciseId}
                                    onChange={(e) => setFormData({ ...formData, exerciseId: e.target.value })}
                                    className="w-full p-2 border rounded"
                                    required
                                >
                                    <option value="">Select an exercise</option>
                                    {exercises.map(ex => (
                                        <option key={ex.id} value={ex.id}>{ex.title}</option>
                                    ))}
                                </select>
                            </div>
                        )}

                        <div className="grid grid-cols-2 gap-4 mb-4">
                            <div>
                                <label className="block mb-2 font-semibold">Pocket Money per Question</label>
                                <input
                                    type="number"
                                    value={formData.pocketMoneyPerQuestion}
                                    onChange={(e) => setFormData({ ...formData, pocketMoneyPerQuestion: parseInt(e.target.value) })}
                                    className="w-full p-2 border rounded"
                                    min="1"
                                    required
                                />
                            </div>
                            <div>
                                <label className="block mb-2 font-semibold">Pass Percentage</label>
                                <input
                                    type="number"
                                    value={formData.passPercent}
                                    onChange={(e) => setFormData({ ...formData, passPercent: parseInt(e.target.value) })}
                                    className="w-full p-2 border rounded"
                                    min="0"
                                    max="100"
                                    required
                                />
                            </div>
                        </div>

                        <div className="mb-4">
                            <div className="flex justify-between items-center mb-2">
                                <h3 className="text-xl font-semibold">Questions</h3>
                                <button
                                    type="button"
                                    onClick={addQuestion}
                                    className="px-3 py-1 bg-green-600 text-white rounded text-sm"
                                >
                                    Add Question
                                </button>
                            </div>

                            {formData.questions.map((question, qIndex) => (
                                <div key={qIndex} className="mb-6 p-4 border rounded bg-gray-50">
                                    <div className="flex justify-between items-center mb-2">
                                        <h4 className="font-semibold">Question {qIndex + 1}</h4>
                                        {formData.questions.length > 1 && (
                                            <button
                                                type="button"
                                                onClick={() => removeQuestion(qIndex)}
                                                className="px-2 py-1 bg-red-600 text-white rounded text-sm"
                                            >
                                                Remove
                                            </button>
                                        )}
                                    </div>

                                    <input
                                        type="text"
                                        value={question.text}
                                        onChange={(e) => updateQuestion(qIndex, 'text', e.target.value)}
                                        placeholder="Enter question text"
                                        className="w-full p-2 border rounded mb-2"
                                        required
                                    />

                                    <div className="mb-2">
                                        <label className="block mb-1 text-sm font-semibold">Correct Answer</label>
                                        <select
                                            value={question.correctChoiceId}
                                            onChange={(e) => updateQuestion(qIndex, 'correctChoiceId', e.target.value)}
                                            className="w-full p-2 border rounded"
                                            required
                                        >
                                            <option value="a">A</option>
                                            <option value="b">B</option>
                                            <option value="c">C</option>
                                        </select>
                                    </div>

                                    <div className="space-y-2">
                                        {question.choices.map((choice, cIndex) => (
                                            <div key={cIndex} className="flex gap-2 items-center">
                                                <span className="font-semibold">{choice.choiceId.toUpperCase()}:</span>
                                                <input
                                                    type="text"
                                                    value={choice.text}
                                                    onChange={(e) => updateChoice(qIndex, cIndex, 'text', e.target.value)}
                                                    placeholder={`Choice ${choice.choiceId.toUpperCase()}`}
                                                    className="flex-1 p-2 border rounded"
                                                    required
                                                />
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            ))}
                        </div>

                        <button
                            type="submit"
                            className="w-full px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                        >
                            {editingQuizId ? "Update Quiz" : "Create Quiz"}
                        </button>
                    </form>
                </div>
            )}

            <div>
                <h2 className="text-2xl font-bold mb-4">Existing Quizzes</h2>
                {quizzes.length === 0 ? (
                    <p className="text-gray-500">No quizzes found. Create one to get started!</p>
                ) : (
                    <div className="space-y-4">
                        {quizzes.map(quiz => (
                            <div key={quiz.id} className="p-4 border rounded bg-white shadow">
                                <div className="flex justify-between items-start">
                                    <div>
                                        <h3 className="text-xl font-semibold">{quiz.title}</h3>
                                        <p className="text-gray-600">Questions: {quiz.questions.length}</p>
                                        <p className="text-gray-600">
                                            Reward: {quiz.pocketMoneyPerQuestion} per question |
                                            Pass: {quiz.passPercent}%
                                        </p>
                                    </div>
                                    <div className="flex gap-2">
                                        <button
                                            onClick={() => handleViewAttempts(quiz.id)}
                                            className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700"
                                        >
                                            View Attempts
                                        </button>
                                        <button
                                            onClick={() => handleEdit(quiz)}
                                            className="px-3 py-1 bg-yellow-600 text-white rounded hover:bg-yellow-700"
                                        >
                                            Edit
                                        </button>
                                        <button
                                            onClick={() => handleDelete(quiz.id)}
                                            className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700"
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {viewingAttemptsQuizId && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto p-6">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-2xl font-bold">Quiz Attempts</h2>
                            <button
                                onClick={() => {
                                    setViewingAttemptsQuizId(null);
                                    setAttempts([]);
                                }}
                                className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
                            >
                                Close
                            </button>
                        </div>

                        {attempts.length === 0 ? (
                            <p className="text-gray-500">No attempts yet for this quiz.</p>
                        ) : (
                            <div className="space-y-4">
                                {attempts.map(attempt => (
                                    <div key={attempt.id} className="p-4 border rounded bg-gray-50">
                                        <div className="grid grid-cols-2 gap-4">
                                            <div>
                                                <p className="font-semibold">Child: {attempt.childName}</p>
                                                <p className="text-gray-600">
                                                    Date: {new Date(attempt.attemptedAt).toLocaleString()}
                                                </p>
                                            </div>
                                            <div>
                                                <p className="font-semibold">
                                                    Score: {attempt.correctAnswers}/{attempt.totalQuestions} ({attempt.scorePercent}%)
                                                </p>
                                                <p className={`font-semibold ${attempt.passed ? 'text-green-600' : 'text-red-600'}`}>
                                                    {attempt.passed ? 'PASSED' : 'FAILED'}
                                                </p>
                                                <p className="text-gray-600">
                                                    Pocket Money: {attempt.pocketMoneyAwarded}
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
