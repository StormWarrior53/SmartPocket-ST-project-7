import { useState } from "react";

export default function TradePredictionGame() {
    const [result, setResult] = useState(null);
    const [message, setMessage] = useState("");
    const [score, setScore] = useState(0);

    const play = (prediction) => {
        const actual = Math.random() > 0.5 ? "UP" : "DOWN";
        setResult(actual);

        if (prediction === actual) {
            setScore(score + 10);
            setMessage("✅ Correct! You earned 10 points.");
        } else {
            setMessage("❌ Wrong prediction. Try again!");
        }
    };

    return (
        <section className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 to-indigo-200 px-6">
            <div className="bg-white p-8 rounded-2xl shadow-xl w-full max-w-md text-center">
                <h1 className="text-3xl font-bold text-blue-600 mb-4">
                    📊 Trade Prediction Game
                </h1>

                <p className="text-slate-700 mb-6">
                    Predict if the market will go up or down.
                </p>

                <div className="flex justify-center gap-6 mb-6">
                    <button
                        onClick={() => play("UP")}
                        className="px-6 py-3 bg-green-500 text-white rounded-xl hover:bg-green-600 transition"
                    >
                        📈 UP
                    </button>

                    <button
                        onClick={() => play("DOWN")}
                        className="px-6 py-3 bg-red-500 text-white rounded-xl hover:bg-red-600 transition"
                    >
                        📉 DOWN
                    </button>
                </div>

                {result && (
                    <p className="text-lg font-semibold mb-2">
                        Market move: <span className="text-blue-600">{result}</span>
                    </p>
                )}

                <p className="mb-4">{message}</p>

                <div className="text-xl font-bold text-indigo-600">
                    Score: {score}
                </div>
            </div>
        </section>
    );
}
