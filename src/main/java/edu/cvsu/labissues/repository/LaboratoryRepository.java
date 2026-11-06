package edu.cvsu.labissues.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import edu.cvsu.labissues.model.ComputerOption;
import edu.cvsu.labissues.model.ComputerUnit;
import edu.cvsu.labissues.model.LabOption;
import edu.cvsu.labissues.model.Laboratory;

/** SQL for LABORATORY and COMPUTER_UNIT. */
@Repository
public class LaboratoryRepository {

    private static final RowMapper<Laboratory> LAB = (rs, n) -> new Laboratory(
            rs.getInt("laboratory_id"), rs.getString("lab_name"), rs.getString("building"),
            rs.getString("room_number"), rs.getBoolean("is_active"),
            rs.getInt("computer_count"), rs.getInt("report_count"));

    private static final RowMapper<LabOption> LAB_OPTION = (rs, n) -> new LabOption(
            rs.getInt("laboratory_id"), rs.getString("lab_name"), rs.getString("building"), rs.getString("room_number"));

    private static final RowMapper<ComputerUnit> COMPUTER = (rs, n) -> new ComputerUnit(
            rs.getInt("computer_id"), rs.getInt("laboratory_id"), rs.getString("unit_label"),
            rs.getString("asset_tag"), rs.getBoolean("is_active"), rs.getInt("report_count"));

    private static final RowMapper<ComputerOption> COMPUTER_OPTION = (rs, n) -> new ComputerOption(
            rs.getInt("computer_id"), rs.getString("unit_label"));

    private static final String SELECT_LAB =
            "SELECT l.laboratory_id, l.lab_name, l.building, l.room_number, l.is_active, "
            + "(SELECT COUNT(*) FROM computer_units c WHERE c.laboratory_id = l.laboratory_id) AS computer_count, "
            + "(SELECT COUNT(*) FROM issue_reports r WHERE r.laboratory_id = l.laboratory_id) AS report_count "
            + "FROM laboratories l ";

    private static final String SELECT_COMPUTER =
            "SELECT c.computer_id, c.laboratory_id, c.unit_label, c.asset_tag, c.is_active, "
            + "(SELECT COUNT(*) FROM issue_reports r WHERE r.computer_id = c.computer_id) AS report_count "
            + "FROM computer_units c ";

    private final JdbcTemplate jdbc;

    public LaboratoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ------------------------------------------------ laboratories

    public List<Laboratory> findAllLaboratories() {
        return jdbc.query(SELECT_LAB + "ORDER BY l.lab_name", LAB);
    }

    public Optional<Laboratory> findLaboratory(int laboratoryId) {
        return jdbc.query(SELECT_LAB + "WHERE l.laboratory_id = ?", LAB, laboratoryId).stream().findFirst();
    }

    /** Active laboratories for the "report a problem" dropdown. */
    public List<LabOption> findActiveLaboratoryOptions() {
        return jdbc.query("SELECT laboratory_id, lab_name, building, room_number FROM laboratories "
                + "WHERE is_active = 1 ORDER BY lab_name", LAB_OPTION);
    }

    public boolean isActiveLaboratory(int laboratoryId) {
        return count("SELECT COUNT(*) FROM laboratories WHERE laboratory_id = ? AND is_active = 1", laboratoryId) > 0;
    }

    /** excludeId is 0 when adding a new laboratory. */
    public boolean roomExists(String building, String roomNumber, int excludeId) {
        return count("SELECT COUNT(*) FROM laboratories WHERE building = ? AND room_number = ? AND laboratory_id <> ?",
                building, roomNumber, excludeId) > 0;
    }

    public int insertLaboratory(String labName, String building, String roomNumber) {
        return SqlSupport.insertAndGetId(jdbc,
                "INSERT INTO laboratories (lab_name, building, room_number, is_active) VALUES (?, ?, ?, 1)",
                labName, building, roomNumber);
    }

