package org.example.server.service;

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
        StoreItem item = mapper.toEntity(request);
        StoreItem saved = repository.save(item);
        return mapper.toResponse(saved);
    }

    public StoreItemResponseDTO updateItem(UUID id, StoreItemRequestDTO request) {
        StoreItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        mapper.updateEntity(item, request);
        StoreItem updated = repository.save(item);
        return mapper.toResponse(updated);
    }

    public StoreItemResponseDTO patchItem(UUID id, StoreItemRequestDTO request) {
        StoreItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (request.name() != null) item.setName(request.name());
        if (request.description() != null) item.setDescription(request.description());
        if (request.emoji() != null) item.setEmoji(request.emoji());
        if (request.price() != null) item.setPrice(request.price());
        if (request.stock() != null) item.setStock(request.stock());
        StoreItem updated = repository.save(item);
        return mapper.toResponse(updated);
    }

    public void deleteItem(UUID id) {
        repository.deleteById(id);
    }

    // ----------------- Stock management -----------------

    public StoreItemResponseDTO restockItem(UUID id, int additionalStock) {
        StoreItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        item.setStock(item.getStock() + additionalStock);
        StoreItem updated = repository.save(item);
        return mapper.toResponse(updated);
    }

    // ----------------- Retrieval & filtering -----------------

    public StoreItemResponseDTO getItemById(UUID id) {
        StoreItem item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return mapper.toResponse(item);
    }

    public List<StoreItemResponseDTO> getAllItems() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<StoreItemResponseDTO> getItemsSortedAsc() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "price")).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<StoreItemResponseDTO> getItemsSortedDesc() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "price")).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<StoreItemResponseDTO> getItemsBelowPrice(int price) {
        return repository.findAllByOrderByPriceDesc(price).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<StoreItemResponseDTO> getItemsAbovePrice(int price) {
        return repository.findAllByOrderByPriceAsc(price).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
