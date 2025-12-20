package org.example.server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PatchMoneyRequestDTO(
        @NotNull
        @Min(0)
        Integer amount
) {
}
