package org.example.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage blacklisted (revoked) JWT tokens.
 * Uses in-memory storage with automatic cleanup of expired tokens.
 */
@Slf4j
@Service
public class TokenBlacklistService {

  private final Map<String, TokenBlacklistEntry> blacklistedTokens = new ConcurrentHashMap<>();

  /**
   * Add a token to the blacklist
   * 
   * @param token          The JWT token to blacklist
   * @param expirationTime When the token naturally expires
   * @param userId         The user ID associated with the token
   * @param reason         The reason for blacklisting (logout, pattern reset,
   *                       etc.)
   */
  public void blacklistToken(String token, Instant expirationTime, UUID userId, String reason) {
    TokenBlacklistEntry entry = new TokenBlacklistEntry(
        token,
        expirationTime,
        userId,
        reason,
        Instant.now());

    blacklistedTokens.put(token, entry);
    log.info("Token blacklisted for user {}: {} (expires at {})", userId, reason, expirationTime);

    cleanupExpiredTokens();
  }

  /**
   * Check if a token is blacklisted
   * 
   * @param token The JWT token to check
   * @return true if the token is blacklisted, false otherwise
   */
  public boolean isTokenBlacklisted(String token) {
    if (token == null) {
      return false;
    }

    TokenBlacklistEntry entry = blacklistedTokens.get(token);

    if (entry == null) {
      return false;
    }

    // If token has expired naturally, remove it from blacklist
    if (entry.expirationTime.isBefore(Instant.now())) {
      blacklistedTokens.remove(token);
      return false;
    }

    return true;
  }

  /**
   * Blacklist all tokens for a specific user
   * Useful when a child's pattern is reset
   * 
   * @param userId         The user ID to blacklist all tokens for
   * @param expirationTime The expiration time for the blacklist
   * @param reason         The reason for blacklisting
   */
  public void blacklistAllUserTokens(UUID userId, Instant expirationTime, String reason) {
    String userMarker = "USER_ALL_TOKENS_" + userId.toString();
    TokenBlacklistEntry entry = new TokenBlacklistEntry(
        userMarker,
        expirationTime,
        userId,
        reason,
        Instant.now());

    blacklistedTokens.put(userMarker, entry);
    log.warn("All tokens blacklisted for user {}: {}", userId, reason);
  }

  /**
   * Check if all tokens for a user are blacklisted
   * 
   * @param userId The user ID to check
   * @return true if all user tokens are blacklisted
   */
  public boolean areAllUserTokensBlacklisted(UUID userId) {
    if (userId == null) {
      return false;
    }

    String userMarker = "USER_ALL_TOKENS_" + userId.toString();
    TokenBlacklistEntry entry = blacklistedTokens.get(userMarker);

    if (entry == null) {
      return false;
    }

    if (entry.expirationTime.isBefore(Instant.now())) {
      blacklistedTokens.remove(userMarker);
      return false;
    }

    return true;
  }

  /**
   * Remove expired tokens from the blacklist to prevent memory leaks
   */
  private void cleanupExpiredTokens() {
    Instant now = Instant.now();
    blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().expirationTime.isBefore(now));
  }

  /**
   * Get statistics about the blacklist
   * 
   * @return Map with blacklist statistics
   */
  public Map<String, Object> getStatistics() {
    cleanupExpiredTokens();
    return Map.of(
        "blacklistedTokensCount", blacklistedTokens.size(),
        "oldestEntry", blacklistedTokens.values().stream()
            .map(e -> e.blacklistedAt)
            .min(Instant::compareTo)
            .orElse(Instant.now()));
  }

  /**
   * Internal class to store blacklist entry details
   */
  private record TokenBlacklistEntry(
      String token,
      Instant expirationTime,
      UUID userId,
      String reason,
      Instant blacklistedAt) {
  }
}
