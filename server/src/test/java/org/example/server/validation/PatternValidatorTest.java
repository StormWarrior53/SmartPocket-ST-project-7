package org.example.server.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PatternValidator.
 *
 * Covers:
 * - Valid pattern formats
 * - Invalid pattern formats
 * - Minimum length requirements
 * - Unique point validation
 * - Number range validation
 */
@DisplayName("PatternValidator - Unit Tests")
class PatternValidatorTest {

  private PatternValidator validator;

  @Mock
  private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new PatternValidator();
  }

  // ---------- VALID PATTERNS ----------

  @Test
  @DisplayName("isValid() - Should accept valid 4-point pattern")
  void isValid_FourPoints_ReturnsTrue() {
    String pattern = "1,2,3,4";

    boolean result = validator.isValid(pattern, context);

    assertTrue(result);
  }

  @Test
  @DisplayName("isValid() - Should accept valid 9-point pattern")
  void isValid_NinePoints_ReturnsTrue() {
    String pattern = "1,2,3,4,5,6,7,8,9";

    boolean result = validator.isValid(pattern, context);

    assertTrue(result);
  }

  @Test
  @DisplayName("isValid() - Should accept pattern with different order")
  void isValid_DifferentOrder_ReturnsTrue() {
    String pattern = "9,5,1,3";

    boolean result = validator.isValid(pattern, context);

    assertTrue(result);
  }

  @Test
  @DisplayName("isValid() - Should accept pattern with spaces")
  void isValid_WithSpaces_ReturnsTrue() {
    String pattern = "1, 2, 5, 8";

    boolean result = validator.isValid(pattern, context);

    assertTrue(result);
  }

  // ---------- INVALID PATTERNS - LENGTH ----------

  @Test
  @DisplayName("isValid() - Should reject null pattern")
  void isValid_NullPattern_ReturnsFalse() {
    boolean result = validator.isValid(null, context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject empty pattern")
  void isValid_EmptyPattern_ReturnsFalse() {
    boolean result = validator.isValid("", context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject pattern with only spaces")
  void isValid_OnlySpaces_ReturnsFalse() {
    boolean result = validator.isValid("   ", context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject pattern with less than 4 points")
  void isValid_ThreePoints_ReturnsFalse() {
    String pattern = "1,2,3";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject pattern with only 1 point")
  void isValid_OnePoint_ReturnsFalse() {
    String pattern = "5";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  // ---------- INVALID PATTERNS - RANGE ----------

  @Test
  @DisplayName("isValid() - Should reject pattern with number less than 1")
  void isValid_NumberLessThan1_ReturnsFalse() {
    String pattern = "0,1,2,3";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject pattern with number greater than 9")
  void isValid_NumberGreaterThan9_ReturnsFalse() {
    String pattern = "1,2,3,10";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject pattern with negative number")
  void isValid_NegativeNumber_ReturnsFalse() {
    String pattern = "-1,2,3,4";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  // ---------- INVALID PATTERNS - FORMAT ----------

  @Test
  @DisplayName("isValid() - Should reject pattern with non-numeric values")
  void isValid_NonNumeric_ReturnsFalse() {
    String pattern = "a,b,c,d";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject pattern with mixed numeric and non-numeric")
  void isValid_MixedContent_ReturnsFalse() {
    String pattern = "1,2,x,4";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject pattern with special characters")
  void isValid_SpecialCharacters_ReturnsFalse() {
    String pattern = "1,2,#,4";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  // ---------- INVALID PATTERNS - UNIQUENESS ----------

  @Test
  @DisplayName("isValid() - Should reject pattern with duplicate points")
  void isValid_DuplicatePoints_ReturnsFalse() {
    String pattern = "1,2,3,1";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject pattern with all same points")
  void isValid_AllSamePoints_ReturnsFalse() {
    String pattern = "5,5,5,5";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  @Test
  @DisplayName("isValid() - Should reject pattern with multiple duplicates")
  void isValid_MultipleDuplicates_ReturnsFalse() {
    String pattern = "1,2,1,2";

    boolean result = validator.isValid(pattern, context);

    assertFalse(result);
  }

  // ---------- EDGE CASES ----------

  @Test
  @DisplayName("isValid() - Should accept minimum valid pattern")
  void isValid_MinimumPattern_ReturnsTrue() {
    String pattern = "1,2,3,4";

    boolean result = validator.isValid(pattern, context);

    assertTrue(result);
  }

  @Test
  @DisplayName("isValid() - Should accept maximum valid pattern")
  void isValid_MaximumPattern_ReturnsTrue() {
    String pattern = "1,2,3,4,5,6,7,8,9";

    boolean result = validator.isValid(pattern, context);

    assertTrue(result);
  }

  @Test
  @DisplayName("isValid() - Should handle extra commas gracefully")
  void isValid_ExtraCommas_ReturnsFalse() {
    String pattern = "1,,2,3,4";

    boolean result = validator.isValid(pattern, context);

    // This should fail because empty string between commas can't be parsed as
    // integer
    assertFalse(result);
  }
}
