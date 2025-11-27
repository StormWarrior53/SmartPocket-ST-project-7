export default function Leaderboard() {
    // Example data
    // TODO: Fetch children from API
    const users = Array.from({ length: 100 }, (_, i) => ({
        name: `User${i + 1}`,
        xp: Math.floor(Math.random() * 5000),
    })).sort((a, b) => b.xp - a.xp);

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
                                    key={index}
                                    className={index % 2 === 0 ? "bg-white" : "bg-blue-50"}
                                >
                                    <td className="px-4 py-2 font-medium">{index + 1}</td>
                                    <td className="px-4 py-2">{user.name}</td>
                                    <td className="px-4 py-2 font-semibold text-blue-600">{user.xp}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
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