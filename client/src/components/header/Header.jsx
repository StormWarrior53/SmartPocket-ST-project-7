import { Link, useNavigate } from "react-router";
import { useUser } from "../../context/UserContext";

export default function Header() {
    const { user, logout, isAuthenticated } = useUser();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate("/");
    };

    return (
        <header className="bg-blue-600 text-white shadow-md">
            <div className="max-w-7xl mx-auto w-full flex items-center justify-between py-4 px-6">

                <div className="flex items-center gap-3">
                    <Link to="/" className="flex items-center gap-3">
                        <img
                            src="/images/smart-pocket-logo.png"
                            alt="SmartPocket Logo"
                            className="w-12 h-12 object-contain"
                        />
                        <span className="text-2xl font-bold tracking-wide">SmartPocket</span>
                    </Link>
                </div>

                <div className="flex items-center gap-6 text-lg font-medium">
                    <Link to="/games" className="hover:text-blue-200 transition">Games</Link>
                    <Link to="/store" className="hover:text-blue-200 transition">Store</Link>
                    <Link to="/leaderboard" className="hover:text-blue-200 transition">Leaderboard</Link>

                    {isAuthenticated ? (
                        <>
                            <Link to="/profile" className="hover:text-blue-200 transition">
                                Profile
                            </Link>
                            <span className="text-blue-200">
                                Hello, {user?.firstName}!
                            </span>
                            <button
                                onClick={handleLogout}
                                className="hover:text-blue-200 transition cursor-pointer"
                            >
                                Logout
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="hover:text-blue-200 transition">Login</Link>
                            <Link to="/register" className="hover:text-blue-200 transition">Register</Link>
                        </>
                    )}
                </div>
            </div>
        </header>
    );
}
