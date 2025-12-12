package org.example.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.AuthResponse;
import org.example.server.dto.LoginRequest;
import org.example.server.dto.ParentResponse;
import org.example.server.dto.RegisterParentRequest;
import org.example.server.exception.DuplicateResourceException;
import org.example.server.exception.InvalidCredentialsException;
import org.example.server.exception.ResourceNotFoundException;
import org.example.server.model.Parent;
import org.example.server.repository.ParentRepository;
import org.example.server.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentService {

  private final ParentRepository parentRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final TokenBlacklistService tokenBlacklistService;

  @Transactional
  public AuthResponse registerParent(RegisterParentRequest request) {
    log.info("Attempting to register parent. Email={}", request.email());

    if (parentRepository.existsByEmail(request.email())) {
      log.warn("Registration failed – email already registered: {}", request.email());
      throw new DuplicateResourceException("Email already registered");
    }

    Parent parent = Parent.builder()
        .email(request.email())
        .firstName(request.firstName())
        .lastName(request.lastName())
        .passwordHash(passwordEncoder.encode(request.password()))
        .build();

    Parent savedParent = parentRepository.save(parent);
    log.info("Parent registered successfully. ParentId={}", savedParent.getId());

    String token = jwtUtil.generateToken(savedParent.getId(), savedParent.getEmail(), "parent");

    return new AuthResponse(
        savedParent.getId(),
        savedParent.getEmail(),
        savedParent.getFirstName(),
        savedParent.getLastName(),
        "parent",
        savedParent.getCreatedAt(),
        token,
        "Bearer"
    );
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    log.info("Login attempt. Email={}", request.email());

    Parent parent = parentRepository.findByEmail(request.email())
        .orElseThrow(() -> {
          log.warn("Login failed – email not found: {}", request.email());
          return new InvalidCredentialsException("Invalid email or password");
        });

    if (!passwordEncoder.matches(request.password(), parent.getPasswordHash())) {
      log.warn("Login failed – wrong password. Email={}", request.email());
      throw new InvalidCredentialsException("Invalid email or password");
    }

    log.info("Login successful. ParentId={}", parent.getId());

    String token = jwtUtil.generateToken(parent.getId(), parent.getEmail(), "parent");

    return new AuthResponse(
        parent.getId(),
        parent.getEmail(),
        parent.getFirstName(),
        parent.getLastName(),
        "parent",
        parent.getCreatedAt(),
        token,
        "Bearer"
    );
  }

  @Transactional(readOnly = true)
  public ParentResponse getParentProfile(java.util.UUID parentId) {
    Parent parent = parentRepository.findById(parentId)
        .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

    return new ParentResponse(
        parent.getId(),
        parent.getEmail(),
        parent.getFirstName(),
        parent.getLastName(),
        parent.getCreatedAt()
    );
  }

  /**
   * Logout a parent by blacklisting their token
   */
  public void logout(java.util.UUID parentId, String token) {
    log.info("Logout attempt. ParentId={}", parentId);

    if (token == null || token.isEmpty()) {
      throw new IllegalArgumentException("Token is required for logout");
    }

    java.util.Date expirationDate = jwtUtil.getExpirationDate(token);
    java.time.Instant expirationTime = expirationDate.toInstant();

    tokenBlacklistService.blacklistToken(token, expirationTime, parentId, "User logout");
    log.info("Parent logged out successfully. ParentId={}", parentId);
  }
}
