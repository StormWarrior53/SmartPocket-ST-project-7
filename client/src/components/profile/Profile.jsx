import { useUser } from "../../context/UserContext.jsx";
import ChildProfile from "./child-profile/ChildProfile.jsx";
import ParentProfile from "./parent-profile/ParentProfile.jsx";

export default function Profile() {
    const { user, isAuthenticated, loading } = useUser();

    if (loading) return <p>Loading...</p>;
    if (!isAuthenticated) return <p>You must be logged in to view your profile.</p>;

    const isParent = user?.role === "parent"
        || user?.role === "admin"
        || (Array.isArray(user?.roles) && (user.roles.includes("parent") || user.roles.includes("admin")))
        || user?.isParent === true
        || user?.isAdmin === true;

    return isParent ? <ParentProfile /> : <ChildProfile />;
}
