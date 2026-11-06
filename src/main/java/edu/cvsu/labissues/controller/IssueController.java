package edu.cvsu.labissues.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.cvsu.labissues.dto.CreateIssueRequest;
import edu.cvsu.labissues.model.IssueDetail;
import edu.cvsu.labissues.model.IssueRecord;
import edu.cvsu.labissues.security.AuthInterceptor;
import edu.cvsu.labissues.security.RequireRole;
import edu.cvsu.labissues.security.SessionUser;
import edu.cvsu.labissues.service.IssueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/** Endpoints for Students and Instructors (reporters). */
@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issues;

    public IssueController(IssueService issues) {
        this.issues = issues;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole({SessionUser.ROLE_STUDENT, SessionUser.ROLE_INSTRUCTOR})
    public IssueDetail submit(@Valid @RequestBody CreateIssueRequest req, HttpServletRequest http) {
        return issues.submit(user(http), req);
    }

    @GetMapping("/mine")
    @RequireRole({SessionUser.ROLE_STUDENT, SessionUser.ROLE_INSTRUCTOR})
    public List<IssueRecord> mine(HttpServletRequest http) {
        return issues.mine(user(http));
    }

    /** Reporters may open only their own reports; the service checks that. */
    @GetMapping("/{id}")
    public IssueDetail detail(@PathVariable int id, HttpServletRequest http) {
        return issues.detailFor(user(http), id);
    }

    private SessionUser user(HttpServletRequest http) {
        return (SessionUser) http.getSession().getAttribute(AuthInterceptor.SESSION_KEY);
    }
}
