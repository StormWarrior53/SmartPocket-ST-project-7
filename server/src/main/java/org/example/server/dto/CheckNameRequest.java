package org.example.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckNameRequest {

    @NotBlank(message = "Child name is required")
    private String childName;

    @NotBlank(message = "Parent name is required")
    private String parentName;
}
