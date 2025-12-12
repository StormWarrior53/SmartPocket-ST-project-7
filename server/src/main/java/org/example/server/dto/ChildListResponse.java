package org.example.server.dto;

import java.util.UUID;

public record ChildListResponse(
    UUID id,
    String name,
    int age
) {
}
