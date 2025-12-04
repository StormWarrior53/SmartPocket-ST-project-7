package org.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetupPatternRequest {

    @NotBlank(message = "Child name is required")
    private String childName;

    @NotBlank(message = "Parent name is required")
    private String parentName;

    @NotBlank(message = "Pattern is required")
    private String pattern; // e.g., "1,2,5,8,9" or "1-2-5-8-9"
}
