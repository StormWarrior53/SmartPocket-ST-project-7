package org.example.server.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PatternValidator.class)
@Documented
public @interface ValidPattern {
    String message() default "Invalid pattern format. Pattern must contain at least 4 unique points (e.g., '1,2,5,8')";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
