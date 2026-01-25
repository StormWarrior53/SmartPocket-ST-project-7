package org.example.server.dto.budgetgame;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetGameConfigResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private Double minSavingsRate;
    private Integer mealCost;
    private Integer minMeals;
    private Integer minElectricity;
    private Integer minWater;
    private Integer minGas;
    private Integer minPlayBudget;
    private Integer prizePocketMoney;
    private String eventConfig;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
