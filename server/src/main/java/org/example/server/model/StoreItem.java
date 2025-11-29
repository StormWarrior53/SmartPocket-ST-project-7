package org.example.server.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "store_items")
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "emoji", length = 10)
    private String emoji;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "stock")
    private int stock;

    // Constructors
    public StoreItem() {}

    public StoreItem(String name, String description, String emoji, int price, int stock) {
        this.name = name;
        this.description = description;
        this.emoji = emoji;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }




}
