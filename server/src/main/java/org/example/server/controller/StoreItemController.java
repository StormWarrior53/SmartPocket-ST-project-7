package org.example.server.controller;


import jakarta.validation.Valid;
import org.example.server.dto.storeItem.StoreItemRequestDTO;
import org.example.server.dto.storeItem.StoreItemResponseDTO;
import org.example.server.service.StoreItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/store")
public class StoreItemController {

    private final StoreItemService service;

    @Autowired
    public StoreItemController(StoreItemService service) { this.service = service; }

    // ----------------- LISTING -----------------
    @GetMapping
    public List<StoreItemResponseDTO> getAllItems() {
        return service.getAllItems();
    }

    @GetMapping("/asc")
    public List<StoreItemResponseDTO> getItemsSortedAsc() {
        return service.getItemsSortedAsc();
    }

    @GetMapping("/desc")
    public List<StoreItemResponseDTO> getItemsSortedDesc() {
        return service.getItemsSortedDesc();
    }

    @GetMapping("/below/{price}")
    public List<StoreItemResponseDTO> getItemsBelowPrice(@PathVariable int price) {
        return service.getItemsBelowPrice(price);
    }

    @GetMapping("/above/{price}")
    public List<StoreItemResponseDTO> getItemsAbovePrice(@PathVariable int price) {
        return service.getItemsAbovePrice(price);
    }

    @GetMapping("/{id}")
    public StoreItemResponseDTO getItemById(@PathVariable UUID id) {
        return service.getItemById(id);
    }

    // ----------------- CRUD -----------------
    @PostMapping
    public StoreItemResponseDTO createItem(@Valid @RequestBody StoreItemRequestDTO request) {
        return service.createItem(request);
    }

    @PutMapping("/{id}")
    public StoreItemResponseDTO updateItem(@PathVariable UUID id,
                                        @Valid @RequestBody StoreItemRequestDTO request) {
        return service.updateItem(id, request);
    }

    @PatchMapping("/{id}")
    public StoreItemResponseDTO patchItem(@PathVariable UUID id,
                                       @RequestBody StoreItemRequestDTO request) {
        return service.patchItem(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable UUID id) {
        service.deleteItem(id);
    }

    // ----------------- STOCK -----------------
    @PostMapping("/{id}/restock")
    public StoreItemResponseDTO restockItem(@PathVariable UUID id,
                                         @RequestParam int amount) {
        return service.restockItem(id, amount);
    }
}
