package org.example.server.service;

import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.storeItem.StoreItemRequestDTO;
import org.example.server.dto.storeItem.StoreItemResponseDTO;
import org.example.server.mapper.StoreItemMapper;
import org.example.server.model.StoreItem;
import org.example.server.repository.StoreItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
public class StoreItemService {
    private final StoreItemRepository repository;
    private final StoreItemMapper mapper;

    @Autowired
    public StoreItemService(StoreItemRepository repository, StoreItemMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ----------------- CRUD -----------------

    public StoreItemResponseDTO createItem(StoreItemRequestDTO request) {
        log.info("Creating store item: {}", request.name());
        log.debug("CreateItem payload: {}", request);

        StoreItem item = mapper.toEntity(request);
        StoreItem saved = repository.save(item);

        log.info("Store item created with id: {}", saved.getId());
        return mapper.toResponse(saved);
    }

    public StoreItemResponseDTO updateItem(UUID id, StoreItemRequestDTO request) {
        log.info("Updating store item with id: {}", id);
        log.debug("UpdateItem payload: {}", request);

        StoreItem item = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Store item with id={} not found", id);
                   return new RuntimeException("Item not found");
                });

        mapper.updateEntity(item, request);
        StoreItem updated = repository.save(item);

        log.info("Store item updated successfully with id: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    public StoreItemResponseDTO patchItem(UUID id, StoreItemRequestDTO request) {
        log.info("Patching store item with id: {}", id);
        log.debug("PatchItem payload: {}", request);

        StoreItem item = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Store item with id={} not found for patch", id);
                    return new RuntimeException("Item not found");
                });

        if (request.name() != null) item.setName(request.name());
        if (request.description() != null) item.setDescription(request.description());
        if (request.emoji() != null) item.setEmoji(request.emoji());
        if (request.price() != null) item.setPrice(request.price());
        if (request.stock() != null) item.setStock(request.stock());

        StoreItem updated = repository.save(item);

        log.info("Store item patched successfully with id: {}", updated.getId());
        return mapper.toResponse(updated);
    }

    public void deleteItem(UUID id) {
        log.info("Deleting store item with id: {}", id);

        if (!repository.existsById(id)) {
            log.warn("Cannot delete — store item with id={} does not exist", id);
            throw new RuntimeException("Item not found");
        }

        repository.deleteById(id);
        log.info("Store item deleted successfully with id: {}", id);
    }

    // ----------------- Stock management -----------------

    public StoreItemResponseDTO restockItem(UUID id, int additionalStock) {
        log.info("Restocking store item id={} with {} units", id, additionalStock);

        StoreItem item = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Store item with id={} not found for restock", id);
                       return new RuntimeException("Item not found");
                });

        item.setStock(item.getStock() + additionalStock);
        StoreItem updated = repository.save(item);

        log.info("Store item restocked successfully with id={} new stock={}", id, updated.getStock());
        return mapper.toResponse(updated);
    }

    // ----------------- Retrieval & filtering -----------------

    public StoreItemResponseDTO getItemById(UUID id) {
        log.info("Fetching store item with id: {}", id);

        StoreItem item = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Store item with id={} not found", id);
                    return new RuntimeException("Item not found");
                });

        log.debug("Fetched store item: {}", item);
        return mapper.toResponse(item);
    }

    public List<StoreItemResponseDTO> getAllItems() {
        log.info("Fetching all store items");

        List<StoreItemResponseDTO> items = repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        log.debug("Total store items fetched: {}", items.size());
        return items;
    }

    public List<StoreItemResponseDTO> getItemsSortedAsc() {
        log.info("Fetching store items sorted ascending by price");

        List<StoreItemResponseDTO> items = repository.findAll(Sort.by(Sort.Direction.ASC, "price")).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        log.debug("Total items fetched (ASC): {}", items.size());
        return items;
    }

    public List<StoreItemResponseDTO> getItemsSortedDesc() {
        log.info("Fetching store items sorted descending by price");

        List<StoreItemResponseDTO> items = repository.findAll(Sort.by(Sort.Direction.DESC, "price")).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        log.debug("Total items fetched (DESC): {}", items.size());
        return items;
    }

    public List<StoreItemResponseDTO> getItemsBelowPrice(int price) {
        log.info("Fetching store items below price: {}", price);

        List<StoreItemResponseDTO> items = repository.findAllByOrderByPriceDesc(price).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        log.debug("Total items fetched below price {}: {}", price, items.size());
        return items;
    }

    public List<StoreItemResponseDTO> getItemsAbovePrice(int price) {
        log.info("Fetching store items above price: {}", price);

        List<StoreItemResponseDTO> items = repository.findAllByOrderByPriceAsc(price).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        log.debug("Total items fetched above price {}: {}", price, items.size());
        return items;
    }
}
