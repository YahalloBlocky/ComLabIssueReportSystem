package edu.cvsu.labissues.security;

/** What we remember about the logged-in person inside their HTTP session. */
public record SessionUser(int userId, String role, String fullName) {

    public static final String ROLE_STUDENT = "Student";
    public static final String ROLE_INSTRUCTOR = "Instructor";
    public static final String ROLE_ADMIN = "Admin";

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    public boolean isInstructor() {
        return ROLE_INSTRUCTOR.equals(role);
    }
}
