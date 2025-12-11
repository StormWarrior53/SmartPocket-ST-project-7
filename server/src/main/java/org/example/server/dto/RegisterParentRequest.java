package org.example.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterParentRequest(
    @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

    @NotBlank(message = "First name is required") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,

    @NotBlank(message = "Last name is required") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName,

    @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password) {
  @Override
  public String toString() {
    return "RegisterParentRequest[email=" + email +
        ", firstName=" + firstName +
        ", lastName=" + lastName +
        ", password=***]";
  }
}
