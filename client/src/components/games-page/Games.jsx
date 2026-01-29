import { Link } from "react-router";

export default function Games() {
    return (
        <section
            className="min-h-screen bg-cover bg-center py-12 px-6 sm:px-12"
            style={{ backgroundImage: "url('/images/games2.png')" }}
        >
            <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-10">

                {/* Budget Game */}
                <div className="backdrop-blur-lg bg-white/50 p-8 rounded-2xl shadow-lg w-full md:w-1/3 text-center">
                    <h2 className="text-2xl font-bold text-blue-600">Budget Game</h2>
                    <p className="mt-4 text-slate-700">
                        Learn how to manage money by making smart budgeting choices.
                        Fully interactive and fun!
                    </p>

                    <Link to="/budget-game">
                        <button className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-xl shadow hover:bg-blue-700 transition">
                            Play Now
                        </button>
                    </Link>
                </div>

                {/* Center Image */}
                <div className="flex justify-center w-full md:w-1/3 mt-10">
                    <img
                        src="/images/games.png"
                        alt="Boy thinking"
                        className="max-w-xs drop-shadow-xl"
                    />
                </div>

                {/* Trade Prediction Game */}
                <div className="backdrop-blur-lg bg-white/50 p-8 rounded-2xl shadow-lg w-full md:w-1/3 text-center">
                    <h2 className="text-2xl font-bold text-blue-600">Trade Prediction</h2>
                    <p className="mt-4 text-slate-700">
                        Predict future market moves and earn points.
                    </p>

                    <Link to="/trade-prediction">
                        <button className="mt-4 px-6 py-2 bg-indigo-600 text-white rounded-xl shadow hover:bg-indigo-700 transition">
                            Play Now
                        </button>
                    </Link>
                </div>

            </div>
        </section>
    );
}
