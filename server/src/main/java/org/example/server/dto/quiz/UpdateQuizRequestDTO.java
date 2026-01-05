package org.example.server.dto.quiz;

import java.util.List;

public record UpdateQuizRequestDTO(
    String title,
    Integer pocketMoneyPerQuestion,
    Integer passPercent,
    List<CreateQuestionDTO> questions
) {}
