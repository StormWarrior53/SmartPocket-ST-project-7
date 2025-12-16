import { useEffect, useState } from "react";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export default function EditChild({ child, authFetch, onClose, onSaved }) {
    const [name, setName] = useState("");
    const [age, setAge] = useState("");

    useEffect(() => {
        if (!child) return;
        // prefer explicit name field, fall back to firstName if present
        setName(child.name ?? child.firstName ?? "");
        setAge(child.age ?? "");
    }, [child]);

    if (!child) return null;

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const payload = { name, age: age === "" ? null : Number(age) };
            const res = await authFetch(`${API_BASE_URL}/parents/me/children/${child.id}`, {
                method: "PUT",
                body: JSON.stringify(payload),
            });

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                alert(err.message || err.error || "Failed to update child.");
                return;
            }

            const updated = await res.json().catch(() => null);
            if (updated) onSaved(updated); else onClose();
        } catch (err) {
            console.error("Update child error", err);
            alert("Something went wrong while updating the child.");
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
            <div className="absolute inset-0 bg-black opacity-40" onClick={onClose} />
            <form onSubmit={handleSubmit} className="relative bg-white rounded-lg p-6 z-10 w-full max-w-md">
                <h3 className="text-xl font-semibold mb-4">Edit Child</h3>

                <div className="mb-3">
                    <label className="block text-sm text-slate-700 mb-1">Name</label>
                    <input
                        className="w-full border rounded px-2 py-1"
                        value={name}
                        type="text"
                        onChange={(e) => setName(e.target.value)}
                        required
                    />
                </div>

                <div className="mb-3">
                    <label className="block text-sm text-slate-700 mb-1">Age</label>
                    <input
                        className="w-full border rounded px-2 py-1"
                        value={age}
                        type="number"
                        min="0"
                        onChange={(e) => setAge(e.target.value === "" ? "" : Number(e.target.value))}
                        required
                    />
                </div>

                <div className="flex justify-end gap-2 mt-4">
                    <button type="button" onClick={onClose} className="px-4 py-2 rounded bg-gray-200">Cancel</button>
                    <button type="submit" className="px-4 py-2 rounded bg-blue-600 text-white">Save</button>
                </div>
            </form>
        </div>
    );
}