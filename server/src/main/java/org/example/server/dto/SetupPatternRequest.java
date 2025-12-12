package org.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.server.validation.ValidPattern;

public record SetupPatternRequest(
    @NotBlank(message = "Child name is required")
    @Size(min = 2, max = 50, message = "Child name must be between 2 and 50 characters")
    String childName,

    @NotBlank(message = "Parent name is required")
    @Size(min = 2, max = 50, message = "Parent name must be between 2 and 50 characters")
    String parentName,

    @NotBlank(message = "Pattern is required")
    @ValidPattern
    String pattern
) {
}
