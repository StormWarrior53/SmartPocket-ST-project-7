package org.example.server.dto.exerciseItem;

import org.example.server.model.DifficultyLevel;

import java.util.UUID;

public record ExerciseResponseDTO(
    UUID id,
    String title,
    String path,
    String description,
    DifficultyLevel difficultyLevel) {
}
