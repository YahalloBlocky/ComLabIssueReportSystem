package edu.cvsu.labissues.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cvsu.labissues.dto.ComputerRequest;
import edu.cvsu.labissues.dto.LaboratoryRequest;
import edu.cvsu.labissues.exception.ApiException;
import edu.cvsu.labissues.model.ComputerUnit;
import edu.cvsu.labissues.model.CountItem;
import edu.cvsu.labissues.model.DashboardStats;
import edu.cvsu.labissues.model.Laboratory;
import edu.cvsu.labissues.model.UserSummary;
import edu.cvsu.labissues.repository.DashboardRepository;
import edu.cvsu.labissues.repository.IssueRepository;
import edu.cvsu.labissues.repository.LaboratoryRepository;
import edu.cvsu.labissues.repository.LookupRepository;
import edu.cvsu.labissues.repository.UserRepository;

/** Admin-only work: dashboard, laboratories, computers, user list. */
@Service
public class AdminService {

    private final LaboratoryRepository labs;
    private final DashboardRepository dashboard;
    private final IssueRepository issues;
    private final LookupRepository lookups;
    private final UserRepository users;

    public AdminService(LaboratoryRepository labs, DashboardRepository dashboard, IssueRepository issues,
                        LookupRepository lookups, UserRepository users) {
        this.labs = labs;
        this.dashboard = dashboard;
        this.issues = issues;
        this.lookups = lookups;
        this.users = users;
    }

    // ------------------------------------------------ dashboard / users

    public DashboardStats dashboardStats() {
        long total = dashboard.countAll();
        int resolvedId = lookups.findStatusIdByName("Resolved").orElse(-1);
        long resolved = dashboard.countByStatus(resolvedId);
        List<CountItem> byStatus = dashboard.byStatus();
        return new DashboardStats(total, total - resolved, resolved,
                dashboard.countUnprioritizedOpen(resolvedId),
                byStatus, dashboard.byPriority(), dashboard.byCategory(), dashboard.byLaboratory(),
                issues.findRecent(5));
    }

    public List<UserSummary> users() {
        return users.findAllSummaries();
    }

    /**
     * Deletes a Student or Instructor account together with the reports they filed
     * (and those reports' history and resolutions), all in one transaction.
     * Admin accounts cannot be deleted here, so the system can never be left without an Admin.
     */
    @Transactional
    public void deleteUser(int adminId, int userId) {
        if (userId == adminId) {
            throw ApiException.conflict("You cannot delete your own account.");
        }
        var account = users.findById(userId).orElseThrow(() -> ApiException.notFound("Account not found."));
        if ("Admin".equals(account.role())) {
            throw ApiException.conflict("Admin accounts cannot be deleted from the system.");
        }
        users.deleteResolutionsOfReporter(userId);
        users.deleteHistoryOfReporter(userId);
        users.deleteReportsOfReporter(userId);
        users.deleteUser(userId);
    }

    // ------------------------------------------------ laboratories

    public List<Laboratory> laboratories() {
        return labs.findAllLaboratories();
    }

    @Transactional
    public Laboratory createLaboratory(LaboratoryRequest req) {
        String building = req.building().trim();
        String room = req.roomNumber().trim();
        if (labs.roomExists(building, room, 0)) {
            throw ApiException.conflict("That room already exists in that building.");
        }
        int id = labs.insertLaboratory(req.labName().trim(), building, room);
        return labs.findLaboratory(id).orElseThrow();
    }

    @Transactional
    public Laboratory updateLaboratory(int id, LaboratoryRequest req) {
        requireLab(id);
        String building = req.building().trim();
        String room = req.roomNumber().trim();
        if (labs.roomExists(building, room, id)) {
            throw ApiException.conflict("That room already exists in that building.");
        }
        labs.updateLaboratory(id, req.labName().trim(), building, room);
        return labs.findLaboratory(id).orElseThrow();
    }

    public Laboratory setLaboratoryActive(int id, boolean active) {
        requireLab(id);
        labs.setLaboratoryActive(id, active);
        return labs.findLaboratory(id).orElseThrow();
    }

    /** A laboratory with reports cannot be deleted (it would erase history); make it inactive instead. */
    @Transactional
    public void deleteLaboratory(int id) {
        requireLab(id);
        if (labs.countReportsInLaboratory(id) > 0) {
            throw ApiException.conflict("This laboratory already has reports, so it cannot be deleted. "
                    + "Mark it as inactive instead.");
        }
        labs.deleteComputersInLaboratory(id);
        labs.deleteLaboratory(id);
    }

    // ------------------------------------------------ computers

    public List<ComputerUnit> computers(int laboratoryId) {
        requireLab(laboratoryId);
        return labs.findComputers(laboratoryId);
    }

    @Transactional
    public ComputerUnit createComputer(ComputerRequest req) {
        if (req.laboratoryId() == null) {
            throw ApiException.badRequest("Choose a laboratory.");
        }
        requireLab(req.laboratoryId());
        String label = req.unitLabel().trim();
        String tag = cleanTag(req.assetTag());
        if (labs.unitLabelExists(req.laboratoryId(), label, 0)) {
            throw ApiException.conflict("That unit label is already used in this laboratory.");
        }
        if (tag != null && labs.assetTagExists(tag, 0)) {
            throw ApiException.conflict("That asset tag is already used by another computer.");
        }
        int id = labs.insertComputer(req.laboratoryId(), label, tag);
        return labs.findComputer(id).orElseThrow();
    }

    @Transactional
    public ComputerUnit updateComputer(int id, ComputerRequest req) {
        ComputerUnit current = requireComputer(id);
        String label = req.unitLabel().trim();
        String tag = cleanTag(req.assetTag());
        if (labs.unitLabelExists(current.laboratoryId(), label, id)) {
            throw ApiException.conflict("That unit label is already used in this laboratory.");
        }
        if (tag != null && labs.assetTagExists(tag, id)) {
            throw ApiException.conflict("That asset tag is already used by another computer.");
        }
        labs.updateComputer(id, label, tag);
        return labs.findComputer(id).orElseThrow();
    }

    public ComputerUnit setComputerActive(int id, boolean active) {
        requireComputer(id);
        labs.setComputerActive(id, active);
        return labs.findComputer(id).orElseThrow();
    }

    public void deleteComputer(int id) {
        requireComputer(id);
        if (labs.countReportsForComputer(id) > 0) {
            throw ApiException.conflict("This computer already has reports, so it cannot be deleted. "
                    + "Mark it as inactive instead.");
        }
        labs.deleteComputer(id);
    }

    private void requireLab(int id) {
        labs.findLaboratory(id).orElseThrow(() -> ApiException.notFound("Laboratory not found."));
    }

    private ComputerUnit requireComputer(int id) {
        return labs.findComputer(id).orElseThrow(() -> ApiException.notFound("Computer not found."));
    }

    private String cleanTag(String tag) {
        return tag == null || tag.isBlank() ? null : tag.trim();
    }
}
