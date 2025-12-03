package org.example.server.dto.exerciseItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.server.model.DifficultyLevel;

public record ExerciseRequestDTO(

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @NotBlank(message = "Path is required")
        @Size(max = 100, message = "Path must be at most 100 characters")
        String path,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotNull(message = "Difficulty level is required")
        DifficultyLevel difficultyLevel
) {
}
