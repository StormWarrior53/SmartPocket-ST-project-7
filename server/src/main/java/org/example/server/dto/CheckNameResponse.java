package org.example.server.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckNameResponse {

    private boolean exists;
    private boolean hasPattern;
    private String message;
}
