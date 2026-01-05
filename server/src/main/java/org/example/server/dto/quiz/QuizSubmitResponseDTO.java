package org.example.server.dto.quiz;

public record QuizSubmitResponseDTO(
    Integer total,
    Integer correct,
    Integer percent,
    Boolean passed,
    Integer awarded,
    String message
) {}
