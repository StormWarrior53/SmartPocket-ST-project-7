package org.example.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.dto.quiz.*;
import org.example.server.exception.AccessDeniedException;
import org.example.server.service.QuizService;
import org.example.server.util.AuthenticationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final AuthenticationUtil authenticationUtil;

    @GetMapping("/exercises/{exerciseId}/quiz")
    public ResponseEntity<QuizResponseDTO> getQuizByExerciseId(@PathVariable UUID exerciseId) {
        QuizResponseDTO quiz = quizService.getQuizByExerciseId(exerciseId);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/quizzes/{quizId}/submit")
    public ResponseEntity<QuizSubmitResponseDTO> submitQuiz(
            @PathVariable UUID quizId,
            @Valid @RequestBody QuizSubmitRequestDTO request) {
        UUID childId = authenticationUtil.getCurrentUserId();
        QuizSubmitResponseDTO result = quizService.submitQuiz(quizId, childId, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizResponseDTO>> getAllQuizzes() {
        List<QuizResponseDTO> quizzes = quizService.getAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizResponseDTO> getQuizById(@PathVariable UUID quizId) {
        QuizResponseDTO quiz = quizService.getQuizById(quizId);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/quizzes")
    public ResponseEntity<QuizResponseDTO> createQuiz(@Valid @RequestBody CreateQuizRequestDTO request) {
        if (!authenticationUtil.isAdmin()) {
            throw new AccessDeniedException("Only admins can create quizzes");
        }
        QuizResponseDTO quiz = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }

    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<?> deleteQuiz(
            @PathVariable UUID quizId,
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        if (!authenticationUtil.isAdmin()) {
            throw new AccessDeniedException("Only admins can delete quizzes");
        }

        try {
            quizService.deleteQuiz(quizId, force);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    private record ErrorResponse(String message) {}


    @PutMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizResponseDTO> updateQuiz(
            @PathVariable UUID quizId,
            @Valid @RequestBody UpdateQuizRequestDTO request) {
        if (!authenticationUtil.isAdmin()) {
            throw new AccessDeniedException("Only admins can update quizzes");
        }
        QuizResponseDTO quiz = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(quiz);
    }

    @GetMapping("/quizzes/{quizId}/attempts")
    public ResponseEntity<List<QuizAttemptResponseDTO>> getQuizAttempts(@PathVariable UUID quizId) {
        if (!authenticationUtil.isAdmin()) {
            throw new AccessDeniedException("Only admins can view all quiz attempts");
        }
        List<QuizAttemptResponseDTO> attempts = quizService.getQuizAttempts(quizId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/children/me/quiz-attempts")
    public ResponseEntity<List<QuizAttemptResponseDTO>> getMyQuizAttempts() {
        UUID childId = authenticationUtil.getCurrentUserId();
        List<QuizAttemptResponseDTO> attempts = quizService.getChildAttempts(childId);
        return ResponseEntity.ok(attempts);
    }
}
