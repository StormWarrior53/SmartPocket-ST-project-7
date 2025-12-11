package org.example.server.dto.inventory;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemResponse {

    private UUID inventoryItemId;
    private UUID storeItemId;
    private String name;
    private String description;
    private String emoji;
    private int pricePaid;
    private int quantity;
    private LocalDateTime purchasedAt;
}
