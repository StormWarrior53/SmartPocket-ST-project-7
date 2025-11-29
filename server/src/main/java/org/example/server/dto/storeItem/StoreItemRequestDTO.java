package org.example.server.dto.storeItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record StoreItemRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @Size(max = 255, message = "Description must be at most 255 characters")
        String description,

        @NotBlank(message = "Emoji is required")
        @Size(max = 10, message = "Emoji must be at most 10 characters")
        String emoji,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        Integer price,

        @NotNull(message = "Stock is required")
        @Positive(message = "Stock must be positive")
        Integer stock
) {
}
