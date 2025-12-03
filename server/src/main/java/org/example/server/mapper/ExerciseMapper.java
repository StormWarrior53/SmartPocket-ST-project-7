package org.example.server.mapper;

import org.example.server.dto.exerciseItem.ExerciseRequestDTO;
import org.example.server.dto.exerciseItem.ExerciseResponseDTO;
import org.example.server.model.Exercise;
import org.springframework.stereotype.Component;

public interface ExerciseMapper {

    ExerciseResponseDTO toResponse(Exercise exercise);

    Exercise toEntity(ExerciseRequestDTO request);

    void updateEntity(Exercise exercise, ExerciseRequestDTO request);

    @Component
    class Impl implements ExerciseMapper {

        @Override
        public ExerciseResponseDTO toResponse(Exercise exercise) {
            if (exercise == null) return null;
            return new ExerciseResponseDTO(
                    exercise.getId(),
                    exercise.getTitle(),
                    exercise.getPath(),
                    exercise.getDescription(),
                    exercise.getDifficultyLevel(),
                    exercise.getCreatedAt(),
                    exercise.getUpdatedAt()
            );
        }

        @Override
        public Exercise toEntity(ExerciseRequestDTO request) {
            if (request == null) return null;
            Exercise exercise = new Exercise();
            exercise.setTitle(request.title());
            exercise.setPath(request.path());
            exercise.setDescription(request.description());
            exercise.setDifficultyLevel(request.difficultyLevel());
            return exercise;
        }

        @Override
        public void updateEntity(Exercise exercise, ExerciseRequestDTO request) {
            if (exercise == null || request == null) return;
            exercise.setTitle(request.title());
            exercise.setPath(request.path());
            exercise.setDescription(request.description());
            exercise.setDifficultyLevel(request.difficultyLevel());
        }
    }
}