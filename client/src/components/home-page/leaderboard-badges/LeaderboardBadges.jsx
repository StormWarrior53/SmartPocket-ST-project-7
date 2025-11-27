import { Link } from "react-router";

const topUsers = Array.from({ length: 5 }, (_, i) => ({
    name: `User${i + 1}`,
    xp: Math.floor(Math.random() * 5000),
})).sort((a, b) => b.xp - a.xp);

const badges = [
    { name: "Savings Star", icon: "/images/step3.png" },
    { name: "Budget Boss", icon: "/images/step3.png" },
    { name: "Goal Getter", icon: "/images/step3.png" },
];

export default function LeaderboardBadges() {
    return (
        <section className="flex flex-col lg:flex-row gap-6">
            <div className="flex-1 bg-blue-50 border border-blue-100 shadow-sm rounded-2xl p-8">
                <h2 className="text-2xl font-bold text-blue-600 text-center">Top XP Kids</h2>
                <div className="mt-6 overflow-x-auto">
                    <table className="w-full table-auto text-left border-collapse">
                        <thead>
                            <tr className="bg-blue-100">
                                <th className="px-4 py-2">#</th>
                                <th className="px-4 py-2">Child</th>
                                <th className="px-4 py-2">XP</th>
                            </tr>
                        </thead>
                        <tbody>
                            {topUsers.map((user, index) => (
                                <tr key={index} className={index % 2 === 0 ? "bg-white" : "bg-blue-50"}>
                                    <td className="px-4 py-2 font-medium">{index + 1}</td>
                                    <td className="px-4 py-2">{user.name}</td>
                                    <td className="px-4 py-2 font-semibold text-blue-600">{user.xp}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
                <div className="mt-4 text-center">
                    <Link to={"/leaderboard"}>
                        <button className="text-blue-600 font-semibold hover:underline">See Full Leaderboard</button>
                    </Link>
                </div>
            </div>

            {/* Badges */}
            <div className="flex-1 bg-white border border-blue-100 shadow-sm rounded-2xl p-8">
                <h2 className="text-2xl font-bold text-blue-600 text-center">Badges & Rewards</h2>
                <div className="mt-6 flex flex-wrap justify-center gap-8">
                    {badges.map((badge, i) => (
                        <div key={i} className="flex flex-col items-center">
                            <img src={badge.icon} alt={badge.name} className="w-20 h-20 mb-2" />
                            <p className="text-slate-700 font-medium">{badge.name}</p>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    )
}