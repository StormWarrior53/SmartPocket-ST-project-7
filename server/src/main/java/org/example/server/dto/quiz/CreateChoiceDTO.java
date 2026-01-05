package org.example.server.dto.quiz;

public record CreateChoiceDTO(
    String choiceId,
    String text,
    Integer orderIndex
) {}
