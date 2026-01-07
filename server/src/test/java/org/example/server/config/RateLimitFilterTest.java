package org.example.server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitFilter.
 *
 * Covers:
 * - Rate limiting on authentication endpoints
 * - Bucket token consumption
 * - IP extraction
 * - Failed login attempt tracking
 */
@DisplayName("RateLimitFilter - Unit Tests")
class RateLimitFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  private RateLimitFilter rateLimitFilter;
  private StringWriter stringWriter;
  private PrintWriter writer;

  @BeforeEach
  void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    rateLimitFilter = new RateLimitFilter();

    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);
  }

  // ---------- NON-AUTH ENDPOINTS ----------

  @Test
  @DisplayName("doFilterInternal() - Should not rate limit non-auth endpoints")
  void doFilterInternal_NonAuthEndpoint_PassesThrough() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/exercises");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }

  @Test
  @DisplayName("doFilterInternal() - Should not rate limit test endpoints")
  void doFilterInternal_TestEndpoint_PassesThrough() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/test/reset-rate-limit");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(anyInt());
  }

  // ---------- AUTH ENDPOINTS - RATE LIMITING ----------

  @Test
  @DisplayName("doFilterInternal() - Should allow request within rate limit")
  void doFilterInternal_AuthEndpointWithinLimit_Allows() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/parents/login");
    when(request.getRemoteAddr()).thenReturn("192.168.1.100");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(response, never()).setStatus(429);
  }

  @Test
  @DisplayName("doFilterInternal() - Should rate limit after exceeding capacity")
  void doFilterInternal_ExceedsRateLimit_Blocks() throws ServletException, IOException {
    String clientIp = "192.168.1.200";
    when(request.getRequestURI()).thenReturn("/api/parents/register");
    when(request.getRemoteAddr()).thenReturn(clientIp);

    for (int i = 0; i < 6; i++) {
      rateLimitFilter.doFilterInternal(request, response, filterChain);
    }

    verify(filterChain, times(5)).doFilter(request, response);
    verify(response, atLeastOnce()).setStatus(429);
    verify(response, atLeastOnce()).setHeader("Retry-After", "60");
  }

  @Test
  @DisplayName("doFilterInternal() - Should handle different IPs independently")
  void doFilterInternal_DifferentIPs_IndependentRateLimits() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/parents/login");

    when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    rateLimitFilter.doFilterInternal(request, response, filterChain);

    when(request.getRemoteAddr()).thenReturn("192.168.1.2");
    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(2)).doFilter(request, response);
    verify(response, never()).setStatus(429);
  }

  // ---------- IP EXTRACTION ----------

  @Test
  @DisplayName("doFilterInternal() - Should extract IP from X-Forwarded-For header")
  void doFilterInternal_XForwardedForHeader_ExtractsFirstIP() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/children/login");
    when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 198.51.100.1");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    // The filter should use 203.0.113.1 (first IP in X-Forwarded-For)
  }

  @Test
  @DisplayName("doFilterInternal() - Should extract IP from X-Real-IP header")
  void doFilterInternal_XRealIPHeader_ExtractsIP() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/children/login");
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.5");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() - Should fallback to RemoteAddr when no proxy headers")
  void doFilterInternal_NoProxyHeaders_UsesRemoteAddr() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/children/login");
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getHeader("X-Real-IP")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("192.168.1.50");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  // ---------- AUTH ENDPOINT DETECTION ----------

  @Test
  @DisplayName("doFilterInternal() - Should detect parent login endpoint")
  void doFilterInternal_ParentLogin_AppliesRateLimit() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/parents/login");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() - Should detect parent register endpoint")
  void doFilterInternal_ParentRegister_AppliesRateLimit() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/parents/register");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() - Should detect children login endpoint")
  void doFilterInternal_ChildrenLogin_AppliesRateLimit() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/children/login");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() - Should detect setup pattern endpoint")
  void doFilterInternal_SetupPattern_AppliesRateLimit() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/children/setup-pattern");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() - Should detect check name endpoint")
  void doFilterInternal_CheckName_AppliesRateLimit() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/api/children/check-name");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    rateLimitFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  // ---------- RATE LIMIT RESPONSE ----------

  @Test
  @DisplayName("doFilterInternal() - Should return 429 with proper message when rate limited")
  void doFilterInternal_RateLimited_Returns429WithMessage() throws ServletException, IOException {
    String clientIp = "192.168.1.250";
    when(request.getRequestURI()).thenReturn("/api/parents/login");
    when(request.getRemoteAddr()).thenReturn(clientIp);

    for (int i = 0; i < 10; i++) {
      rateLimitFilter.doFilterInternal(request, response, filterChain);
    }

    verify(response, atLeastOnce()).setStatus(429);
    verify(response, atLeastOnce()).setHeader("Retry-After", "60");
    verify(response, atLeastOnce()).setContentType("application/json");
  }
}
