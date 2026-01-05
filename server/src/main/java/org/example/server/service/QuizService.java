package org.example.server.service;

import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.quiz.*;
import org.example.server.exception.ResourceNotFoundException;
import org.example.server.mapper.QuizMapper;
import org.example.server.model.*;
import org.example.server.repository.ChildRepository;
import org.example.server.repository.ExerciseRepository;
import org.example.server.repository.QuizAttemptRepository;
import org.example.server.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ChildRepository childRepository;
    private final ExerciseRepository exerciseRepository;
    private final QuizMapper quizMapper;

    @Autowired
    public QuizService(QuizRepository quizRepository,
                      QuizAttemptRepository quizAttemptRepository,
                      ChildRepository childRepository,
                      ExerciseRepository exerciseRepository,
                      QuizMapper quizMapper) {
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.childRepository = childRepository;
        this.exerciseRepository = exerciseRepository;
        this.quizMapper = quizMapper;
    }

    public QuizResponseDTO getQuizByExerciseId(UUID exerciseId) {
        log.info("Fetching quiz for exercise id: {}", exerciseId);

        Quiz quiz = quizRepository.findByExerciseId(exerciseId)
            .orElseThrow(() -> {
                log.warn("Quiz not found for exercise id: {}", exerciseId);
                return new ResourceNotFoundException("Quiz not found for exercise");
            });

        log.debug("Found quiz with id: {}", quiz.getId());
        return quizMapper.toResponse(quiz);
    }

    @Transactional
    public QuizSubmitResponseDTO submitQuiz(UUID quizId, UUID childId, QuizSubmitRequestDTO request) {
        log.info("Processing quiz submission - quizId: {}, childId: {}", quizId, childId);

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> {
                log.warn("Quiz not found with id: {}", quizId);
                return new ResourceNotFoundException("Quiz not found");
            });

        Child child = childRepository.findById(childId)
            .orElseThrow(() -> {
                log.warn("Child not found with id: {}", childId);
                return new ResourceNotFoundException("Child not found");
            });

        Map<UUID, String> answers = request.answers();
        int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = 0;

        for (Question question : quiz.getQuestions()) {
            String submittedAnswer = answers.get(question.getId());
            if (submittedAnswer != null && submittedAnswer.equals(question.getCorrectChoiceId())) {
                correctAnswers++;
            }
        }

        int scorePercent = totalQuestions == 0 ? 0 : Math.round((float) correctAnswers / totalQuestions * 100);
        boolean passed = scorePercent >= quiz.getPassPercent();
        int pocketMoneyAwarded = passed ? (correctAnswers * quiz.getPocketMoneyPerQuestion()) : 0;

        if (passed && pocketMoneyAwarded > 0) {
            child.setPocketMoney(child.getPocketMoney() + pocketMoneyAwarded);
            childRepository.save(child);
            log.info("Awarded {} pocket money to child {}", pocketMoneyAwarded, childId);
        }

        QuizAttempt attempt = new QuizAttempt(
            quiz,
            child,
            totalQuestions,
            correctAnswers,
            scorePercent,
            passed,
            pocketMoneyAwarded
        );
        quizAttemptRepository.save(attempt);

        log.info("Quiz attempt saved - Score: {}/{} ({}%), Passed: {}, Awarded: {}",
            correctAnswers, totalQuestions, scorePercent, passed, pocketMoneyAwarded);

        String message = passed
            ? "Congrats! You passed the quiz."
            : "You did not pass. Try again.";

        return new QuizSubmitResponseDTO(
            totalQuestions,
            correctAnswers,
            scorePercent,
            passed,
            pocketMoneyAwarded,
            message
        );
    }

    public List<QuizResponseDTO> getAllQuizzes() {
        log.info("Fetching all quizzes");
        return quizRepository.findAll().stream()
            .map(quizMapper::toResponse)
            .collect(Collectors.toList());
    }

    public QuizResponseDTO getQuizById(UUID quizId) {
        log.info("Fetching quiz by id: {}", quizId);
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        return quizMapper.toResponse(quiz);
    }

    @Transactional
    public QuizResponseDTO createQuiz(CreateQuizRequestDTO request) {
        log.info("Creating new quiz: {}", request.title());

        Exercise exercise = exerciseRepository.findById(request.exerciseId())
            .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));

        Quiz quiz = new Quiz();
        quiz.setTitle(request.title());
        quiz.setExercise(exercise);
        quiz.setPocketMoneyPerQuestion(request.pocketMoneyPerQuestion() != null ? request.pocketMoneyPerQuestion() : 5);
        quiz.setPassPercent(request.passPercent() != null ? request.passPercent() : 70);

        for (CreateQuestionDTO questionDTO : request.questions()) {
            Question question = new Question();
            question.setText(questionDTO.text());
            question.setCorrectChoiceId(questionDTO.correctChoiceId());
            question.setOrderIndex(questionDTO.orderIndex() != null ? questionDTO.orderIndex() : 0);
            quiz.addQuestion(question);

            for (CreateChoiceDTO choiceDTO : questionDTO.choices()) {
                Choice choice = new Choice();
                choice.setChoiceId(choiceDTO.choiceId());
                choice.setText(choiceDTO.text());
                choice.setOrderIndex(choiceDTO.orderIndex() != null ? choiceDTO.orderIndex() : 0);
                question.addChoice(choice);
            }
        }

        Quiz savedQuiz = quizRepository.save(quiz);
        log.info("Quiz created successfully with id: {}", savedQuiz.getId());
        return quizMapper.toResponse(savedQuiz);
    }

    @Transactional
    public void deleteQuiz(UUID quizId, boolean force) {
        log.info("Deleting quiz with id: {} (force: {})", quizId, force);

        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found");
        }

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        if (!attempts.isEmpty() && !force) {
            log.warn("Cannot delete quiz with id: {} - has {} existing attempts (use force=true to confirm)", quizId, attempts.size());
            throw new IllegalStateException(
                String.format("This quiz has %d attempt(s). Are you sure you want to delete it? All attempt history will be lost.", attempts.size())
            );
        }

        if (force && !attempts.isEmpty()) {
            log.info("Force deleting quiz with {} attempts", attempts.size());
            quizAttemptRepository.deleteAll(attempts);
        }

        quizRepository.deleteById(quizId);
        log.info("Quiz deleted successfully");
    }

    @Transactional
    public QuizResponseDTO updateQuiz(UUID quizId, UpdateQuizRequestDTO request) {
        log.info("Updating quiz with id: {}", quizId);

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        quiz.setTitle(request.title());
        quiz.setPocketMoneyPerQuestion(request.pocketMoneyPerQuestion() != null ? request.pocketMoneyPerQuestion() : 5);
        quiz.setPassPercent(request.passPercent() != null ? request.passPercent() : 70);

        quiz.getQuestions().clear();

        for (CreateQuestionDTO questionDTO : request.questions()) {
            Question question = new Question();
            question.setText(questionDTO.text());
            question.setCorrectChoiceId(questionDTO.correctChoiceId());
            question.setOrderIndex(questionDTO.orderIndex() != null ? questionDTO.orderIndex() : 0);
            quiz.addQuestion(question);

            for (CreateChoiceDTO choiceDTO : questionDTO.choices()) {
                Choice choice = new Choice();
                choice.setChoiceId(choiceDTO.choiceId());
                choice.setText(choiceDTO.text());
                choice.setOrderIndex(choiceDTO.orderIndex() != null ? choiceDTO.orderIndex() : 0);
                question.addChoice(choice);
            }
        }

        Quiz updatedQuiz = quizRepository.save(quiz);
        log.info("Quiz updated successfully with id: {}", updatedQuiz.getId());
        return quizMapper.toResponse(updatedQuiz);
    }

    public List<QuizAttemptResponseDTO> getQuizAttempts(UUID quizId) {
        log.info("Fetching attempts for quiz id: {}", quizId);

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);

        return attempts.stream()
            .map(attempt -> new QuizAttemptResponseDTO(
                attempt.getId(),
                attempt.getQuiz().getId(),
                attempt.getQuiz().getTitle(),
                attempt.getChild().getId(),
                attempt.getChild().getName(),
                attempt.getTotalQuestions(),
                attempt.getCorrectAnswers(),
                attempt.getScorePercent(),
                attempt.getPassed(),
                attempt.getPocketMoneyAwarded(),
                attempt.getAttemptedAt()
            ))
            .collect(Collectors.toList());
    }

    public List<QuizAttemptResponseDTO> getChildAttempts(UUID childId) {
        log.info("Fetching attempts for child id: {}", childId);

        List<QuizAttempt> attempts = quizAttemptRepository.findByChildId(childId);

        return attempts.stream()
            .map(attempt -> new QuizAttemptResponseDTO(
                attempt.getId(),
                attempt.getQuiz().getId(),
                attempt.getQuiz().getTitle(),
                attempt.getChild().getId(),
                attempt.getChild().getName(),
                attempt.getTotalQuestions(),
                attempt.getCorrectAnswers(),
                attempt.getScorePercent(),
                attempt.getPassed(),
                attempt.getPocketMoneyAwarded(),
                attempt.getAttemptedAt()
            ))
            .collect(Collectors.toList());
    }
}
