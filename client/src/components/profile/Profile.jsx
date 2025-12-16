import { useUser } from "../../context/UserContext.jsx";
import ChildProfile from "./child-profile/ChildProfile.jsx";
import ParentProfile from "./parent-profile/ParentProfile.jsx";

export default function Profile() {
    const { user, isAuthenticated, loading } = useUser();

    if (loading) return <p>Loading...</p>;
    if (!isAuthenticated) return <p>You must be logged in to view your profile.</p>;

    return user?.role === "parent" ? <ParentProfile /> : <ChildProfile />;
}