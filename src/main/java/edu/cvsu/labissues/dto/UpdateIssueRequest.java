package edu.cvsu.labissues.dto;

import jakarta.validation.constraints.Size;

/** What the Admin can change on a report: priority and/or status (with a remark or the action taken). */
public record UpdateIssueRequest(
        Integer statusId,
        Integer priorityId,

        @Size(max = 255, message = "At most 255 characters.")
        String remarks,

        @Size(max = 2000, message = "At most 2000 characters.")
        String actionTaken) {
}
