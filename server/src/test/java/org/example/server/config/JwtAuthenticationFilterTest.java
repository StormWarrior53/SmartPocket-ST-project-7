package org.example.server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.server.service.TokenBlacklistService;
import org.example.server.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 *
 * Covers:
 * - JWT token extraction and validation
 * - Token blacklist checking
 * - Security context setup
 * - Error handling
 */
@DisplayName("JwtAuthenticationFilter - Unit Tests")
class JwtAuthenticationFilterTest {

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private TokenBlacklistService tokenBlacklistService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private UUID userId;
  private String validToken;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    SecurityContextHolder.clearContext();

    userId = UUID.randomUUID();
    validToken = "valid.jwt.token";

    when(request.getRequestURI()).thenReturn("/api/test");
  }

  // ---------- VALID TOKEN TESTS ----------

  @Test
  @DisplayName("doFilterInternal() - Should authenticate with valid token")
  void doFilterInternal_ValidToken_AuthenticatesUser() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
    when(jwtUtil.validateToken(validToken)).thenReturn(true);
    when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
    when(jwtUtil.extractEmail(validToken)).thenReturn("user@example.com");
    when(jwtUtil.extractRole(validToken)).thenReturn("parent");
    when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
    when(tokenBlacklistService.areAllUserTokensBlacklisted(userId)).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertEquals(userId, authentication.getPrincipal());
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_PARENT")));

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() - Should set role authority correctly")
  void doFilterInternal_ValidToken_SetsCorrectRole() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
    when(jwtUtil.validateToken(validToken)).thenReturn(true);
    when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
    when(jwtUtil.extractEmail(validToken)).thenReturn("child@example.com");
    when(jwtUtil.extractRole(validToken)).thenReturn("child");
    when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
    when(tokenBlacklistService.areAllUserTokensBlacklisted(userId)).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertTrue(authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_CHILD")));
  }

  // ---------- BLACKLISTED TOKEN TESTS ----------

  @Test
  @DisplayName("doFilterInternal() - Should not authenticate blacklisted token")
  void doFilterInternal_BlacklistedToken_DoesNotAuthenticate() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
    when(jwtUtil.validateToken(validToken)).thenReturn(true);
    when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
    when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(true);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNull(authentication);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() - Should not authenticate when all user tokens blacklisted")
  void doFilterInternal_AllUserTokensBlacklisted_DoesNotAuthenticate() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
    when(jwtUtil.validateToken(validToken)).thenReturn(true);
    when(jwtUtil.extractUserId(validToken)).thenReturn(userId);
    when(tokenBlacklistService.isTokenBlacklisted(validToken)).thenReturn(false);
    when(tokenBlacklistService.areAllUserTokensBlacklisted(userId)).thenReturn(true);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNull(authentication);

    verify(filterChain).doFilter(request, response);
  }

  // ---------- INVALID TOKEN TESTS ----------

  @Test
  @DisplayName("doFilterInternal() - Should not authenticate invalid token")
  void doFilterInternal_InvalidToken_DoesNotAuthenticate() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
    when(jwtUtil.validateToken("invalid.token")).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNull(authentication);

    verify(filterChain).doFilter(request, response);
    verify(tokenBlacklistService, never()).isTokenBlacklisted(any());
  }

  @Test
  @DisplayName("doFilterInternal() - Should handle missing token gracefully")
  void doFilterInternal_NoToken_ContinuesFilterChain() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNull(authentication);

    verify(filterChain).doFilter(request, response);
    verify(jwtUtil, never()).validateToken(any());
  }

  @Test
  @DisplayName("doFilterInternal() - Should handle malformed Authorization header")
  void doFilterInternal_MalformedHeader_DoesNotAuthenticate() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNull(authentication);

    verify(filterChain).doFilter(request, response);
    verify(jwtUtil, never()).validateToken(any());
  }

  // ---------- ERROR HANDLING TESTS ----------

  @Test
  @DisplayName("doFilterInternal() - Should handle JWT validation exception gracefully")
  void doFilterInternal_JwtException_ContinuesFilterChain() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
    when(jwtUtil.validateToken(validToken)).thenThrow(new RuntimeException("JWT error"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNull(authentication);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() - Should always call filter chain even on error")
  void doFilterInternal_ExceptionThrown_StillCallsFilterChain() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
    when(jwtUtil.validateToken(validToken)).thenThrow(new RuntimeException("Error"));

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }
}
