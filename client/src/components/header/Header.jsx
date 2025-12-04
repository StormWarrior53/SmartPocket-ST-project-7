import { Link, useNavigate } from "react-router";
import { useUser } from "../../context/UserContext";
import { useState } from "react";

export default function Header() {
    const { user, logout, isAuthenticated } = useUser();
    const navigate = useNavigate();

    const [open, setOpen] = useState(false);

    const handleLogout = () => {
        logout();
        navigate("/");
        setOpen(false);
    };

    return (
        <header className="bg-blue-600 text-white shadow-md">
            <div className="max-w-7xl mx-auto flex justify-between items-center px-6 py-4">

                {/* Logo */}
                <Link to="/" className="flex items-center gap-3">
                    <img
                        src="/images/smart-pocket-logo.png"
                        alt="SmartPocket Logo"
                        className="w-12 h-12 object-contain"
                    />
                    <span className="text-2xl font-bold tracking-wide">
                        SmartPocket
                    </span>
                </Link>

                {/* Hamburger Button (mobile only) */}
                <button
                    className="md:hidden text-white"
                    onClick={() => setOpen(!open)}
                >
                    {!open ? (
                        /* Hamburger icon */
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="w-8 h-8"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            strokeWidth={2}
                        >
                            <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
                        </svg>
                    ) : (
                        /* Close icon */
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="w-8 h-8"
                            fill="none"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                            strokeWidth={2}
                        >
                            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    )}
                </button>

                {/* Desktop Navigation */}
                <nav className="hidden md:flex items-center gap-6 text-lg font-medium">
                    <Link to="/games" className="hover:text-blue-200">Games</Link>
                    <Link to="/store" className="hover:text-blue-200">Store</Link>
                    <Link to="/leaderboard" className="hover:text-blue-200">Leaderboard</Link>

                    {isAuthenticated ? (
                        <>
                            <Link to="/profile" className="hover:text-blue-200">Profile</Link>
                            <span className="text-blue-200">Hello, {user?.firstName}!</span>
                            <button onClick={handleLogout} className="hover:text-blue-200">
                                Logout
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="hover:text-blue-200">Login</Link>
                            <Link to="/register" className="hover:text-blue-200">Register</Link>
                        </>
                    )}
                </nav>
            </div>

            {/* Mobile Dropdown */}
            {open && (
                <div className="md:hidden bg-blue-700 text-white px-6 py-4 space-y-4 text-lg font-medium animate-slideDown">

                    <Link to="/games" onClick={() => setOpen(false)} className="block hover:text-blue-200">
                        Games
                    </Link>

                    <Link to="/store" onClick={() => setOpen(false)} className="block hover:text-blue-200">
                        Store
                    </Link>

                    <Link to="/leaderboard" onClick={() => setOpen(false)} className="block hover:text-blue-200">
                        Leaderboard
                    </Link>

                    {isAuthenticated ? (
                        <>
                            <Link to="/profile" onClick={() => setOpen(false)} className="block hover:text-blue-200">
                                Profile
                            </Link>

                            <span className="block text-blue-200">
                                Hello, {user?.firstName}!
                            </span>

                            <button onClick={handleLogout} className="block hover:text-blue-200">
                                Logout
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" onClick={() => setOpen(false)} className="block hover:text-blue-200">
                                Login
                            </Link>

                            <Link to="/register" onClick={() => setOpen(false)} className="block hover:text-blue-200">
                                Register
                            </Link>
                        </>
                    )}
                </div>
            )}
        </header>
    );
}
