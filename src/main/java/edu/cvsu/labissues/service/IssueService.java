package edu.cvsu.labissues.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cvsu.labissues.dto.CreateIssueRequest;
import edu.cvsu.labissues.dto.UpdateIssueRequest;
import edu.cvsu.labissues.exception.ApiException;
import edu.cvsu.labissues.model.IssueDetail;
import edu.cvsu.labissues.model.IssueFilter;
import edu.cvsu.labissues.model.IssueRecord;
import edu.cvsu.labissues.repository.IssueRepository;
import edu.cvsu.labissues.repository.LaboratoryRepository;
import edu.cvsu.labissues.repository.LookupRepository;
import edu.cvsu.labissues.security.SessionUser;

/** The business rules for issue reports. */
@Service
public class IssueService {

    private static final String PENDING = "Pending";
    private static final String RESOLVED = "Resolved";
    private static final String OTHER = "Other";

    private final IssueRepository issues;
    private final LaboratoryRepository labs;
    private final LookupRepository lookups;

    public IssueService(IssueRepository issues, LaboratoryRepository labs, LookupRepository lookups) {
        this.issues = issues;
        this.labs = labs;
        this.lookups = lookups;
    }

    /** A Student or Instructor submits a report. It always starts as Pending with no priority. */
    @Transactional
    public IssueDetail submit(SessionUser user, CreateIssueRequest req) {
        String categoryName = lookups.categories().stream()
                .filter(c -> c.id() == req.categoryId()).map(c -> c.name()).findFirst()
                .orElseThrow(() -> ApiException.badRequest("That category does not exist."));
        // "Other" means the reporter must say what kind of problem it is.
        String description = req.description().trim();
        if (OTHER.equalsIgnoreCase(categoryName)) {
            String other = req.otherDescription() == null ? "" : req.otherDescription().trim();
            if (other.length() < 3) {
                throw ApiException.badRequest("Please describe the type of problem (at least 3 characters) when you choose Other.");
            }
            description = "Type of problem (Other): " + other + "\n\n" + description;
        }
        if (!labs.isActiveLaboratory(req.laboratoryId())) {
            throw ApiException.badRequest("Choose an active laboratory.");
        }
        Integer computerId = req.computerId();
        if (computerId != null && !labs.isActiveComputerInLaboratory(computerId, req.laboratoryId())) {
            throw ApiException.badRequest("That computer is not an active unit of the chosen laboratory.");
        }
        int pendingId = lookups.findStatusIdByName(PENDING)
                .orElseThrow(() -> new IllegalStateException("Status 'Pending' is missing. Run seed.sql."));
        int resolvedId = statusId(RESOLVED);

        if (issues.existsOpenDuplicate(user.userId(), req.laboratoryId(), computerId, req.categoryId(), resolvedId)) {
            throw ApiException.conflict("You already have an unresolved report about this same problem.");
        }

        // Only an Instructor may write down who told them about the problem.
        String informant = null;
        if (user.isInstructor() && req.informantName() != null && !req.informantName().isBlank()) {
            informant = req.informantName().trim();
        }

        int reportId = issues.insert(user.userId(), req.laboratoryId(), computerId, req.categoryId(), pendingId,
                req.title().trim(), description, informant);
        issues.insertHistory(reportId, null, pendingId, user.userId(), "Report submitted");
        return detail(reportId);
    }

    public List<IssueRecord> mine(SessionUser user) {
        return issues.search(new IssueFilter(user.userId(), null, null, null, null, null));
    }

    public List<IssueRecord> search(IssueFilter filter) {
        return issues.search(filter);
    }

    /** Admins can open any report; everybody else only their own. */
    public IssueDetail detailFor(SessionUser user, int reportId) {
        IssueDetail d = detail(reportId);
        if (!user.isAdmin() && d.issue().reporterId() != user.userId()) {
            throw ApiException.forbidden("You can only open your own reports.");
        }
        return d;
    }

    /** Admin sets the priority and/or moves the status forward. */
    @Transactional
    public IssueDetail adminUpdate(SessionUser admin, int reportId, UpdateIssueRequest req) {
        IssueRecord current = issues.findById(reportId)
                .orElseThrow(() -> ApiException.notFound("Report not found."));
        int resolvedId = statusId(RESOLVED);
        if (current.statusId() == resolvedId) {
            throw ApiException.conflict("This report is already Resolved and can no longer be changed.");
        }

        int newStatus = req.statusId() == null ? current.statusId() : req.statusId();
        if (req.statusId() != null && !lookups.statusExists(newStatus)) {
            throw ApiException.badRequest("That status does not exist.");
        }
        if (newStatus < current.statusId()) {
            throw ApiException.badRequest("A report cannot go back to an earlier status.");
        }

        Integer newPriority = current.priorityId();
        if (req.priorityId() != null) {
            if (!lookups.priorityExists(req.priorityId())) {
                throw ApiException.badRequest("That priority does not exist.");
            }
            newPriority = req.priorityId();
        }

        boolean statusChanged = newStatus != current.statusId();
        boolean priorityChanged = !java.util.Objects.equals(newPriority, current.priorityId());
        if (!statusChanged && !priorityChanged) {
            throw ApiException.badRequest("Nothing to change.");
        }

        String action = req.actionTaken() == null ? "" : req.actionTaken().trim();
        if (newStatus == resolvedId && action.isEmpty()) {
            throw ApiException.badRequest("Describe the action taken before marking the report as Resolved.");
        }

        issues.updateStatusAndPriority(reportId, newStatus, newPriority);
        if (statusChanged) {
            String remarks = req.remarks() == null || req.remarks().isBlank() ? null : req.remarks().trim();
            if (newStatus == resolvedId && remarks == null) {
                remarks = "Resolved";
            }
            issues.insertHistory(reportId, current.statusId(), newStatus, admin.userId(), remarks);
            if (newStatus == resolvedId) {
                issues.insertResolution(reportId, admin.userId(), action);
            }
        }
        return detail(reportId);
    }

    private IssueDetail detail(int reportId) {
        IssueRecord issue = issues.findById(reportId)
                .orElseThrow(() -> ApiException.notFound("Report not found."));
        return new IssueDetail(issue, issues.findHistory(reportId), issues.findResolutions(reportId));
    }

    private int statusId(String name) {
        return lookups.findStatusIdByName(name)
                .orElseThrow(() -> new IllegalStateException("Status '" + name + "' is missing. Run seed.sql."));
    }
}
