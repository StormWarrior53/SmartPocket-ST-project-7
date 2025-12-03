import { useMemo } from "react";
import { useParams } from "react-router";
import useCourse from "./useCourse";


export default function useLection() {
const { path, index } = useParams();
const { getLecture, getNextLecture, getPrevLecture, isLectureUnlocked } = useCourse();


const idx = index ? parseInt(index, 10) : 0;


const lecture = useMemo(() => getLecture(path, idx), [getLecture, path, idx]);
const next = useMemo(() => getNextLecture(path, idx), [getNextLecture, path, idx]);
const prev = useMemo(() => getPrevLecture(path, idx), [getPrevLecture, path, idx]);
const unlocked = useMemo(() => isLectureUnlocked(lecture), [isLectureUnlocked, lecture]);


return { lecture, next, prev, unlocked };
}