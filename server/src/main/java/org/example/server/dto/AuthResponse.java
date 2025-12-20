package org.example.server.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuthResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String role,
    LocalDateTime createdAt,
    String token,
    String tokenType,
    Long expiresAt) {
  public AuthResponse(UUID id, String email, String firstName, String lastName,
      String role, LocalDateTime createdAt, String token, Long expiresAt) {
    this(id, email, firstName, lastName, role, createdAt, token, "Bearer", expiresAt);
  }
}
