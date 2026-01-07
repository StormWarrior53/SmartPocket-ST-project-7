export default function QuizList({ quizzes = [], onViewAttempts, onEdit, onDelete }) {
    if (!quizzes.length) return <p className="text-gray-500">No quizzes found. Create one to get started!</p>;

    return (
        <div className="space-y-4">
            {quizzes.map(quiz => (
                <div key={quiz.id} className="p-4 border rounded bg-white shadow">
                    <div className="flex justify-between items-start">
                        <div>
                            <h3 className="text-xl font-semibold">{quiz.title}</h3>
                            <p className="text-gray-600">Questions: {quiz.questions.length}</p>
                            <p className="text-gray-600">Reward: {quiz.pocketMoneyPerQuestion} per question | Pass: {quiz.passPercent}%</p>
                        </div>
                        <div className="flex gap-2">
                            <button onClick={() => onViewAttempts(quiz.id)} className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700">View Attempts</button>
                            <button onClick={() => onEdit(quiz)} className="px-3 py-1 bg-yellow-600 text-white rounded hover:bg-yellow-700">Edit</button>
                            <button onClick={() => onDelete(quiz.id)} className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700">Delete</button>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
}