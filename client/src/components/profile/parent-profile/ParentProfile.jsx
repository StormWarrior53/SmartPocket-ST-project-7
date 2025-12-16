import { useEffect, useState } from "react";
import { useUser } from "../../../context/UserContext.jsx";
import { Link } from "react-router";
import ChildCard from "../child-card/ChildCard.jsx";
import EditChild from "../edit-modal/EditChild.jsx";

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

export default function ParentProfile() {
    const { user, authFetch, loading: userLoading, isAuthenticated } = useUser();
    const [children, setChildren] = useState([]);
    const [loading, setLoading] = useState(true);

    const [isEditOpen, setIsEditOpen] = useState(false);
    const [selectedChild, setSelectedChild] = useState(null);

    useEffect(() => {
        if (userLoading) return;
        if (!isAuthenticated) return;

        const fetchChildren = async () => {
            try {
                const res = await authFetch(`${API_BASE_URL}/parents/me/children`);

                if (!res.ok) {
                    console.error("Failed to fetch children");
                    setChildren([]);
                    return;
                }

                const data = await res.json();

                if (Array.isArray(data) && data.length > 0) {
                    const inventories = await Promise.all(
                        data.map(async (child) => {
                            try {
                                const invRes = await authFetch(`${API_BASE_URL}/parents/me/children/${child.id}/inventory`);
                                if (!invRes.ok) return [];
                                const inv = await invRes.json();
                                return Array.isArray(inv) ? inv : [];
                            } catch (err) {
                                console.error("Failed to fetch inventory for", child.id, err);
                                return [];
                            }
                        })
                    );

                    const combined = data.map((c, i) => ({ ...c, inventory: inventories[i] || [] }));
                    setChildren(combined);
                } else {
                    setChildren(Array.isArray(data) ? data.map(c => ({ ...c, inventory: [] })) : []);
                }
            } catch (e) {
                console.error("Error fetching children", e);
                setChildren([]);
            } finally {
                setLoading(false);
            }
        };

        fetchChildren();
    }, [authFetch, userLoading, isAuthenticated]);

    if (loading || userLoading) return <p>Loading...</p>;
    if (!isAuthenticated) return <p>You must be logged in to view children.</p>;
    if (!user) return <p>Loading...</p>;

    const handleRemoveChild = async (childId) => {
        if (!confirm("Are you sure you want to delete this child? This action cannot be undone.")) return;
        try {
            setLoading(true);
            const res = await authFetch(`${API_BASE_URL}/parents/me/children/${childId}`, {
                method: "DELETE",
            });

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                const msg = err.message || err.error || "Failed to delete child.";
                alert(msg);
                return;
            }

            setChildren((prev) => prev.filter((c) => c.id !== childId));
        } catch (e) {
            console.error("Error deleting child", e);
            alert("Something went wrong while deleting the child.");
        } finally {
            setLoading(false);
        }
    };

    const handleAllowance = async (childId) => {
        const input = prompt("Enter amount to add (e.g. 5.00):");
        if (input === null) return;
        const amount = Number(input);
        if (Number.isNaN(amount) || amount <= 0) {
            alert("Please enter a valid positive number.");
            return;
        }

        try {
            setLoading(true);
            const res = await authFetch(
                `${API_BASE_URL}/parents/me/children/${childId}/add-money`,
                {
                    method: "POST",
                    body: JSON.stringify({ amount }),
                }
            );

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                const msg = err.message || err.error || "Failed to add money.";
                alert(msg);
                return;
            }

            const updated = await res.json().catch(() => null);
            if (updated && updated.id) {
                setChildren((prev) => prev.map(c => c.id === updated.id ? updated : c));
            } else {
                const listRes = await authFetch(`${API_BASE_URL}/parents/me/children`);
                if (listRes.ok) {
                    const list = await listRes.json();
                    setChildren(list);
                }
            }
        } catch (e) {
            console.error("Error adding allowance", e);
            alert("Something went wrong while adding money.");
        } finally {
            setLoading(false);
        }
    };

    const openEditModal = (child) => {
        setSelectedChild(child);
        setIsEditOpen(true);
    };

    const closeEditModal = () => {
        setIsEditOpen(false);
        setSelectedChild(null);
    };

    const handleEditSaved = (updatedChild) => {
        setChildren(prev => prev.map(c => c.id === updatedChild.id ? updatedChild : c));
        closeEditModal();
    };

    return (
        <section className="min-h-screen bg-white text-slate-800 py-12 px-6 sm:px-12">
            <div className="max-w-5xl mx-auto space-y-8">
                <div className="flex flex-col items-center space-y-4">
                    <img
                        src="/images/profile.png"
                        alt="Profile"
                        className="w-32 h-32 rounded-full border-4 border-blue-100 shadow-sm object-cover"
                    />
                    <h1 className="text-3xl font-bold text-blue-600">{user.firstName} {user.lastName}</h1>
                    <p className="text-slate-700">{user.email}</p>
                </div>

                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-bold text-blue-600">Children</h2>
                        <Link to="/create-child">
                            <button className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700">
                                Create Child
                            </button>
                        </Link>
                    </div>

                    <div className="flex flex-wrap gap-4">
                        <div>
                            {children.length === 0 ? (
                                <p>No children found.</p>
                            ) : (
                                children.map(child => (
                                    <div key={child.id} className="relative border p-3 m-5 rounded-lg">
                                        <ChildCard
                                            child={child}
                                            onAddAllowance={handleAllowance}
                                            onDelete={handleRemoveChild}
                                        />
                                        <div className="mt-2 flex gap-2">
                                            <button
                                                onClick={() => openEditModal(child)}
                                                className="bg-yellow-400 text-white px-3 py-1 rounded hover:bg-yellow-500"
                                            >
                                                Edit
                                            </button>
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {isEditOpen && selectedChild && (
                <EditChild
                    child={selectedChild}
                    authFetch={authFetch}
                    onClose={closeEditModal}
                    onSaved={handleEditSaved}
                />
            )}
        </section>
    );
}