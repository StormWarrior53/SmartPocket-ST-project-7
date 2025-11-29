package org.example.server.dto.storeItem;

import java.util.UUID;

public record StoreItemResponseDTO(
        UUID id,
        String name,
        String description,
        String emoji,
        int price,
        int stock
) {
}
