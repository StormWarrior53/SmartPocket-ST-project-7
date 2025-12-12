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

            return new InventoryItemResponse(
                    item.getId(),
                    storeItem.getId(),
                    storeItem.getName(),
                    storeItem.getDescription(),
                    storeItem.getEmoji(),
                    item.getPricePaid(),
                    item.getQuantity(),
                    item.getPurchasedAt()
            );
        }
    }
}
