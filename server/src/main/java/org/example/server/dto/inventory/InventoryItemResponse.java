package org.example.server.dto.inventory;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryItemResponse(
    UUID inventoryItemId,
    UUID storeItemId,
    String name,
    String description,
    String emoji,
    int pricePaid,
    int quantity,
    LocalDateTime purchasedAt
) {
}
