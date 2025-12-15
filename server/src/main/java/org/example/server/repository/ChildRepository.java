package org.example.server.repository;

import org.example.server.model.Child;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChildRepository extends JpaRepository<Child, UUID>
{
    List<Child> findByParentId(UUID parentId);

    List<Child> findByName(String name);

    Optional<Child> findFirstByName(String name);

    Optional<Child> findByNameAndParentEmail(String childName, String parentEmail);
}
