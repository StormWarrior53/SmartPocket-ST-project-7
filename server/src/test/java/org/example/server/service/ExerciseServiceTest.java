package org.example.server.service;

import org.example.server.dto.exerciseItem.ExerciseRequestDTO;
import org.example.server.dto.exerciseItem.ExerciseResponseDTO;
import org.example.server.model.DifficultyLevel;
import org.example.server.model.Exercise;
import org.example.server.repository.ExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExerciseService.
 *
 * Covers:
 * - CRUD business logic
 * - Filtering & searching
 * - Error scenarios
 */
@DisplayName("ExerciseService - Business Logic Unit Tests")
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    private UUID exerciseId;
    private Exercise exercise;
    private ExerciseRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        exerciseId = UUID.randomUUID();

        exercise = new Exercise();
        exercise.setId(exerciseId);
        exercise.setTitle("Math Basics");
        exercise.setDescription("Simple math exercise");
        exercise.setDifficulty(DifficultyLevel.EASY);
        exercise.setReward(10);

        requestDTO = new ExerciseRequestDTO(
                "Math Basics",
                "Simple math exercise",
                DifficultyLevel.EASY,
                10
        );
    }

    // ---------- GET BY ID ----------

    @Test
    @DisplayName("getExerciseById() - Should return exercise when found")
    void getExerciseById_Found_ReturnsExercise() {
        // Arrange
        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));

        // Act
        ExerciseResponseDTO result =
                exerciseService.getExerciseById(exerciseId);

        // Assert
        assertNotNull(result);
        assertEquals(exerciseId, result.id());
        assertEquals("Math Basics", result.title());

        verify(exerciseRepository).findById(exerciseId);
    }

    @Test
    @DisplayName("getExerciseById() - Should throw exception when not found")
    void getExerciseById_NotFound_ThrowsException() {
        // Arrange
        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> exerciseService.getExerciseById(exerciseId));

        verify(exerciseRepository).findById(exerciseId);
    }

    // ---------- GET ALL ----------

    @Test
    @DisplayName("getAllExercises() - Should return all exercises")
    void getAllExercises_ReturnsList() {
        // Arrange
        when(exerciseRepository.findAll())
                .thenReturn(List.of(exercise));

        // Act
        List<ExerciseResponseDTO> result =
                exerciseService.getAllExercises();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Math Basics", result.get(0).title());

        verify(exerciseRepository).findAll();
    }

    @Test
    @DisplayName("getAllExercises() - Should return empty list when no data")
    void getAllExercises_NoData_ReturnsEmptyList() {
        // Arrange
        when(exerciseRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<ExerciseResponseDTO> result =
                exerciseService.getAllExercises();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---------- CREATE ----------

    @Test
    @DisplayName("createExercise() - Should save and return created exercise")
    void createExercise_ValidRequest_SavesExercise() {
        // Arrange
        when(exerciseRepository.save(any(Exercise.class)))
                .thenReturn(exercise);

        // Act
        ExerciseResponseDTO result =
                exerciseService.createExercise(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Math Basics", result.title());
        assertEquals(10, result.reward());

        verify(exerciseRepository).save(any(Exercise.class));
    }

    // ---------- UPDATE ----------

    @Test
    @DisplayName("updateExercise() - Should update existing exercise")
    void updateExercise_ExistingExercise_UpdatesSuccessfully() {
        // Arrange
        ExerciseRequestDTO updateRequest = new ExerciseRequestDTO(
                "Advanced Math",
                "Harder math",
                DifficultyLevel.HARD,
                30
        );

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(exercise));
        when(exerciseRepository.save(any(Exercise.class)))
                .thenReturn(exercise);

        // Act
        ExerciseResponseDTO result =
                exerciseService.updateExercise(exerciseId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Advanced Math", result.title());
        assertEquals(DifficultyLevel.HARD, result.difficulty());
        assertEquals(30, result.reward());

        verify(exerciseRepository).findById(exerciseId);
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    @DisplayName("updateExercise() - Should throw exception when exercise not found")
    void updateExercise_NotFound_ThrowsException() {
        // Arrange
        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> exerciseService.updateExercise(exerciseId, requestDTO));

        verify(exerciseRepository).findById(exerciseId);
        verify(exerciseRepository, never()).save(any());
    }

    // ---------- DELETE ----------

    @Test
    @DisplayName("deleteExercise() - Should delete exercise when exists")
    void deleteExercise_ExistingExercise_DeletesSuccessfully() {
        // Arrange
        when(exerciseRepository.existsById(exerciseId))
                .thenReturn(true);

        // Act
        exerciseService.deleteExercise(exerciseId);

        // Assert
        verify(exerciseRepository).deleteById(exerciseId);
    }

    @Test
    @DisplayName("deleteExercise() - Should throw exception when exercise not found")
    void deleteExercise_NotFound_ThrowsException() {
        // Arrange
        when(exerciseRepository.existsById(exerciseId))
                .thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> exerciseService.deleteExercise(exerciseId));

        verify(exerciseRepository, never()).deleteById(any());
    }

    // ---------- FILTER ----------

    @Test
    @DisplayName("getExercisesByDifficulty() - Should return exercises by difficulty")
    void getExercisesByDifficulty_ReturnsFilteredExercises() {
        // Arrange
        when(exerciseRepository.findByDifficulty(DifficultyLevel.EASY))
                .thenReturn(List.of(exercise));

        // Act
        List<ExerciseResponseDTO> result =
                exerciseService.getExercisesByDifficulty(DifficultyLevel.EASY);

        // Assert
        assertEquals(1, result.size());
        assertEquals(DifficultyLevel.EASY, result.get(0).difficulty());

        verify(exerciseRepository).findByDifficulty(DifficultyLevel.EASY);
    }

    // ---------- SEARCH ----------

    @Test
    @DisplayName("searchExercisesByTitle() - Should return exercises matching keyword")
    void searchExercisesByTitle_ReturnsMatchingExercises() {
        // Arrange
        when(exerciseRepository.findByTitleContainingIgnoreCase("math"))
                .thenReturn(List.of(exercise));

        // Act
        List<ExerciseResponseDTO> result =
                exerciseService.searchExercisesByTitle("math");

        // Assert
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).title().contains("Math"));

        verify(exerciseRepository)
                .findByTitleContainingIgnoreCase("math");
    }
}