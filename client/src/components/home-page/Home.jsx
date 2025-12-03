import { Link } from "react-router";
import ParentBenefits from "./parent-benefits/ParentBenefits.jsx";
import Hero from "./hero-section/Hero.jsx";
import Steps from "./steps/Steps.jsx";
import LeaderboardBadges from "./leaderboard-badges/LeaderboardBadges.jsx";
import RoadmapPart from "./roadmap/RoadmapPart.jsx";

export default function Home() {

    return (
        <main className="min-h-screen bg-white text-slate-800 py-12 px-6 sm:px-12 space-y-12">

            {/* Hero Section */}
            <Hero />

            {/* How it works */}
            <Steps />

            {/* RoadMap */}
            <RoadmapPart />

            {/* Leaderboard + Badges */}
            <LeaderboardBadges />

            {/* Parent Benefits */}
            <ParentBenefits />

        </main>
    );
}
