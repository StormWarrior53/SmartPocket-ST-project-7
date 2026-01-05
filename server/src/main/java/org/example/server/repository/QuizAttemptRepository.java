package org.example.server.repository;

import org.example.server.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    List<QuizAttempt> findByChildId(UUID childId);

    List<QuizAttempt> findByQuizId(UUID quizId);

    List<QuizAttempt> findByChildIdAndQuizId(UUID childId, UUID quizId);
}
