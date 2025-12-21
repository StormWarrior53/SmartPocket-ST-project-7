package org.example.server.repository;

import org.example.server.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {

    // Get the most recent quiz for an exercise
    @Query("SELECT q FROM Quiz q WHERE q.exercise.id = :exerciseId ORDER BY q.createdAt DESC LIMIT 1")
    Optional<Quiz> findByExerciseId(UUID exerciseId);
}
