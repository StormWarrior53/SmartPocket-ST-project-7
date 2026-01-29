package org.example.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budget_game_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetGameConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "min_savings_rate", nullable = false)
    private Double minSavingsRate;

    @Column(name = "meal_cost", nullable = false)
    private Integer mealCost;

    @Column(name = "min_meals", nullable = false)
    private Integer minMeals;

    @Column(name = "min_electricity", nullable = false)
    private Integer minElectricity;

    @Column(name = "min_water", nullable = false)
    private Integer minWater;

    @Column(name = "min_gas", nullable = false)
    private Integer minGas;

    @Column(name = "min_play_budget", nullable = false)
    private Integer minPlayBudget;

    @Column(name = "prize_pocket_money", nullable = false)
    private Integer prizePocketMoney;

    @Column(name = "event_config", nullable = false, columnDefinition = "TEXT")
    private String eventConfig;

    @Column(name = "is_active")
    private Boolean isActive = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
