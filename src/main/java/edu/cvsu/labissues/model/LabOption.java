package edu.cvsu.labissues.model;

/** A laboratory as shown in the "choose a laboratory" dropdown. */
public record LabOption(int laboratoryId, String labName, String building, String roomNumber) {
}
