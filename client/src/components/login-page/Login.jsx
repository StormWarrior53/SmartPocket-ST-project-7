import { useEffect, useState } from "react";
import ParentLogin from "./parent-login/ParentLogin.jsx";
import ChildLogin from "./child-login/ChildLogin.jsx";
import { useUser } from "../../context/UserContext.jsx";
import { useNavigate } from "react-router";

export default function Login() {
    const [mode, setMode] = useState("parent"); // 'parent' | 'child'

    const { isAuthenticated } = useUser();
    const navigate = useNavigate();

    useEffect(() => {
        if (isAuthenticated) {
            navigate("/", { replace: true });
        }
    }, [isAuthenticated, navigate]);

    if (isAuthenticated) return null;

    return (
        <section className="flex flex-col md:flex-row items-center justify-center min-h-screen gap-10 p-6">
            <div className="w-full md:w-1/2 p-10">
                <img
                    src="/images/register.png"
                    alt="Login"
                    className="rounded-xl shadow-lg w-full h-auto object-cover"
                />
            </div>

            <div className="w-full max-w-xl mx-auto">
                <div className="flex justify-center mb-6">
                    <div className="inline-flex bg-gray-100 rounded-lg overflow-hidden">
                        <button
                            className={`px-4 py-2 ${mode === "parent" ? "bg-blue-600 text-white" : "text-gray-700"}`}
                            onClick={() => setMode("parent")}
                        >
                            Parent Login
                        </button>
                        <button
                            className={`px-4 py-2 ${mode === "child" ? "bg-blue-600 text-white" : "text-gray-700"}`}
                            onClick={() => setMode("child")}
                        >
                            Child Login
                        </button>
                    </div>
                </div>

                {mode === "parent" ? <ParentLogin /> : <ChildLogin />}
            </div>
        </section>
    );
}