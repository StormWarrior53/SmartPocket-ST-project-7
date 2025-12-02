package org.example.server.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private String token;

    @Builder.Default
    private String tokenType = "Bearer";
}
