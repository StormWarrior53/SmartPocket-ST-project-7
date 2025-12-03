import { Route, Routes } from "react-router"
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
function App() {

    return (
        <>
                <Header />

                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/about" element={<About />} />
                    <Route path="/leaderboard" element={<Leaderboard />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/store" element={<Store />} />
                    <Route path="/roadmap" element={<Roadmap />} />

                    <Route path='*' element={<NotFound />} />
                </Routes>

                <Footer />
        </>
    )
}

export default App
