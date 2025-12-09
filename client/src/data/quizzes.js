const MOCK_QUIZZES = {
    // example for lecture id "1"
    "3c32de6d-72c1-41fa-9bc7-d1e2c8900cc3": {
        id: "1",
        title: "Intro: Finance Basics Quiz",
        pocketMoneyPerQuestion: 5,
        passPercent: 70,
        questions: [
            {
                id: "q1",
                text: "What is a budget?",
                choices: [
                    { id: "a", text: "A plan for spending and saving" },
                    { id: "b", text: "A type of bank account" },
                    { id: "c", text: "A loan" }
                ],
                correctChoiceId: "a"
            },
            {
                id: "q2",
                text: "Which is good for emergencies?",
                choices: [
                    { id: "a", text: "High-risk stocks" },
                    { id: "b", text: "Emergency savings" },
                    { id: "c", text: "Luxury purchases" }
                ],
                correctChoiceId: "b"
            }
        ]
    },

    // example for lecture id "2" (budgeting path)
    "a6745a26-2be8-4d1a-9344-768dbf82b816": {
        id: "1",
        title: "Intro: Finance Basics Quiz",
        pocketMoneyPerQuestion: 5,
        passPercent: 70,
        questions: [
            {
                id: "q1",
                text: "What is a budget primarily used for?",
                choices: [
                    { id: "a", text: "Tracking spending and planning savings" },
                    { id: "b", text: "Getting a loan" },
                    { id: "c", text: "Buying stocks" }
                ],
                correctChoiceId: "a"
            },
            {
                id: "q2",
                text: "Which account is best for emergency funds?",
                choices: [
                    { id: "a", text: "High-risk investment account" },
                    { id: "b", text: "Savings account" },
                    { id: "c", text: "Credit card" }
                ],
                correctChoiceId: "b"
            },
            {
                id: "q3",
                text: "What does 'compound interest' mean?",
                choices: [
                    { id: "a", text: "Interest calculated only on the initial amount" },
                    { id: "b", text: "Interest paid by banks to employees" },
                    { id: "c", text: "Interest on the initial amount plus accumulated interest" }
                ],
                correctChoiceId: "c"
            },
            {
                id: "q4",
                text: "Why is it good to save a portion of income regularly?",
                choices: [
                    { id: "a", text: "To increase chances of impulse buying" },
                    { id: "b", text: "To build financial security and reach goals" },
                    { id: "c", text: "To avoid paying taxes" }
                ],
                correctChoiceId: "b"
            },
            {
                id: "q5",
                text: "Which is a low-risk way to keep money liquid for short-term needs?",
                choices: [
                    { id: "a", text: "Emergency savings in a savings account" },
                    { id: "b", text: "Long-term stock investments" },
                    { id: "c", text: "Collectible investments" }
                ],
                correctChoiceId: "a"
            }
        ]
    }
};

export function getQuizByLectureId(id) {
    return MOCK_QUIZZES[id] ?? null;
}