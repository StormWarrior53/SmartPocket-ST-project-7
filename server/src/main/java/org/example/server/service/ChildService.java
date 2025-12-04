package org.example.server.service;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.*;
import org.example.server.model.Child;
import org.example.server.model.Parent;
import org.example.server.repository.ChildRepository;
import org.example.server.repository.ParentRepository;
import org.example.server.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChildService {

    private final ChildRepository childRepository;
    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public ChildAuthResponse login(ChildLoginRequest request) {
        // Find child by child name and parent name
        Child child = childRepository.findByNameAndParentFirstName(
                request.getChildName(),
                request.getParentName()
        ).orElseThrow(() -> new IllegalArgumentException("Invalid child name, parent name, or pattern"));

        // Validate pattern against stored pinHash
        if (!passwordEncoder.matches(request.getPattern(), child.getPinHash())) {
            throw new IllegalArgumentException("Invalid child name, parent name, or pattern");
        }

        // Generate JWT token with child role
        String token = jwtUtil.generateToken(child.getId(), child.getName(), "child");

        return ChildAuthResponse.builder()
                .id(child.getId())
                .name(child.getName())
                .age(child.getAge())
                .xp(child.getXp())
                .pocketMoney(child.getPocketMoney())
                .allowanceMoney(child.getAllowanceMoney())
                .role("child")
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    @Transactional(readOnly = true)
    public CheckNameResponse checkNameExists(String childName, String parentName) {
        Optional<Child> childOptional = childRepository.findByNameAndParentFirstName(childName, parentName);

        if (childOptional.isEmpty()) {
            return CheckNameResponse.builder()
                    .exists(false)
                    .hasPattern(false)
                    .message("Child not found under this parent")
                    .build();
        }

        Child child = childOptional.get();
        boolean hasPattern = child.getPinHash() != null && !child.getPinHash().isEmpty();

        return CheckNameResponse.builder()
                .exists(true)
                .hasPattern(hasPattern)
                .message(hasPattern ?
                    "Child found, please enter your pattern" :
                    "Child found, please set up your pattern")
                .build();
    }

    @Transactional
    public ChildAuthResponse setupPattern(SetupPatternRequest request) {
        // Find child by child name and parent name
        Child child = childRepository.findByNameAndParentFirstName(
                request.getChildName(),
                request.getParentName()
        ).orElseThrow(() -> new IllegalArgumentException("Child not found under this parent"));

        // Check if pattern is already set
        if (child.getPinHash() != null && !child.getPinHash().isEmpty()) {
            throw new IllegalArgumentException("Pattern is already set for this child");
        }

        // Hash and set the pattern
        child.setPinHash(passwordEncoder.encode(request.getPattern()));
        Child savedChild = childRepository.save(child);

        // Generate JWT token and return auth response
        String token = jwtUtil.generateToken(savedChild.getId(), savedChild.getName(), "child");

        return ChildAuthResponse.builder()
                .id(savedChild.getId())
                .name(savedChild.getName())
                .age(savedChild.getAge())
                .xp(savedChild.getXp())
                .pocketMoney(savedChild.getPocketMoney())
                .allowanceMoney(savedChild.getAllowanceMoney())
                .role("child")
                .token(token)
                .tokenType("Bearer")
                .build();
    }

    @Transactional(readOnly = true)
    public List<ChildListResponse> getAllChildren() {
        return childRepository.findAll().stream()
                .map(child -> ChildListResponse.builder()
                        .id(child.getId())
                        .name(child.getName())
                        .age(child.getAge())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChildListResponse> getChildrenByParentId(UUID parentId) {
        return childRepository.findByParentId(parentId).stream()
                .map(child -> ChildListResponse.builder()
                        .id(child.getId())
                        .name(child.getName())
                        .age(child.getAge())
                        .build())
                .collect(Collectors.toList());
    }

    // CRUD operations for parents to manage their children

    @Transactional
    public ChildResponse createChild(UUID parentId, CreateChildRequest request) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent not found"));

        Child child = Child.builder()
                .parent(parent)
                .name(request.getName())
                .age(request.getAge())
                .pinHash(null) // No pattern set initially
                .xp(0)
                .pocketMoney(0)
                .allowanceMoney(0)
                .build();

        Child savedChild = childRepository.save(child);

        return toChildResponse(savedChild);
    }

    @Transactional(readOnly = true)
    public ChildResponse getChildById(UUID parentId, UUID childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        // Verify ownership
        if (!child.getParent().getId().equals(parentId)) {
            throw new IllegalArgumentException("Access denied: Child does not belong to this parent");
        }

        return toChildResponse(child);
    }

    @Transactional(readOnly = true)
    public List<ChildResponse> getChildrenByParent(UUID parentId) {
        return childRepository.findByParentId(parentId).stream()
                .map(this::toChildResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChildResponse updateChild(UUID parentId, UUID childId, UpdateChildRequest request) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        // Verify ownership
        if (!child.getParent().getId().equals(parentId)) {
            throw new IllegalArgumentException("Access denied: Child does not belong to this parent");
        }

        child.setName(request.getName());
        child.setAge(request.getAge());

        Child updatedChild = childRepository.save(child);
        return toChildResponse(updatedChild);
    }

    @Transactional
    public void deleteChild(UUID parentId, UUID childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        // Verify ownership
        if (!child.getParent().getId().equals(parentId)) {
            throw new IllegalArgumentException("Access denied: Child does not belong to this parent");
        }

        childRepository.delete(child);
    }

    @Transactional
    public ChildResponse resetPattern(UUID parentId, UUID childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("Child not found"));

        // Verify ownership
        if (!child.getParent().getId().equals(parentId)) {
            throw new IllegalArgumentException("Access denied: Child does not belong to this parent");
        }

        // Reset the pattern by setting pinHash to null
        child.setPinHash(null);
        Child updatedChild = childRepository.save(child);

        return toChildResponse(updatedChild);
    }

    // Helper method to convert Child to ChildResponse
    private ChildResponse toChildResponse(Child child) {
        return ChildResponse.builder()
                .id(child.getId())
                .name(child.getName())
                .age(child.getAge())
                .xp(child.getXp())
                .pocketMoney(child.getPocketMoney())
                .allowanceMoney(child.getAllowanceMoney())
                .hasPattern(child.getPinHash() != null && !child.getPinHash().isEmpty())
                .build();
    }
}
