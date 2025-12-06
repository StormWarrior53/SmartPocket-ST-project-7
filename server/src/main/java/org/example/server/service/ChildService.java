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
    Child child = childRepository.findByNameAndParentFirstName(
        request.getChildName(),
        request.getParentName())
        .orElseThrow(() -> new InvalidCredentialsException("Invalid child name, parent name, or pattern"));

    // Validate pattern against stored pinHash
    if (!passwordEncoder.matches(request.getPattern(), child.getPinHash())) {
      throw new InvalidCredentialsException("Invalid child name, parent name, or pattern");
    }

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
        .message(hasPattern ? "Child found, please enter your pattern" : "Child found, please set up your pattern")
        .build();
  }

  @Transactional
  public ChildAuthResponse setupPattern(SetupPatternRequest request) {
    Child child = childRepository.findByNameAndParentFirstName(
        request.getChildName(),
        request.getParentName()).orElseThrow(() -> new ResourceNotFoundException("Child not found under this parent"));

    if (child.getPinHash() != null && !child.getPinHash().isEmpty()) {
      throw new DuplicateResourceException("Pattern is already set for this child");
    }

    child.setPinHash(passwordEncoder.encode(request.getPattern()));
    Child savedChild = childRepository.save(child);

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

    child.setName(request.getName());
    child.setAge(request.getAge());

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
