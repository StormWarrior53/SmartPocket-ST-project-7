import { Route, Routes } from "react-router"
import About from "./components/about-page/About.jsx"
import Footer from "./components/footer/Footer.jsx"

function App() {

    return (
        <>

            <Routes>
                <Route path="/about" element={<About />} />
            </Routes>

            <Footer />
        </>
    )
}

export default App
