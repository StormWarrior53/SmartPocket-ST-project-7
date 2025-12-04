import { Link } from "react-router";

export default function RoadmapItem({
    type,
    path
}) {
    return (
        <div className="bg-white p-6 rounded-3xl shadow border border-gray-200">
            <h3 className="text-2xl font-bold mb-4 text-center text-blue-900 flex gap-2 justify-center">
                💰 {path} Track
            </h3>

            <div className="space-y-4">
                {type.map((lecture, i) => (
                    <Link
                        to={`/roadmap/${lecture.id}`}
                        key={i}
                        className="block p-4 border rounded-2xl shadow-sm bg-blue-50 hover:bg-blue-100 transition"
                    >
                        <h4 className="font-semibold text-blue-900">{lecture.title}</h4>
                        <p className="text-sm text-gray-600">{lecture.difficultyLevel}</p>
                        <p className="text-sm text-gray-600">
                            {lecture.description?.slice(0, 50) + "..."}
                        </p>
                    </Link>
                ))}
            </div>
        </div>
    )
}