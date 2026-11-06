package edu.cvsu.labissues.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import edu.cvsu.labissues.model.UserAccount;
import edu.cvsu.labissues.model.UserSummary;

/** All SQL for the USER (and ROLE) tables. Every value goes through a ? placeholder. */
@Repository
public class UserRepository {

    private static final String SELECT_ACCOUNT =
            "SELECT u.user_id, u.role_id, r.role_name, u.school_id, u.first_name, u.last_name, u.email, u.password "
            + "FROM users u JOIN roles r ON r.role_id = u.role_id ";

    private static final RowMapper<UserAccount> ACCOUNT_MAPPER = (rs, rowNum) -> new UserAccount(
            rs.getInt("user_id"), rs.getInt("role_id"), rs.getString("role_name"), rs.getString("school_id"),
            rs.getString("first_name"), rs.getString("last_name"), rs.getString("email"), rs.getString("password"));

    private static final RowMapper<UserSummary> SUMMARY_MAPPER = (rs, rowNum) -> new UserSummary(
            rs.getInt("user_id"), rs.getString("role_name"), rs.getString("school_id"),
            rs.getString("first_name"), rs.getString("last_name"), rs.getString("email"), rs.getInt("report_count"));

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<UserAccount> findBySchoolId(String schoolId) {
        List<UserAccount> rows = jdbc.query(SELECT_ACCOUNT + "WHERE u.school_id = ?", ACCOUNT_MAPPER, schoolId);
        return rows.stream().findFirst();
    }

    public Optional<UserAccount> findById(int userId) {
        List<UserAccount> rows = jdbc.query(SELECT_ACCOUNT + "WHERE u.user_id = ?", ACCOUNT_MAPPER, userId);
        return rows.stream().findFirst();
    }

    public boolean existsBySchoolId(String schoolId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE school_id = ?", Integer.class, schoolId);
        return n != null && n > 0;
    }

    public boolean existsByEmail(String email) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, email);
        return n != null && n > 0;
    }

    public Optional<Integer> findRoleId(String roleName) {
        List<Integer> ids = jdbc.queryForList("SELECT role_id FROM roles WHERE role_name = ?", Integer.class, roleName);
        return ids.stream().findFirst();
    }

    public int insert(int roleId, String schoolId, String firstName, String lastName, String email, String passwordHash) {
        return SqlSupport.insertAndGetId(jdbc,
                "INSERT INTO users (role_id, school_id, first_name, last_name, email, password) VALUES (?, ?, ?, ?, ?, ?)",
                roleId, schoolId, firstName, lastName, email, passwordHash);
    }

    /** For the Admin > Users page (read only). */
    public List<UserSummary> findAllSummaries() {
        return jdbc.query("SELECT u.user_id, r.role_name, u.school_id, u.first_name, u.last_name, u.email, "
                + "(SELECT COUNT(*) FROM issue_reports ir WHERE ir.reporter_id = u.user_id) AS report_count "
                + "FROM users u JOIN roles r ON r.role_id = u.role_id "
                + "ORDER BY u.role_id DESC, u.last_name, u.first_name", SUMMARY_MAPPER);
    }

    /** Deleting an account: first remove what depends on it (children before parents). */
    public int deleteResolutionsOfReporter(int userId) {
        return jdbc.update("DELETE FROM resolutions WHERE report_id IN "
                + "(SELECT report_id FROM issue_reports WHERE reporter_id = ?)", userId);
    }

    public int deleteHistoryOfReporter(int userId) {
        return jdbc.update("DELETE FROM status_history WHERE report_id IN "
                + "(SELECT report_id FROM issue_reports WHERE reporter_id = ?)", userId);
    }

    public int deleteReportsOfReporter(int userId) {
        return jdbc.update("DELETE FROM issue_reports WHERE reporter_id = ?", userId);
    }

    public int deleteUser(int userId) {
        return jdbc.update("DELETE FROM users WHERE user_id = ?", userId);
    }

    /** Used only by DemoAccountInitializer: replaces a placeholder password with a real hash. */
    public int replacePlaceholderPassword(String schoolId, String placeholder, String newHash) {
        return jdbc.update("UPDATE users SET password = ? WHERE school_id = ? AND password = ?",
                newHash, schoolId, placeholder);
    }
}
