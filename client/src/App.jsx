import { Route, Routes } from "react-router"
import About from "./components/about-page/About.jsx"

function App() {

    return (
        <>
        <header></header>

        <Routes>
            <Route path="/about" element={<About />} />
        </Routes>
        <footer></footer>
        </>
    )
}

export default App
