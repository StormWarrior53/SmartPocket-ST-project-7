import { useState, useEffect } from "react";
import { useUser } from "../../context/UserContext.jsx";
import { useNavigate } from "react-router";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
const GAME_START_BUDGET = 10;
const MIN_BUDGET_REQUIRED = 10;

export default function TradePredictionGame({ testMode = false, testConfig = null }) {
    const { user, authFetch, loading: userLoading, isAuthenticated } = useUser();
    const navigate = useNavigate();

    // Game state
    const [gameStarted, setGameStarted] = useState(false);
    const [currentBudget, setCurrentBudget] = useState(GAME_START_BUDGET);
    const [startingAmount, setStartingAmount] = useState(GAME_START_BUDGET);
    const [guessCount, setGuessCount] = useState(0);
    const [result, setResult] = useState(null);
    const [message, setMessage] = useState("");
    const [statistics, setStatistics] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [childInfo, setChildInfo] = useState(null);

    // Predefined pattern for trade predictions
    const TRADE_PATTERN = ["BUY", "SELL", "BUY", "BUY", "SELL", "SELL", "BUY", "SELL"];

    // Check if child and has budget
    useEffect(() => {
        if (testMode) {
            // In test mode, set dummy child info with sufficient budget
            setChildInfo({
                pocketMoney: 100,
                xp: 0,
                name: "Test User"
            });
            setLoading(false);
            return;
        }

        if (userLoading || !isAuthenticated || !user?.id) return;

        const loadChildInfo = async () => {
            setLoading(true);
            setError("");
            try {
                // Only accessible if child profile
                const res = await authFetch(`${API_BASE_URL}/children/me`);
                if (!res.ok) {
                    throw new Error("This game is only for child profiles");
                }

                const data = await res.json();
                setChildInfo(data);

                if ((data.pocketMoney ?? 0) < MIN_BUDGET_REQUIRED) {
                    setError(`You need at least €${MIN_BUDGET_REQUIRED} to play. Current balance: €${data.pocketMoney ?? 0}`);
                }
            } catch (e) {
                setError(e.message || "Failed to load child info. This game is only for child profiles.");
            } finally {
                setLoading(false);
            }
        };

        loadChildInfo();
    }, [testMode, userLoading, isAuthenticated, user?.id, authFetch]);

    // Generate random modifier for this guess
    const generateModifier = () => {
        const types = ['multiply2', 'multiply3', 'add10', 'subtract10', 'divide2', 'divide3'];
        return types[Math.floor(Math.random() * types.length)];
    };

    // Apply modifier to current budget
    const applyModifier = (modifier, amount) => {
        switch (modifier) {
            case 'multiply2':
                return amount * 2;
            case 'multiply3':
                return amount * 3;
            case 'add10':
                return amount + 10;
            case 'subtract10':
                return Math.max(0, amount - 10);
            case 'divide2':
                return Math.floor(amount / 2);
            case 'divide3':
                return Math.floor(amount / 3);
            default:
                return amount;
        }
    };

    // Format modifier display
    const getModifierDisplay = (modifier) => {
        switch (modifier) {
            case 'multiply2':
                return '×2';
            case 'multiply3':
                return '×3';
            case 'add10':
                return '+€10';
            case 'subtract10':
                return '-€10';
            case 'divide2':
                return '÷2';
            case 'divide3':
                return '÷3';
            default:
                return '';
        }
    };

    const startGame = () => {
        if ((childInfo?.pocketMoney ?? 0) < MIN_BUDGET_REQUIRED) {
            setError(`You need at least €${MIN_BUDGET_REQUIRED} to play`);
            return;
        }
        setGameStarted(true);
        setCurrentBudget(GAME_START_BUDGET);
        setStartingAmount(GAME_START_BUDGET);
        setGuessCount(0);
        setStatistics([]);
        setResult(null);
        setMessage("");
    };

    const play = (prediction) => {
        // Get the actual result from the pattern based on guess count
        const actual = TRADE_PATTERN[guessCount % TRADE_PATTERN.length];
        const modifier = generateModifier();
        const newBudget = applyModifier(modifier, currentBudget);

        setResult(actual);
        setGuessCount(guessCount + 1);

        // Add to statistics
        const stat = {
            guess: guessCount + 1,
            prediction,
            actual,
            modifier,
            budgetBefore: currentBudget,
            budgetAfter: newBudget,
            isCorrect: prediction === actual,
        };

        if (prediction === actual) {
            setCurrentBudget(newBudget);
            setMessage(`Correct! Budget: €${currentBudget} → €${newBudget} (${getModifierDisplay(modifier)})`);
        } else {
            setMessage(`Wrong! You predicted ${prediction} but it was ${actual}. Budget stays: €${currentBudget}`);
        }

        setStatistics([...statistics, stat]);
    };

    const leaveGame = async () => {
        if (!gameStarted || guessCount === 0) {
            setGameStarted(false);
            setStatistics([]);
            return;
        }

        setLoading(true);
        try {
            // Calculate XP based on number of correct guesses
            const correctGuesses = statistics.filter(s => s.isCorrect).length;
            const xpToAdd = correctGuesses * 5; // 5 XP per correct guess

            // In test mode, skip API calls
            if (testMode) {
                setMessage(`Test Mode: You got ${correctGuesses} correct predictions and would gain ${xpToAdd} XP!`);
                setGameStarted(false);
                setStatistics([]);
                setLoading(false);
                return;
            }

            // Add XP
            if (xpToAdd > 0) {
                const xpRes = await authFetch(`${API_BASE_URL}/children/me/xp`, {
                    method: "PATCH",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ xp: xpToAdd }),
                });
                if (!xpRes.ok) throw new Error("Failed to add XP");
            }

            // Calculate final amount gained/lost
            const amountGained = currentBudget - startingAmount;

            // Add to pocket money
            const pocketRes = await authFetch(`${API_BASE_URL}/children/me/pocket`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ amount: Math.max(0, amountGained) }),
            });
            if (!pocketRes.ok) throw new Error("Failed to update pocket money");

            setMessage(`Game Over! You gained €${Math.max(0, amountGained)} and ${xpToAdd} XP!`);
            setGameStarted(false);
            setStatistics([]);

            // Refresh child info
            const childRes = await authFetch(`${API_BASE_URL}/children/me`);
            if (childRes.ok) {
                const data = await childRes.json();
                setChildInfo(data);
            }
        } catch (e) {
            setError(e.message || "Failed to save game results");
        } finally {
            setLoading(false);
        }
    };

    if (userLoading && !testMode) {
        return (
            <section className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 to-indigo-200 px-6">
                <div className="text-center">
                    <p className="text-slate-700">Loading...</p>
                </div>
            </section>
        );
    }

    if (!testMode && (!isAuthenticated || !user?.id)) {
        return (
            <section className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 to-indigo-200 px-6">
                <div className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-md text-center">
                    <p className="text-red-600">Please log in as a child to play this game.</p>
                    <button
                        onClick={() => navigate("/login")}
                        className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                    >
                        Go to Login
                    </button>
                </div>
            </section>
        );
    }

    if (!testMode && error && !gameStarted && (!childInfo || (childInfo.pocketMoney ?? 0) < MIN_BUDGET_REQUIRED)) {
        return (
            <section className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 to-indigo-200 px-6">
                <div className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-md text-center">
                    <h1 className="text-3xl font-bold text-red-600 mb-4">Cannot Play</h1>
                    <p className="text-slate-700 mb-6">{error}</p>
                    <button
                        onClick={() => navigate("/profile")}
                        className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                    >
                        Go to Profile
                    </button>
                </div>
            </section>
        );
    }

    return (
        <section className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 to-indigo-200 px-6 py-8">
            <div className="w-full max-w-2xl">
                {!gameStarted ? (
                    <div className="bg-white p-8 rounded-2xl shadow-xl text-center">
                        <h1 className="text-3xl font-bold text-blue-600 mb-4">
                            Trade Prediction Game
                        </h1>

                        <div className="mb-6 space-y-3">
                            <p className="text-slate-700">
                                Test your trading skills and multiply your budget!
                            </p>
                            <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
                                <p className="text-sm text-slate-600">Current Balance:</p>
                              <p className="text-2xl font-bold text-blue-600">€{childInfo?.pocketMoney ?? 0}</p>
                            </div>
                            <p className="text-slate-600 text-sm">Minimum required: €{MIN_BUDGET_REQUIRED}</p>
                        </div>

                        <button
                            onClick={startGame}
                            disabled={loading || (childInfo?.pocketMoney ?? 0) < MIN_BUDGET_REQUIRED}
                            className="w-full px-6 py-3 bg-green-500 text-white rounded-xl hover:bg-green-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition font-semibold"
                        >
                            {loading ? "Loading..." : "Start Game"}
                        </button>
                    </div>
                ) : (
                    <div className="space-y-6">
                        {/* Game Header */}
                        <div className="bg-white p-6 rounded-2xl shadow-xl">
                            <div className="grid grid-cols-3 gap-4 text-center mb-6">
                                <div className="bg-blue-50 p-3 rounded-lg">
                                    <p className="text-xs text-slate-600">Started With</p>
                                    <p className="text-2xl font-bold text-blue-600">€{startingAmount}</p>
                                </div>
                                <div className="bg-indigo-50 p-3 rounded-lg">
                                    <p className="text-xs text-slate-600">Current Budget</p>
                                    <p className="text-2xl font-bold text-indigo-600">€{currentBudget}</p>
                                </div>
                                <div className="bg-purple-50 p-3 rounded-lg">
                                    <p className="text-xs text-slate-600">Guesses</p>
                                    <p className="text-2xl font-bold text-purple-600">{guessCount}</p>
                                </div>
                            </div>

                            <p className="text-lg text-center font-semibold text-slate-700 mb-6">
                                {message || "Make a prediction!"}
                            </p>

                            <div className="flex justify-center gap-6 mb-6">
                                <button
                                    onClick={() => play("BUY")}
                                    className="px-8 py-3 bg-green-500 text-white rounded-xl hover:bg-green-600 transition font-semibold text-lg"
                                >
                                    BUY
                                </button>

                                <button
                                    onClick={() => play("SELL")}
                                    className="px-8 py-3 bg-red-500 text-white rounded-xl hover:bg-red-600 transition font-semibold text-lg"
                                >
                                    SELL
                                </button>
                            </div>

                            {result && (
                                <p className="text-lg font-semibold text-center mb-4">
                                    Result: <span className="text-blue-600">{result}</span>
                                </p>
                            )}

                            <button
                                onClick={leaveGame}
                                disabled={loading}
                                className="w-full px-6 py-3 bg-orange-500 text-white rounded-xl hover:bg-orange-600 disabled:bg-gray-400 disabled:cursor-not-allowed transition font-semibold"
                            >
                                {loading ? "Processing..." : "Leave Game & Save Earnings"}
                            </button>
                        </div>

                        {/* Statistics */}
                        {statistics.length > 0 && (
                            <div className="bg-white p-6 rounded-2xl shadow-xl">
                                <h2 className="text-2xl font-bold text-blue-600 mb-4">Trade History</h2>
                                <div className="overflow-x-auto">
                                    <table className="w-full text-sm">
                                        <thead>
                                            <tr className="bg-blue-50 border-b-2 border-blue-200">
                                                <th className="px-4 py-2 text-left">Trade</th>
                                                <th className="px-4 py-2 text-left">Prediction</th>
                                                <th className="px-4 py-2 text-left">Result</th>
                                                <th className="px-4 py-2 text-left">Modifier</th>
                                                <th className="px-4 py-2 text-left">Budget</th>
                                                <th className="px-4 py-2 text-left">Status</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            {statistics.map((stat, idx) => (
                                                <tr
                                                    key={idx}
                                                    className={`border-b ${
                                                        stat.isCorrect
                                                            ? "bg-green-50"
                                                            : "bg-red-50"
                                                    }`}
                                                >
                                                    <td className="px-4 py-2 font-semibold">
                                                        #{stat.guess}
                                                    </td>
                                                    <td className="px-4 py-2">
                                                        {stat.prediction}
                                                    </td>
                                                    <td className="px-4 py-2">
                                                        {stat.actual}
                                                    </td>
                                                    <td className="px-4 py-2 font-semibold text-indigo-600">
                                                        {getModifierDisplay(stat.modifier)}
                                                    </td>
                                                    <td className="px-4 py-2">
                                                        €{stat.budgetBefore} → €{stat.budgetAfter}
                                                    </td>
                                                    <td className="px-4 py-2">
                                                        {stat.isCorrect ? (
                                                            <span className="text-green-600 font-semibold">Correct</span>
                                                        ) : (
                                                            <span className="text-red-600 font-semibold">Wrong</span>
                                                        )}
                                                    </td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </section>
    );
}
