import { useEffect, useState } from "react";
import RoadmapItem from "./roadmap-item/RoadmapItem.jsx";
import { Link } from "react-router";

export default function Roadmap() {
    const [lectures, setLectures] = useState([]);

    useEffect(() => {
        fetch('http://localhost:8080/api/exercises')
            .then(response => response.json())
            .then(result => setLectures(result))
            .catch(err => alert(err));
    }, [])

    // Separate the data into budget and invest paths
    const introLecture = lectures.find(m => m.path === "intro");
    const budgetLectures = lectures.filter(m => m.path === "budget");
    const investLectures = lectures.filter(m => m.path === "finance");

    return (
        <div className="w-full flex flex-col items-center p-6 bg-gray-50">

            {/* Intro Module */}
            {introLecture && (
                <Link
                    to={`/roadmap/${introLecture.id}`}
                    className="w-full max-w-3xl bg-blue-200 p-6 rounded-3xl shadow-lg mb-10 text-center block hover:bg-blue-300 transition"
                >
                    <h2 className="text-3xl font-bold mb-2 flex justify-center items-center gap-2 text-blue-900">
                        {introLecture.title}
                    </h2>

                    <p className="text-gray-700 mb-4 text-lg">10 pocketMoney | 30 minutes</p>

                    <p className="mb-4 text-gray-800 text-md">
                        {introLecture.description.slice(0, 60)}...
                    </p>

                    <div className="font-bold text-blue-900 text-xl">Finance Basics 🌟</div>
                </Link>
            )}

            {/* Split Section */}
            <div className="w-full max-w-5xl bg-blue-900 text-white p-6 rounded-3xl shadow text-center mb-10">
                <h2 className="text-3xl font-bold flex items-center justify-center gap-2">
                    🎯 Choose Your Path
                </h2>
                <p className="mt-2 text-blue-100 text-lg">
                    Pick your financial adventure: Budgeting or Investing!
                </p>
            </div>

            {/* Paths */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-5xl">

                {/* Budgeting Path */}
                <RoadmapItem type={budgetLectures} path="Budgeting" />


                {/* Investing Path */}
                <RoadmapItem type={investLectures} path="Investing" />

            </div>
        </div>
    );
}