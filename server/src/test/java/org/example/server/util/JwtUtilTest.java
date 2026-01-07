package org.example.server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil.
 *
 * Covers:
 * - Token generation
 * - Token validation
 * - Claims extraction
 * - Expiration handling
 * - Error scenarios
 */
@DisplayName("JwtUtil - Unit Tests")
class JwtUtilTest {

  private JwtUtil jwtUtil;
  private UUID userId;
  private String email;
  private String role;
  private static final String TEST_SECRET = "testSecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmTesting";
  private static final Long TEST_EXPIRATION = 3600000L; // 1 hour

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();

    ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
    ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);

    userId = UUID.randomUUID();
    email = "test@example.com";
    role = "parent";
  }

  // ---------- GENERATE TOKEN TESTS ----------

  @Test
  @DisplayName("generateToken() - Should generate valid JWT token")
  void generateToken_ValidInput_GeneratesToken() {
    String token = jwtUtil.generateToken(userId, email, role);

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
  }

  @Test
  @DisplayName("generateToken() - Should include userId in subject")
  void generateToken_ValidInput_IncludesUserId() {
    String token = jwtUtil.generateToken(userId, email, role);
    UUID extractedUserId = jwtUtil.extractUserId(token);

    assertEquals(userId, extractedUserId);
  }

  @Test
  @DisplayName("generateToken() - Should include email in claims")
  void generateToken_ValidInput_IncludesEmail() {
    String token = jwtUtil.generateToken(userId, email, role);
    String extractedEmail = jwtUtil.extractEmail(token);

    assertEquals(email, extractedEmail);
  }

  @Test
  @DisplayName("generateToken() - Should include role in claims")
  void generateToken_ValidInput_IncludesRole() {
    String token = jwtUtil.generateToken(userId, email, role);
    String extractedRole = jwtUtil.extractRole(token);

    assertEquals(role, extractedRole);
  }

  @Test
  @DisplayName("generateToken() - Should set expiration time correctly")
  void generateToken_ValidInput_SetsExpirationCorrectly() {
    long beforeGeneration = System.currentTimeMillis();

    String token = jwtUtil.generateToken(userId, email, role);
    Date expirationDate = jwtUtil.getExpirationDate(token);

    long expectedExpiration = beforeGeneration + TEST_EXPIRATION;
    assertTrue(expirationDate.getTime() >= expectedExpiration - 1000); // Allow 1s tolerance
    assertTrue(expirationDate.getTime() <= expectedExpiration + 1000);
  }

  @Test
  @DisplayName("generateToken() - Should generate different tokens for different users")
  void generateToken_DifferentUsers_GeneratesDifferentTokens() {
    UUID userId2 = UUID.randomUUID();

    String token1 = jwtUtil.generateToken(userId, email, role);
    String token2 = jwtUtil.generateToken(userId2, email, role);

    assertNotEquals(token1, token2);
  }

  // ---------- EXTRACT CLAIMS TESTS ----------

  @Test
  @DisplayName("extractClaims() - Should extract claims from valid token")
  void extractClaims_ValidToken_ExtractsClaims() {
    String token = jwtUtil.generateToken(userId, email, role);

    Claims claims = jwtUtil.extractClaims(token);

    assertNotNull(claims);
    assertEquals(userId.toString(), claims.getSubject());
    assertEquals(email, claims.get("email", String.class));
    assertEquals(role, claims.get("role", String.class));
  }

  @Test
  @DisplayName("extractClaims() - Should throw exception for invalid token")
  void extractClaims_InvalidToken_ThrowsException() {
    String invalidToken = "invalid.jwt.token";

    assertThrows(MalformedJwtException.class, () -> jwtUtil.extractClaims(invalidToken));
  }

  @Test
  @DisplayName("extractClaims() - Should throw exception for tampered token")
  void extractClaims_TamperedToken_ThrowsException() {
    String token = jwtUtil.generateToken(userId, email, role);
    String tamperedToken = token.substring(0, token.length() - 5) + "AAAAA";

    assertThrows(SignatureException.class, () -> jwtUtil.extractClaims(tamperedToken));
  }

  // ---------- EXTRACT USER ID TESTS ----------

  @Test
  @DisplayName("extractUserId() - Should extract correct user ID")
  void extractUserId_ValidToken_ExtractsCorrectId() {
    String token = jwtUtil.generateToken(userId, email, role);

    UUID extractedId = jwtUtil.extractUserId(token);

    assertEquals(userId, extractedId);
  }

  @Test
  @DisplayName("extractUserId() - Should handle different UUID formats")
  void extractUserId_DifferentUUIDs_ExtractsCorrectly() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    String token1 = jwtUtil.generateToken(uuid1, email, role);
    String token2 = jwtUtil.generateToken(uuid2, email, role);

    assertEquals(uuid1, jwtUtil.extractUserId(token1));
    assertEquals(uuid2, jwtUtil.extractUserId(token2));
  }

  // ---------- EXTRACT EMAIL TESTS ----------

  @Test
  @DisplayName("extractEmail() - Should extract correct email")
  void extractEmail_ValidToken_ExtractsCorrectEmail() {
    String token = jwtUtil.generateToken(userId, email, role);

    String extractedEmail = jwtUtil.extractEmail(token);

    assertEquals(email, extractedEmail);
  }

  @Test
  @DisplayName("extractEmail() - Should handle different email formats")
  void extractEmail_DifferentEmails_ExtractsCorrectly() {
    String email1 = "user@example.com";
    String email2 = "child@test.org";

    String token1 = jwtUtil.generateToken(userId, email1, role);
    String token2 = jwtUtil.generateToken(userId, email2, role);

    assertEquals(email1, jwtUtil.extractEmail(token1));
    assertEquals(email2, jwtUtil.extractEmail(token2));
  }

  // ---------- EXTRACT ROLE TESTS ----------

  @Test
  @DisplayName("extractRole() - Should extract correct role")
  void extractRole_ValidToken_ExtractsCorrectRole() {
    String token = jwtUtil.generateToken(userId, email, role);

    String extractedRole = jwtUtil.extractRole(token);

    assertEquals(role, extractedRole);
  }

  @Test
  @DisplayName("extractRole() - Should handle different roles")
  void extractRole_DifferentRoles_ExtractsCorrectly() {
    String parentToken = jwtUtil.generateToken(userId, email, "parent");
    String childToken = jwtUtil.generateToken(userId, email, "child");

    assertEquals("parent", jwtUtil.extractRole(parentToken));
    assertEquals("child", jwtUtil.extractRole(childToken));
  }

  // ---------- VALIDATE TOKEN TESTS ----------

  @Test
  @DisplayName("validateToken() - Should return true for valid token")
  void validateToken_ValidToken_ReturnsTrue() {
    String token = jwtUtil.generateToken(userId, email, role);

    boolean isValid = jwtUtil.validateToken(token);

    assertTrue(isValid);
  }

  @Test
  @DisplayName("validateToken() - Should return false for invalid token")
  void validateToken_InvalidToken_ReturnsFalse() {
    String invalidToken = "invalid.jwt.token";

    boolean isValid = jwtUtil.validateToken(invalidToken);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("validateToken() - Should return false for tampered token")
  void validateToken_TamperedToken_ReturnsFalse() {
    String token = jwtUtil.generateToken(userId, email, role);
    String tamperedToken = token.substring(0, token.length() - 5) + "AAAAA";

    boolean isValid = jwtUtil.validateToken(tamperedToken);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("validateToken() - Should return false for expired token")
  void validateToken_ExpiredToken_ReturnsFalse() {
    ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1 millisecond
    String token = jwtUtil.generateToken(userId, email, role);

    // Wait for token to expire
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    boolean isValid = jwtUtil.validateToken(token);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("validateToken() - Should return false for null token")
  void validateToken_NullToken_ReturnsFalse() {
    boolean isValid = jwtUtil.validateToken(null);

    assertFalse(isValid);
  }

  @Test
  @DisplayName("validateToken() - Should return false for empty token")
  void validateToken_EmptyToken_ReturnsFalse() {
    boolean isValid = jwtUtil.validateToken("");

    assertFalse(isValid);
  }

  // ---------- GET EXPIRATION DATE TESTS ----------

  @Test
  @DisplayName("getExpirationDate() - Should return correct expiration date")
  void getExpirationDate_ValidToken_ReturnsCorrectDate() {
    long beforeGeneration = System.currentTimeMillis();
    String token = jwtUtil.generateToken(userId, email, role);

    Date expirationDate = jwtUtil.getExpirationDate(token);

    assertNotNull(expirationDate);
    long expectedExpiration = beforeGeneration + TEST_EXPIRATION;
    assertTrue(expirationDate.getTime() >= expectedExpiration - 1000);
    assertTrue(expirationDate.getTime() <= expectedExpiration + 1000);
  }

  @Test
  @DisplayName("getExpirationDate() - Should be in the future for new tokens")
  void getExpirationDate_NewToken_IsInFuture() {
    String token = jwtUtil.generateToken(userId, email, role);

    Date expirationDate = jwtUtil.getExpirationDate(token);

    assertTrue(expirationDate.after(new Date()));
  }

  // ---------- INTEGRATION TESTS ----------

  @Test
  @DisplayName("Integration - Complete token lifecycle")
  void integration_CompleteTokenLifecycle_WorksCorrectly() {
    String token = jwtUtil.generateToken(userId, email, role);

    assertTrue(jwtUtil.validateToken(token));

    assertEquals(userId, jwtUtil.extractUserId(token));
    assertEquals(email, jwtUtil.extractEmail(token));
    assertEquals(role, jwtUtil.extractRole(token));

    Date expirationDate = jwtUtil.getExpirationDate(token);
    assertTrue(expirationDate.after(new Date()));
  }

  @Test
  @DisplayName("Integration - Multiple tokens for same user")
  void integration_MultipleTokens_EachValid() {
    // Generate multiple tokens
    String token1 = jwtUtil.generateToken(userId, email, "parent");
    String token2 = jwtUtil.generateToken(userId, email, "child");

    // Both should be valid
    assertTrue(jwtUtil.validateToken(token1));
    assertTrue(jwtUtil.validateToken(token2));

    // But have different roles
    assertEquals("parent", jwtUtil.extractRole(token1));
    assertEquals("child", jwtUtil.extractRole(token2));
  }

  @Test
  @DisplayName("Integration - Token with different secret should fail validation")
  void integration_DifferentSecret_FailsValidation() {
    // Generate token with current secret
    String token = jwtUtil.generateToken(userId, email, role);
    assertTrue(jwtUtil.validateToken(token));

    // Change secret
    ReflectionTestUtils.setField(jwtUtil, "secret", "differentSecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmTest");

    // Token should no longer be valid
    assertFalse(jwtUtil.validateToken(token));
  }

  @Test
  @DisplayName("Integration - Token parsing should handle all claim types")
  void integration_AllClaimTypes_ParsedCorrectly() {
    UUID testUserId = UUID.randomUUID();
    String testEmail = "integration@test.com";
    String testRole = "admin";

    String token = jwtUtil.generateToken(testUserId, testEmail, testRole);
    Claims claims = jwtUtil.extractClaims(token);

    assertNotNull(claims);
    assertEquals(testUserId.toString(), claims.getSubject());
    assertEquals(testEmail, claims.get("email", String.class));
    assertEquals(testRole, claims.get("role", String.class));
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
  }
}
