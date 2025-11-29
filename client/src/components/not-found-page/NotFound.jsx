import { Link } from "react-router";

export default function NotFound() {
    return (
        <section className="min-h-screen flex flex-col items-center justify-center bg-blue-50 p-6">
            <img
                src="/images/404.png"
                alt="404 - Not Found"
                className="w-full max-w-3xl mb-8 drop-shadow-lg"
            />

            <h1 className="text-3xl font-bold text-blue-800 mb-4">
                Oops! Page Not Found
            </h1>

            <p className="text-lg text-gray-600 mb-6">
                Looks like this page wandered off with the missing coins!
            </p>

            <Link
                to="/"
                className="px-6 py-3 rounded-xl bg-orange-500 text-white font-semibold hover:bg-orange-600 transition-all shadow-md"
            >
                Go Back Home
            </Link>
        </section>
    )
}