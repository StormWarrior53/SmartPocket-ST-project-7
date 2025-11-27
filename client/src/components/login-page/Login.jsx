import { useState } from "react";
import { Link } from "react-router";

const initialValues = {
    email: "",
    password: "",
};

function validate(values) {
    let errors = {};

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

export default function Login() {
    const [data, setData] = useState(initialValues);
    const [errors, setErrors] = useState({});

    const changeHandler = (e) => {
        setData((state) => ({
            ...state,
            [e.target.name]: e.target.value,
        }));
    };

    const submitAction = () => {
        const errors = validate(data);
        setErrors(errors);

        if (Object.keys(errors).length > 0) {
            return;
        }

        console.log("Login submitted:", data);
        // In future: send POST → backend /api/parents/login
    };

    const inputClass = (field) =>
        `${errors[field] ? "border-red-500" : "border-gray-300"} w-full px-3 py-2 border rounded-lg`;

    const errorText = (field) =>
        errors[field] && <p className="text-red-600 text-sm mt-1">{errors[field]}</p>;

    return (
        <section className="flex flex-col md:flex-row items-center justify-center min-h-screen gap-10 p-6">
            <div className="w-full md:w-1/2 p-10">
                <img
                    src="/images/register.png"
                    alt="Parent login"
                    className="rounded-xl shadow-lg w-full h-auto object-cover"
                />
            </div>
            <form
                className="mt-10 bg-white p-8 rounded-xl shadow-lg w-full max-w-lg mx-auto space-y-6"
                action={submitAction}
            >
                <h2 className="text-3xl font-bold text-center text-blue-600">Login</h2>

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
                <button className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition">
                    Login
                </button>

                {/* Register link */}
                <p className="text-center text-sm text-gray-600 mt-2">
                    Don't have an account?{" "}
                    <Link to="/register" className="text-blue-600 font-medium hover:underline">
                        Register here
                    </Link>
                </p>
            </form>
        </section>
    );
}
