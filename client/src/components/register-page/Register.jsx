import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import { authApi, ApiError } from "../../services/api";
import { useUser } from "../../context/UserContext.jsx";



const initialValues = {
    firstName: "",
    lastName: "",
    email: "",
    password: "",
};

function validate(values) {
    let errors = {};

    // First Name
    if (!values.firstName.trim()) {
        errors.firstName = "First name is required";
    } else if (values.firstName.length < 2) {
        errors.firstName = "First name must be at least 2 characters";
    }

    // Last Name
    if (!values.lastName.trim()) {
        errors.lastName = "Last name is required";
    } else if (values.lastName.length < 2) {
        errors.lastName = "Last name must be at least 2 characters";
    }

    // Email
    if (!values.email.trim()) {
        errors.email = "Email is required";
    } else if (!/^\S+@\S+\.\S+$/.test(values.email)) {
        errors.email = "Invalid email format";
    }

    // Password
    if (!values.password.trim()) {
        errors.password = "Password is required";
    } else if (values.password.length < 6) {
        errors.password = "Password must be at least 6 characters long";
    }

    return errors;
}

export default function Register() {
    const [data, setData] = useState(initialValues);
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [apiError, setApiError] = useState("");

    const { isAuthenticated } = useUser();
    const navigate = useNavigate();

    useEffect(() => {
        if (isAuthenticated) {
            navigate("/", { replace: true });
        }
    }, [isAuthenticated, navigate]);

    if (isAuthenticated) return null;

    const changeHandler = (e) => {
        setData((state) => ({
            ...state,
            [e.target.name]: e.target.value
        }));
    };

    const submitAction = async (e) => {
        e.preventDefault();

        const validationErrors = validate(data);
        setErrors(validationErrors);
        setApiError("");

        if (Object.keys(validationErrors).length > 0) {
            return;
        }

        setLoading(true);

        try {
            await authApi.register(data);
            alert("Registration successful! Please login.");
            navigate("/login");
        } catch (error) {
            if (error instanceof ApiError) {
                if (error.errors) {
                    setErrors(error.errors);
                } else {
                    setApiError(error.message);
                }
            } else {
                setApiError("An unexpected error occurred. Please try again.");
            }
        } finally {
            setLoading(false);
        }
    };

    const inputClass = (field) => {
        return `${errors[field] ? "border-red-500" : "border-gray-300"} 
                w-full px-3 py-2 border rounded-lg`;
    };

    const errorText = (field) =>
        errors[field] && (
            <p className="text-red-600 text-sm mt-1">{errors[field]}</p>
        );

    return (
        <section className="flex flex-col md:flex-row items-center justify-center min-h-screen gap-10 p-6">
            <div className="w-full md:w-1/2 p-10">
                <img
                    src="/images/register.png"
                    alt="Parent registration"
                    className="rounded-xl shadow-lg w-full h-auto object-cover"
                />
            </div>
            <form
                className="mt-10 bg-white p-8 rounded-xl shadow-lg w-full max-w-lg mx-auto space-y-6"
                onSubmit={submitAction}
            >
                <h2 className="text-3xl font-bold text-center text-blue-600">Register</h2>

                {/* API Error */}
                {apiError && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                        {apiError}
                    </div>
                )}

                {/* First Name */}
                <div>
                    <label className="block text-gray-700 mb-1">First Name</label>
                    <input
                        type="text"
                        name="firstName"
                        value={data.firstName}
                        onChange={changeHandler}
                        className={inputClass("firstName")}
                        placeholder="Enter first name"
                    />
                    {errorText("firstName")}
                </div>

                {/* Last Name */}
                <div>
                    <label className="block text-gray-700 mb-1">Last Name</label>
                    <input
                        type="text"
                        name="lastName"
                        value={data.lastName}
                        onChange={changeHandler}
                        className={inputClass("lastName")}
                        placeholder="Enter last name"
                    />
                    {errorText("lastName")}
                </div>

                {/* Email */}
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

                {/* Password */}
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

                {/* Submit */}
                <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    {loading ? "Creating Account..." : "Create Account"}
                </button>

                {/* Already Registered */}
                <p className="text-center text-sm text-gray-600 mt-2">
                    Already have an account?{" "}
                    <Link to="/login" className="text-blue-600 font-medium hover:underline">
                        Log in here
                    </Link>
                </p>
            </form>
        </section>
    );
}