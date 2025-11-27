import { Link } from "react-router";

export default function Hero() {
    return (
        <section className="bg-blue-50 border border-blue-100 shadow-sm rounded-2xl p-12">
            <div className="flex flex-col md:flex-row items-center gap-8">
                {/* Image Section */}
                <div className="flex-1 flex justify-center md:justify-end">
                    <img
                        src="/images/home1.png"
                        alt="Kids learning finance concepts"
                        className="w-full max-w-md rounded-2xl border border-blue-100 shadow-sm object-cover"
                    />
                </div>
                {/* Text Section */}
                <div className="flex-1 text-center md:text-left">
                    <h1 className="text-3xl sm:text-4xl font-bold text-blue-600">
                        Help Your Child Learn Money Skills Through Fun Games!
                    </h1>
                    <p className="mt-4 text-slate-700 text-lg sm:text-xl">
                        Create an account for your child, track their progress, and watch them earn badges and XP while learning financial skills.
                    </p>
                    <div className="mt-6 flex flex-col sm:flex-row justify-center md:justify-start gap-4">
                        <Link to="/register">
                        <button className="bg-blue-600 text-white font-semibold px-6 py-3 rounded-xl shadow hover:bg-blue-700 transition">
                            Create Parent Account
                        </button>
                        </Link>
                        <Link to="/about">
                            <button className="bg-white border border-blue-600 text-blue-600 font-semibold px-6 py-3 rounded-xl shadow hover:bg-blue-50 transition">
                                Learn About Us
                            </button>
                        </Link>
                    </div>
                </div>


            </div>
        </section>
    )
}