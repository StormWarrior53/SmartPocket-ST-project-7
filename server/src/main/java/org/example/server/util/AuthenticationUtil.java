package org.example.server.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    /**
     * Checks if the currently authenticated user has a specific role
     * @param role the role to check (without ROLE_ prefix)
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String roleWithPrefix = "ROLE_" + role.toUpperCase();
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals(roleWithPrefix));
    }

    /**
     * Checks if the currently authenticated user is a parent
     * @return true if user is a parent, false otherwise
     */
    public boolean isParent() {
        return hasRole("PARENT");
    }

    /**
     * Checks if the currently authenticated user is a child
     * @return true if user is a child, false otherwise
     */
    public boolean isChild() {
        return hasRole("CHILD");
    }

    /**
     * Checks if the currently authenticated user is an admin
     * @return true if user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
