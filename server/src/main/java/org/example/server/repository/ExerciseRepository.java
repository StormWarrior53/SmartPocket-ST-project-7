package org.example.server.repository;

import org.example.server.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // Намира упражнение по заглавие
    Optional<Exercise> findByTitle(String title);

    // Връща всички упражнения с дадено ниво на трудност
    List<Exercise> findByDifficultyLevel(String difficultyLevel);

    // Връща всички упражнения, съдържащи определен текст в заглавието
    List<Exercise> findByTitleContainingIgnoreCase(String keyword);

    // Връща всички упражнения, сортирани по заглавие (ASC)
    List<Exercise> findAllByOrderByTitleAsc();

    // Връща всички упражнения, сортирани по създаване (createdAt DESC)
    List<Exercise> findAllByOrderByCreatedAtDesc();
}