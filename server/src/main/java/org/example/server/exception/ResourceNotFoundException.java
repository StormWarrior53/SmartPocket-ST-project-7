package org.example.server.exception;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

import java.util.Map;

public class ResourceNotFoundException extends RuntimeException implements ErrorResponse {
    private final ProblemDetail body;
    private final HttpStatus status;


    public ResourceNotFoundException(Class<?> resource, Object id) {
        super(resource.getSimpleName() + " with ID:  " + id + " was not found");

        this.status = HttpStatus.NOT_FOUND;

        //design pattern factory method
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, getMessage());
        problemDetail.setTitle("Resource Not Found");

        problemDetail.setProperties(Map.of(
                "resource", resource,
                "searchedId", id
        ));

        this.body = problemDetail;

    }

    @Override
    @NonNull
    public HttpStatusCode getStatusCode() {
        return status;
    }

    @Override
    @NonNull
    public ProblemDetail getBody() {
        return body;
    }

    private static String extractResourceName(Class<?> resource) {
        return resource == null ? "Resource" : resource.getSimpleName();
    }
}
