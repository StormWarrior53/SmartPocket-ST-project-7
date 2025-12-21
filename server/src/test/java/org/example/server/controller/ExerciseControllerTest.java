package org.example.server.controller;

import org.example.server.dto.exerciseItem.ExerciseRequestDTO;
import org.example.server.dto.exerciseItem.ExerciseResponseDTO;
import org.example.server.model.DifficultyLevel;
import org.example.server.service.ExerciseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExerciseController.
 *
 * Covers:
 * - CRUD operations
 * - Filtering by difficulty
 * - Searching by title
 */
@DisplayName("ExerciseController - Unit Tests")
class ExerciseControllerTest {

    @Mock
    private ExerciseService exerciseService;

    @InjectMocks
    private ExerciseController exerciseController;

    private UUID exerciseId;
    private ExerciseRequestDTO requestDTO;
    private ExerciseResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        exerciseId = UUID.randomUUID();

        requestDTO = new ExerciseRequestDTO(
                "Math Basics",
                "Simple math exercise",
                DifficultyLevel.EASY,
                10
        );

        responseDTO = new ExerciseResponseDTO(
                exerciseId,
                "Math Basics",
                "Simple math exercise",
                DifficultyLevel.EASY,
                10
        );
    }

    // ---------- GET BY ID ----------

    @Test
    @DisplayName("getExerciseById() - Should return exercise when ID is valid")
    void getExerciseById_ValidId_ReturnsExercise() {
        // Arrange
        when(exerciseService.getExerciseById(exerciseId)).thenReturn(responseDTO);

        // Act
        ExerciseResponseDTO result = exerciseController.getExerciseById(exerciseId);

        // Assert
        assertNotNull(result);
        assertEquals(exerciseId, result.id());
        assertEquals("Math Basics", result.title());

        verify(exerciseService).getExerciseById(exerciseId);
    }

    // ---------- GET ALL ----------

    @Test
    @DisplayName("getAllExercises() - Should return list of exercises")
    void getAllExercises_ReturnsExerciseList() {
        // Arrange
        List<ExerciseResponseDTO> exercises = List.of(responseDTO);
        when(exerciseService.getAllExercises()).thenReturn(exercises);

        // Act
        List<ExerciseResponseDTO> result = exerciseController.getAllExercises();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Math Basics", result.get(0).title());

        verify(exerciseService).getAllExercises();
    }

    @Test
    @DisplayName("getAllExercises() - Should return empty list when no exercises exist")
    void getAllExercises_NoData_ReturnsEmptyList() {
        // Arrange
        when(exerciseService.getAllExercises()).thenReturn(List.of());

        // Act
        List<ExerciseResponseDTO> result = exerciseController.getAllExercises();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---------- CREATE ----------

    @Test
    @DisplayName("createExercise() - Should create exercise successfully")
    void createExercise_ValidRequest_ReturnsCreatedExercise() {
        // Arrange
        when(exerciseService.createExercise(any(ExerciseRequestDTO.class)))
                .thenReturn(responseDTO);

        // Act
        ExerciseResponseDTO result = exerciseController.createExercise(requestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Math Basics", result.title());
        assertEquals(DifficultyLevel.EASY, result.difficulty());

        verify(exerciseService).createExercise(requestDTO);
    }

    // ---------- UPDATE ----------

    @Test
    @DisplayName("updateExercise() - Should update exercise successfully")
    void updateExercise_ValidData_ReturnsUpdatedExercise() {
        // Arrange
        ExerciseRequestDTO updatedRequest = new ExerciseRequestDTO(
                "Advanced Math",
                "Harder math exercise",
                DifficultyLevel.HARD,
                30
        );

        ExerciseResponseDTO updatedResponse = new ExerciseResponseDTO(
                exerciseId,
                "Advanced Math",
                "Harder math exercise",
                DifficultyLevel.HARD,
                30
        );

        when(exerciseService.updateExercise(exerciseId, updatedRequest))
                .thenReturn(updatedResponse);

        // Act
        ExerciseResponseDTO result =
                exerciseController.updateExercise(exerciseId, updatedRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Advanced Math", result.title());
        assertEquals(DifficultyLevel.HARD, result.difficulty());

        verify(exerciseService).updateExercise(exerciseId, updatedRequest);
    }

    // ---------- DELETE ----------

    @Test
    @DisplayName("deleteExercise() - Should delete exercise successfully")
    void deleteExercise_ValidId_DeletesExercise() {
        // Arrange
        doNothing().when(exerciseService).deleteExercise(exerciseId);

        // Act
        exerciseController.deleteExercise(exerciseId);

        // Assert
        verify(exerciseService).deleteExercise(exerciseId);
        verifyNoMoreInteractions(exerciseService);
    }

    // ---------- FILTER BY DIFFICULTY ----------

    @Test
    @DisplayName("getExercisesByDifficulty() - Should return exercises for given difficulty")
    void getExercisesByDifficulty_ReturnsFilteredExercises() {
        // Arrange
        when(exerciseService.getExercisesByDifficulty(DifficultyLevel.EASY))
                .thenReturn(List.of(responseDTO));

        // Act
        List<ExerciseResponseDTO> result =
                exerciseController.getExercisesByDifficulty(DifficultyLevel.EASY);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DifficultyLevel.EASY, result.get(0).difficulty());

        verify(exerciseService).getExercisesByDifficulty(DifficultyLevel.EASY);
    }

    // ---------- SEARCH ----------

    @Test
    @DisplayName("searchExercisesByTitle() - Should return matching exercises")
    void searchExercisesByTitle_WithKeyword_ReturnsMatchingExercises() {
        // Arrange
        when(exerciseService.searchExercisesByTitle("math"))
                .thenReturn(List.of(responseDTO));

        // Act
        List<ExerciseResponseDTO> result =
                exerciseController.searchExercisesByTitle("math");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0).title().contains("Math"));

        verify(exerciseService).searchExercisesByTitle("math");
    }
}