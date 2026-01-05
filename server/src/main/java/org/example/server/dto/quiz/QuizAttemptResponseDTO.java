package org.example.server.dto.quiz;

import java.time.LocalDateTime;
import java.util.UUID;

public record QuizAttemptResponseDTO(
    UUID id,
    UUID quizId,
    String quizTitle,
    UUID childId,
    String childName,
    Integer totalQuestions,
    Integer correctAnswers,
    Integer scorePercent,
    Boolean passed,
    Integer pocketMoneyAwarded,
    LocalDateTime attemptedAt
) {}
