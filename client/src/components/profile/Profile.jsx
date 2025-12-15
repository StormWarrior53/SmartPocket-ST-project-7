import { useEffect, useState } from "react";
import { useUser } from "../../context/UserContext.jsx";
import { Link } from "react-router";

export default function Profile() {



    const { user, authFetch, loading: userLoading, isAuthenticated } = useUser();
    const [children, setChildren] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (userLoading) return;          // wait for context to load
        if (!isAuthenticated) return;     // don't fetch if not logged in

        const fetchChildren = async () => {
            try {
                const res = await authFetch("http://localhost:8080/api/parents/me/children");

                if (!res.ok) {
                    console.error("Failed to fetch children");
                    return;
                }

                const data = await res.json();
                setChildren(data);
            } catch (e) {
                console.error("Error fetching children", e);
            } finally {
                setLoading(false);
            }
        };

        fetchChildren();
    }, [authFetch, userLoading, isAuthenticated]);

    if (loading || userLoading) return <p>Loading...</p>;
    if (!isAuthenticated) return <p>You must be logged in to view children.</p>;

    // const { user } = useUser();

    const handleCreateChild = () => {
        console.log("Create child clicked");
    };

    const handleRemoveChild = (id) => {
        console.log("Remove child", id);
    };

    const handleAllowance = (id) => {
        console.log("Add allowance to child", id);
    };

    if (!user) {
        return <p>Loading...</p>;
    }

    return (
        <section className="min-h-screen bg-white text-slate-800 py-12 px-6 sm:px-12">
            <div className="max-w-5xl mx-auto space-y-8">

                {/* Profile Header */}
                <div className="flex flex-col items-center space-y-4">
                    <img
                        src="/images/profile.png"
                        alt="Profile"
                        className="w-32 h-32 rounded-full border-4 border-blue-100 shadow-sm object-cover"
                    />
                    <h1 className="text-3xl font-bold text-blue-600">{user.firstName} {user.lastName}</h1>
                    <p className="text-slate-700">{user.email}</p>
                </div>

                {/* Children Section */}
                <div className="bg-white border border-blue-100 shadow-sm rounded-2xl p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-2xl font-bold text-blue-600">Children</h2>
                        <Link to="/create-child">
                            <button
                                onClick={handleCreateChild}
                                className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
                            >
                                Create Child
                            </button>
                        </Link>
                    </div>

                    {/* Children Cards */}
                    <div className="flex flex-wrap gap-4">
                        <div>
                            <h2>My Children</h2>
                            {children.length === 0 ? (
                                <p>No children found.</p>
                            ) : (
                                children.map(child => (
                                    <div key={child.id} style={{ marginBottom: "10px" }}>
                                        <strong>{child.name}</strong><br />
                                        Balance: {child.balance}
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
}