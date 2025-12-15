import { useEffect, useState } from "react";
import { useUser } from "../../context/UserContext.jsx";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export default function Leaderboard() {
    const { authFetch, loading: userLoading } = useUser();
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (userLoading) return;

        const fetchChildren = async () => {
            setLoading(true);
            setError("");
            try {
                const res = await authFetch(`${API_BASE_URL}/children`);
                if (!res.ok) {
                    const errData = await res.json().catch(() => ({}));
                    setError(errData.message || "Failed to fetch children.");
                    return;
                }
                const data = await res.json();
                const sorted = Array.isArray(data)
                    ? data.sort((a, b) => (b.xp || 0) - (a.xp || 0)).slice(0, 100)
                    : [];
                setUsers(sorted);
            } catch (e) {
                console.error(e);
                setError("Something went wrong while fetching children.");
            } finally {
                setLoading(false);
            }
        };

        fetchChildren();
    }, [authFetch, userLoading]);

    if (loading || userLoading) {
        return (
            <section className="min-h-screen bg-white text-slate-800 py-12 px-6 sm:px-12">
                <div className="max-w-5xl mx-auto">
                    <p>Loading leaderboard...</p>
                </div>
            </section>
        );
    }

    return (
        <section className="min-h-screen bg-white text-slate-800 py-12 px-6 sm:px-12">
            <div className="max-w-5xl mx-auto space-y-10">
                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8 text-center">
                    <h2 className="text-2xl font-bold text-blue-600">Top 100 XP Leaders</h2>
                    <p className="mt-2 text-slate-600">
                        Check out the players with the highest XP and see where you stand!
                    </p>
                </div>

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-6 overflow-x-auto">
                    {error ? (
                        <p className="text-red-500">{error}</p>
                    ) : (
                        <table className="w-full table-auto text-left border-collapse">
                            <thead>
                                <tr className="bg-blue-50">
                                    <th className="px-4 py-2 text-slate-700">#</th>
                                    <th className="px-4 py-2 text-slate-700">User</th>
                                    <th className="px-4 py-2 text-slate-700">XP</th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.map((user, index) => (
                                    <tr
                                        key={user.id ?? index}
                                        className={index % 2 === 0 ? "bg-white" : "bg-blue-50"}
                                    >
                                        <td className="px-4 py-2 font-medium">{index + 1}</td>
                                        <td className="px-4 py-2">{user.name ?? `${user.firstName ?? ""} ${user.lastName ?? ""}`}</td>
                                        <td className="px-4 py-2 font-semibold text-blue-600">{user.xp ?? 0}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8 text-center">
                    <p className="text-slate-600">
                        Keep playing mini-games and completing challenges to climb the leaderboard!
                    </p>
                </div>
            </div>
        </section>
    )
}