package org.example.server.dto.budgetgame;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBudgetGameConfigDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Minimum savings rate is required")
    @DecimalMin(value = "0.0", message = "Savings rate cannot be negative")
    @DecimalMax(value = "1.0", message = "Savings rate cannot exceed 100%")
    private Double minSavingsRate;

    @NotNull(message = "Meal cost is required")
    @Min(value = 1, message = "Meal cost must be at least 1")
    private Integer mealCost;

    @NotNull(message = "Minimum meals is required")
    @Min(value = 1, message = "Minimum meals must be at least 1")
    private Integer minMeals;

    @NotNull(message = "Minimum electricity is required")
    @Min(value = 0, message = "Minimum electricity cannot be negative")
    private Integer minElectricity;

    @NotNull(message = "Minimum water is required")
    @Min(value = 0, message = "Minimum water cannot be negative")
    private Integer minWater;

    @NotNull(message = "Minimum gas is required")
    @Min(value = 0, message = "Minimum gas cannot be negative")
    private Integer minGas;

    @NotNull(message = "Minimum play budget is required")
    @Min(value = 1, message = "Minimum play budget must be at least 1")
    private Integer minPlayBudget;

    @NotNull(message = "Prize pocket money is required")
    @Min(value = 0, message = "Prize cannot be negative")
    private Integer prizePocketMoney;

    @NotBlank(message = "Event configuration is required")
    private String eventConfig;
}
