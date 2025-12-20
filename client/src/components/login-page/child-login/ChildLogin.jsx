import { useState, useRef, useEffect } from "react";
import { childApi, ApiError } from "../../../services/api";
import { useUser } from "../../../context/UserContext";
import { useNavigate } from "react-router";

const initialValues = { childName: "", parentEmail: "" };

export default function ChildLogin() {
    const navigate = useNavigate();
    const { login } = useUser(); // reuse context to store child session token if backend returns one
    const [data, setData] = useState(initialValues);
    const [pattern, setPattern] = useState([]);
    const [isDrawing, setIsDrawing] = useState(false);
    const [errors, setErrors] = useState({});
    const [apiError, setApiError] = useState("");
    const [loading, setLoading] = useState(false);
    const [setupMode, setSetupMode] = useState(true); // First-time setup toggle
    const [logoutMessage, setLogoutMessage] = useState("");
    const gridRef = useRef(null);

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

    const validate = () => {
        const errs = {};
        if (!data.childName.trim()) errs.childName = "Child name is required";
        if (!data.parentEmail.trim()) errs.parentEmail = "Parent email is required";
        else if (!/^\S+@\S+\.\S+$/.test(data.parentEmail)) errs.parentEmail = "Invalid email format";
        if (pattern.length < 3) errs.pattern = "Connect at least 3 dots";
        return errs;
    };

    const toggleSetupMode = () => {
        setSetupMode((s) => !s);
        setPattern([]);
        setApiError("");
        setErrors({});
    };

    const dotEnter = (n) => {
        if (!isDrawing) return;
        setPattern((prev) => (prev.includes(n) ? prev : [...prev, n]));
    };
    const dotClick = (n) => {
        // Allow click-based selection as well
        setPattern((prev) => (prev.includes(n) ? prev : [...prev, n]));
    };

    const startDraw = () => setIsDrawing(true);
    const endDraw = () => setIsDrawing(false);

    const clearPattern = () => setPattern([]);

    const submitAction = async (e) => {
        e.preventDefault();
        const v = validate();
        setErrors(v);
        setApiError("");
        if (Object.keys(v).length > 0) return;

        setLoading(true);
        const payload = { childName: data.childName.trim(), parentEmail: data.parentEmail.trim(), pattern: pattern.join(",") };
        try {
            let resData;
            if (setupMode) {
                resData = await childApi.setupPattern(payload);
            } else {
                resData = await childApi.login(payload);
            }

            // Build a user object for the header and auth usage
            const token = resData?.token || resData?.accessToken || null;
            const tokenType = resData?.tokenType || "Bearer";
            const firstName = resData?.firstName || resData?.childName || data.childName; // ensure header shows name

            login({
                ...resData,
                firstName,
                email: payload.parentEmail, // keep parentEmail if useful
                token,
                tokenType,
                role: "child",
            });

            alert(setupMode ? "Pattern set successfully!" : "Logged in successfully!");
            // After first-time setup, switch to login mode
            if (setupMode) setSetupMode(false);
            clearPattern();

            // Redirect to home
            navigate("/");
        } catch (error) {
            if (error instanceof ApiError) setApiError(error.message);
            else setApiError("An unexpected error occurred. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const Dot = ({ n }) => (
        <button
            type="button"
            onMouseEnter={() => dotEnter(n)}
            onMouseDown={startDraw}
            onMouseUp={endDraw}
            onTouchStart={startDraw}
            onTouchEnd={endDraw}
            onClick={() => dotClick(n)}
            className={`w-16 h-16 rounded-full border-2 transition ${pattern.includes(n) ? "bg-blue-600 border-blue-700" : "bg-white border-gray-400"
                }`}
            aria-label={`Dot ${n}`}
        />
    );

    return (
        <form className="bg-white p-8 rounded-xl shadow-lg space-y-6" onSubmit={submitAction}>
            <div className="flex items-center justify-between">
                <h2 className="text-3xl font-bold text-blue-600">{setupMode ? "Child Pattern Setup" : "Child Login"}</h2>
                <button type="button" onClick={toggleSetupMode} className="text-sm text-blue-600 underline">
                    {setupMode ? "Switch to Login" : "First-time Setup"}
                </button>
            </div>

            {logoutMessage && <div className="bg-yellow-100 border border-yellow-400 text-yellow-800 px-4 py-3 rounded">{logoutMessage}</div>}

            {apiError && <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">{apiError}</div>}

            <div>
                <label className="block text-gray-700 mb-1">Child Name</label>
                <input
                    type="text"
                    name="childName"
                    value={data.childName}
                    onChange={changeHandler}
                    className={`w-full px-3 py-2 border rounded-lg ${errors.childName ? "border-red-500" : "border-gray-300"}`}
                    placeholder="Enter child's name"
                />
                {errors.childName && <p className="text-red-600 text-sm mt-1">{errors.childName}</p>}
            </div>

            <div>
                <label className="block text-gray-700 mb-1">Parent Email</label>
                <input
                    type="email"
                    name="parentEmail"
                    value={data.parentEmail}
                    onChange={changeHandler}
                    className={`w-full px-3 py-2 border rounded-lg ${errors.parentEmail ? "border-red-500" : "border-gray-300"}`}
                    placeholder="Enter parent email"
                />
                {errors.parentEmail && <p className="text-red-600 text-sm mt-1">{errors.parentEmail}</p>}
            </div>

            <div>
                <label className="block text-gray-700 mb-2">Pattern</label>
                <div
                    ref={gridRef}
                    onMouseLeave={endDraw}
                    className="grid grid-cols-3 gap-6 justify-items-center items-center py-4"
                >
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((n) => <Dot key={n} n={n} />)}
                </div>
                {errors.pattern && <p className="text-red-600 text-sm mt-1">{errors.pattern}</p>}
                <div className="flex justify-between mt-2">
                    <div className="text-sm text-gray-600">Selected: {pattern.join(" → ") || "-"}</div>
                    <button type="button" onClick={clearPattern} className="text-sm text-gray-700 underline">Clear</button>
                </div>
            </div>

            <button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
                {loading ? (setupMode ? "Setting pattern..." : "Logging in...") : (setupMode ? "Set Pattern" : "Login")}
            </button>
        </form>
    );
}