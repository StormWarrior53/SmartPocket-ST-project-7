import { Route, Routes } from "react-router"
import About from "./components/about-page/About.jsx"
import Footer from "./components/footer/Footer.jsx"
import Header from "./components/header/Header.jsx"

function App() {

    return (
        <>
            <Header />

            <Routes>
                <Route path="/" element={<h1 className="p-6 text-3xl">Home Page</h1>} />
                <Route path="/about" element={<About />} />
            </Routes>

            <Footer />
        </>
    )
}

export default App
