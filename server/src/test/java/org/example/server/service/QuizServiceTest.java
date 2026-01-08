package org.example.server.service;

import org.example.server.dto.quiz.*;
import org.example.server.exception.ResourceNotFoundException;
import org.example.server.mapper.QuizMapper;
import org.example.server.model.*;
import org.example.server.repository.*;
import org.example.server.util.AuthenticationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

  @Mock
  private QuizRepository quizRepository;

  @Mock
  private ExerciseRepository exerciseRepository;

  @Mock
  private ChildRepository childRepository;

  @Mock
  private QuizAttemptRepository quizAttemptRepository;

  @Mock
  private AuthenticationUtil authenticationUtil;

  @Mock
  private QuizMapper quizMapper;

  @InjectMocks
  private QuizService quizService;

  private Quiz testQuiz;
  private Exercise testExercise;
  private Child testChild;
  private UUID exerciseId;
  private UUID quizId;
  private UUID childId;

  @BeforeEach
  void setUp() {
    exerciseId = UUID.randomUUID();
    quizId = UUID.randomUUID();
    childId = UUID.randomUUID();

    // Setup test exercise
    testExercise = new Exercise();
    testExercise.setId(exerciseId);
    testExercise.setTitle("Test Exercise");

    // Setup test quiz
    testQuiz = new Quiz();
    testQuiz.setId(quizId);
    testQuiz.setTitle("Test Quiz");
    testQuiz.setExercise(testExercise);
    testQuiz.setPocketMoneyPerQuestion(10);
    testQuiz.setPassPercent(70);

    // Setup test questions
    List<Question> questions = new ArrayList<>();
    Question question1 = new Question();
    question1.setId(UUID.randomUUID());
    question1.setText("Question 1?");
    question1.setCorrectChoiceId("a");
    question1.setOrderIndex(0);
    question1.setQuiz(testQuiz);

    Choice choice1a = new Choice();
    choice1a.setChoiceId("a");
    choice1a.setText("Correct Answer");
    choice1a.setOrderIndex(0);
    choice1a.setQuestion(question1);

    Choice choice1b = new Choice();
    choice1b.setChoiceId("b");
    choice1b.setText("Wrong Answer");
    choice1b.setOrderIndex(1);
    choice1b.setQuestion(question1);

    question1.setChoices(Arrays.asList(choice1a, choice1b));
    questions.add(question1);

    testQuiz.setQuestions(questions);

    // Setup test child
    testChild = new Child();
    testChild.setId(childId);
    testChild.setName("Test Child");
    testChild.setPocketMoney(100);
  }

  @Test
  void getQuizByExerciseId_Success() {
    QuizResponseDTO mockResponse = new QuizResponseDTO(
        quizId, "Test Quiz", 10, 70, List.of());

    when(quizRepository.findByExerciseId(exerciseId)).thenReturn(Optional.of(testQuiz));
    when(quizMapper.toResponse(testQuiz)).thenReturn(mockResponse);

    QuizResponseDTO result = quizService.getQuizByExerciseId(exerciseId);

    assertNotNull(result);
    assertEquals("Test Quiz", result.title());
    verify(quizRepository).findByExerciseId(exerciseId);
    verify(quizMapper).toResponse(testQuiz);
  }

  @Test
  void getQuizByExerciseId_NotFound() {
    when(quizRepository.findByExerciseId(exerciseId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> quizService.getQuizByExerciseId(exerciseId));
  }

  @Test
  void submitQuiz_PassingScore() {
    UUID questionId = testQuiz.getQuestions().get(0).getId();
    Map<UUID, String> answers = Map.of(questionId, "a"); // Correct answer

    QuizSubmitRequestDTO request = new QuizSubmitRequestDTO(answers);

    when(quizRepository.findById(quizId)).thenReturn(Optional.of(testQuiz));
    when(childRepository.findById(childId)).thenReturn(Optional.of(testChild));
    when(childRepository.save(any(Child.class))).thenReturn(testChild);
    when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

    QuizSubmitResponseDTO result = quizService.submitQuiz(quizId, childId, request);

    assertNotNull(result);
    assertTrue(result.passed());
    assertEquals(100, result.percent());
    assertEquals(10, result.awarded()); // 1 question * 10
    verify(childRepository).save(any(Child.class));
    verify(quizAttemptRepository).save(any(QuizAttempt.class));
  }

  @Test
  void submitQuiz_FailingScore() {
    UUID questionId = testQuiz.getQuestions().get(0).getId();
    Map<UUID, String> answers = Map.of(questionId, "b"); // Wrong answer

    QuizSubmitRequestDTO request = new QuizSubmitRequestDTO(answers);

    when(quizRepository.findById(quizId)).thenReturn(Optional.of(testQuiz));
    when(childRepository.findById(childId)).thenReturn(Optional.of(testChild));
    when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

    QuizSubmitResponseDTO result = quizService.submitQuiz(quizId, childId, request);

    assertNotNull(result);
    assertFalse(result.passed());
    assertEquals(0, result.percent());
    assertEquals(0, result.awarded());
    verify(childRepository, never()).save(any(Child.class)); // No money awarded
    verify(quizAttemptRepository).save(any(QuizAttempt.class));
  }

  @Test
  void getAllQuizzes_Success() {
    QuizResponseDTO mockResponse = new QuizResponseDTO(
        quizId, "Test Quiz", 10, 70, List.of());

    when(quizRepository.findAll()).thenReturn(Arrays.asList(testQuiz));
    when(quizMapper.toResponse(testQuiz)).thenReturn(mockResponse);

    List<QuizResponseDTO> result = quizService.getAllQuizzes();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test Quiz", result.get(0).title());
    verify(quizRepository).findAll();
    verify(quizMapper).toResponse(testQuiz);
  }

  @Test
  void createQuiz_Success() {
    QuizResponseDTO mockResponse = new QuizResponseDTO(
        quizId, "Test Quiz", 10, 70, List.of());

    when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(testExercise));
    when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);
    when(quizMapper.toResponse(any(Quiz.class))).thenReturn(mockResponse);

    CreateQuestionDTO questionDTO = new CreateQuestionDTO(
        "Question 1?",
        "a",
        0,
        Arrays.asList(
            new CreateChoiceDTO("a", "Correct Answer", 0),
            new CreateChoiceDTO("b", "Wrong Answer", 1)));

    CreateQuizRequestDTO request = new CreateQuizRequestDTO(
        "Test Quiz",
        exerciseId,
        10,
        70,
        Arrays.asList(questionDTO));

    QuizResponseDTO result = quizService.createQuiz(request);

    assertNotNull(result);
    verify(quizRepository).save(any(Quiz.class));
    verify(quizMapper).toResponse(any(Quiz.class));
  }

  @Test
  void deleteQuiz_WithoutForce_HasAttempts() {
    List<QuizAttempt> attempts = Arrays.asList(new QuizAttempt());

    when(quizRepository.existsById(quizId)).thenReturn(true);
    when(quizAttemptRepository.findByQuizId(quizId)).thenReturn(attempts);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> quizService.deleteQuiz(quizId, false));

    assertTrue(exception.getMessage().contains("attempt"));
    verify(quizRepository, never()).deleteById(quizId);
  }

  @Test
  void deleteQuiz_WithForce_DeletesAttemptsAndQuiz() {
    QuizAttempt attempt = new QuizAttempt();
    List<QuizAttempt> attempts = Arrays.asList(attempt);

    when(quizRepository.existsById(quizId)).thenReturn(true);
    when(quizAttemptRepository.findByQuizId(quizId)).thenReturn(attempts);

    quizService.deleteQuiz(quizId, true);

    verify(quizAttemptRepository).deleteAll(attempts);
    verify(quizRepository).deleteById(quizId);
  }

  @Test
  void deleteQuiz_NoAttempts_DeletesQuiz() {
    when(quizRepository.existsById(quizId)).thenReturn(true);
    when(quizAttemptRepository.findByQuizId(quizId)).thenReturn(new ArrayList<>());

    quizService.deleteQuiz(quizId, false);

    verify(quizRepository).deleteById(quizId);
    verify(quizAttemptRepository, never()).deleteAll(any());
  }

  @Test
  void submitQuiz_RetakeAfterPassing_NoMoneyAwarded() {
    UUID questionId = testQuiz.getQuestions().get(0).getId();
    Map<UUID, String> answers = Map.of(questionId, "a"); // Correct answer

    // Create a previous passing attempt
    QuizAttempt previousAttempt = new QuizAttempt(testQuiz, testChild, 1, 1, 100, true, 10);
    List<QuizAttempt> previousAttempts = Arrays.asList(previousAttempt);

    QuizSubmitRequestDTO request = new QuizSubmitRequestDTO(answers);

    when(quizRepository.findById(quizId)).thenReturn(Optional.of(testQuiz));
    when(childRepository.findById(childId)).thenReturn(Optional.of(testChild));
    when(quizAttemptRepository.findByChildIdAndQuizId(childId, quizId)).thenReturn(previousAttempts);
    when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

    QuizSubmitResponseDTO result = quizService.submitQuiz(quizId, childId, request);

    assertNotNull(result);
    assertTrue(result.passed());
    assertEquals(100, result.percent());
    assertEquals(0, result.awarded()); // No money awarded on retake
    assertTrue(result.message().contains("already awarded"));
    verify(childRepository, never()).save(any(Child.class)); // No money transaction
    verify(quizAttemptRepository).save(any(QuizAttempt.class)); // But attempt is still recorded
  }

  @Test
  void submitQuiz_RetakeAfterFailing_MoneyAwardedIfPassed() {
    UUID questionId = testQuiz.getQuestions().get(0).getId();
    Map<UUID, String> answers = Map.of(questionId, "a"); // Correct answer

    // Create a previous failing attempt
    QuizAttempt previousAttempt = new QuizAttempt(testQuiz, testChild, 1, 0, 0, false, 0);
    List<QuizAttempt> previousAttempts = Arrays.asList(previousAttempt);

    QuizSubmitRequestDTO request = new QuizSubmitRequestDTO(answers);

    when(quizRepository.findById(quizId)).thenReturn(Optional.of(testQuiz));
    when(childRepository.findById(childId)).thenReturn(Optional.of(testChild));
    when(quizAttemptRepository.findByChildIdAndQuizId(childId, quizId)).thenReturn(previousAttempts);
    when(childRepository.save(any(Child.class))).thenReturn(testChild);
    when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

    QuizSubmitResponseDTO result = quizService.submitQuiz(quizId, childId, request);

    assertNotNull(result);
    assertTrue(result.passed());
    assertEquals(100, result.percent());
    assertEquals(10, result.awarded()); // Money awarded on first pass
    verify(childRepository).save(any(Child.class)); // Money transaction occurs
    verify(quizAttemptRepository).save(any(QuizAttempt.class));
  }
}
