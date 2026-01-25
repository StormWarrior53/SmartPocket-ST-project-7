package org.example.server.service;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.budgetgame.BudgetGameConfigResponseDTO;
import org.example.server.dto.budgetgame.CreateBudgetGameConfigDTO;
import org.example.server.dto.budgetgame.UpdateBudgetGameConfigDTO;
import org.example.server.exception.ResourceNotFoundException;
import org.example.server.model.BudgetGameConfig;
import org.example.server.repository.BudgetGameConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetGameConfigService {

    private final BudgetGameConfigRepository repository;

    /**
     * Get the currently active game configuration
     */
    public BudgetGameConfigResponseDTO getActiveConfig() {
        BudgetGameConfig config = repository.findFirstByIsActiveTrue()
            .orElseThrow(() -> new ResourceNotFoundException("No active game configuration found"));

        return mapToResponseDTO(config);
    }

    /**
     * Get all configurations
     */
    public List<BudgetGameConfigResponseDTO> getAllConfigs() {
        return repository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::mapToResponseDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific configuration by ID
     */
    public BudgetGameConfigResponseDTO getConfigById(UUID id) {
        BudgetGameConfig config = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with id: " + id));

        return mapToResponseDTO(config);
    }

    /**
     * Create a new configuration
     */
    @Transactional
    public BudgetGameConfigResponseDTO createConfig(CreateBudgetGameConfigDTO dto) {
        BudgetGameConfig config = new BudgetGameConfig();

        config.setName(dto.getName());
        config.setDescription(dto.getDescription());
        config.setMinSavingsRate(dto.getMinSavingsRate());
        config.setMealCost(dto.getMealCost());
        config.setMinMeals(dto.getMinMeals());
        config.setMinElectricity(dto.getMinElectricity());
        config.setMinWater(dto.getMinWater());
        config.setMinGas(dto.getMinGas());
        config.setMinPlayBudget(dto.getMinPlayBudget());
        config.setPrizePocketMoney(dto.getPrizePocketMoney());
        config.setEventConfig(dto.getEventConfig());
        config.setIsActive(false);

        BudgetGameConfig saved = repository.save(config);

        return mapToResponseDTO(saved);
    }

    /**
     * Update an existing configuration
     */
    @Transactional
    public BudgetGameConfigResponseDTO updateConfig(UUID id, UpdateBudgetGameConfigDTO dto) {
        BudgetGameConfig config = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with id: " + id));

        config.setName(dto.getName());
        config.setDescription(dto.getDescription());
        config.setMinSavingsRate(dto.getMinSavingsRate());
        config.setMealCost(dto.getMealCost());
        config.setMinMeals(dto.getMinMeals());
        config.setMinElectricity(dto.getMinElectricity());
        config.setMinWater(dto.getMinWater());
        config.setMinGas(dto.getMinGas());
        config.setMinPlayBudget(dto.getMinPlayBudget());
        config.setPrizePocketMoney(dto.getPrizePocketMoney());
        config.setEventConfig(dto.getEventConfig());

        BudgetGameConfig updated = repository.save(config);

        return mapToResponseDTO(updated);
    }

    /**
     * Delete a configuration
     */
    @Transactional
    public void deleteConfig(UUID id) {
        BudgetGameConfig config = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with id: " + id));

        if (Boolean.TRUE.equals(config.getIsActive())) {
            throw new IllegalStateException("Cannot delete the active configuration. Set another config as active first.");
        }

        repository.delete(config);
    }

    /**
     * Set a configuration as active
     */
    @Transactional
    public BudgetGameConfigResponseDTO setActiveConfig(UUID id) {
        List<BudgetGameConfig> allConfigs = repository.findAll();
        for (BudgetGameConfig config : allConfigs) {
            config.setIsActive(false);
        }
        repository.saveAll(allConfigs);

        BudgetGameConfig config = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Configuration not found with id: " + id));

        config.setIsActive(true);
        BudgetGameConfig activated = repository.save(config);

        return mapToResponseDTO(activated);
    }

    /**
     * Helper method: Convert entity to DTO
     */
    private BudgetGameConfigResponseDTO mapToResponseDTO(BudgetGameConfig config) {
        return new BudgetGameConfigResponseDTO(
            config.getId(),
            config.getName(),
            config.getDescription(),
            config.getMinSavingsRate(),
            config.getMealCost(),
            config.getMinMeals(),
            config.getMinElectricity(),
            config.getMinWater(),
            config.getMinGas(),
            config.getMinPlayBudget(),
            config.getPrizePocketMoney(),
            config.getEventConfig(),
            config.getIsActive(),
            config.getCreatedAt(),
            config.getUpdatedAt()
        );
    }
}
