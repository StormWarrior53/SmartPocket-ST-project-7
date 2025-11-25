import { Route, Routes } from "react-router"
import About from "./components/about-page/About.jsx"

function App() {

    return (
        <>

        <Routes>
            <Route path="/about" element={<About />} />
        </Routes>
        </>
    )
}

export default App
