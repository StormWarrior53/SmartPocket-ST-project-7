package org.example.server.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class PatternValidator implements ConstraintValidator<ValidPattern, String> {

    @Override
    public boolean isValid(String pattern, ConstraintValidatorContext context) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return false;
        }

        // Pattern should be comma-separated numbers (e.g., "1,2,5,8,9")
        String[] points = pattern.split(",");
        
        // Must have at least 4 points for a secure pattern
        if (points.length < 4) {
            return false;
        }

        // Each point should be a valid number between 1-9 (typical 3x3 grid)
        Set<Integer> uniquePoints = new HashSet<>();
        for (String point : points) {
            try {
                int num = Integer.parseInt(point.trim());
                // Valid points are 1-9 for a 3x3 grid
                if (num < 1 || num > 9) {
                    return false;
                }
                uniquePoints.add(num);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        // All points must be unique
        if (uniquePoints.size() != points.length) {
            return false;
        }

        return true;
    }
}
