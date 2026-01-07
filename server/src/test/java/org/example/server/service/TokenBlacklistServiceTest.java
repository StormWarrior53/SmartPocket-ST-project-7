package org.example.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TokenBlacklistService.
 *
 * Covers:
 * - Token blacklisting and checking
 * - User-level token blacklisting
 * - Automatic cleanup of expired tokens
 * - Statistics and edge cases
 */
@DisplayName("TokenBlacklistService - Unit Tests")
class TokenBlacklistServiceTest {

  private TokenBlacklistService tokenBlacklistService;
  private UUID userId;
  private String token;
  private Instant futureExpiration;
  private Instant pastExpiration;

  @BeforeEach
  void setUp() {
    tokenBlacklistService = new TokenBlacklistService();
    userId = UUID.randomUUID();
    token = "test.jwt.token";
    futureExpiration = Instant.now().plus(1, ChronoUnit.HOURS);
    pastExpiration = Instant.now().minus(1, ChronoUnit.HOURS);
  }

  // ---------- BLACKLIST TOKEN TESTS ----------

  @Test
  @DisplayName("blacklistToken() - Should blacklist token successfully")
  void blacklistToken_ValidToken_BlacklistsSuccessfully() {
    tokenBlacklistService.blacklistToken(token, futureExpiration, userId, "User logout");

    assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
  }

  @Test
  @DisplayName("blacklistToken() - Should store token with correct expiration")
  void blacklistToken_ValidToken_StoresCorrectExpiration() {
    tokenBlacklistService.blacklistToken(token, futureExpiration, userId, "User logout");

    assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
  }

  @Test
  @DisplayName("blacklistToken() - Should handle multiple tokens for same user")
  void blacklistToken_MultipleTokens_AllBlacklisted() {
    String token1 = "token1.jwt";
    String token2 = "token2.jwt";

    tokenBlacklistService.blacklistToken(token1, futureExpiration, userId, "Logout");
    tokenBlacklistService.blacklistToken(token2, futureExpiration, userId, "Password change");

    assertTrue(tokenBlacklistService.isTokenBlacklisted(token1));
    assertTrue(tokenBlacklistService.isTokenBlacklisted(token2));
  }

  @Test
  @DisplayName("blacklistToken() - Should trigger cleanup of expired tokens")
  void blacklistToken_WithExpiredTokens_CleansUpExpired() {
    String expiredToken = "expired.token";
    tokenBlacklistService.blacklistToken(expiredToken, pastExpiration, userId, "Test");

    tokenBlacklistService.blacklistToken(token, futureExpiration, userId, "Logout");

    assertFalse(tokenBlacklistService.isTokenBlacklisted(expiredToken));
    assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
  }

  // ---------- IS TOKEN BLACKLISTED TESTS ----------

  @Test
  @DisplayName("isTokenBlacklisted() - Should return false for non-blacklisted token")
  void isTokenBlacklisted_NonBlacklistedToken_ReturnsFalse() {
    boolean result = tokenBlacklistService.isTokenBlacklisted("non.existent.token");

    assertFalse(result);
  }

  @Test
  @DisplayName("isTokenBlacklisted() - Should return false for null token")
  void isTokenBlacklisted_NullToken_ReturnsFalse() {
    boolean result = tokenBlacklistService.isTokenBlacklisted(null);

    assertFalse(result);
  }

  @Test
  @DisplayName("isTokenBlacklisted() - Should return true for blacklisted token")
  void isTokenBlacklisted_BlacklistedToken_ReturnsTrue() {
    tokenBlacklistService.blacklistToken(token, futureExpiration, userId, "Logout");

    boolean result = tokenBlacklistService.isTokenBlacklisted(token);

    assertTrue(result);
  }

  @Test
  @DisplayName("isTokenBlacklisted() - Should return false for expired blacklisted token")
  void isTokenBlacklisted_ExpiredBlacklistedToken_ReturnsFalse() {
    tokenBlacklistService.blacklistToken(token, pastExpiration, userId, "Logout");

    boolean result = tokenBlacklistService.isTokenBlacklisted(token);

    assertFalse(result);
  }

