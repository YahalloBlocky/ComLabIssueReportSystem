package edu.cvsu.labissues.model;

/** The safe version of a user that can be sent to the browser (no password). */
public record UserSummary(int userId, String role, String schoolId,
                          String firstName, String lastName, String email, int reportCount) {
}
