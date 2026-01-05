import { useState, useEffect } from "react";
import { useParams, Link } from "react-router";
import { quizApi } from "../../services/api.js";

export default function Quiz() {
    const { id } = useParams();
    const [quiz, setQuiz] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [answers, setAnswers] = useState({});
    const [result, setResult] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        async function fetchQuiz() {
            try {
                setLoading(true);
                const data = await quizApi.getQuizByExerciseId(id);
                setQuiz(data);
                setError(null);
            } catch (err) {
                setError(err.message || "Failed to load quiz");
            } finally {
                setLoading(false);
            }
        }
        fetchQuiz();
    }, [id]);

    if (loading) {
        return <div className="p-6 max-w-3xl mx-auto">Loading quiz...</div>;
    }

    if (error) {
        return (
            <div className="p-6 max-w-3xl mx-auto">
                <h2 className="text-2xl font-bold mb-4">Error</h2>
                <p className="mb-4 text-red-500">{error}</p>
                <Link to={`/roadmap/${id}`} className="px-4 py-2 bg-gray-200 rounded">Back</Link>
            </div>
        );
    }

    if (!quiz) {
        return (
            <div className="p-6 max-w-3xl mx-auto">
                <h2 className="text-2xl font-bold mb-4">No Quiz Found</h2>
                <p className="mb-4">This lecture does not have a quiz yet.</p>
                <Link to={`/roadmap/${id}`} className="px-4 py-2 bg-gray-200 rounded">Back</Link>
            </div>
        );
    }

    function selectAnswer(qId, choiceId) {
        setAnswers(prev => ({ ...prev, [qId]: choiceId }));
    }

    async function submitQuiz() {
        try {
            setSubmitting(true);
            const result = await quizApi.submitQuiz(quiz.id, answers);
            setResult(result);
            setError(null);
        } catch (err) {
            setError(err.message || "Failed to submit quiz");
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <h2 className="text-2xl font-bold mb-4">{quiz.title}</h2>

            <div className="space-y-6">
                {quiz.questions.map(q => (
                    <div key={q.id} className="p-4 border rounded">
                        <div className="font-semibold mb-2">{q.text}</div>
                        <div className="flex flex-col gap-2">
                            {q.choices.map(choice => (
                                <label key={choice.id} className="inline-flex items-center gap-2">
                                    <input
                                        type="radio"
                                        name={`q-${q.id}`}
                                        checked={answers[q.id] === choice.id}
                                        onChange={() => selectAnswer(q.id, choice.id)}
                                    />
                                    <span>{choice.text}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            <div className="mt-6 flex gap-3">
                <button
                    onClick={submitQuiz}
                    disabled={submitting}
                    className="px-4 py-2 bg-green-600 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {submitting ? "Submitting..." : "Submit Quiz"}
                </button>

                <Link to={`/roadmap/${id}`} className="px-4 py-2 bg-gray-200 rounded">Back</Link>
            </div>

            {result && (
                <div className="mt-6 p-4 border rounded bg-white">
                    <h3 className="font-bold mb-2">Result</h3>
                    <p>Score: {result.correct} / {result.total} ({result.percent}%)</p>
                    <p className="font-semibold">{result.message}</p>
                    <p className="mt-2">PocketMoney awarded: <span className="font-bold">{result.awarded}</span></p>
                </div>
            )}
        </div>
    );
}