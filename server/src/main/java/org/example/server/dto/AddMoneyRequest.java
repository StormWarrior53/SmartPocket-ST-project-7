package org.example.server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddMoneyRequest(
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    Integer amount
) {
}
