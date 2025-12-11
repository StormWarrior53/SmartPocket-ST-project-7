package org.example.server.service;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.inventory.InventoryItemResponse;
import org.example.server.dto.inventory.PurchaseItemRequest;
import org.example.server.exception.AccessDeniedException;
import org.example.server.exception.ResourceNotFoundException;
import org.example.server.mapper.InventoryMapper;
import org.example.server.model.Child;
import org.example.server.model.InventoryItem;
import org.example.server.model.StoreItem;
import org.example.server.repository.ChildRepository;
import org.example.server.repository.InventoryItemRepository;
import org.example.server.repository.StoreItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

  private final InventoryItemRepository inventoryItemRepository;
  private final ChildRepository childRepository;
  private final StoreItemRepository storeItemRepository;
  private final InventoryMapper inventoryMapper;

  @Transactional
  public InventoryItemResponse purchaseItem(UUID childId, PurchaseItemRequest request) {
    Child child = childRepository.findById(childId)
        .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

    StoreItem storeItem = storeItemRepository.findById(request.getStoreItemId())
        .orElseThrow(() -> new ResourceNotFoundException("Store item not found"));

    if (storeItem.getStock() < request.getQuantity()) {
      throw new IllegalArgumentException("Insufficient stock. Available: " + storeItem.getStock());
    }

    int totalCost = storeItem.getPrice() * request.getQuantity();

    if (child.getPocketMoney() < totalCost) {
      throw new IllegalArgumentException(
          "Insufficient funds. Need: " + totalCost + ", Have: " + child.getPocketMoney());
    }

    InventoryItem inventoryItem = inventoryItemRepository
        .findByChildIdAndStoreItemId(childId, request.getStoreItemId())
        .orElse(null);

    if (inventoryItem != null) {
      inventoryItem.setQuantity(inventoryItem.getQuantity() + request.getQuantity());
    } else {
      inventoryItem = InventoryItem.builder()
          .child(child)
          .storeItem(storeItem)
          .quantity(request.getQuantity())
          .pricePaid(storeItem.getPrice())
          .build();
    }

    child.setPocketMoney(child.getPocketMoney() - totalCost);

    storeItem.setStock(storeItem.getStock() - request.getQuantity());

    inventoryItem = inventoryItemRepository.save(inventoryItem);
    childRepository.save(child);
    storeItemRepository.save(storeItem);

    return inventoryMapper.toResponse(inventoryItem);
  }

  @Transactional
  public void removeItemFromInventory(UUID childId, UUID inventoryItemId) {
    InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryItemId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

    if (!inventoryItem.getChild().getId().equals(childId)) {
      throw new AccessDeniedException("Access denied: This inventory item does not belong to you");
    }

    inventoryItemRepository.delete(inventoryItem);
  }

  @Transactional(readOnly = true)
  public List<InventoryItemResponse> getMyInventory(UUID childId) {
    if (!childRepository.existsById(childId)) {
      throw new ResourceNotFoundException("Child not found");
    }

    List<InventoryItem> inventoryItems = inventoryItemRepository.findByChildIdWithStoreItem(childId);

    return inventoryItems.stream()
        .map(inventoryMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<InventoryItemResponse> getChildInventoryForParent(UUID parentId, UUID childId) {
    Child child = childRepository.findById(childId)
        .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

    if (!child.getParent().getId().equals(parentId)) {
      throw new AccessDeniedException("Access denied: Child does not belong to this parent");
    }

    List<InventoryItem> inventoryItems = inventoryItemRepository.findByChildIdWithStoreItem(childId);

    return inventoryItems.stream()
        .map(inventoryMapper::toResponse)
        .collect(Collectors.toList());
  }
}
