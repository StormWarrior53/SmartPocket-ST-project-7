package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.dto.inventory.InventoryItemResponse;
import org.example.server.dto.inventory.PurchaseItemRequest;
import org.example.server.service.InventoryService;
import org.example.server.util.AuthenticationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final AuthenticationUtil authenticationUtil;

    @GetMapping("/me/inventory")
    public ResponseEntity<List<InventoryItemResponse>> getMyInventory() {
        UUID childId = authenticationUtil.getCurrentUserId();
        List<InventoryItemResponse> inventory = inventoryService.getMyInventory(childId);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/me/inventory/purchase")
    public ResponseEntity<InventoryItemResponse> purchaseItem(@Valid @RequestBody PurchaseItemRequest request) {
        UUID childId = authenticationUtil.getCurrentUserId();
        InventoryItemResponse response = inventoryService.purchaseItem(childId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/me/inventory/{inventoryItemId}")
    public ResponseEntity<Void> removeItemFromInventory(@PathVariable UUID inventoryItemId) {
        UUID childId = authenticationUtil.getCurrentUserId();
        inventoryService.removeItemFromInventory(childId, inventoryItemId);
        return ResponseEntity.noContent().build();
    }
}