    public int updateLaboratory(int laboratoryId, String labName, String building, String roomNumber) {
        return jdbc.update("UPDATE laboratories SET lab_name = ?, building = ?, room_number = ? WHERE laboratory_id = ?",
                labName, building, roomNumber, laboratoryId);
    }

    public int setLaboratoryActive(int laboratoryId, boolean active) {
        return jdbc.update("UPDATE laboratories SET is_active = ? WHERE laboratory_id = ?", active ? 1 : 0, laboratoryId);
    }

    public int countReportsInLaboratory(int laboratoryId) {
        return count("SELECT COUNT(*) FROM issue_reports WHERE laboratory_id = ?", laboratoryId);
    }

    public int deleteComputersInLaboratory(int laboratoryId) {
        return jdbc.update("DELETE FROM computer_units WHERE laboratory_id = ?", laboratoryId);
    }

    public int deleteLaboratory(int laboratoryId) {
        return jdbc.update("DELETE FROM laboratories WHERE laboratory_id = ?", laboratoryId);
    }

    // ------------------------------------------------ computer units

    public List<ComputerUnit> findComputers(int laboratoryId) {
        return jdbc.query(SELECT_COMPUTER + "WHERE c.laboratory_id = ? ORDER BY c.unit_label", COMPUTER, laboratoryId);
    }

    public Optional<ComputerUnit> findComputer(int computerId) {
        return jdbc.query(SELECT_COMPUTER + "WHERE c.computer_id = ?", COMPUTER, computerId).stream().findFirst();
    }

    /** Active computers of one laboratory for the "report a problem" dropdown. */
    public List<ComputerOption> findActiveComputerOptions(int laboratoryId) {
        return jdbc.query("SELECT computer_id, unit_label FROM computer_units "
                + "WHERE laboratory_id = ? AND is_active = 1 ORDER BY unit_label", COMPUTER_OPTION, laboratoryId);
    }

    public boolean isActiveComputerInLaboratory(int computerId, int laboratoryId) {
        return count("SELECT COUNT(*) FROM computer_units WHERE computer_id = ? AND laboratory_id = ? AND is_active = 1",
                computerId, laboratoryId) > 0;
    }

    public boolean unitLabelExists(int laboratoryId, String unitLabel, int excludeId) {
        return count("SELECT COUNT(*) FROM computer_units WHERE laboratory_id = ? AND unit_label = ? AND computer_id <> ?",
                laboratoryId, unitLabel, excludeId) > 0;
    }

    public boolean assetTagExists(String assetTag, int excludeId) {
        return count("SELECT COUNT(*) FROM computer_units WHERE asset_tag = ? AND computer_id <> ?", assetTag, excludeId) > 0;
    }

    public int insertComputer(int laboratoryId, String unitLabel, String assetTag) {
        return SqlSupport.insertAndGetId(jdbc,
                "INSERT INTO computer_units (laboratory_id, unit_label, asset_tag, is_active) VALUES (?, ?, ?, 1)",
                laboratoryId, unitLabel, assetTag);
    }

    public int updateComputer(int computerId, String unitLabel, String assetTag) {
        return jdbc.update("UPDATE computer_units SET unit_label = ?, asset_tag = ? WHERE computer_id = ?",
                unitLabel, assetTag, computerId);
    }

    public int setComputerActive(int computerId, boolean active) {
        return jdbc.update("UPDATE computer_units SET is_active = ? WHERE computer_id = ?", active ? 1 : 0, computerId);
    }

    public int countReportsForComputer(int computerId) {
        return count("SELECT COUNT(*) FROM issue_reports WHERE computer_id = ?", computerId);
    }

    public int deleteComputer(int computerId) {
        return jdbc.update("DELETE FROM computer_units WHERE computer_id = ?", computerId);
    }

    private int count(String sql, Object... args) {
        Integer n = jdbc.queryForObject(sql, Integer.class, args);
        return n == null ? 0 : n;
    }
}
