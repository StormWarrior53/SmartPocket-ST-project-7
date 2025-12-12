import { useState, useMemo } from "react";
import { useParams, Link } from "react-router";
import { getQuizByLectureId } from "../../data/quizzes.js";

export default function Quiz() {
    const { id } = useParams();
    const quiz = useMemo(() => getQuizByLectureId(id), [id]);
    const [answers, setAnswers] = useState({});
    const [result, setResult] = useState(null);

    if (!quiz) {
        return (
            <div className="p-6 max-w-3xl mx-auto">
                <h2 className="text-2xl font-bold mb-4">No Quiz Found</h2>
                <p className="mb-4">This lecture does not have a quiz yet.</p>
                <Link to={`/roadmap/${id}`} className="px-4 py-2 bg-gray-200 rounded">Back</Link>
            </div>
        );
    }

    function selectAnswer(qId, choiceId) {
        setAnswers(prev => ({ ...prev, [qId]: choiceId }));
    }

    function submitQuiz() {
        const total = quiz.questions.length;
        let correct = 0;
        quiz.questions.forEach(q => {
            if (answers[q.id] && answers[q.id] === q.correctChoiceId) correct++;
        });

        const percent = total === 0 ? 0 : Math.round((correct / total) * 100);
        const passed = percent >= (quiz.passPercent ?? 70);
        const awarded = passed ? (correct * (quiz.pocketMoneyPerQuestion ?? 5)) : 0;

        setResult({
            total,
            correct,
            percent,
            passed,
            awarded,
            message: passed ? "Congrats! You passed the quiz." : "You did not pass. Try again."
        });
    }

    return (
        <div className="p-6 max-w-3xl mx-auto">
            <h2 className="text-2xl font-bold mb-4">{quiz.title}</h2>

            <div className="space-y-6">
                {quiz.questions.map(q => (
                    <div key={q.id} className="p-4 border rounded">
                        <div className="font-semibold mb-2">{q.text}</div>
                        <div className="flex flex-col gap-2">
                            {q.choices.map(choice => (
                                <label key={choice.id} className="inline-flex items-center gap-2">
                                    <input
                                        type="radio"
                                        name={`q-${q.id}`}
                                        checked={answers[q.id] === choice.id}
                                        onChange={() => selectAnswer(q.id, choice.id)}
                                    />
                                    <span>{choice.text}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                ))}
            </div>

            <div className="mt-6 flex gap-3">
                <button
                    onClick={submitQuiz}
                    className="px-4 py-2 bg-green-600 text-white rounded"
                >
                    Submit Quiz
                </button>

                <Link to={`/roadmap/${id}`} className="px-4 py-2 bg-gray-200 rounded">Back</Link>
            </div>

            {result && (
                <div className="mt-6 p-4 border rounded bg-white">
                    <h3 className="font-bold mb-2">Result</h3>
                    <p>Score: {result.correct} / {result.total} ({result.percent}%)</p>
                    <p className="font-semibold">{result.message}</p>
                    <p className="mt-2">PocketMoney awarded: <span className="font-bold">{result.awarded}</span></p>
                </div>
            )}
        </div>
    );
}