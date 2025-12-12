package org.example.server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.server.service.TokenBlacklistService;
import org.example.server.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                UUID userId = jwtUtil.extractUserId(jwt);

                // Check if token is blacklisted
                if (tokenBlacklistService.isTokenBlacklisted(jwt) ||
                    tokenBlacklistService.areAllUserTokensBlacklisted(userId)) {
                    log.warn("Attempted use of blacklisted token for user: {} at {}", userId, requestURI);
                    // Don't authenticate - token is revoked
                } else {
                    String email = jwtUtil.extractEmail(jwt);

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT authentication successful for user {} (id={}) at {}", email, userId, requestURI);
                }
            } else if (jwt != null) {
                log.warn("Invalid JWT token received for request to {}", requestURI);
            }
        } catch (Exception ex) {
            log.error("JWT authentication failed for request to {}: {}", requestURI, ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
