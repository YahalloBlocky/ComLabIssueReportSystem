package edu.cvsu.labissues.dto;

import jakarta.validation.constraints.NotNull;

/** Used to switch a laboratory or computer between active and inactive. */
public record ActiveRequest(@NotNull(message = "active must be true or false.") Boolean active) {
}
