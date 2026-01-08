package org.example.server.service;

import org.example.server.dto.AuthResponse;
import org.example.server.dto.LoginRequest;
import org.example.server.dto.ParentResponse;
import org.example.server.dto.RegisterParentRequest;
import org.example.server.exception.DuplicateResourceException;
import org.example.server.exception.InvalidCredentialsException;
import org.example.server.exception.ResourceNotFoundException;
import org.example.server.model.AuthProvider;
import org.example.server.model.Parent;
import org.example.server.repository.ParentRepository;
import org.example.server.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ParentService.
 *
 * Covers:
 * - Parent registration
 * - Parent login
 * - Profile retrieval
 * - Logout functionality
 */
@DisplayName("ParentService - Unit Tests")
class ParentServiceTest {

  @Mock
  private ParentRepository parentRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private TokenBlacklistService tokenBlacklistService;

  @InjectMocks
  private ParentService parentService;

  private Parent parent;
  private UUID parentId;
  private String email;
  private String password;
  private String token;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    parentId = UUID.randomUUID();
    email = "parent@example.com";
    password = "Password123";
    token = "jwt.token.here";

    parent = Parent.builder()
        .id(parentId)
        .email(email)
        .firstName("John")
        .lastName("Doe")
        .passwordHash("hashedPassword")
        .build();
  }

  // ---------- REGISTER PARENT TESTS ----------

  @Test
  @DisplayName("registerParent() - Should register parent successfully")
  void registerParent_ValidRequest_RegistersSuccessfully() {
    // Arrange
    RegisterParentRequest request = new RegisterParentRequest(
        email, "John", "Doe", password);

    when(parentRepository.existsByEmail(email)).thenReturn(false);
    when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
    when(parentRepository.save(any(Parent.class))).thenReturn(parent);
    when(jwtUtil.generateToken(any(), any(), any())).thenReturn(token);
    when(jwtUtil.getExpirationDate(token)).thenReturn(new Date(System.currentTimeMillis() + 86400000));

    AuthResponse response = parentService.registerParent(request);

    assertNotNull(response);
    assertEquals(parentId, response.id());
    assertEquals(email, response.email());
    assertEquals("John", response.firstName());
    assertEquals("Doe", response.lastName());
    assertEquals("parent", response.role());
    assertNotNull(response.token());

    verify(parentRepository).existsByEmail(email);
    verify(passwordEncoder).encode(password);
    verify(parentRepository).save(any(Parent.class));
    verify(jwtUtil).generateToken(parentId, email, "parent");
  }

  @Test
  @DisplayName("registerParent() - Should throw exception for duplicate email")
  void registerParent_DuplicateEmail_ThrowsException() {
    RegisterParentRequest request = new RegisterParentRequest(
        email, "John", "Doe", password);

    when(parentRepository.existsByEmail(email)).thenReturn(true);

    assertThrows(DuplicateResourceException.class,
        () -> parentService.registerParent(request));

    verify(parentRepository).existsByEmail(email);
    verify(parentRepository, never()).save(any(Parent.class));
    verify(jwtUtil, never()).generateToken(any(), any(), any());
  }

  @Test
  @DisplayName("registerParent() - Should hash password before saving")
  void registerParent_ValidRequest_HashesPassword() {
    RegisterParentRequest request = new RegisterParentRequest(
        email, "John", "Doe", password);

    when(parentRepository.existsByEmail(email)).thenReturn(false);
    when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
    when(parentRepository.save(any(Parent.class))).thenReturn(parent);
    when(jwtUtil.generateToken(any(), any(), any())).thenReturn(token);
    when(jwtUtil.getExpirationDate(token)).thenReturn(new Date());

    parentService.registerParent(request);

    verify(passwordEncoder).encode(password);
  }

  // ---------- LOGIN TESTS ----------

  @Test
  @DisplayName("login() - Should login successfully with correct credentials")
  void login_CorrectCredentials_LoginSuccessfully() {
    // Arrange
    LoginRequest request = new LoginRequest(email, password);

    when(parentRepository.findByEmail(email)).thenReturn(Optional.of(parent));
    when(passwordEncoder.matches(password, parent.getPasswordHash())).thenReturn(true);
    when(jwtUtil.generateToken(any(), any(), any())).thenReturn(token);
    when(jwtUtil.getExpirationDate(token)).thenReturn(new Date());

    AuthResponse response = parentService.login(request);

    assertNotNull(response);
    assertEquals(parentId, response.id());
    assertEquals(email, response.email());
    assertEquals("parent", response.role());
    assertNotNull(response.token());

    verify(parentRepository).findByEmail(email);
    verify(passwordEncoder).matches(password, parent.getPasswordHash());
    verify(jwtUtil).generateToken(parentId, email, "parent");
  }

  @Test
  @DisplayName("login() - Should throw exception for non-existent email")
  void login_NonExistentEmail_ThrowsException() {
    LoginRequest request = new LoginRequest("nonexistent@example.com", password);

    when(parentRepository.findByEmail("nonexistent@example.com"))
        .thenReturn(Optional.empty());

    assertThrows(InvalidCredentialsException.class,
        () -> parentService.login(request));

    verify(parentRepository).findByEmail("nonexistent@example.com");
    verify(passwordEncoder, never()).matches(any(), any());
    verify(jwtUtil, never()).generateToken(any(), any(), any());
  }

  @Test
  @DisplayName("login() - Should throw exception for wrong password")
  void login_WrongPassword_ThrowsException() {
    LoginRequest request = new LoginRequest(email, "WrongPassword");

    when(parentRepository.findByEmail(email)).thenReturn(Optional.of(parent));
    when(passwordEncoder.matches("WrongPassword", parent.getPasswordHash()))
        .thenReturn(false);

    assertThrows(InvalidCredentialsException.class,
        () -> parentService.login(request));

    verify(parentRepository).findByEmail(email);
    verify(passwordEncoder).matches("WrongPassword", parent.getPasswordHash());
    verify(jwtUtil, never()).generateToken(any(), any(), any());
  }

  @Test
  @DisplayName("login() - Should throw exception for OAuth account")
  void login_OAuthAccount_ThrowsException() {
    Parent oauthParent = Parent.builder()
        .id(parentId)
        .email(email)
        .firstName("John")
        .lastName("Doe")
        .googleId("google123")
        .authProvider(AuthProvider.GOOGLE)
        .build();

    LoginRequest request = new LoginRequest(email, password);

    when(parentRepository.findByEmail(email)).thenReturn(Optional.of(oauthParent));

    InvalidCredentialsException exception = assertThrows(
        InvalidCredentialsException.class,
        () -> parentService.login(request));

    assertTrue(exception.getMessage().contains("Google Sign-In"));

    verify(parentRepository).findByEmail(email);
    verify(passwordEncoder, never()).matches(any(), any());
    verify(jwtUtil, never()).generateToken(any(), any(), any());
  }

  @Test
  @DisplayName("login() - Should return admin role for admin user")
  void login_AdminUser_ReturnsAdminRole() {
    Parent adminParent = Parent.builder()
        .id(parentId)
        .email(email)
        .firstName("Admin")
        .lastName("User")
        .passwordHash("hashedPassword")
        .isAdmin(true)
        .build();

    LoginRequest request = new LoginRequest(email, password);

    when(parentRepository.findByEmail(email)).thenReturn(Optional.of(adminParent));
    when(passwordEncoder.matches(password, adminParent.getPasswordHash())).thenReturn(true);
    when(jwtUtil.generateToken(any(), any(), eq("admin"))).thenReturn(token);
    when(jwtUtil.getExpirationDate(token)).thenReturn(new Date());

    AuthResponse response = parentService.login(request);

    assertEquals("admin", response.role());
    verify(jwtUtil).generateToken(parentId, email, "admin");
  }

  // ---------- GET PARENT PROFILE TESTS ----------

  @Test
  @DisplayName("getParentProfile() - Should return profile response")
  void getParentProfile_ValidId_ReturnsProfile() {
    when(parentRepository.findById(parentId)).thenReturn(Optional.of(parent));

    ParentResponse profile = parentService.getParentProfile(parentId);

    assertNotNull(profile);
    assertEquals(parent.getId(), profile.id());
    assertEquals(parent.getEmail(), profile.email());
    assertEquals(parent.getFirstName(), profile.firstName());
    assertEquals(parent.getLastName(), profile.lastName());

    verify(parentRepository).findById(parentId);
  }

  @Test
  @DisplayName("getParentProfile() - Should throw exception when parent not found")
  void getParentProfile_NonExistentId_ThrowsException() {
    when(parentRepository.findById(parentId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> parentService.getParentProfile(parentId));

    verify(parentRepository).findById(parentId);
  }

  // ---------- LOGOUT TESTS ----------

  @Test
  @DisplayName("logout() - Should blacklist token successfully")
  void logout_ValidToken_BlacklistsToken() {
    Date expirationDate = new Date(System.currentTimeMillis() + 86400000);
    when(jwtUtil.getExpirationDate(token)).thenReturn(expirationDate);
    doNothing().when(tokenBlacklistService).blacklistToken(any(), any(), any(), any());

    parentService.logout(parentId, token);

    verify(jwtUtil).getExpirationDate(token);
    verify(tokenBlacklistService).blacklistToken(
        eq(token),
        any(Instant.class),
        eq(parentId),
        eq("User logout"));
  }

  @Test
  @DisplayName("logout() - Should throw exception for null token")
  void logout_NullToken_ThrowsException() {
    assertThrows(IllegalArgumentException.class,
        () -> parentService.logout(parentId, null));

    verify(jwtUtil, never()).getExpirationDate(any());
    verify(tokenBlacklistService, never()).blacklistToken(any(), any(), any(), any());
  }

  @Test
  @DisplayName("logout() - Should throw exception for empty token")
  void logout_EmptyToken_ThrowsException() {
    assertThrows(IllegalArgumentException.class,
        () -> parentService.logout(parentId, ""));

    verify(jwtUtil, never()).getExpirationDate(any());
    verify(tokenBlacklistService, never()).blacklistToken(any(), any(), any(), any());
  }
}
