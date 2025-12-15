package org.example.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckNameRequest(
    @NotBlank(message = "Child name is required")
    @Size(min = 2, max = 50, message = "Child name must be between 2 and 50 characters")
    String childName,

    @NotBlank(message = "Parent email is required")
    @Email(message = "Parent email must be valid")
    String parentEmail
) {
}
