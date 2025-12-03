package org.example.server.controller;

import jakarta.validation.Valid;
import org.example.server.dto.exerciseItem.ExerciseRequestDTO;
import org.example.server.dto.exerciseItem.ExerciseResponseDTO;
import org.example.server.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ExerciseResponseDTO getExerciseById(@PathVariable Long id) {
        return service.getExerciseById(id);
    }

    @GetMapping
    public List<ExerciseResponseDTO> getAllExercises() {
        return service.getAllExercises();
    }

    @GetMapping("/paged")
    public Page<ExerciseResponseDTO> getExercisesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "true") boolean asc
    ) {
        return service.getExercisesPaged(page, size, sortBy, asc);
    }

    @PostMapping
    public ExerciseResponseDTO createExercise(@Valid @RequestBody ExerciseRequestDTO request) {
        return service.createExercise(request);
    }

    @PutMapping("/{id}")
    public ExerciseResponseDTO updateExercise(@PathVariable Long id,
                                              @Valid @RequestBody ExerciseRequestDTO request) {
        return service.updateExercise(id, request);
    }

    @PatchMapping("/{id}")
    public ExerciseResponseDTO patchExercise(@PathVariable Long id,
                                             @RequestBody ExerciseRequestDTO request) {
        return service.patchExercise(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteExercise(@PathVariable Long id) {
        service.deleteExercise(id);
    }

    // Filtering
    @GetMapping("/difficulty/{level}")
    public List<ExerciseResponseDTO> getExercisesByDifficulty(@PathVariable String level) {
        return service.getExercisesByDifficulty(level);
    }

    @GetMapping("/search")
    public List<ExerciseResponseDTO> searchExercisesByTitle(@RequestParam String keyword) {
        return service.searchExercisesByTitle(keyword);
    }
}