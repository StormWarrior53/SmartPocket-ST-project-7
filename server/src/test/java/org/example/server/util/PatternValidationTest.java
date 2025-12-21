package org.example.server.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PatternValidator - Unit Tests")
class PatternValidatorTest {

    private PatternValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PatternValidator();
    }

    @Test
    @DisplayName("isValid() - Should return true for valid input")
    void isValid_ValidInput_ShouldReturnTrue() {
        assertTrue(validator.isValid("ValidPattern123"));
    }

    @Test
    @DisplayName("isValid() - Should return false for invalid input")
    void isValid_InvalidInput_ShouldReturnFalse() {
        assertFalse(validator.isValid("invalid pattern!"));
    }

    @Test
    @DisplayName("isValid() - Should handle null input")
    void isValid_NullInput_ShouldReturnFalse() {
        assertFalse(validator.isValid(null));
    }
}