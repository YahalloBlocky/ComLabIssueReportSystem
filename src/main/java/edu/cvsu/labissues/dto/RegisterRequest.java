package edu.cvsu.labissues.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** The registration form. Only Student and Instructor accounts can be created here; Admin cannot. */
public record RegisterRequest(
        @NotBlank(message = "ID number is required.")
        @Pattern(regexp = "^[A-Za-z0-9-]{4,20}$", message = "Use 4 to 20 letters, numbers or dashes.")
        String schoolId,

        @NotBlank(message = "First name is required.")
        @Size(max = 50, message = "At most 50 characters.")
        String firstName,

        @NotBlank(message = "Last name is required.")
        @Size(max = 50, message = "At most 50 characters.")
        String lastName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter a valid email address.")
        @Size(max = 100, message = "At most 100 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 72, message = "Use 8 to 72 characters.")
        String password,

        @NotBlank(message = "Choose Student or Instructor.")
        String role) {
}
