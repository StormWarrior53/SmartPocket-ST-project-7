package org.example.server.mapper;

import org.example.server.dto.inventory.InventoryItemResponse;
import org.example.server.model.InventoryItem;
import org.example.server.model.StoreItem;
import org.springframework.stereotype.Component;

public interface InventoryMapper {

    InventoryItemResponse toResponse(InventoryItem inventoryItem);

    @Component
    class Impl implements InventoryMapper {

        @Override
        public InventoryItemResponse toResponse(InventoryItem item) {
            if (item == null) {
                return null;
            }

            StoreItem storeItem = item.getStoreItem();

            return InventoryItemResponse.builder()
                    .inventoryItemId(item.getId())
                    .storeItemId(storeItem.getId())
                    .name(storeItem.getName())
                    .description(storeItem.getDescription())
                    .emoji(storeItem.getEmoji())
                    .pricePaid(item.getPricePaid())
                    .quantity(item.getQuantity())
                    .purchasedAt(item.getPurchasedAt())
                    .build();
        }
    }
}
