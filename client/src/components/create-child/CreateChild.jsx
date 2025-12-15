import { useState } from "react";
import { useNavigate } from "react-router";
import { useUser } from "../../context/UserContext.jsx";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export default function CreateChild() {
    const { authFetch } = useUser();
    const navigate = useNavigate();

    const [form, setForm] = useState({ name: "", age: "" });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!form.name || !form.age) {
            setError("All fields are required.");
            return;
        }

        try {
            setLoading(true);
            setError("");

            const res = await authFetch(`${API_BASE_URL}/parents/me/children`, {
                method: "POST",
                body: JSON.stringify({
                    name: form.name.trim(),      // must be non-empty
                    age: Number(form.age),       // must be number, 7 <= age <= 24
                })
            });

            if (!res.ok) {
                const errorData = await res.json().catch(() => ({}));
                const errorMessage = errorData.message || errorData.error || "Failed to create child.";
                setError(errorMessage);
                return;
            }

            // Successfully created → redirect back to Profile
            navigate("/profile");

        } catch (err) {
            console.error(err);
            setError("Something went wrong.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <section className="min-h-screen bg-white text-slate-800 py-12 px-6">
            <div className="max-w-lg mx-auto bg-white border border-blue-100 shadow-sm rounded-2xl p-6">
                <h1 className="text-2xl font-bold text-blue-600 mb-6">Create New Child</h1>

                {error && <p className="text-red-500 mb-3">{error}</p>}

                <form onSubmit={handleSubmit} className="space-y-4">

                    <div>
                        <label className="block text-sm font-medium mb-1">Name</label>
                        <input
                            type="text"
                            name="name"
                            value={form.name}
                            onChange={handleChange}
                            className="w-full border p-2 rounded"
                            placeholder="Child's name"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium mb-1">Age</label>
                        <input
                            type="number"
                            name="age"
                            value={form.age}
                            onChange={handleChange}
                            className="w-full border p-2 rounded"
                            placeholder="Child's age"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700"
                    >
                        {loading ? "Creating..." : "Create Child"}
                    </button>

                </form>
            </div>
        </section>
    );
}