package org.example.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.example.server.model.BudgetGameConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetGameConfigRepository extends JpaRepository<BudgetGameConfig, UUID> {

    Optional<BudgetGameConfig> findFirstByIsActiveTrue();

    List<BudgetGameConfig> findAllByOrderByCreatedAtDesc();
}
