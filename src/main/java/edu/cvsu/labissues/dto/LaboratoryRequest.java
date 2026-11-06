package edu.cvsu.labissues.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LaboratoryRequest(
        @NotBlank(message = "Laboratory name is required.") @Size(max = 50, message = "At most 50 characters.") String labName,
        @NotBlank(message = "Building is required.") @Size(max = 50, message = "At most 50 characters.") String building,
        @NotBlank(message = "Room number is required.") @Size(max = 20, message = "At most 20 characters.") String roomNumber) {
}
