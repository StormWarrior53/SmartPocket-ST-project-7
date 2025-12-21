package org.example.server.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil - Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        jwtUtil.setSecret("testsecret1234567890"); // ако има setSecret
    }

    @Test
    @DisplayName("generateToken() - Should generate a valid JWT token")
    void generateToken_ShouldReturnToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateToken(userId);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "Token should have 3 parts (JWT format)");
    }

    @Test
    @DisplayName("validateToken() - Should return true for valid token")
    void validateToken_ValidToken_ShouldReturnTrue() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateToken(userId);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("extractUserId() - Should extract correct userId")
    void extractUserId_ShouldReturnCorrectUUID() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateToken(userId);

        UUID extracted = jwtUtil.extractUserId(token);
        assertEquals(userId, extracted);
    }

    @Test
    @DisplayName("validateToken() - Should return false for invalid token")
    void validateToken_InvalidToken_ShouldReturnFalse() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }
}