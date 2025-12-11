package org.example.server.dto;

import java.util.UUID;

public record ChildResponse(
    UUID id,
    String name,
    int age,
    int xp,
    int pocketMoney,
    int allowanceMoney,
    boolean hasPattern
) {
}
