package org.example.server.security.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String clientIp = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Log Request (SAFE)
        log.info("[REQUEST] method={} uri={} query={} ip={} agent={}",
                method,
                uri,
                query,
                clientIp,
                userAgent
        );

        // continue filter pipeline
        filterChain.doFilter(request, response);

        // Log Response
        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        log.info("[RESPONSE] method={} uri={} status={} duration={}ms",
                method,
                uri,
                status,
                duration
        );
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // игнорирай static resources и actuator metrics
        String path = request.getRequestURI();

        return path.startsWith("/actuator")
                || path.startsWith("/static")
                || path.startsWith("/favicon");
    }
}
