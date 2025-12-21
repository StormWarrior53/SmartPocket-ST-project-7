package org.example.server.dto.quiz;

import java.util.List;
import java.util.UUID;

public record QuizResponseDTO(
    UUID id,
    String title,
    Integer pocketMoneyPerQuestion,
    Integer passPercent,
    List<QuestionDTO> questions
) {}
