package org.example.server.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticationUtil {

    /**
     * Gets the currently authenticated user's ID from the security context
     * @return UUID of the authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UUID) {
            return (UUID) principal;
        }

        throw new IllegalStateException("Invalid authentication principal type");
    }

    /**
     * Checks if there is a currently authenticated user
     * @return true if a user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
