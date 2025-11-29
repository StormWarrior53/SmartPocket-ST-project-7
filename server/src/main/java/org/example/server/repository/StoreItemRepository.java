package org.example.server.repository;

import org.example.server.model.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StoreItemRepository extends JpaRepository<StoreItem, UUID> {

    // Find all items sorted by price ascending
    List<StoreItem> findAllByOrderByPriceAsc(int price);

    // Find all items sorted by price descending
    List<StoreItem> findAllByOrderByPriceDesc(int price);
}
