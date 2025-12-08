import { Link } from "react-router";

export default function Footer() {
    return (
        <footer className="bg-blue-600 shadow-sm">
            <div className="max-w-5xl mx-auto py-8 px-6 sm:px-12 text-center space-y-4">

                <h2 className="text-2xl font-bold text-white">SmartPocket</h2>

                <p className="text-white/90 leading-relaxed">
                    Helping kids learn financial literacy early through fun challenges and games.
                </p>

                <div className="flex justify-center gap-6 mt-4">
                    <Link to="mailto:support@smartpocket.com" className="text-white hover:underline">
                        Email
                    </Link>
                    <Link to="/facebook" className="text-white hover:underline">
                        Facebook
                    </Link>
                    <Link to="/x" className="text-white hover:underline">
                        X
                    </Link>
                    <Link to="/about" className="text-white hover:underline">
                        About us
                    </Link>
                </div>

                <p className="text-white/70 text-sm mt-4">
                    &copy; {new Date().getFullYear()} SmartPocket. All rights reserved.
                </p>
            </div>
        </footer>
    );
}