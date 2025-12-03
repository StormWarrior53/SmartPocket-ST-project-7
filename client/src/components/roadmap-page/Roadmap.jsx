export default function Roadmap() {
    // STATIC JSON DATA (your backend shape simplified)
    const modules = [
        { title: "Budgeting Essentials", path: "budget", description: "Learn the basics of managing money!" },
        { title: "Expense Tracking", path: "budget", description: "Understand where your money goes." },
        { title: "Debt Management", path: "budget", description: "Learn how to handle and avoid debt." },
        { title: "Investing Fundamentals", path: "invest", description: "Start learning how investing works!" },
        { title: "Stock Market Basics", path: "invest", description: "See how stocks move and grow." },
        { title: "Portfolio Building", path: "invest", description: "Build your own mini-portfolio!" },
    ];

    // Separate the data into budget and invest paths
    const budgetModules = modules.filter(m => m.path === "budget");
    const investModules = modules.filter(m => m.path === "invest");

    return (
        <div className="w-full flex flex-col items-center p-6 bg-gray-50">

            {/* Intro Module */}
            <div className="w-full max-w-3xl bg-blue-200 p-6 rounded-3xl shadow-lg mb-10 text-center">
                <h2 className="text-3xl font-bold mb-2 flex justify-center items-center gap-2 text-blue-900">
                    🚀 Entry Module
                </h2>

                <p className="text-gray-700 mb-4 text-lg">10 pocketMoney | 30 minutes</p>

                <p className="mb-4 text-gray-800 text-md">
                    Learn the basics of money: saving, spending, and preparing for your adventure!
                </p>

                <div className="font-bold text-blue-900 text-xl">Finance Basics 🌟</div>
            </div>

            {/* Split Section */}
            <div className="w-full max-w-5xl bg-blue-900 text-white p-6 rounded-3xl shadow text-center mb-10">
                <h2 className="text-3xl font-bold flex items-center justify-center gap-2">
                    🎯 Choose Your Path
                </h2>
                <p className="mt-2 text-blue-100 text-lg">
                    Pick your financial adventure: Budgeting or Investing!
                </p>
            </div>

            {/* Paths */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-5xl">

                {/* Budgeting Path */}
                <div className="bg-white p-6 rounded-3xl shadow border border-gray-200">
                    <h3 className="text-2xl font-bold mb-4 text-center text-blue-900 flex gap-2 justify-center">
                        💰 Budgeting Track
                    </h3>

                    <div className="space-y-4">
                        {budgetModules.map((module, i) => (
                            <div
                                key={i}
                                className="p-4 border rounded-2xl shadow-sm bg-blue-50"
                            >
                                <h4 className="font-semibold text-blue-900">{module.title}</h4>
                                <p className="text-sm text-gray-600">{module.description}</p>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Investing Path */}
                <div className="bg-white p-6 rounded-3xl shadow border border-gray-200">
                    <h3 className="text-2xl font-bold mb-4 text-center text-blue-900 flex gap-2 justify-center">
                        📈 Investing Track
                    </h3>

                    <div className="space-y-4">
                        {investModules.map((module, i) => (
                            <div
                                key={i}
                                className="p-4 border rounded-2xl shadow-sm bg-blue-50"
                            >
                                <h4 className="font-semibold text-blue-900">{module.title}</h4>
                                <p className="text-sm text-gray-600">{module.description}</p>
                            </div>
                        ))}
                    </div>
                </div>

            </div>
        </div>
    );
}