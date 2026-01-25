package org.example.server.controller;

import org.example.server.dto.budgetgame.BudgetGameConfigResponseDTO;
import org.example.server.service.BudgetGameConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/budget-game-config")
@RequiredArgsConstructor
public class BudgetGameConfigPublicController {

    private final BudgetGameConfigService service;

    /**
     * GET /api/budget-game-config/active
     * Returns the currently active game configuration
     * Public endpoint - any authenticated user can call this
     */
    @GetMapping("/active")
    public ResponseEntity<BudgetGameConfigResponseDTO> getActiveConfig() {
        BudgetGameConfigResponseDTO config = service.getActiveConfig();
        return ResponseEntity.ok(config);
    }
}
