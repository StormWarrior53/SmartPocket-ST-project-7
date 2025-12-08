package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.exerciseItem.ExerciseRequestDTO;
import org.example.server.dto.exerciseItem.ExerciseResponseDTO;
import org.example.server.model.DifficultyLevel;
import org.example.server.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseService service;

    @Autowired
    public ExerciseController(ExerciseService service) {
        this.service = service;
    }

    // CRUD
    @GetMapping("/{id}")
    public ExerciseResponseDTO getExerciseById(@PathVariable UUID id) {log.info("GET /api/exercises/{} called", id);
        ExerciseResponseDTO response = service.getExerciseById(id);
        log.debug("Fetched exercise: {}", response);
        return response;
    }

    @GetMapping
    public List<ExerciseResponseDTO> getAllExercises() {
        log.info("GET /api/exercises called to fetch all exercises");
        List<ExerciseResponseDTO> response = service.getAllExercises();
        log.debug("Total exercises fetched: {}", response.size());
        return response;
    }

    @PostMapping
    public ExerciseResponseDTO createExercise(@Valid @RequestBody ExerciseRequestDTO request) {
        log.info("POST /api/exercises called with payload: {}", request);
        ExerciseResponseDTO response = service.createExercise(request);
        log.info("Exercise created successfully with id: {}", response.id());
        return response;
    }

    @PutMapping("/{id}")
    public ExerciseResponseDTO updateExercise(@PathVariable UUID id,
                                              @Valid @RequestBody ExerciseRequestDTO request) {
        log.info("PUT /api/exercises/{} called with payload: {}", id, request);
        ExerciseResponseDTO response = service.updateExercise(id, request);
        log.info("Exercise updated successfully with id: {}", response.id());
        return response;
    }

    @DeleteMapping("/{id}")
    public void deleteExercise(@PathVariable UUID id) {
        log.info("DELETE /api/exercises/{} called", id);
        service.deleteExercise(id);
        log.info("Exercise deleted successfully with id: {}", id);
    }

    // Filtering
    @GetMapping("/difficulty/{level}")
    public List<ExerciseResponseDTO> getExercisesByDifficulty(@PathVariable DifficultyLevel level) {
        log.info("GET /api/exercises/difficulty/{} called", level);
        List<ExerciseResponseDTO> response = service.getExercisesByDifficulty(level);
        log.debug("Total exercises fetched for difficulty {}: {}", level, response.size());
        return response;
    }

    @GetMapping("/search")
    public List<ExerciseResponseDTO> searchExercisesByTitle(@RequestParam String keyword) {
        log.info("GET /api/exercises/search called with keyword: {}", keyword);
        List<ExerciseResponseDTO> response = service.searchExercisesByTitle(keyword);
        log.debug("Total exercises found for keyword '{}': {}", keyword, response.size());
        return response;
    }
}