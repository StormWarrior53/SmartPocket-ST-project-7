export default function AttemptsModal({ attempts = [], onClose }) {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto p-6">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-2xl font-bold">Quiz Attempts</h2>
                    <button onClick={onClose} className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600">Close</button>
                </div>

                {attempts.length === 0 ? (
                    <p className="text-gray-500">No attempts yet for this quiz.</p>
                ) : (
                    <div className="space-y-4">
                        {attempts.map(attempt => (
                            <div key={attempt.id} className="p-4 border rounded bg-gray-50">
                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <p className="font-semibold">Child: {attempt.childName}</p>
                                        <p className="text-gray-600">Date: {new Date(attempt.attemptedAt).toLocaleString()}</p>
                                    </div>
                                    <div>
                                        <p className="font-semibold">Score: {attempt.correctAnswers}/{attempt.totalQuestions} ({attempt.scorePercent}%)</p>
                                        <p className={`font-semibold ${attempt.passed ? 'text-green-600' : 'text-red-600'}`}>{attempt.passed ? 'PASSED' : 'FAILED'}</p>
                                        <p className="text-gray-600">Pocket Money: {attempt.pocketMoneyAwarded}</p>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}