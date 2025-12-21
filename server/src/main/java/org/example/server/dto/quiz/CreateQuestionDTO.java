package org.example.server.dto.quiz;

import java.util.List;

public record CreateQuestionDTO(
    String text,
    String correctChoiceId,
    Integer orderIndex,
    List<CreateChoiceDTO> choices
) {}
