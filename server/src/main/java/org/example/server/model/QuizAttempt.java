package org.example.server.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "score_percent", nullable = false)
    private Integer scorePercent;

    @Column(name = "passed", nullable = false)
    private Boolean passed;

    @Column(name = "pocket_money_awarded", nullable = false)
    private Integer pocketMoneyAwarded;

    @Column(nullable = false, updatable = false)
    private LocalDateTime attemptedAt;

    @PrePersist
    protected void onCreate() {
        attemptedAt = LocalDateTime.now();
    }

    // Constructors
    public QuizAttempt() {}

    public QuizAttempt(Quiz quiz, Child child, Integer totalQuestions, Integer correctAnswers,
                       Integer scorePercent, Boolean passed, Integer pocketMoneyAwarded) {
        this.quiz = quiz;
        this.child = child;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.scorePercent = scorePercent;
        this.passed = passed;
        this.pocketMoneyAwarded = pocketMoneyAwarded;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Child getChild() {
        return child;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Integer getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(Integer scorePercent) {
        this.scorePercent = scorePercent;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public Integer getPocketMoneyAwarded() {
        return pocketMoneyAwarded;
    }

    public void setPocketMoneyAwarded(Integer pocketMoneyAwarded) {
        this.pocketMoneyAwarded = pocketMoneyAwarded;
    }

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }
}
