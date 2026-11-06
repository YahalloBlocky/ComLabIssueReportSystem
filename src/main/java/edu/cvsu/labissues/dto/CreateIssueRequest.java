package edu.cvsu.labissues.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A new issue report. computerId is optional (leave empty when the whole laboratory is affected). */
public record CreateIssueRequest(
        @NotNull(message = "Choose a laboratory.") Integer laboratoryId,
        Integer computerId,
        @NotNull(message = "Choose a category.") Integer categoryId,

        @NotBlank(message = "Enter a short title.")
        @Size(min = 5, max = 150, message = "Use 5 to 150 characters.")
        String title,

        @NotBlank(message = "Describe the problem.")
        @Size(min = 10, max = 2000, message = "Use 10 to 2000 characters.")
        String description,

        @Size(max = 100, message = "At most 100 characters.")
        String informantName,
        @Size(max = 100, message = "At most 100 characters.")
        String otherDescription) {
}
