package org.example.server.service;

import lombok.RequiredArgsConstructor;
import org.example.server.dto.*;
import org.example.server.exception.AccessDeniedException;
import org.example.server.exception.DuplicateResourceException;
import org.example.server.exception.InvalidCredentialsException;
import org.example.server.exception.ResourceNotFoundException;
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
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional(readOnly = true)
    public ChildAuthResponse login(ChildLoginRequest request) {
        Child child = childRepository.findByNameAndParentEmail(
                        request.childName(),
                        request.parentEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid child name, parent email, or pattern"));

        if (!passwordEncoder.matches(request.pattern(), child.getPinHash())) {
            throw new InvalidCredentialsException("Invalid child name, parent email, or pattern");
        }

        String token = jwtUtil.generateToken(child.getId(), child.getName(), "child");

        return new ChildAuthResponse(
                child.getId(),
                child.getName(),
                child.getAge(),
                child.getXp(),
                child.getPocketMoney(),
                child.getAllowanceMoney(),
                child.getParent().getId(),
                "child",
                token,
                "Bearer");
    }

    @Transactional(readOnly = true)
    public CheckNameResponse checkNameExists(String childName, String parentEmail) {
        Optional<Child> childOptional = childRepository.findByNameAndParentEmail(childName, parentEmail);

        if (childOptional.isEmpty()) {
            return new CheckNameResponse(
                    false,
                    false,
                    "Child not found under this parent");
        }

        Child child = childOptional.get();
        boolean hasPattern = child.getPinHash() != null && !child.getPinHash().isEmpty();

        return new CheckNameResponse(
                true,
                hasPattern,
                hasPattern ? "Child found, please enter your pattern" : "Child found, please set up your pattern");
    }

    @Transactional
    public ChildAuthResponse setupPattern(SetupPatternRequest request) {
        Child child = childRepository.findByNameAndParentEmail(
                request.childName(),
                request.parentEmail()).orElseThrow(() -> new ResourceNotFoundException("Child not found under this parent"));

        if (child.getPinHash() != null && !child.getPinHash().isEmpty()) {
            throw new DuplicateResourceException("Pattern is already set for this child");
        }

        child.setPinHash(passwordEncoder.encode(request.pattern()));
        Child savedChild = childRepository.save(child);

        String token = jwtUtil.generateToken(savedChild.getId(), savedChild.getName(), "child");

        return new ChildAuthResponse(
                savedChild.getId(),
                savedChild.getName(),
                savedChild.getAge(),
                savedChild.getXp(),
                savedChild.getPocketMoney(),
                savedChild.getAllowanceMoney(),
                savedChild.getParent().getId(),
                "child",
                token,
                "Bearer");
    }

    @Transactional(readOnly = true)
    public List<ChildListResponse> getAllChildren() {
        return childRepository.findAll().stream()
                .map(child -> new ChildListResponse(
                        child.getId(),
                        child.getName(),
                        child.getAge()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChildListResponse> getChildrenByParentId(UUID parentId) {
        return childRepository.findByParentId(parentId).stream()
                .map(child -> new ChildListResponse(
                        child.getId(),
                        child.getName(),
                        child.getAge()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChildResponse getChildProfile(UUID childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));
        return toChildResponse(child);
    }

    @Transactional
    public ChildResponse createChild(UUID parentId, CreateChildRequest request) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

        Child child = Child.builder()
                .parent(parent)
                .name(request.name())
                .age(request.age())
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
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (!child.getParent().getId().equals(parentId)) {
            throw new AccessDeniedException("Access denied: Child does not belong to this parent");
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
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (!child.getParent().getId().equals(parentId)) {
            throw new AccessDeniedException("Access denied: Child does not belong to this parent");
        }

        child.setName(request.name());
        child.setAge(request.age());

        Child updatedChild = childRepository.save(child);
        return toChildResponse(updatedChild);
    }

    @Transactional
    public void deleteChild(UUID parentId, UUID childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (!child.getParent().getId().equals(parentId)) {
            throw new AccessDeniedException("Access denied: Child does not belong to this parent");
        }

        childRepository.delete(child);
    }

    @Transactional
    public ChildResponse resetPattern(UUID parentId, UUID childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (!child.getParent().getId().equals(parentId)) {
            throw new AccessDeniedException("Access denied: Child does not belong to this parent");
        }

        java.time.Instant expirationTime = java.time.Instant.now().plus(java.time.Duration.ofHours(24));
        tokenBlacklistService.blacklistAllUserTokens(childId, expirationTime, "Pattern reset by parent");

        child.setPinHash(null);
        Child updatedChild = childRepository.save(child);

        return toChildResponse(updatedChild);
    }

    public void logout(UUID childId, String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token is required for logout");
        }

        java.util.Date expirationDate = jwtUtil.getExpirationDate(token);
        java.time.Instant expirationTime = expirationDate.toInstant();

        tokenBlacklistService.blacklistToken(token, expirationTime, childId, "User logout");
    }

    @Transactional
    public ChildResponse addMoneyToChild(UUID parentId, UUID childId, int amount) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (!child.getParent().getId().equals(parentId)) {
            throw new AccessDeniedException("Access denied: Child does not belong to this parent");
        }

        child.setAllowanceMoney(child.getAllowanceMoney() + amount);
        child = childRepository.save(child);

        return toChildResponse(child);
    }

//  ---------------

    @Transactional
    public ChildResponse patchAllowanceMoneyMe(UUID childId, int newAllowanceMoney) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (newAllowanceMoney < 0) {
            throw new IllegalArgumentException("Allowance money must be non-negative");
        }

        child.setAllowanceMoney(newAllowanceMoney);
        Child updated = childRepository.save(child);
        return toChildResponse(updated);
    }

    @Transactional
    public ChildResponse patchPocketMoneyMe(UUID childId, int amount) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }

        child.setPocketMoney(child.getPocketMoney() + amount);
        Child updated = childRepository.save(child);
        return toChildResponse(updated);
    }

    @Transactional
    public ChildResponse patchXpMe(UUID childId, int amount) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found"));

        if (amount < 0) {
            throw new IllegalArgumentException("XP amount must be non-negative");
        }

        child.setXp(child.getXp() + amount);
        Child updated = childRepository.save(child);
        return toChildResponse(updated);
    }


//    -------------------

    private ChildResponse toChildResponse(Child child) {
        return new ChildResponse(
                child.getId(),
                child.getName(),
                child.getAge(),
                child.getXp(),
                child.getPocketMoney(),
                child.getAllowanceMoney(),
                child.getPinHash() != null && !child.getPinHash().isEmpty(),
                child.getParent().getId());
    }
}
