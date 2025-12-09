package org.example.server.security.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class AccessDeniedLogger implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        log.warn("Access denied at {} method={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                accessDeniedException.getMessage());

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
    }
}