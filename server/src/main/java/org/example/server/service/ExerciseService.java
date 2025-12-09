package org.example.server.service;

import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.exerciseItem.ExerciseRequestDTO;
import org.example.server.dto.exerciseItem.ExerciseResponseDTO;
import org.example.server.exception.ExerciseNotFoundException;
import org.example.server.mapper.ExerciseMapper;
import org.example.server.model.DifficultyLevel;
import org.example.server.model.Exercise;
import org.example.server.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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
    log.info("Creating new exercise with title: {}", request.title());
    log.debug("CreateExercise payload: {}", request);

    Exercise exercise = mapper.toEntity(request);
    Exercise saved = repository.save(exercise);

    log.info("Exercise created successfully with id: {}", saved.getId());
    return mapper.toResponse(saved);
  }

  public ExerciseResponseDTO updateExercise(UUID id, ExerciseRequestDTO request) {
    log.info("Updating exercise with id: {}", id);
    log.debug("UpdateExercise payload: {}", request);

    Exercise exercise = repository.findById(id)
        .orElseThrow(() -> {
          log.warn("Exercise with id={} not found", id);
          return new ExerciseNotFoundException(id);
        });

    mapper.updateEntity(exercise, request);
    Exercise updated = repository.save(exercise);

    log.info("Exercise updated successfully with id: {}", updated.getId());
    return mapper.toResponse(updated);
  }

  public void deleteExercise(UUID id) {
    log.info("Deleting exercise with id: {}", id);

    if (!repository.existsById(id)) {
      log.warn("Cannot delete — exercise with id={} does not exist", id);
      throw new ExerciseNotFoundException(id);
    }
    repository.deleteById(id);
    log.info("Exercise deleted successfully with id: {}", id);
  }

  public ExerciseResponseDTO getExerciseById(UUID id) {
    log.info("Fetching exercise with id: {}", id);

    Exercise exercise = repository.findById(id)
        .orElseThrow(() -> {
          log.warn("Exercise with id={} not found", id);
          return new ExerciseNotFoundException(id);
        });

    log.debug("Fetched exercise: {}", exercise);
    return mapper.toResponse(exercise);
  }

  public List<ExerciseResponseDTO> getAllExercises() {
    log.info("Fetching all exercises");

    List<ExerciseResponseDTO> exercises = repository.findAll().stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());

    log.debug("Total exercises fetched: {}", exercises.size());
    return exercises;
  }

  public List<ExerciseResponseDTO> getExercisesByDifficulty(DifficultyLevel difficultyLevel) {
    log.info("Fetching exercises with difficulty level: {}", difficultyLevel);

    List<ExerciseResponseDTO> exercises = repository.findByDifficultyLevel(difficultyLevel).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());

    log.debug("Total exercises fetched for difficulty {}: {}", difficultyLevel, exercises.size());
    return exercises;
  }

  public List<ExerciseResponseDTO> searchExercisesByTitle(String keyword) {
    log.info("Searching exercises with keyword: {}", keyword);

    List<ExerciseResponseDTO> exercises = repository.findByTitleContainingIgnoreCase(keyword).stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());

    log.debug("Total exercises found for keyword '{}': {}", keyword, exercises.size());
    return exercises;
  }
}
