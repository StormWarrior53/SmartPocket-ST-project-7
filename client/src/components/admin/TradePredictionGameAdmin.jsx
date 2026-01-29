import { useState, useEffect } from "react";
import { useUser } from "../../context/UserContext.jsx";
import { useNavigate } from "react-router";
import TradePredictionGame from "../games/TradePredictionGame.jsx";

export default function TradePredictionGameAdmin() {
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

    return (
        <div className="p-6 max-w-6xl mx-auto">
            <div className="mb-6">
                <h1 className="text-3xl font-bold text-blue-600 mb-2">Trade Prediction Game Admin</h1>
                <p className="text-slate-600">Test and verify all Trade Prediction Game features</p>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-6">
                <h2 className="text-lg font-semibold text-blue-900 mb-3">Features to Test:</h2>
                <ul className="list-disc list-inside space-y-2 text-slate-700">
                    <li>Budget requirement check (€10 minimum)</li>
                    <li>BUY/SELL prediction buttons</li>
                    <li>Random modifiers (×2, ×3, +€10, -€10, ÷2, ÷3)</li>
                    <li>Correct/Wrong prediction handling</li>
                    <li>XP calculation (5 XP per correct guess)</li>
                    <li>Pocket money updates</li>
                    <li>Trade history statistics display</li>
                    <li>Leave game and save earnings</li>
                </ul>
            </div>

            <div className="bg-white border border-slate-200 rounded-lg shadow-lg p-6">
                <div className="mb-4">
                    <h2 className="text-xl font-semibold text-slate-800">Game Test Area</h2>
                    <p className="text-sm text-slate-600 mt-1">This is a test instance with dummy data. No real data will be saved.</p>
                </div>
                <TradePredictionGame testMode={true} testConfig={null} />
            </div>
        </div>
    );
}
