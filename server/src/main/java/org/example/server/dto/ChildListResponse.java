package org.example.server.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildListResponse {

    private UUID id;
    private String name;
    private int age;
}
