import { useEffect, useState } from "react";
import { useUser } from "../../../context/UserContext.jsx";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export default function ChildProfile() {
    const { user, authFetch, isAuthenticated, loading } = useUser();

    const [childInfo, setChildInfo] = useState({
        name: "",
        age: null,
        xp: 0,
        pocketMoney: 0,
        allowanceMoney: 0,
        parentEmail: "",
    });
    const [infoLoading, setInfoLoading] = useState(true);

    const [inventory, setInventory] = useState([]);
    const [invLoading, setInvLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (loading || !isAuthenticated) return;

        const loadInfo = async () => {
            setInfoLoading(true);
            try {
                const res = await authFetch(`${API_BASE_URL}/children/me`);
                if (!res.ok) {
                    const err = await res.json().catch(() => ({}));
                    throw new Error(err.message || err.error || "Failed to load child info");
                }
                const data = await res.json();
                setChildInfo({
                    name: data.name ?? "",
                    age: data.age ?? null,
                    xp: Number(data.xp ?? 0),
                    pocketMoney: Number(data.pocketMoney ?? 0),
                    allowanceMoney: Number(data.allowanceMoney ?? 0),
                    parentEmail: data.parentEmail ?? user?.email ?? "",
                });
            } catch (e) {
                // Fallback: still show parent's email from context
                setChildInfo((prev) => ({ ...prev, parentEmail: user?.email ?? prev.parentEmail }));
                setError(e.message || "Failed to load child info");
            } finally {
                setInfoLoading(false);
            }
        };

        const loadInventory = async () => {
            setInvLoading(true);
            setError("");
            try {
                const res = await authFetch(`${API_BASE_URL}/children/me/inventory`);
                if (!res.ok) {
                    const err = await res.json().catch(() => ({}));
                    throw new Error(err.message || err.error || "Failed to load inventory");
                }
                const data = await res.json();
                setInventory(Array.isArray(data) ? data : []);
            } catch (e) {
                setError(e.message || "Failed to load inventory");
                setInventory([]);
            } finally {
                setInvLoading(false);
            }
        };

        loadInfo();
        loadInventory();
    }, [authFetch, isAuthenticated, loading, user?.email]);

    if (loading) return <p>Loading...</p>;
    if (!isAuthenticated) return <p>You must be logged in to view your profile.</p>;

    return (
        <section className="min-h-screen bg-white text-slate-800 py-12 px-6 sm:px-12">
            <div className="max-w-5xl mx-auto space-y-8">
                <div className="flex flex-col items-center space-y-2">
                    <img
                        src="/images/profile.png"
                        alt="Profile"
                        className="w-32 h-32 rounded-full border-4 border-blue-100 shadow-sm object-cover"
                    />
                    <h1 className="text-3xl font-bold text-blue-600">
                        {infoLoading ? "…" : (childInfo.name || "My")}
                    </h1>
                    {childInfo.age != null && (
                        <h3 className="text-lg font-medium text-slate-700">Age: {childInfo.age}</h3>
                    )}
                    {(childInfo.parentEmail) && (
                        <p className="text-slate-700">Parent: {childInfo.parentEmail}</p>
                    )}
                    <div className="flex gap-6 mt-2">
                        <div className="bg-indigo-50 border border-indigo-100 rounded-lg px-4 py-2">
                            <span className="text-slate-600 text-sm">XP</span>
                            <div className="text-indigo-700 font-semibold">
                                {infoLoading ? "…" : childInfo.xp}
                            </div>
                        </div>
                        <div className="bg-blue-50 border border-blue-100 rounded-lg px-4 py-2">
                            <span className="text-slate-600 text-sm">Pocket Money</span>
                            <div className="text-blue-700 font-semibold">
                                {infoLoading ? "…" : childInfo.pocketMoney}
                            </div>
                        </div>
                        <div className="bg-green-50 border border-green-100 rounded-lg px-4 py-2">
                            <span className="text-slate-600 text-sm">Allowance Money</span>
                            <div className="text-green-700 font-semibold">
                                {infoLoading ? "…" : childInfo.allowanceMoney}
                            </div>
                        </div>
                    </div>
                </div>

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-bold text-blue-600">My Inventory</h2>
                    </div>

                    {invLoading ? (
                        <p>Loading inventory...</p>
                    ) : error ? (
                        <p className="text-red-600">{error}</p>
                    ) : inventory.length === 0 ? (
                        <p>No items in inventory.</p>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            {inventory.map((item, idx) => (
                                <div key={item.id || idx} className="border rounded-lg p-4">
                                    <h3 className="font-semibold text-blue-600">
                                        {item.name || item.productName || `Item ${idx + 1}`}
                                    </h3>
                                    {"quantity" in item && (
                                        <p className="text-slate-700 mt-1">Quantity: {item.quantity}</p>
                                    )}
                                    {"price" in item && (
                                        <p className="text-slate-700">Price: {item.price}</p>
                                    )}
                                    <details className="mt-2">
                                        <summary className="text-sm text-slate-500">Details</summary>
                                        <pre className="text-xs bg-slate-50 p-2 rounded border mt-1 overflow-x-auto">
                                            {JSON.stringify(item, null, 2)}
                                        </pre>
                                    </details>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </section>
    );
}