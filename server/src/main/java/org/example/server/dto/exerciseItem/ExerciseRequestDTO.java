package org.example.server.dto.exerciseItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExerciseRequestDTO(

        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,

        @NotBlank(message = "Path is required")
        @Size(max = 100, message = "Path must be at most 100 characters")
        String path,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotBlank(message = " is required")
        @Size(max = 100, message = "Path must be at most 100 characters")
                String difficultyLevel
) {
}
