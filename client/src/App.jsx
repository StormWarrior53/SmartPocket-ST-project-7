import { Route, Routes } from "react-router"
import About from "./components/about-page/About.jsx"
import Footer from "./components/footer/Footer.jsx"
import Header from "./components/header/Header.jsx"
import Leaderboard from "./components/leaderboard-page/Leaderboard.jsx"
import Home from "./components/home-page/Home.jsx"
import Register from "./components/register-page/Register.jsx"

function App() {

    return (
        <>
            <Header />

            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/about" element={<About />} />
                <Route path="/leaderboard" element={<Leaderboard />} />
                <Route path="/register" element={<Register />} />
            </Routes>

            <Footer />
        </>
    )
}

export default App
