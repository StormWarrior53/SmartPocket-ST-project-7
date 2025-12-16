import { useEffect, useState } from "react";
import { Link } from "react-router";
import { useUser } from "../../../context/UserContext.jsx";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export default function LeaderboardBadges() {
    const { authFetch, loading: userLoading, user: currentUser } = useUser();
    const [topUsers, setTopUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (userLoading) return;

        const fetchTopChildren = async () => {
            setLoading(true);
            setError("");
            try {
                const res = await authFetch(`${API_BASE_URL}/children`);
                if (!res.ok) {
                    const err = await res.json().catch(() => ({}));
                    setError(err.message || "Failed to fetch children.");
                    setTopUsers([]);
                    return;
                }
                const data = await res.json();
                const sorted = Array.isArray(data)
                    ? data.sort((a, b) => (b.xp || 0) - (a.xp || 0)).slice(0, 5)
                    : [];
                setTopUsers(sorted);
            } catch (e) {
                console.error(e);
                setError("Something went wrong while fetching children.");
                setTopUsers([]);
            } finally {
                setLoading(false);
            }
        };

        fetchTopChildren();
    }, [authFetch, userLoading]);

    return (
        <section className="flex flex-col lg:flex-row gap-6">
            <div className="flex-1 bg-blue-50 border border-blue-100 shadow-sm rounded-2xl p-8">
                <h2 className="text-2xl font-bold text-blue-600 text-center">Top XP Kids</h2>
                <div className="mt-6 overflow-x-auto">
                    {loading ? (
                        <p className="text-center">Loading...</p>
                    ) : error ? (
                        <p className="text-red-500 text-center">{error}</p>
                    ) : (
                        <table className="w-full table-auto text-left border-collapse">
                            <thead>
                                <tr className="bg-blue-100">
                                    <th className="px-4 py-2">#</th>
                                    <th className="px-4 py-2">Child</th>
                                    <th className="px-4 py-2">XP</th>
                                </tr>
                            </thead>
                            <tbody>
                                {/* {topUsers.map((user, index) => (
                                    <tr key={user.id ?? index} className={index % 2 === 0 ? "bg-white" : "bg-blue-50"}>
                                        <td className="px-4 py-2 font-medium">{index + 1}</td>
                                        <td className="px-4 py-2">{user.name ?? `${user.firstName ?? ""} ${user.lastName ?? ""}`}</td>
                                        <td className="px-4 py-2 font-semibold text-blue-600">{user.xp ?? 0}</td>
                                    </tr>
                                ))} */}
                                {topUsers.map((user, index) => {
                                    const displayName = user.name ?? `${user.firstName ?? ""} ${user.lastName ?? ""}`.trim();
                                    const isMe =
                                        (currentUser?.id && user.id === currentUser.id) ||
                                        (!!currentUser?.name && displayName?.toLowerCase() === currentUser.name?.toLowerCase());

                                    const rowClass = isMe
                                        ? "bg-blue-100/70 text-blue-900 ring-1 ring-blue-300"
                                        : index % 2 === 0
                                            ? "bg-white"
                                            : "bg-blue-50";

                                    return (
                                        <tr key={user.id ?? index} className={`transition ${rowClass}`}>
                                            <td className="px-4 py-2 font-medium">{index + 1}</td>
                                            <td className="px-4 py-2 font-semibold">{displayName || "Unnamed"}</td>
                                            <td className="px-4 py-2 font-semibold text-blue-600">{user.xp ?? 0}</td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    )}
                </div>
                <div className="mt-4 text-center">
                    <Link to={"/leaderboard"}>
                        <button className="text-blue-600 font-semibold hover:underline">See Full Leaderboard</button>
                    </Link>
                </div>
            </div>
        </section>
    )
}