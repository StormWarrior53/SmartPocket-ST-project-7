package org.example.server.dto.exerciseItem;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExerciseResponseDTO(
        UUID id,
        String title,
        String path,
        String description,
        String difficultyLevel
) {}

