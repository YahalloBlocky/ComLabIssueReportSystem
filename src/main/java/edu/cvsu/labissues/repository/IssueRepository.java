package edu.cvsu.labissues.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import edu.cvsu.labissues.model.HistoryEntry;
import edu.cvsu.labissues.model.IssueFilter;
import edu.cvsu.labissues.model.IssueRecord;
import edu.cvsu.labissues.model.ResolutionEntry;

/** SQL for ISSUE_REPORT, STATUS_HISTORY and RESOLUTION. */
@Repository
public class IssueRepository {

    /** One query that joins a report with the names of everything it points to. */
    static final String BASE_SELECT =
            "SELECT r.report_id, CONCAT('ICL-', LPAD(r.report_id, 5, '0')) AS reference_no, "
            + "r.title, r.description, r.informant_name, "
            + "r.reporter_id, CONCAT(u.first_name, ' ', u.last_name) AS reporter_name, "
            + "u.school_id AS reporter_school_id, ro.role_name AS reporter_role, "
            + "r.laboratory_id, l.lab_name, l.room_number, "
            + "r.computer_id, c.unit_label, "
            + "r.category_id, cat.category_name, "
            + "r.status_id, s.status_name, "
            + "r.priority_id, p.priority_name, "
            + "r.created_at, r.updated_at "
            + "FROM issue_reports r "
            + "JOIN users u ON u.user_id = r.reporter_id "
            + "JOIN roles ro ON ro.role_id = u.role_id "
            + "JOIN laboratories l ON l.laboratory_id = r.laboratory_id "
            + "LEFT JOIN computer_units c ON c.computer_id = r.computer_id "
            + "JOIN issue_categories cat ON cat.category_id = r.category_id "
            + "JOIN issue_statuses s ON s.status_id = r.status_id "
            + "LEFT JOIN priority_levels p ON p.priority_id = r.priority_id ";

    static final RowMapper<IssueRecord> ISSUE_MAPPER = (rs, n) -> new IssueRecord(
            rs.getInt("report_id"), rs.getString("reference_no"), rs.getString("title"),
            rs.getString("description"), rs.getString("informant_name"),
            rs.getInt("reporter_id"), rs.getString("reporter_name"), rs.getString("reporter_school_id"),
            rs.getString("reporter_role"),
            rs.getInt("laboratory_id"), rs.getString("lab_name"), rs.getString("room_number"),
            SqlSupport.getInteger(rs, "computer_id"), rs.getString("unit_label"),
            rs.getInt("category_id"), rs.getString("category_name"),
            rs.getInt("status_id"), rs.getString("status_name"),
            SqlSupport.getInteger(rs, "priority_id"), rs.getString("priority_name"),
            SqlSupport.getDateTime(rs, "created_at"), SqlSupport.getDateTime(rs, "updated_at"));

    private static final RowMapper<HistoryEntry> HISTORY_MAPPER = (rs, n) -> new HistoryEntry(
            rs.getInt("history_id"), rs.getString("old_status"), rs.getString("new_status"),
            rs.getString("changed_by_name"), rs.getString("changed_by_role"),
            rs.getString("remarks"), SqlSupport.getDateTime(rs, "changed_at"));

    private static final RowMapper<ResolutionEntry> RESOLUTION_MAPPER = (rs, n) -> new ResolutionEntry(
            rs.getInt("resolution_id"), rs.getString("resolved_by_name"),
            rs.getString("action_taken"), SqlSupport.getDateTime(rs, "resolved_at"));

    private final JdbcTemplate jdbc;

    public IssueRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------ create / update

