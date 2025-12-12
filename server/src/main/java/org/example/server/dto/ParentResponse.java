package org.example.server.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParentResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    LocalDateTime createdAt
) {
}
