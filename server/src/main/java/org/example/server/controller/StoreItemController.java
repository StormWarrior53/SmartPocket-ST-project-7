package org.example.server.controller;


import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.storeItem.StoreItemRequestDTO;
import org.example.server.dto.storeItem.StoreItemResponseDTO;
import org.example.server.service.StoreItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/store")
public class StoreItemController {

    private final StoreItemService service;

    @Autowired
    public StoreItemController(StoreItemService service) { this.service = service; }

    // ----------------- LISTING -----------------
    @GetMapping
    public List<StoreItemResponseDTO> getAllItems() {
        log.info("GET /api/store called to fetch all items");
        List<StoreItemResponseDTO> response = service.getAllItems();
        log.debug("Total items fetched: {}", response.size());
        return response;
    }

    @GetMapping("/asc")
    public List<StoreItemResponseDTO> getItemsSortedAsc() {
        log.info("GET /api/store/asc called to fetch items sorted ascending by price");
        List<StoreItemResponseDTO> response = service.getItemsSortedAsc();
        log.debug("Total items fetched (ASC): {}", response.size());
        return response;
    }

    @GetMapping("/desc")
    public List<StoreItemResponseDTO> getItemsSortedDesc() {
        log.info("GET /api/store/desc called to fetch items sorted descending by price");
        List<StoreItemResponseDTO> response = service.getItemsSortedDesc();
        log.debug("Total items fetched (DESC): {}", response.size());
        return response;
    }

    @GetMapping("/below/{price}")
    public List<StoreItemResponseDTO> getItemsBelowPrice(@PathVariable int price) {
        log.info("GET /api/store/below/{} called", price);
        List<StoreItemResponseDTO> response = service.getItemsBelowPrice(price);
        log.debug("Total items fetched below price {}: {}", price, response.size());
        return response;
    }

    @GetMapping("/above/{price}")
    public List<StoreItemResponseDTO> getItemsAbovePrice(@PathVariable int price) {
        log.info("GET /api/store/above/{} called", price);
        List<StoreItemResponseDTO> response = service.getItemsAbovePrice(price);
        log.debug("Total items fetched above price {}: {}", price, response.size());
        return response;
    }

    @GetMapping("/{id}")
    public StoreItemResponseDTO getItemById(@PathVariable UUID id) {
        log.info("GET /api/store/{} called", id);
        StoreItemResponseDTO response = service.getItemById(id);
        log.debug("Fetched store item: {}", response);
        return response;
    }

    // ----------------- CRUD -----------------
    @PostMapping
    public StoreItemResponseDTO createItem(@Valid @RequestBody StoreItemRequestDTO request) {
        log.info("POST /api/store called with payload: {}", request);
        StoreItemResponseDTO response =  service.createItem(request);
        log.info("Store item created successfully with id: {}", response.id());
        return response;
    }

    @PutMapping("/{id}")
    public StoreItemResponseDTO updateItem(@PathVariable UUID id,
                                        @Valid @RequestBody StoreItemRequestDTO request) {
        log.info("PUT /api/store/{} called with payload: {}", id, request);
        StoreItemResponseDTO response = service.updateItem(id, request);
        log.info("Store item updated successfully with id: {}", response.id());
        return response;
    }

    @PatchMapping("/{id}")
    public StoreItemResponseDTO patchItem(@PathVariable UUID id,
                                       @RequestBody StoreItemRequestDTO request) {
        log.info("PATCH /api/store/{} called with payload: {}", id, request);
        StoreItemResponseDTO response = service.patchItem(id, request);
        log.info("Store item patched successfully with id: {}", response.id());
        return response;
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable UUID id) {
        log.info("DELETE /api/store/{} called", id);
        service.deleteItem(id);
        log.info("Store item deleted successfully with id: {}", id);
    }

    // ----------------- STOCK -----------------
    @PostMapping("/{id}/restock")
    public StoreItemResponseDTO restockItem(@PathVariable UUID id,
                                         @RequestParam int amount) {
        log.info("POST /api/store/{}/restock called with amount: {}", id, amount);
        StoreItemResponseDTO response = service.restockItem(id, amount);
        log.info("Store item restocked successfully with id={} new stock={}", id, response.name());
        return response;
    }
}
