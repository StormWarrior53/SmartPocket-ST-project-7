package org.example.server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PatchXpRequestDTO(
        @NotNull
        @Min(0)
        Integer xp
) {
}
