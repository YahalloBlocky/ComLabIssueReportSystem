package edu.cvsu.labissues.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Enter your ID number.") String schoolId,
        @NotBlank(message = "Enter your password.") String password) {
}
