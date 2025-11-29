package org.example.server.mapper;

import org.example.server.dto.storeItem.StoreItemRequestDTO;
import org.example.server.dto.storeItem.StoreItemResponseDTO;
import org.example.server.model.StoreItem;
import org.springframework.stereotype.Component;

public interface StoreItemMapper {

    StoreItemResponseDTO toResponse(StoreItem item);

    StoreItem toEntity(StoreItemRequestDTO request);

    void updateEntity(StoreItem item, StoreItemRequestDTO request);

    @Component
    class Impl implements StoreItemMapper {

        @Override
        public StoreItemResponseDTO toResponse(StoreItem item) {
            if (item == null) return null;
            return new StoreItemResponseDTO(
                    item.getId(),
                    item.getName(),
                    item.getDescription(),
                    item.getEmoji(),
                    item.getPrice(),
                    item.getStock()
            );
        }

        @Override
        public StoreItem toEntity(StoreItemRequestDTO request) {
            if (request == null) return null;
            return new StoreItem(
                    request.name(),
                    request.description(),
                    request.emoji(),
                    request.price(),
                    request.stock()
            );
        }

        @Override
        public void updateEntity(StoreItem item, StoreItemRequestDTO request) {
            if (item == null || request == null) return;
            item.setName(request.name());
            item.setDescription(request.description());
            item.setEmoji(request.emoji());
            item.setPrice(request.price());
            item.setStock(request.stock());
        }
    }
}
