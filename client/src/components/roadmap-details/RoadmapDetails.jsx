import { useEffect, useState } from "react";
import { useParams } from "react-router"
import { useUser } from "../../context/UserContext.jsx";

export default function RoadmapDetails() {
    const { lectureId } = useParams();
    const [lecture, setLecture] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [pocketMoneyGiven, setPocketMoneyGiven] = useState(false);
    const { user, isAuthenticated } = useUser();

    useEffect(() => {
        const given = localStorage.getItem(`pocketMoney_${lectureId}`);
        if (given) setPocketMoneyGiven(true);
    }, [lectureId]);

    const handleGivePocketMoney = () => {
        // Logic to add pocketMoney (static for now)
        alert("🎉 You received 10 pocketMoney!");

        // Disable button forever
        setPocketMoneyGiven(true);
        localStorage.setItem(`pocketMoney_${lectureId}`, "true");
    };

    useEffect(() => {
        setLoading(true);
        fetch(`http://localhost:8080/api/exercises/${lectureId}`)
            .then(response => {
                if (!response.ok) throw new Error("Lecture not found");
                return response.json();
            })
            .then(result => setLecture(result))
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [lectureId]);

    if (loading) return <p className="p-6">Loading lecture...</p>;
    if (error) return <p className="p-6 text-red-500">{error}</p>;
    if (!lecture) return <p className="p-6">No lecture data available</p>;

    return (
        <div className="max-w-3xl mx-auto p-6 bg-white shadow rounded-3xl mt-6">
            <h1 className="text-3xl font-bold text-blue-900 mb-4">{lecture.title}</h1>
            <p className="text-gray-600 mb-2">Difficulty: {lecture.difficultyLevel}</p>
            <p className="text-gray-800 mb-4">{lecture.description}</p>

            {user && isAuthenticated && <button
                onClick={handleGivePocketMoney}
                disabled={pocketMoneyGiven}
                className={`px-4 py-2 rounded-lg text-white font-semibold ${pocketMoneyGiven ? "bg-gray-400 cursor-not-allowed" : "bg-green-500 hover:bg-green-600"
                    }`}
            >
                {pocketMoneyGiven ? "PocketMoney Received" : "Get PocketMoney"}
            </button>}

        </div>


    );
}