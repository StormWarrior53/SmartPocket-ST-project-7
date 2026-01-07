package org.example.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.AuthResponse;
import org.example.server.exception.InvalidCredentialsException;
import org.example.server.model.AuthProvider;
import org.example.server.model.Parent;
import org.example.server.repository.ParentRepository;
import org.example.server.util.JwtUtil;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

  private final ParentRepository parentRepository;
  private final JwtUtil jwtUtil;

  /**
   * Process Google OAuth login and return authentication response.
   * Handles three scenarios:
   * 1. Existing OAuth user - return JWT token
   * 2. Existing local user with matching email - link account and return JWT
   * 3. New user - create account and return JWT
   *
   * @param oAuth2User OAuth2 user information from Google
   * @return AuthResponse containing JWT token and user information
   * @throws InvalidCredentialsException if email is not verified
   */
  @Transactional
  public AuthResponse processOAuthLogin(OAuth2User oAuth2User) {
    String googleId = oAuth2User.getAttribute("sub");
    String email = oAuth2User.getAttribute("email");
    String firstName = oAuth2User.getAttribute("given_name");
    String lastName = oAuth2User.getAttribute("family_name");
    Boolean emailVerified = oAuth2User.getAttribute("email_verified");

    log.info("Processing OAuth login for email: {}, googleId: {}", email, googleId);

    if (emailVerified == null || !emailVerified) {
      log.warn("OAuth login rejected - email not verified: {}", email);
      throw new InvalidCredentialsException("Email not verified by Google");
    }

    Parent parent = parentRepository.findByGoogleId(googleId)
        .orElseGet(() -> {
          return parentRepository.findByEmail(email)
              .map(existingParent -> {
                log.info("Linking existing local account to Google OAuth: {}", email);
                existingParent.setGoogleId(googleId);
                existingParent.setAuthProvider(AuthProvider.GOOGLE);
                return parentRepository.save(existingParent);
              })
              .orElseGet(() -> {
                log.info("Creating new parent account via Google OAuth: {}", email);
                Parent newParent = Parent.builder()
                    .email(email)
                    .firstName(firstName != null ? firstName : "")
                    .lastName(lastName != null ? lastName : "")
                    .googleId(googleId)
                    .authProvider(AuthProvider.GOOGLE)
                    .passwordHash(null) // No password for OAuth users
                    .build();
                return parentRepository.save(newParent);
              });
        });

    log.info("OAuth login successful. ParentId={}, email={}", parent.getId(), parent.getEmail());

    // Generate JWT token (same as regular login)
    String role = parent.isAdmin() ? "admin" : "parent";
    String token = jwtUtil.generateToken(parent.getId(), parent.getEmail(), role);
    Date expirationDate = jwtUtil.getExpirationDate(token);
    Long expiresAt = expirationDate.getTime();

    return new AuthResponse(
        parent.getId(),
        parent.getEmail(),
        parent.getFirstName(),
        parent.getLastName(),
        role,
        parent.getCreatedAt(),
        token,
        expiresAt);
  }
}