  @Test
  @DisplayName("isTokenBlacklisted() - Should remove expired token from blacklist")
  void isTokenBlacklisted_ExpiredToken_RemovesFromBlacklist() {
    tokenBlacklistService.blacklistToken(token, pastExpiration, userId, "Logout");

    tokenBlacklistService.isTokenBlacklisted(token);
    boolean secondCheck = tokenBlacklistService.isTokenBlacklisted(token);

    assertFalse(secondCheck);
  }

  // ---------- BLACKLIST ALL USER TOKENS TESTS ----------

  @Test
  @DisplayName("blacklistAllUserTokens() - Should blacklist all tokens for user")
  void blacklistAllUserTokens_ValidUser_BlacklistsAllTokens() {
    tokenBlacklistService.blacklistAllUserTokens(userId, futureExpiration, "Pattern reset");

    assertTrue(tokenBlacklistService.areAllUserTokensBlacklisted(userId));
  }

  @Test
  @DisplayName("blacklistAllUserTokens() - Should not affect other users")
  void blacklistAllUserTokens_OneUser_DoesNotAffectOthers() {
    UUID otherUserId = UUID.randomUUID();

    tokenBlacklistService.blacklistAllUserTokens(userId, futureExpiration, "Pattern reset");

    assertTrue(tokenBlacklistService.areAllUserTokensBlacklisted(userId));
    assertFalse(tokenBlacklistService.areAllUserTokensBlacklisted(otherUserId));
  }

  @Test
  @DisplayName("blacklistAllUserTokens() - Should respect expiration time")
  void blacklistAllUserTokens_WithExpiration_RespectsExpiration() {
    tokenBlacklistService.blacklistAllUserTokens(userId, pastExpiration, "Pattern reset");

    boolean result = tokenBlacklistService.areAllUserTokensBlacklisted(userId);

    assertFalse(result);
  }

  // ---------- ARE ALL USER TOKENS BLACKLISTED TESTS ----------

  @Test
  @DisplayName("areAllUserTokensBlacklisted() - Should return false for non-blacklisted user")
  void areAllUserTokensBlacklisted_NonBlacklistedUser_ReturnsFalse() {
    boolean result = tokenBlacklistService.areAllUserTokensBlacklisted(userId);

    assertFalse(result);
  }

  @Test
  @DisplayName("areAllUserTokensBlacklisted() - Should return false for null userId")
  void areAllUserTokensBlacklisted_NullUserId_ReturnsFalse() {
    boolean result = tokenBlacklistService.areAllUserTokensBlacklisted(null);

    assertFalse(result);
  }

  @Test
  @DisplayName("areAllUserTokensBlacklisted() - Should return true for blacklisted user")
  void areAllUserTokensBlacklisted_BlacklistedUser_ReturnsTrue() {
    tokenBlacklistService.blacklistAllUserTokens(userId, futureExpiration, "Pattern reset");

    boolean result = tokenBlacklistService.areAllUserTokensBlacklisted(userId);

    assertTrue(result);
  }

  @Test
  @DisplayName("areAllUserTokensBlacklisted() - Should remove expired user blacklist")
  void areAllUserTokensBlacklisted_ExpiredBlacklist_RemovesFromBlacklist() {
    tokenBlacklistService.blacklistAllUserTokens(userId, pastExpiration, "Pattern reset");

    boolean firstCheck = tokenBlacklistService.areAllUserTokensBlacklisted(userId);
    boolean secondCheck = tokenBlacklistService.areAllUserTokensBlacklisted(userId);

    assertFalse(firstCheck);
    assertFalse(secondCheck);
  }

  // ---------- STATISTICS TESTS ----------

  @Test
  @DisplayName("getStatistics() - Should return zero count for empty blacklist")
  void getStatistics_EmptyBlacklist_ReturnsZeroCount() {
    Map<String, Object> stats = tokenBlacklistService.getStatistics();

    assertEquals(0, stats.get("blacklistedTokensCount"));
  }

