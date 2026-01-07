package org.example.server.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.AuthResponse;
import org.example.server.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OAuth2Controller {

  private final OAuth2Service oAuth2Service;

  @Value("${frontend.url:http://localhost:5173}")
  private String frontendUrl;

  /**
   * OAuth2 callback endpoint - receives authorization from Google.
   * This endpoint is called by Google after user authorizes the app.
   * It processes the OAuth login and redirects to the frontend with the JWT
   * token.
   *
   * @param oAuth2User OAuth2 user information from Google (injected by Spring
   *                   Security)
   * @param response   HTTP response for redirecting to frontend
   * @throws IOException if redirect fails
   */
  @GetMapping("/google/callback")
  public void googleCallback(HttpServletResponse response) throws IOException {

    log.info("Google OAuth callback received");

    try {
      // Get OAuth2User from security context
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
        log.error("No OAuth2User found in security context");
        throw new RuntimeException("Authentication failed - no OAuth2 user found");
      }

      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
      String userEmail = oAuth2User.getAttribute("email");
      log.info("Processing OAuth for user: {}", userEmail);

      AuthResponse authResponse = oAuth2Service.processOAuthLogin(oAuth2User);

      // Redirect to frontend with token in URL fragment (more secure than query
      // params)
      // URL fragment (#) is not sent to the server, only accessible to JavaScript
      String redirectUrl = UriComponentsBuilder
          .fromUriString(frontendUrl + "/auth/google/callback")
          .fragment("token=" + authResponse.token() +
              "&expiresAt=" + authResponse.expiresAt() +
              "&id=" + authResponse.id() +
              "&email=" + authResponse.email() +
              "&firstName=" + authResponse.firstName() +
              "&lastName=" + authResponse.lastName() +
              "&role=" + authResponse.role())
          .build()
          .toUriString();

      log.info("Redirecting to frontend: {}", frontendUrl + "/auth/google/callback");
      response.sendRedirect(redirectUrl);

    } catch (Exception e) {
      log.error("OAuth callback failed: {}", e.getMessage(), e);

      // Redirect to frontend with error
      String errorUrl = UriComponentsBuilder
          .fromUriString(frontendUrl + "/auth/google/callback")
          .queryParam("error", "authentication_failed")
          .queryParam("message", e.getMessage())
          .build()
          .toUriString();

      response.sendRedirect(errorUrl);
    }
  }

  /**
   * Alternative JSON endpoint for OAuth callback.
   * Useful for mobile apps or custom OAuth flows that prefer JSON responses.
   *
   * @param oAuth2User OAuth2 user information from Google
   * @return AuthResponse containing JWT token and user information
   */
  @GetMapping("/google/callback/json")
  public ResponseEntity<AuthResponse> googleCallbackJson(
      @AuthenticationPrincipal OAuth2User oAuth2User) {

    log.info("Google OAuth callback (JSON) received");
    AuthResponse authResponse = oAuth2Service.processOAuthLogin(oAuth2User);
    return ResponseEntity.ok(authResponse);
  }
}
