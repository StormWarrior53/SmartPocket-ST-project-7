package org.example.server.dto.quiz;

import java.util.List;
import java.util.UUID;

public record QuestionDTO(
    UUID id,
    String text,
    List<ChoiceDTO> choices
) {}
