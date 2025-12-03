package org.example.server.exception;


import java.util.UUID;

public class ExerciseNotFoundException extends RuntimeException {
    public ExerciseNotFoundException(String message) {
        super(message);
    }

    public ExerciseNotFoundException(UUID id) {
        super("Exercise not found with id: " + id);
    }
}