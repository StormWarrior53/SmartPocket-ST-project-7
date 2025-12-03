package org.example.server.service;

import org.example.server.dto.exerciseItem.ExerciseRequestDTO;
import org.example.server.dto.exerciseItem.ExerciseResponseDTO;
import org.example.server.exception.ExerciseNotFoundException;
import org.example.server.mapper.ExerciseMapper;
import org.example.server.model.DifficultyLevel;
import org.example.server.model.Exercise;
import org.example.server.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExerciseService {

    private final ExerciseRepository repository;
    private final ExerciseMapper mapper;

    @Autowired
    public ExerciseService(ExerciseRepository repository, ExerciseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ExerciseResponseDTO createExercise(ExerciseRequestDTO request) {
        Exercise exercise = mapper.toEntity(request);
        Exercise saved = repository.save(exercise);
        return mapper.toResponse(saved);
    }

    public ExerciseResponseDTO updateExercise(UUID id, ExerciseRequestDTO request) {
        Exercise exercise = repository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
        mapper.updateEntity(exercise, request);
        Exercise updated = repository.save(exercise);
        return mapper.toResponse(updated);
    }

    public void deleteExercise(UUID id) {
        if (!repository.existsById(id)) throw new ExerciseNotFoundException(id);
        repository.deleteById(id);
    }

    public ExerciseResponseDTO getExerciseById(UUID id) {
        Exercise exercise = repository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
        return mapper.toResponse(exercise);
    }

    public List<ExerciseResponseDTO> getAllExercises() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExerciseResponseDTO> getExercisesByDifficulty(DifficultyLevel difficultyLevel) {
        return repository.findByDifficultyLevel(difficultyLevel).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ExerciseResponseDTO> searchExercisesByTitle(String keyword) {
        return repository.findByTitleContainingIgnoreCase(keyword).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}