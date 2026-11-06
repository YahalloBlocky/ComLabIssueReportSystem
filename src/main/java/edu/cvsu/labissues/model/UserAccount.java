package edu.cvsu.labissues.model;

/** A row of USER joined with its ROLE. Used inside the server only (it holds the password hash). */
public record UserAccount(int userId, int roleId, String role, String schoolId,
                          String firstName, String lastName, String email, String passwordHash) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}
