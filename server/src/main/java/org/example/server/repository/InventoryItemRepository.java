package org.example.server.repository;

import org.example.server.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

  // Find all inventory items for a specific child, ordered by purchase date (most
  // recent first)
  @Query("SELECT ii FROM InventoryItem ii JOIN FETCH ii.storeItem WHERE ii.child.id = :childId ORDER BY ii.purchasedAt DESC")
  List<InventoryItem> findByChildIdWithStoreItem(@Param("childId") UUID childId);

  Optional<InventoryItem> findByChildIdAndStoreItemId(UUID childId, UUID storeItemId);

  boolean existsByChildIdAndStoreItemId(UUID childId, UUID storeItemId);
}
