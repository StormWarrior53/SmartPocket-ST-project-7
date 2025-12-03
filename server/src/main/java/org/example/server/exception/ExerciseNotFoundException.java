package org.example.server.exception;

public class ExerciseNotFoundException extends RuntimeException {

    public ExerciseNotFoundException(Long id) {
        super("Exercise not found with id: " + id);
    }

    public ExerciseNotFoundException(String message) {
        super(message);
    }
}