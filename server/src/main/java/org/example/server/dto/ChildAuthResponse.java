package org.example.server.dto;

import java.util.UUID;

public record ChildAuthResponse(
    UUID id,
    String name,
    int age,
    int xp,
    int pocketMoney,
    int allowanceMoney,
    String role,
    String token,
    String tokenType) {
  public ChildAuthResponse(UUID id, String name, int age, int xp,
      int pocketMoney, int allowanceMoney,
      String role, String token) {
    this(id, name, age, xp, pocketMoney, allowanceMoney, role, token, "Bearer");
  }
}
