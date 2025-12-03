package org.example.server.repository;

import org.example.server.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    // Finds Exercise by Title
    Optional<Exercise> findByTitle(String title);

    // Returns Exercises by set difficulty level
    List<Exercise> findByDifficultyLevel(String difficultyLevel);

    // Returns all Exercises with a keyword in their title
    List<Exercise> findByTitleContainingIgnoreCase(String keyword);

    // Returns all Exercises sorted by title (ASC)
    List<Exercise> findAllByOrderByTitleAsc();

    // Returns all Exercises sorted by creation date (createdAt DESC)
    List<Exercise> findAllByOrderByCreatedAtDesc();
}