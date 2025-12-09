package org.example.server.security.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class AuthenticationEntryPointLogger implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        log.warn("Unauthorized request at {} method={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                authException.getMessage());

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}