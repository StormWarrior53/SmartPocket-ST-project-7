package org.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.server.validation.ValidPattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildLoginRequest {

    @NotBlank(message = "Child name is required")
    @Size(min = 2, max = 50, message = "Child name must be between 2 and 50 characters")
    private String childName;

    @NotBlank(message = "Parent name is required")
    @Size(min = 2, max = 50, message = "Parent name must be between 2 and 50 characters")
    private String parentName;

    @NotBlank(message = "Pattern is required")
    @ValidPattern
    private String pattern; // e.g., "1,2,5,8,9"
}
