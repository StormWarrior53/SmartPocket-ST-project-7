package org.example.server.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.dto.AuthResponse;
import org.example.server.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Custom success handler for OAuth2 authentication.
 * Processes the OAuth2User immediately after successful authentication
 * and redirects to the frontend with JWT token.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final OAuth2Service oAuth2Service;

  @Value("${frontend.url:http://localhost:5173}")
  private String frontendUrl;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException {

    log.info("OAuth2 authentication successful");

    try {
      // Get OAuth2User from authentication
      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
      String userEmail = oAuth2User.getAttribute("email");
      log.info("Processing OAuth for user: {}", userEmail);

      // Process OAuth login and generate JWT
      AuthResponse authResponse = oAuth2Service.processOAuthLogin(oAuth2User);

      // Redirect to frontend with token in URL fragment
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
}