    public int insert(int reporterId, int laboratoryId, Integer computerId, int categoryId, int statusId,
                      String title, String description, String informantName) {
        return SqlSupport.insertAndGetId(jdbc,
                "INSERT INTO issue_reports (reporter_id, laboratory_id, computer_id, category_id, status_id, "
                + "title, description, informant_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                reporterId, laboratoryId, computerId, categoryId, statusId, title, description, informantName);
    }

    public void insertHistory(int reportId, Integer oldStatusId, int newStatusId, int changedBy, String remarks) {
        jdbc.update("INSERT INTO status_history (report_id, old_status_id, new_status_id, changed_by, remarks) "
                + "VALUES (?, ?, ?, ?, ?)", reportId, oldStatusId, newStatusId, changedBy, remarks);
    }

    public void insertResolution(int reportId, int resolvedBy, String actionTaken) {
        jdbc.update("INSERT INTO resolutions (report_id, resolved_by, action_taken) VALUES (?, ?, ?)",
                reportId, resolvedBy, actionTaken);
    }

    public int updateStatusAndPriority(int reportId, int statusId, Integer priorityId) {
        return jdbc.update("UPDATE issue_reports SET status_id = ?, priority_id = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE report_id = ?", statusId, priorityId, reportId);
    }

    // ------------------------------------------------ read

    public Optional<IssueRecord> findById(int reportId) {
        return jdbc.query(BASE_SELECT + "WHERE r.report_id = ?", ISSUE_MAPPER, reportId).stream().findFirst();
    }

    /** Builds the WHERE part only from the filters that were given. Values still go through ? placeholders. */
    public List<IssueRecord> search(IssueFilter f) {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (f.reporterId() != null) {
            sql.append(" AND r.reporter_id = ?");
            args.add(f.reporterId());
        }
        if (f.statusId() != null) {
            sql.append(" AND r.status_id = ?");
            args.add(f.statusId());
        }
        if (f.priorityId() != null) {
            if (f.priorityId() == 0) {
                sql.append(" AND r.priority_id IS NULL");
            } else {
                sql.append(" AND r.priority_id = ?");
                args.add(f.priorityId());
            }
        }
        if (f.categoryId() != null) {
            sql.append(" AND r.category_id = ?");
            args.add(f.categoryId());
        }
        if (f.laboratoryId() != null) {
            sql.append(" AND r.laboratory_id = ?");
            args.add(f.laboratoryId());
        }
        if (f.q() != null && !f.q().isBlank()) {
            String like = "%" + f.q().trim() + "%";
            sql.append(" AND (r.title LIKE ? OR r.description LIKE ? OR c.unit_label LIKE ? "
                    + "OR CONCAT('ICL-', LPAD(r.report_id, 5, '0')) LIKE ? "
                    + "OR CONCAT(u.first_name, ' ', u.last_name) LIKE ?)");
            for (int i = 0; i < 5; i++) {
                args.add(like);
            }
        }
        sql.append(" ORDER BY r.created_at DESC, r.report_id DESC");
        return jdbc.query(sql.toString(), ISSUE_MAPPER, args.toArray());
    }

    public List<IssueRecord> findRecent(int limit) {
        return jdbc.query(BASE_SELECT + "ORDER BY r.created_at DESC, r.report_id DESC LIMIT ?", ISSUE_MAPPER, limit);
    }

    public List<HistoryEntry> findHistory(int reportId) {
        return jdbc.query("SELECT h.history_id, os.status_name AS old_status, ns.status_name AS new_status, "
                + "CONCAT(u.first_name, ' ', u.last_name) AS changed_by_name, ro.role_name AS changed_by_role, "
                + "h.remarks, h.changed_at "
                + "FROM status_history h "
                + "LEFT JOIN issue_statuses os ON os.status_id = h.old_status_id "
                + "JOIN issue_statuses ns ON ns.status_id = h.new_status_id "
                + "JOIN users u ON u.user_id = h.changed_by "
                + "JOIN roles ro ON ro.role_id = u.role_id "
                + "WHERE h.report_id = ? ORDER BY h.changed_at, h.history_id", HISTORY_MAPPER, reportId);
    }

    public List<ResolutionEntry> findResolutions(int reportId) {
        return jdbc.query("SELECT x.resolution_id, CONCAT(u.first_name, ' ', u.last_name) AS resolved_by_name, "
                + "x.action_taken, x.resolved_at "
                + "FROM resolutions x JOIN users u ON u.user_id = x.resolved_by "
                + "WHERE x.report_id = ? ORDER BY x.resolved_at, x.resolution_id", RESOLUTION_MAPPER, reportId);
    }

    /**
     * Is there already an unresolved report from the same person about the same laboratory,
     * computer and category? "<=>" is MySQL's NULL-safe equals (NULL <=> NULL is true),
     * which we need because computer_id may be NULL.
     */
    public boolean existsOpenDuplicate(int reporterId, int laboratoryId, Integer computerId, int categoryId,
                                       int resolvedStatusId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM issue_reports WHERE reporter_id = ? "
                + "AND laboratory_id = ? AND computer_id <=> ? AND category_id = ? AND status_id <> ?",
                Integer.class, reporterId, laboratoryId, computerId, categoryId, resolvedStatusId);
        return n != null && n > 0;
    }
}
