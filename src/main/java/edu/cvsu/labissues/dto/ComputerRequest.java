package edu.cvsu.labissues.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** laboratoryId is required when adding a computer and ignored when editing one. */
public record ComputerRequest(
        Integer laboratoryId,
        @NotBlank(message = "Unit label is required.") @Size(max = 30, message = "At most 30 characters.") String unitLabel,
        @Size(max = 50, message = "At most 50 characters.") String assetTag) {
}