  @Test
  @DisplayName("getStatistics() - Should return correct count for blacklisted tokens")
  void getStatistics_WithBlacklistedTokens_ReturnsCorrectCount() {
    tokenBlacklistService.blacklistToken("token1", futureExpiration, userId, "Logout");
    tokenBlacklistService.blacklistToken("token2", futureExpiration, userId, "Logout");
    tokenBlacklistService.blacklistAllUserTokens(userId, futureExpiration, "Pattern reset");

    Map<String, Object> stats = tokenBlacklistService.getStatistics();

    assertEquals(3, stats.get("blacklistedTokensCount"));
  }

  @Test
  @DisplayName("getStatistics() - Should clean up expired tokens before counting")
  void getStatistics_WithExpiredTokens_CleansUpBeforeCounting() {
    tokenBlacklistService.blacklistToken("expired1", pastExpiration, userId, "Logout");
    tokenBlacklistService.blacklistToken("expired2", pastExpiration, userId, "Logout");
    tokenBlacklistService.blacklistToken("valid", futureExpiration, userId, "Logout");

    Map<String, Object> stats = tokenBlacklistService.getStatistics();

    assertEquals(1, stats.get("blacklistedTokensCount"));
  }

  @Test
  @DisplayName("getStatistics() - Should include oldest entry timestamp")
  void getStatistics_WithTokens_IncludesOldestEntry() {
    tokenBlacklistService.blacklistToken(token, futureExpiration, userId, "Logout");

    Map<String, Object> stats = tokenBlacklistService.getStatistics();

    assertNotNull(stats.get("oldestEntry"));
    assertTrue(stats.get("oldestEntry") instanceof Instant);
  }

  // ---------- INTEGRATION TESTS ----------

  @Test
  @DisplayName("Integration - Individual token blacklist does not affect user-level check")
  void integration_IndividualToken_DoesNotAffectUserLevel() {
    tokenBlacklistService.blacklistToken(token, futureExpiration, userId, "Logout");

    assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
    assertFalse(tokenBlacklistService.areAllUserTokensBlacklisted(userId));
  }

  @Test
  @DisplayName("Integration - User-level blacklist affects all tokens")
  void integration_UserLevelBlacklist_AffectsAllTokens() {
    String token1 = "token1.jwt";
    String token2 = "token2.jwt";
    tokenBlacklistService.blacklistToken(token1, futureExpiration, userId, "Logout");

    tokenBlacklistService.blacklistAllUserTokens(userId, futureExpiration, "Pattern reset");

    assertTrue(tokenBlacklistService.isTokenBlacklisted(token1));
    assertTrue(tokenBlacklistService.areAllUserTokensBlacklisted(userId));
    // New token for same user would also be blocked by user-level blacklist
  }

  @Test
  @DisplayName("Integration - Multiple operations with cleanup")
  void integration_MultipleOperations_HandlesCleanupCorrectly() {
    tokenBlacklistService.blacklistToken("expired1", pastExpiration, userId, "Logout");
    tokenBlacklistService.blacklistToken("valid1", futureExpiration, userId, "Logout");
    tokenBlacklistService.blacklistAllUserTokens(UUID.randomUUID(), pastExpiration, "Reset");
    tokenBlacklistService.blacklistAllUserTokens(userId, futureExpiration, "Reset");

    Map<String, Object> stats = tokenBlacklistService.getStatistics();

    assertEquals(2, stats.get("blacklistedTokensCount"));
    assertTrue(tokenBlacklistService.isTokenBlacklisted("valid1"));
    assertTrue(tokenBlacklistService.areAllUserTokensBlacklisted(userId));
  }

  @Test
  @DisplayName("Integration - Same token can be re-blacklisted")
  void integration_SameToken_CanBeReBlacklisted() {
    tokenBlacklistService.blacklistToken(token, pastExpiration, userId, "First blacklist");
    assertFalse(tokenBlacklistService.isTokenBlacklisted(token)); // Expired, removed

    tokenBlacklistService.blacklistToken(token, futureExpiration, userId, "Second blacklist");

    assertTrue(tokenBlacklistService.isTokenBlacklisted(token));
  }
}
