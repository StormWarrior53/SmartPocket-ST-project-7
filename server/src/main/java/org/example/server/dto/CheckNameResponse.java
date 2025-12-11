package org.example.server.dto;

public record CheckNameResponse(
    boolean exists,
    boolean hasPattern,
    String message
) {
}
