package org.example.server.security.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        try {
            // ALWAYS add requestId
            MDC.put("requestId", UUID.randomUUID().toString().substring(0, 8));

            // Add client IP
            MDC.put("ip", request.getRemoteAddr());

            // Add endpoint
            MDC.put("uri", request.getRequestURI());

            // Add userId if authenticated
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
                MDC.put("user", auth.getName());
            } else {
                MDC.put("user", "anonymous");
            }

            chain.doFilter(request, response);

        } finally {
            // IMPORTANT: Clean MDC after each request
            MDC.clear();
        }
    }
}