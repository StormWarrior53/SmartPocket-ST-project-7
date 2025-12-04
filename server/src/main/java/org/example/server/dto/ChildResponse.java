package org.example.server.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChildResponse {

    private UUID id;
    private String name;
    private int age;
    private int xp;
    private int pocketMoney;
    private int allowanceMoney;
    private boolean hasPattern;
}
