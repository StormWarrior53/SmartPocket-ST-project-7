import { Route, Routes } from "react-router"
import { useEffect } from 'react'
import About from "./components/about-page/About.jsx"
import Footer from "./components/footer/Footer.jsx"
import Header from "./components/header/Header.jsx"
import Leaderboard from "./components/leaderboard-page/Leaderboard.jsx"
import Home from "./components/home-page/Home.jsx"
import Register from "./components/register-page/Register.jsx"
import Login from "./components/login-page/Login.jsx"
import NotFound from "./components/not-found-page/NotFound.jsx"
import Store from "./components/store/Store.jsx"
import Roadmap from "./components/roadmap-page/Roadmap.jsx"
import Profile from "./components/profile/Profile.jsx"
import RoadmapDetails from "./components/roadmap-details/RoadmapDetails.jsx"
import Games from "./components/games-page/Games.jsx"
import Quiz from "./components/quiz-page/Quiz.jsx"
import QuizAdmin from "./components/admin/QuizAdmin.jsx"
import QuizHistory from "./components/quiz-history/QuizHistory.jsx"
import CreateChild from "./components/create-child/CreateChild.jsx"
import BudgetGame from "./components/budget-game/BudgetGame.jsx"
import { useTokenExpiration } from './hooks/useTokenExpiration'
import { setApiLogoutCallback } from './services/api'
import { GoogleCallbackPage } from "./components/google-callback-page/GoogleCallbackPage.jsx"

function App() {
    const { handleExpiredToken } = useTokenExpiration(5); // 5-minute warning

    // Set up API interceptor callback
    useEffect(() => {
        setApiLogoutCallback(handleExpiredToken);
        return () => setApiLogoutCallback(null);
    }, [handleExpiredToken]);

    return (
        <>
            <Header />

            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/about" element={<About />} />
                <Route path="/leaderboard" element={<Leaderboard />} />
                <Route path="/register" element={<Register />} />
                <Route path="/login" element={<Login />} />
                <Route path="/auth/google/callback" element={<GoogleCallbackPage />} />
                <Route path="/store" element={<Store />} />
                <Route path="/roadmap" element={<Roadmap />} />
                <Route path="/roadmap/:lectureId" element={<RoadmapDetails />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="/games" element={<Games />} />
                <Route path="budget-game" element={<BudgetGame />} />
                <Route path="/quiz/:id" element={<Quiz />} />
                <Route path="/admin/quiz" element={<QuizAdmin />} />
                <Route path="/quiz-history" element={<QuizHistory />} />

                <Route path="/create-child" element={<CreateChild />} />

                <Route path='*' element={<NotFound />} />
            </Routes>

            <Footer />
        </>
    )
}

export default App
