export default function About() {
    return (
        <section className="min-h-screen bg-white text-slate-800 py-12 px-6 sm:px-12">
            <div className="max-w-5xl mx-auto space-y-10">

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8 flex flex-col md:flex-row items-center gap-8">
                    <div className="flex-1">
                        <h2 className="text-2xl font-bold text-blue-600">Our Mission</h2>
                        <p className="mt-4 text-slate-600 leading-relaxed">
                            SmartPocket helps kids understand money in a fun, safe, and easy way. We want
                            to build positive financial habits early by teaching important concepts through
                            kid-friendly activities and games.
                        </p>
                    </div>
                    <div className="flex-1 flex justify-center">
                        <img
                            src="/images/about1.png"
                            alt="Kids learning about money"
                            className="rounded-xl border border-blue-100 shadow-sm"
                        />
                    </div>
                </div>

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8 flex flex-col md:flex-row items-center gap-8">
                    <div className="flex-1 flex justify-center">
                        <img
                            src="/images/about2.png"
                            alt="Kids learning finance concepts"
                            className="rounded-xl border border-blue-100 shadow-sm"
                        />
                    </div>
                    <div className="flex-1">
                        <h2 className="text-2xl font-bold text-blue-600">What Kids Learn</h2>
                        <ul className="mt-4 space-y-2 text-slate-700">
                            <li>• What money is and how it’s used</li>
                            <li>• Saving and setting goals</li>
                            <li>• Needs vs. wants</li>
                            <li>• Earning money through simple tasks</li>
                            <li>• Basic budgeting skills</li>
                        </ul>
                    </div>

                </div>

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8 flex flex-col md:flex-row items-center gap-8">
                    <div className="flex-1">
                        <h2 className="text-2xl font-bold text-blue-600">How It Works</h2>
                        <p className="mt-4 text-slate-600">Kids learn by playing mini-games and quizzes designed to teach financial concepts.</p>
                        <ol className="mt-4 list-decimal list-inside space-y-2 text-slate-700">
                            <li>Short games and quizzes (3–7 minutes)</li>
                            <li>Fun missions that unlock badges and stickers</li>
                            <li>Visual progress tracking for motivation</li>
                            <li>Optional parent dashboard for goal supervision</li>
                        </ol>
                    </div>

                    <div className="flex-1 flex justify-center md:justify-end">
                        <img
                            src="/images/about4.png"
                            alt="Kids exploring financial concepts"
                            className="rounded-xl border border-blue-100 shadow-sm w-full max-w-lg"
                        />
                    </div>
                </div>

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8 text-center">
                    <h2 className="text-2xl font-bold text-blue-600">For Parents</h2>
                    <ul className="mt-4 space-y-2 text-slate-700">
                        <li>• To have an easy-to-use tool for monitoring the child’s progress.</li>
                        <li>• To encourage the child’s efforts through real rewards.</li>
                        <li>• To integrate financial education into the family’s daily life.</li>
                        <li>• To have access to rankings, statistics, and reports that show the child’s progress and achievements.</li>
                    </ul>
                </div>

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8 flex flex-col md:flex-row items-center gap-8">
                    <div className="flex-1 flex justify-center">
                        <img
                            src="/images/about3.png"
                            alt="Kids learning finance concepts"
                            className="rounded-xl border border-blue-100 shadow-sm w-full max-w-lg"
                        />
                    </div>
                    <div className="flex-1">
                        <h2 className="text-2xl font-bold text-blue-600">Why We Built This</h2>
                        <p className="mt-4 text-slate-600 leading-relaxed">
                            Financial literacy is an essential life skill, yet most people learn it too late.
                            SmartPocket was created to help children understand money early—through fun
                            challenges and simple explanations that build confidence.
                        </p>
                    </div>
                </div>

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-8 text-center">
                    <h2 className="text-2xl font-bold text-blue-600">Contact Us</h2>
                    <p className="mt-4 text-slate-600">Have questions or feedback? We’d love to hear from you!</p>
                    <p className="text-slate-700 mt-2">Email: support@smartpocket.com</p>
                </div>
            </div>
        </section>
    );
}