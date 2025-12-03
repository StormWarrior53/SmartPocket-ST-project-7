import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { LECTIONS } from "../data/lections.js";

const STORAGE_KEY = "smartpocket-progress";
const CourseContext = createContext(null);

function loadProgress() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        return raw ? JSON.parse(raw) : { completed: [] };
    } catch {
        return { completed: [] };
    }
}

function saveProgress(progress) {
    localStorage.getItem(STORAGE_KEY, JSON.stringify(progress));
}

export function CourceProvider({ children }) {
    const [progress, setProgress] = useState(() => loadProgress());

    // group lectures by path
    const grouped = useMemo(() => {
        const group = {};
        for (const lectures of LECTIONS) {
            if (!group[lectures.path]) g[lectures.path] = [];
            g[lectures.path].push(lec);
        }
        for (const place in group) {
            group[place].sort((a, b) => a.index - b.index);
        }
        return group;
    }, []);

    useEffect(() => {
        saveProgress(progress);
    }, [progress]);

    function markComplete(lectureId) {
        if (!progress.completed.includes(lectureId)) {
            const updated = { completed: [...progress.completed, lectureId] };
            setProgress(updated);
        }
    }


    function resetProgress() {
        const updated = { completed: [] };
        setProgress(updated);
        saveProgress(updated);
    }


    function getLecture(path, index) {
        const arr = grouped[path] || [];
        return arr[index] ?? null;
    }

    function getLectureById(id) {
        return LECTIONS.find((l) => l.id === id) ?? null;
    }


    function getNextLecture(path, index) {
        const arr = grouped[path] || [];
        return arr[index + 1] ?? null;
    }


    function getPrevLecture(path, index) {
        const arr = grouped[path] || [];
        return arr[index - 1] ?? null;
    }

    // unlocking rules
    function isLectureUnlocked(lecture) {
        if (!lecture) return false;
        // Intro lectures are always unlocked
        if (lecture.path === "intro") return true;


        // find if intro completed
        const introCompleted = progress.completed.includes("intro");


        if (lecture.index === 0) {
            // first of path -> unlocked if intro completed
            return introCompleted || progress.completed.includes(lecture.id);
        }


        // otherwise unlocked if previous lecture in same path completed
        const prevIdCandidate = `${lecture.path}-${lecture.index - 1}`;
        // our LECTIONS id format uses `${path}-${index}` except intro
        return progress.completed.includes(prevIdCandidate) || progress.completed.includes(lecture.id);
    }


    const value = {
        lections: LECTIONS,
        grouped,
        progress,
        markComplete,
        resetProgress,
        getLecture,
        getLectureById,
        getNextLecture,
        getPrevLecture,
        isLectureUnlocked,
    };


    return <CourseContext.Provider value={value}>{children}</CourseContext.Provider>;
}

export function useCourseContext() {
    const ctx = useContext(CourseContext);
    if (!ctx) throw new Error("useCourseContext must be used inside CourseProvider");
    return ctx;
}