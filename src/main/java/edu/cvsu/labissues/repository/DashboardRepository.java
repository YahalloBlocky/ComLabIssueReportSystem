package edu.cvsu.labissues.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import edu.cvsu.labissues.model.CountItem;

/** Counting queries for the Admin dashboard. */
@Repository
public class DashboardRepository {

    private static final RowMapper<CountItem> COUNT = (rs, n) -> new CountItem(rs.getString("label"), rs.getLong("total"));

    private final JdbcTemplate jdbc;

    public DashboardRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long countAll() {
        return scalar("SELECT COUNT(*) FROM issue_reports");
    }

    public long countByStatus(int statusId) {
        Long n = jdbc.queryForObject("SELECT COUNT(*) FROM issue_reports WHERE status_id = ?", Long.class, statusId);
        return n == null ? 0 : n;
    }

    /** Reports that still have no priority and are not resolved yet (the Admin has to look at them). */
    public long countUnprioritizedOpen(int resolvedStatusId) {
        Long n = jdbc.queryForObject("SELECT COUNT(*) FROM issue_reports WHERE priority_id IS NULL AND status_id <> ?",
                Long.class, resolvedStatusId);
        return n == null ? 0 : n;
    }

    public List<CountItem> byStatus() {
        return jdbc.query("SELECT s.status_name AS label, COUNT(r.report_id) AS total FROM issue_statuses s "
                + "LEFT JOIN issue_reports r ON r.status_id = s.status_id "
                + "GROUP BY s.status_id, s.status_name ORDER BY s.status_id", COUNT);
    }

    /** Every priority level, plus "Not set yet" for reports without a priority. */
    public List<CountItem> byPriority() {
        return jdbc.query("SELECT p.priority_name AS label, COUNT(r.report_id) AS total FROM priority_levels p "
                + "LEFT JOIN issue_reports r ON r.priority_id = p.priority_id "
                + "GROUP BY p.priority_id, p.priority_name "
                + "UNION ALL "
                + "SELECT 'Not set yet' AS label, COUNT(*) AS total FROM issue_reports WHERE priority_id IS NULL", COUNT);
    }

    public List<CountItem> byCategory() {
        return jdbc.query("SELECT c.category_name AS label, COUNT(r.report_id) AS total FROM issue_categories c "
                + "LEFT JOIN issue_reports r ON r.category_id = c.category_id "
                + "GROUP BY c.category_id, c.category_name ORDER BY c.category_id", COUNT);
    }

    public List<CountItem> byLaboratory() {
        return jdbc.query("SELECT l.lab_name AS label, COUNT(r.report_id) AS total FROM laboratories l "
                + "LEFT JOIN issue_reports r ON r.laboratory_id = l.laboratory_id "
                + "GROUP BY l.laboratory_id, l.lab_name ORDER BY total DESC, l.lab_name", COUNT);
    }

    private long scalar(String sql) {
        Long n = jdbc.queryForObject(sql, Long.class);
        return n == null ? 0 : n;
    }
}
