package org.example.server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory_items",
    indexes = {
        @Index(name = "idx_inventory_child_id", columnList = "child_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_child_store", columnNames = {"child_id", "store_item_id"})
    })
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_item_id", nullable = false)
    private StoreItem storeItem;

    @Column(nullable = false)
    @Min(1)
    @Builder.Default
    private int quantity = 1;

    @Column(nullable = false)
    private int pricePaid; // Price at time of purchase

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime purchasedAt;
}
