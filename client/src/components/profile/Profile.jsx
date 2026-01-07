import { useEffect } from "react";
import { useUser } from "../../context/UserContext.jsx";
import ChildProfile from "./child-profile/ChildProfile.jsx";
import ParentProfile from "./parent-profile/ParentProfile.jsx";
import { useNavigate } from "react-router";

export default function Profile() {
    const { user, isAuthenticated, loading } = useUser();
    const navigate = useNavigate();

    useEffect(() => {
        if (!loading && !isAuthenticated) {
            navigate("/login", { replace: true });
        }
    }, [loading, isAuthenticated, navigate]);

    if (loading) return <p>Loading...</p>;
    if (!isAuthenticated) return (
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "2rem" }}>
            <img
                src="/images/boy-arrest.png"
                alt="Illustration of a boy with handcuffs"
                style={{ display: "block", maxWidth: 300, width: "50%", height: "auto", marginBottom: "1rem" }}
            />
            <h1>You must be logged in to view this page</h1>
        </div>
    );

    const isParent = user?.role === "parent"
        || user?.role === "admin"
        || (Array.isArray(user?.roles) && (user.roles.includes("parent") || user.roles.includes("admin")))
        || user?.isParent === true
        || user?.isAdmin === true;

    return isParent ? <ParentProfile /> : <ChildProfile />;
}
