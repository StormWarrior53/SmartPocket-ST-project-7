import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { authApi, ApiError } from "../../../services/api";
import { useUser } from "../../../context/UserContext";



const initialValues = { email: "", password: "" };

function validate(values) {
    const errors = {};
    if (!values.email.trim()) errors.email = "Email is required";
    else if (!/^\S+@\S+\.\S+$/.test(values.email)) errors.email = "Invalid email format";

    if (!values.password.trim()) errors.password = "Password is required";
    else if (values.password.length < 6) errors.password = "Password must be at least 6 characters long";
    return errors;
}

export default function ParentLogin() {
    const navigate = useNavigate();
    const { login } = useUser();
    const [data, setData] = useState(initialValues);
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [apiError, setApiError] = useState("");
    const [logoutMessage, setLogoutMessage] = useState("");

    useEffect(() => {
        const reason = sessionStorage.getItem('logoutReason');
        if (reason) {
            const messages = {
                'expired': 'Your session has expired. Please log in again.',
                'server-rejected': 'Your session is no longer valid. Please log in again.',
            };
            setLogoutMessage(messages[reason] || 'Please log in to continue.');
            sessionStorage.removeItem('logoutReason');
        }
    }, []);

    const changeHandler = (e) => {
        setData((state) => ({ ...state, [e.target.name]: e.target.value }));
    };

    const submitAction = async (e) => {
        e.preventDefault();
        const validationErrors = validate(data);
        setErrors(validationErrors);
        setApiError("");
        if (Object.keys(validationErrors).length > 0) return;

        setLoading(true);
        try {
            const userData = await authApi.login(data);
            login(userData);
            navigate("/");
        } catch (error) {
            if (error instanceof ApiError) {
                if (error.errors) setErrors(error.errors);
                else setApiError(error.message);
            } else {
                setApiError("An unexpected error occurred. Please try again.");
            }
        } finally {
            setLoading(false);
        }
    };

    const inputClass = (field) =>
        `${errors[field] ? "border-red-500" : "border-gray-300"} w-full px-3 py-2 border rounded-lg`;

    const errorText = (field) => errors[field] && <p className="text-red-600 text-sm mt-1">{errors[field]}</p>;

    return (
        <form className="bg-white p-8 rounded-xl shadow-lg space-y-6" onSubmit={submitAction}>
            <h2 className="text-3xl font-bold text-center text-blue-600">Parent Login</h2>

            {logoutMessage && <div className="bg-yellow-100 border border-yellow-400 text-yellow-800 px-4 py-3 rounded">{logoutMessage}</div>}

            {apiError && <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">{apiError}</div>}

            <div>
                <label className="block text-gray-700 mb-1">Email</label>
                <input
                    type="email"
                    name="email"
                    value={data.email}
                    onChange={changeHandler}
                    className={inputClass("email")}
                    placeholder="Enter email"
                />
                {errorText("email")}
            </div>

            <div>
                <label className="block text-gray-700 mb-1">Password</label>
                <input
                    type="password"
                    name="password"
                    value={data.password}
                    onChange={changeHandler}
                    className={inputClass("password")}
                    placeholder="Enter password"
                />
                {errorText("password")}
            </div>

            <button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
                {loading ? "Logging in..." : "Login"}
            </button>

            <p className="text-center text-sm text-gray-600 mt-2">
                Don't have an account? <Link to="/register" className="text-blue-600 font-medium hover:underline">Register here</Link>
            </p>
        </form>
    );
}