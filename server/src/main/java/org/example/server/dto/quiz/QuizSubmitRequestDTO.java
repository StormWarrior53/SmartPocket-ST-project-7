package org.example.server.dto.quiz;

import java.util.Map;
import java.util.UUID;

public record QuizSubmitRequestDTO(
    Map<UUID, String> answers // questionId -> choiceId
) {}
