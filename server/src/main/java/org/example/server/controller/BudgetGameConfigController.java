package org.example.server.controller;

import java.util.List;
import java.util.UUID;

import org.example.server.dto.budgetgame.BudgetGameConfigResponseDTO;
import org.example.server.dto.budgetgame.CreateBudgetGameConfigDTO;
import org.example.server.dto.budgetgame.UpdateBudgetGameConfigDTO;
import org.example.server.service.BudgetGameConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/budget-game-configs")
@RequiredArgsConstructor
public class BudgetGameConfigController {

    private final BudgetGameConfigService service;

    /**
     * GET /api/budget-game-configs
     * Lists all game configurations
     * Admin only
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BudgetGameConfigResponseDTO>> getAllConfigs() {
        List<BudgetGameConfigResponseDTO> configs = service.getAllConfigs();
        return ResponseEntity.ok(configs);
    }

    /**
     * GET /api/budget-game-configs/{id}
     * Get a specific configuration by ID
     * Admin only
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BudgetGameConfigResponseDTO> getConfigById(@PathVariable UUID id) {
        BudgetGameConfigResponseDTO config = service.getConfigById(id);
        return ResponseEntity.ok(config);
    }

    /**
     * POST /api/budget-game-configs
     * Create a new game configuration
     * Admin only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BudgetGameConfigResponseDTO> createConfig(
            @Valid @RequestBody CreateBudgetGameConfigDTO dto) {

        BudgetGameConfigResponseDTO created = service.createConfig(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/budget-game-configs/{id}
     * Update an existing configuration
     * Admin only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BudgetGameConfigResponseDTO> updateConfig(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetGameConfigDTO dto) {

        BudgetGameConfigResponseDTO updated = service.updateConfig(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/budget-game-configs/{id}
     * Delete a configuration
     * Admin only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConfig(@PathVariable UUID id) {
        service.deleteConfig(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/budget-game-configs/{id}/activate
     * Set a configuration as active
     * Admin only
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BudgetGameConfigResponseDTO> setActiveConfig(@PathVariable UUID id) {
        BudgetGameConfigResponseDTO activated = service.setActiveConfig(id);
        return ResponseEntity.ok(activated);
    }
}
