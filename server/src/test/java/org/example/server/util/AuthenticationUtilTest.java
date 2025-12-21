package org.example.server.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthenticationUtil - Unit Tests")
class AuthenticationUtilTest {

    private AuthenticationUtil authUtil;

    @BeforeEach
    void setUp() {
        authUtil = new AuthenticationUtil();
    }

    @Test
    @DisplayName("getCurrentUserId() - Should return UUID from authentication principal")
    void getCurrentUserId_ShouldReturnUUID() {
        UUID userId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null)
        );

        UUID extracted = authUtil.getCurrentUserId();
        assertEquals(userId.toString(), extracted.toString());
    }

    @Test
    @DisplayName("getCurrentUserId() - Should throw exception if not authenticated")
    void getCurrentUserId_NotAuthenticated_ShouldThrow() {
        SecurityContextHolder.clearContext();

        assertThrows(RuntimeException.class, authUtil::getCurrentUserId);
    }
}