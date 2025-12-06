package org.example.server.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private static final int RATE_LIMIT_CAPACITY = 5;
  private static final Duration RATE_LIMIT_REFILL_DURATION = Duration.ofMinutes(1);

  private static final int FAILED_ATTEMPT_LIMIT = 10;

  private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

  private final Map<String, FailedAttemptTracker> failedAttempts = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    if (isAuthEndpoint(path)) {
      String clientIp = getClientIp(request);

      if (isBlocked(clientIp)) {
        log.warn("Blocked request from IP {} due to excessive failed login attempts", clientIp);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", "300"); // 5 minutes
        response.getWriter().write("{\"error\":\"Too many failed login attempts. Please try again in 5 minutes.\"}");
        response.setContentType("application/json");
        return;
      }

      Bucket bucket = resolveBucket(clientIp);

      if (bucket.tryConsume(1)) {
        filterChain.doFilter(request, response);

        if (isLoginEndpoint(path)) {
          trackResponseStatus(clientIp, response.getStatus());
        }
      } else {
        log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, path);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", "60"); // 1 minute
        response.getWriter().write("{\"error\":\"Too many requests. Please try again in 1 minute.\"}");
        response.setContentType("application/json");
      }
    } else {
      // not an auth endpoint
      filterChain.doFilter(request, response);
    }
  }

  private boolean isAuthEndpoint(String path) {
    return path.startsWith("/api/parents/login") ||
        path.startsWith("/api/parents/register") ||
        path.startsWith("/api/children/login") ||
        path.startsWith("/api/children/setup-pattern") ||
        path.startsWith("/api/children/check-name");
  }

  private boolean isLoginEndpoint(String path) {
    return path.startsWith("/api/parents/login") ||
        path.startsWith("/api/children/login");
  }

  private Bucket resolveBucket(String clientIp) {
    return ipBuckets.computeIfAbsent(clientIp, k -> {
      Bandwidth limit = Bandwidth.builder()
          .capacity(RATE_LIMIT_CAPACITY)
          .refillIntervally(RATE_LIMIT_CAPACITY, RATE_LIMIT_REFILL_DURATION)
          .build();
      return Bucket.builder()
          .addLimit(limit)
          .build();
    });
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    return request.getRemoteAddr();
  }

  private void trackResponseStatus(String clientIp, int status) {
    if (status == 401 || status == 403) {
      // Failed login attempt
      FailedAttemptTracker tracker = failedAttempts.computeIfAbsent(
          clientIp,
          k -> new FailedAttemptTracker());
      tracker.recordFailedAttempt();

      if (tracker.getFailedAttempts() >= FAILED_ATTEMPT_LIMIT) {
        log.warn("IP {} has exceeded failed login attempt limit: {}",
            clientIp, tracker.getFailedAttempts());
      }
    } else if (status == 200) {
      // Successful login, reset failed attempts
      failedAttempts.remove(clientIp);
    }
  }

  private boolean isBlocked(String clientIp) {
    FailedAttemptTracker tracker = failedAttempts.get(clientIp);
    return tracker != null && tracker.isBlocked();
  }

  private static class FailedAttemptTracker {
    private int failedAttempts = 0;
    private long firstFailureTime = System.currentTimeMillis();
    private long blockUntil = 0;

    public void recordFailedAttempt() {
      long now = System.currentTimeMillis();

      // reset counter if more than 5 minutes since first failure
      if (now - firstFailureTime > Duration.ofMinutes(5).toMillis()) {
        failedAttempts = 0;
        firstFailureTime = now;
        blockUntil = 0;
      }

      failedAttempts++;

      // block for 5 minutes after 10 failed attempts
      if (failedAttempts >= FAILED_ATTEMPT_LIMIT) {
        blockUntil = now + Duration.ofMinutes(5).toMillis();
      }
    }

    public int getFailedAttempts() {
      return failedAttempts;
    }

    public boolean isBlocked() {
      return System.currentTimeMillis() < blockUntil;
    }
  }
}
