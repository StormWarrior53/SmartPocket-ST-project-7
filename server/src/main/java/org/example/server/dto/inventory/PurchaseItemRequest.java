package org.example.server.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PurchaseItemRequest(
    @NotNull(message = "Store item ID is required") UUID storeItemId,

    @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity) {
}
