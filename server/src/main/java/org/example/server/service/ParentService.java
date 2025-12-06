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
    log.info("Attempting to register parent. Email={}", request.getEmail());

    if (parentRepository.existsByEmail(request.getEmail())) {
      log.warn("Registration failed – email already registered: {}", request.getEmail());
      throw new DuplicateResourceException("Email already registered");
    }

    Parent parent = Parent.builder()
        .email(request.getEmail())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .passwordHash(passwordEncoder.encode(request.getPassword()))
        .build();

    Parent savedParent = parentRepository.save(parent);
    log.info("Parent registered successfully. ParentId={}", savedParent.getId());

    String token = jwtUtil.generateToken(savedParent.getId(), savedParent.getEmail(), "parent");

    return AuthResponse.builder()
        .id(savedParent.getId())
        .email(savedParent.getEmail())
        .firstName(savedParent.getFirstName())
        .lastName(savedParent.getLastName())
        .role("parent")
        .createdAt(savedParent.getCreatedAt())
        .token(token)
        .tokenType("Bearer")
        .build();
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    log.info("Login attempt. Email={}", request.getEmail());

    Parent parent = parentRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> {
          log.warn("Login failed – email not found: {}", request.getEmail());
          return new InvalidCredentialsException("Invalid email or password");
        });

    if (!passwordEncoder.matches(request.getPassword(), parent.getPasswordHash())) {
      log.warn("Login failed – wrong password. Email={}", request.getEmail());
      throw new InvalidCredentialsException("Invalid email or password");
    }

    log.info("Login successful. ParentId={}", parent.getId());

    String token = jwtUtil.generateToken(parent.getId(), parent.getEmail(), "parent");

    return AuthResponse.builder()
        .id(parent.getId())
        .email(parent.getEmail())
        .firstName(parent.getFirstName())
        .lastName(parent.getLastName())
        .role("parent")
        .createdAt(parent.getCreatedAt())
        .token(token)
        .tokenType("Bearer")
        .build();
  }

  @Transactional(readOnly = true)
  public ParentResponse getParentProfile(java.util.UUID parentId) {
    Parent parent = parentRepository.findById(parentId)
        .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));

    return ParentResponse.builder()
        .id(parent.getId())
        .email(parent.getEmail())
        .firstName(parent.getFirstName())
        .lastName(parent.getLastName())
        .createdAt(parent.getCreatedAt())
        .build();
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
