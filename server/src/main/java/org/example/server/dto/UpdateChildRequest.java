package org.example.server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateChildRequest(
    @NotBlank(message = "Child name is required")
    String name,

    @NotNull(message = "Age is required")
    @Min(value = 7, message = "Child must be at least 7 years old")
    @Max(value = 24, message = "Child must be at most 24 years old")
    Integer age
) {
}
