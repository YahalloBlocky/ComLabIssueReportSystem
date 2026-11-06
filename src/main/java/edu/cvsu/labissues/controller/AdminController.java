package edu.cvsu.labissues.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.cvsu.labissues.dto.ActiveRequest;
import edu.cvsu.labissues.dto.ComputerRequest;
import edu.cvsu.labissues.dto.LaboratoryRequest;
import edu.cvsu.labissues.dto.UpdateIssueRequest;
import edu.cvsu.labissues.model.ComputerUnit;
import edu.cvsu.labissues.model.DashboardStats;
import edu.cvsu.labissues.model.IssueDetail;
import edu.cvsu.labissues.model.IssueFilter;
import edu.cvsu.labissues.model.IssueRecord;
import edu.cvsu.labissues.model.Laboratory;
import edu.cvsu.labissues.model.UserSummary;
import edu.cvsu.labissues.security.AuthInterceptor;
import edu.cvsu.labissues.security.RequireRole;
import edu.cvsu.labissues.security.SessionUser;
import edu.cvsu.labissues.service.AdminService;
import edu.cvsu.labissues.service.IssueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/** Everything under /api/admin is for the Admin (laboratory technician) only. */
@RestController
@RequestMapping("/api/admin")
@RequireRole(SessionUser.ROLE_ADMIN)
public class AdminController {

    private final IssueService issues;
    private final AdminService admin;

    public AdminController(IssueService issues, AdminService admin) {
        this.issues = issues;
        this.admin = admin;
    }

    // ---- reports

    @GetMapping("/issues")
    public List<IssueRecord> search(@RequestParam(required = false) Integer statusId,
                                    @RequestParam(required = false) Integer priorityId,
                                    @RequestParam(required = false) Integer categoryId,
                                    @RequestParam(required = false) Integer laboratoryId,
                                    @RequestParam(required = false) String q) {
        return issues.search(new IssueFilter(null, statusId, priorityId, categoryId, laboratoryId, q));
    }

    @PatchMapping("/issues/{id}")
    public IssueDetail update(@PathVariable int id, @Valid @RequestBody UpdateIssueRequest req,
                              HttpServletRequest http) {
        SessionUser me = (SessionUser) http.getSession().getAttribute(AuthInterceptor.SESSION_KEY);
        return issues.adminUpdate(me, id, req);
    }

    // ---- dashboard and users

    @GetMapping("/dashboard")
    public DashboardStats dashboard() {
        return admin.dashboardStats();
    }

    @GetMapping("/users")
    public List<UserSummary> users() {
        return admin.users();
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable int id, HttpServletRequest http) {
        SessionUser me = (SessionUser) http.getSession().getAttribute(AuthInterceptor.SESSION_KEY);
        admin.deleteUser(me.userId(), id);
    }

    // ---- laboratories

    @GetMapping("/laboratories")
    public List<Laboratory> laboratories() {
        return admin.laboratories();
    }

    @PostMapping("/laboratories")
    @ResponseStatus(HttpStatus.CREATED)
    public Laboratory createLaboratory(@Valid @RequestBody LaboratoryRequest req) {
        return admin.createLaboratory(req);
    }

    @PutMapping("/laboratories/{id}")
    public Laboratory updateLaboratory(@PathVariable int id, @Valid @RequestBody LaboratoryRequest req) {
        return admin.updateLaboratory(id, req);
    }

    @PatchMapping("/laboratories/{id}/active")
    public Laboratory laboratoryActive(@PathVariable int id, @Valid @RequestBody ActiveRequest req) {
        return admin.setLaboratoryActive(id, req.active());
    }

    @DeleteMapping("/laboratories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLaboratory(@PathVariable int id) {
        admin.deleteLaboratory(id);
    }

    // ---- computers

    @GetMapping("/laboratories/{id}/computers")
    public List<ComputerUnit> computers(@PathVariable int id) {
        return admin.computers(id);
    }

    @PostMapping("/computers")
    @ResponseStatus(HttpStatus.CREATED)
    public ComputerUnit createComputer(@Valid @RequestBody ComputerRequest req) {
        return admin.createComputer(req);
    }

    @PutMapping("/computers/{id}")
    public ComputerUnit updateComputer(@PathVariable int id, @Valid @RequestBody ComputerRequest req) {
        return admin.updateComputer(id, req);
    }

    @PatchMapping("/computers/{id}/active")
    public ComputerUnit computerActive(@PathVariable int id, @Valid @RequestBody ActiveRequest req) {
        return admin.setComputerActive(id, req.active());
    }

    @DeleteMapping("/computers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComputer(@PathVariable int id) {
        admin.deleteComputer(id);
    }
}
