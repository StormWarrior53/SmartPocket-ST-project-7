package org.example.server.dto.quiz;

import java.util.List;
import java.util.UUID;

public record CreateQuizRequestDTO(
    String title,
    UUID exerciseId,
    Integer pocketMoneyPerQuestion,
    Integer passPercent,
    List<CreateQuestionDTO> questions
) {}
