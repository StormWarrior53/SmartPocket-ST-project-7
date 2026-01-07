package org.example.server.repository;

import org.example.server.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParentRepository extends JpaRepository<Parent, UUID>
{
    Optional<Parent> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<Parent> findByGoogleId(String googleId);
    boolean existsByGoogleId(String googleId);
}
