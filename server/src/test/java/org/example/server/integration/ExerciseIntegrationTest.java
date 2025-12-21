package org.example.server.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.server.dto.exerciseItem.ExerciseRequestDTO;
import org.example.server.model.DifficultyLevel;
import org.example.server.model.Exercise;
import org.example.server.repository.ExerciseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Exercise Integration Tests")
class ExerciseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExerciseRepository repository;

    private ExerciseRequestDTO createRequest() {
        return new ExerciseRequestDTO(
                "Math Task",
                "Solve basic math",
                DifficultyLevel.EASY,
                50
        );
    }

    @Test
    @DisplayName("POST /api/exercises - Should create exercise")
    void createExercise_ShouldReturnCreatedExercise() throws Exception {
        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Math Task"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.rewardXp").value(50));
    }

    @Test
    @DisplayName("GET /api/exercises - Should return all exercises")
    void getAllExercises_ShouldReturnList() throws Exception {
        repository.save(new Exercise(
                UUID.randomUUID(),
                "Reading",
                "Read a book",
                DifficultyLevel.MEDIUM,
                30
        ));

        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/exercises/{id} - Should return exercise by ID")
    void getExerciseById_ShouldReturnExercise() throws Exception {
        Exercise exercise = repository.save(new Exercise(
                UUID.randomUUID(),
                "Writing",
                "Write a short text",
                DifficultyLevel.HARD,
                70
        ));

        mockMvc.perform(get("/api/exercises/{id}", exercise.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Writing"))
                .andExpect(jsonPath("$.difficulty").value("HARD"));
    }

    @Test
    @DisplayName("PUT /api/exercises/{id} - Should update exercise")
    void updateExercise_ShouldReturnUpdatedExercise() throws Exception {
        Exercise exercise = repository.save(new Exercise(
                UUID.randomUUID(),
                "Old Title",
                "Old desc",
                DifficultyLevel.EASY,
                10
        ));

        ExerciseRequestDTO updateRequest = new ExerciseRequestDTO(
                "New Title",
                "Updated desc",
                DifficultyLevel.MEDIUM,
                40
        );

        mockMvc.perform(put("/api/exercises/{id}", exercise.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.rewardXp").value(40));
    }

    @Test
    @DisplayName("DELETE /api/exercises/{id} - Should delete exercise")
    void deleteExercise_ShouldRemoveExercise() throws Exception {
        Exercise exercise = repository.save(new Exercise(
                UUID.randomUUID(),
                "Temp",
                "To be deleted",
                DifficultyLevel.EASY,
                5
        ));

        mockMvc.perform(delete("/api/exercises/{id}", exercise.getId()))
                .andExpect(status().isOk());

        assertTrue(repository.findById(exercise.getId()).isEmpty());
    }

    @Test
    @DisplayName("GET /api/exercises/difficulty/{level} - Should filter by difficulty")
    void getExercisesByDifficulty_ShouldReturnFiltered() throws Exception {
        repository.save(new Exercise(
                UUID.randomUUID(),
                "Easy Task",
                "Easy",
                DifficultyLevel.EASY,
                10
        ));

        mockMvc.perform(get("/api/exercises/difficulty/EASY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].difficulty").value("EASY"));
    }

    @Test
    @DisplayName("GET /api/exercises/search - Should search by title")
    void searchExercises_ShouldReturnMatchingResults() throws Exception {
        repository.save(new Exercise(
                UUID.randomUUID(),
                "Math Homework",
                "Math task",
                DifficultyLevel.EASY,
                20
        ));

        mockMvc.perform(get("/api/exercises/search")
                        .param("keyword", "Math"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", containsString("Math")));
    }

    @Test
    @DisplayName("POST /api/exercises - Should fail validation for invalid request")
    void createExercise_InvalidRequest_ShouldReturn400() throws Exception {
        ExerciseRequestDTO invalidRequest = new ExerciseRequestDTO(
                "",
                "",
                null,
                -10
        );

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}