import { Link } from "react-router";

export default function Roadmap() {
    return (
        <section className="bg-blue-50 border border-blue-100 shadow-sm rounded-2xl p-12 flex flex-col md:flex-row items-center gap-8">
            <div className="flex-1 text-center md:text-left">
                <h2 className="text-2xl sm:text-3xl font-bold text-blue-600">
                    Follow Your Child's Money Learning Roadmap
                </h2>
                <p className="mt-4 text-slate-700 text-lg sm:text-xl">
                    Track progress, unlock lessons, and make learning about budgeting and investing fun!
                </p>
                <div className="mt-6">
                    <Link to="/roadmap">
                        <button className="bg-blue-600 text-white font-semibold px-6 py-3 rounded-xl shadow hover:bg-blue-700 transition">
                            Go to Roadmap
                        </button>
                    </Link>
                </div>
            </div>

            <div className="flex-1 flex justify-center md:justify-end">
                <img
                    src="/images/roadmap.png"
                    alt="Child learning roadmap"
                    className="w-full max-w-md rounded-2xl border border-blue-100 shadow-sm object-cover"
                />
            </div>
        </section>
    );
}