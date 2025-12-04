package org.example.server.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildAuthResponse {

    private UUID id;
    private String name;
    private int age;
    private int xp;
    private int pocketMoney;
    private int allowanceMoney;
    private String role;
    private String token;

    @Builder.Default
    private String tokenType = "Bearer";
}
