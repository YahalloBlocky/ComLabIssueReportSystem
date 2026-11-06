package edu.cvsu.labissues.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import edu.cvsu.labissues.model.Option;

/** SQL for the small fixed lists: ISSUE_CATEGORY, ISSUE_STATUS, PRIORITY_LEVEL. */
@Repository
public class LookupRepository {

    private final JdbcTemplate jdbc;

    public LookupRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Option> CATEGORY = (rs, n) -> new Option(rs.getInt("category_id"), rs.getString("category_name"));
    private static final RowMapper<Option> STATUS = (rs, n) -> new Option(rs.getInt("status_id"), rs.getString("status_name"));
    private static final RowMapper<Option> PRIORITY = (rs, n) -> new Option(rs.getInt("priority_id"), rs.getString("priority_name"));

    public List<Option> categories() {
        return jdbc.query("SELECT category_id, category_name FROM issue_categories ORDER BY category_id", CATEGORY);
    }

    /** Ordered by status_id: Pending, In Progress, Resolved. */
    public List<Option> statuses() {
        return jdbc.query("SELECT status_id, status_name FROM issue_statuses ORDER BY status_id", STATUS);
    }

    /** Ordered by priority_id: Low ... Critical. */
    public List<Option> priorities() {
        return jdbc.query("SELECT priority_id, priority_name FROM priority_levels ORDER BY priority_id", PRIORITY);
    }

    public boolean categoryExists(int categoryId) {
        return count("SELECT COUNT(*) FROM issue_categories WHERE category_id = ?", categoryId) > 0;
    }

    public boolean statusExists(int statusId) {
        return count("SELECT COUNT(*) FROM issue_statuses WHERE status_id = ?", statusId) > 0;
    }

    public boolean priorityExists(int priorityId) {
        return count("SELECT COUNT(*) FROM priority_levels WHERE priority_id = ?", priorityId) > 0;
    }

    public Optional<Integer> findStatusIdByName(String statusName) {
        List<Integer> ids = jdbc.queryForList("SELECT status_id FROM issue_statuses WHERE status_name = ?",
                Integer.class, statusName);
        return ids.stream().findFirst();
    }

    private int count(String sql, Object arg) {
        Integer n = jdbc.queryForObject(sql, Integer.class, arg);
        return n == null ? 0 : n;
    }
}
