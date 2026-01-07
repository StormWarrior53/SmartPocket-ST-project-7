import { useEffect, useState } from "react";

export default function QuizForm({ exercises = [], onSubmit, onCancel, editingQuiz = null }) {
    const initial = {
        title: "",
        exerciseId: "",
        pocketMoneyPerQuestion: 5,
        passPercent: 70,
        questions: [{
            text: "", correctChoiceId: "a", orderIndex: 0, choices: [
                { choiceId: "a", text: "" }, { choiceId: "b", text: "" }, { choiceId: "c", text: "" }
            ]
        }]
    };

    const [formData, setFormData] = useState(initial);

    useEffect(() => {
        if (!editingQuiz) return;
        setFormData({
            title: editingQuiz.title,
            exerciseId: "",
            pocketMoneyPerQuestion: editingQuiz.pocketMoneyPerQuestion,
            passPercent: editingQuiz.passPercent,
            questions: editingQuiz.questions.map(q => ({
                text: q.text,
                correctChoiceId: q.choices.find(c => c.id === q.correctChoiceId)?.id || "a",
                orderIndex: 0,
                choices: q.choices.map((c) => ({ choiceId: c.id, text: c.text }))
            }))
        });
    }, [editingQuiz]);

    function updateQuestion(index, field, value) {
        const newQ = [...formData.questions];
        newQ[index] = { ...newQ[index], [field]: value };
        setFormData({ ...formData, questions: newQ });
    }

    function updateChoice(qIndex, cIndex, value) {
        const newQ = [...formData.questions];
        const choices = [...newQ[qIndex].choices];
        choices[cIndex] = { ...choices[cIndex], text: value };
        newQ[qIndex].choices = choices;
        setFormData({ ...formData, questions: newQ });
    }

    function addQuestion() {
        setFormData({
            ...formData,
            questions: [...formData.questions, {
                text: "",
                correctChoiceId: "a",
                orderIndex: formData.questions.length,
                choices: [{ choiceId: "a", text: "" }, { choiceId: "b", text: "" }, { choiceId: "c", text: "" }]
            }]
        });
    }

    function removeQuestion(index) {
        setFormData({ ...formData, questions: formData.questions.filter((_, i) => i !== index) });
    }

    function submit(e) {
        e.preventDefault();
        onSubmit(formData, !!editingQuiz);
    }

    return (
        <div className="mb-8 p-6 border rounded-lg bg-white shadow">
            <h2 className="text-2xl font-bold mb-4">{editingQuiz ? "Edit Quiz" : "Create New Quiz"}</h2>
            <form onSubmit={submit}>
                <div className="mb-4">
                    <label className="block mb-2 font-semibold">Quiz Title</label>
                    <input type="text" value={formData.title} onChange={e => setFormData({ ...formData, title: e.target.value })} className="w-full p-2 border rounded" required />
                </div>

                {!editingQuiz && (
                    <div className="mb-4">
                        <label className="block mb-2 font-semibold">Exercise</label>
                        <select value={formData.exerciseId} onChange={e => setFormData({ ...formData, exerciseId: e.target.value })} className="w-full p-2 border rounded" required>
                            <option value="">Select an exercise</option>
                            {exercises.map(ex => <option key={ex.id} value={ex.id}>{ex.title}</option>)}
                        </select>
                    </div>
                )}

                <div className="grid grid-cols-2 gap-4 mb-4">
                    <div>
                        <label className="block mb-2 font-semibold">Pocket Money per Question</label>
                        <input type="number" value={formData.pocketMoneyPerQuestion} onChange={e => setFormData({ ...formData, pocketMoneyPerQuestion: parseInt(e.target.value) })} className="w-full p-2 border rounded" min="1" required />
                    </div>
                    <div>
                        <label className="block mb-2 font-semibold">Pass Percentage</label>
                        <input type="number" value={formData.passPercent} onChange={e => setFormData({ ...formData, passPercent: parseInt(e.target.value) })} className="w-full p-2 border rounded" min="0" max="100" required />
                    </div>
                </div>

                <div className="mb-4">
                    <div className="flex justify-between items-center mb-2">
                        <h3 className="text-xl font-semibold">Questions</h3>
                        <button type="button" onClick={addQuestion} className="px-3 py-1 bg-green-600 text-white rounded text-sm">Add Question</button>
                    </div>

                    {formData.questions.map((question, qIndex) => (
                        <div key={qIndex} className="mb-6 p-4 border rounded bg-gray-50">
                            <div className="flex justify-between items-center mb-2">
                                <h4 className="font-semibold">Question {qIndex + 1}</h4>
                                {formData.questions.length > 1 && <button type="button" onClick={() => removeQuestion(qIndex)} className="px-2 py-1 bg-red-600 text-white rounded text-sm">Remove</button>}
                            </div>

                            <input type="text" value={question.text} onChange={e => updateQuestion(qIndex, 'text', e.target.value)} placeholder="Enter question text" className="w-full p-2 border rounded mb-2" required />

                            <div className="mb-2">
                                <label className="block mb-1 text-sm font-semibold">Correct Answer</label>
                                <select value={question.correctChoiceId} onChange={e => updateQuestion(qIndex, 'correctChoiceId', e.target.value)} className="w-full p-2 border rounded" required>
                                    <option value="a">A</option>
                                    <option value="b">B</option>
                                    <option value="c">C</option>
                                </select>
                            </div>

                            <div className="space-y-2">
                                {question.choices.map((choice, cIndex) => (
                                    <div key={cIndex} className="flex gap-2 items-center">
                                        <span className="font-semibold">{choice.choiceId.toUpperCase()}:</span>
                                        <input type="text" value={choice.text} onChange={e => updateChoice(qIndex, cIndex, e.target.value)} placeholder={`Choice ${choice.choiceId.toUpperCase()}`} className="flex-1 p-2 border rounded" required />
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>

                <div className="flex gap-2">
                    <button type="submit" className="w-full px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">{editingQuiz ? "Update Quiz" : "Create Quiz"}</button>
                    <button type="button" onClick={onCancel} className="px-4 py-2 bg-gray-300 rounded">Cancel</button>
                </div>
            </form>
        </div>
    );
}