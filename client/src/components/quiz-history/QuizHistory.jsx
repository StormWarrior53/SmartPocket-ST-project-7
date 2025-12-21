import { useState, useEffect } from "react";
import { quizApi } from "../../services/api.js";
import { Link } from "react-router";

export default function QuizHistory() {
    const [attempts, setAttempts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchAttempts();
    }, []);

    async function fetchAttempts() {
        try {
            setLoading(true);
            const data = await quizApi.getMyQuizAttempts();
            setAttempts(data);
            setError(null);
        } catch (err) {
            setError(err.message || "Failed to load quiz history");
        } finally {
            setLoading(false);
        }
    }

    if (loading) {
        return <div className="p-6">Loading quiz history...</div>;
    }

    return (
        <div className="p-6 max-w-4xl mx-auto">
            <h1 className="text-3xl font-bold mb-6">My Quiz History</h1>

            {error && (
                <div className="mb-4 p-4 bg-red-100 text-red-700 rounded">
                    {error}
                </div>
            )}

            {attempts.length === 0 ? (
                <div className="text-center py-12">
                    <p className="text-gray-500 mb-4">You haven't taken any quizzes yet!</p>
                    <Link to="/roadmap" className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
                        Browse Exercises
                    </Link>
                </div>
            ) : (
                <div className="space-y-4">
                    {attempts.map(attempt => (
                        <div key={attempt.id} className="p-6 border rounded-lg bg-white shadow">
                            <div className="flex justify-between items-start mb-2">
                                <div>
                                    <h3 className="text-xl font-semibold">{attempt.quizTitle}</h3>
                                    <p className="text-gray-600">
                                        {new Date(attempt.attemptedAt).toLocaleDateString()} at{' '}
                                        {new Date(attempt.attemptedAt).toLocaleTimeString()}
                                    </p>
                                </div>
                                <div className={`px-4 py-2 rounded font-bold ${
                                    attempt.passed
                                        ? 'bg-green-100 text-green-800'
                                        : 'bg-red-100 text-red-800'
                                }`}>
                                    {attempt.passed ? 'PASSED' : 'FAILED'}
                                </div>
                            </div>

                            <div className="grid grid-cols-3 gap-4 mt-4">
                                <div className="text-center p-4 bg-gray-50 rounded">
                                    <p className="text-gray-600 text-sm">Score</p>
                                    <p className="text-2xl font-bold">
                                        {attempt.correctAnswers}/{attempt.totalQuestions}
                                    </p>
                                </div>
                                <div className="text-center p-4 bg-gray-50 rounded">
                                    <p className="text-gray-600 text-sm">Percentage</p>
                                    <p className="text-2xl font-bold">{attempt.scorePercent}%</p>
                                </div>
                                <div className="text-center p-4 bg-gray-50 rounded">
                                    <p className="text-gray-600 text-sm">Pocket Money Earned</p>
                                    <p className="text-2xl font-bold text-green-600">
                                        {attempt.pocketMoneyAwarded}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
