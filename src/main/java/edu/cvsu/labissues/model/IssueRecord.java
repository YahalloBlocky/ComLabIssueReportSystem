package edu.cvsu.labissues.model;

import java.time.LocalDateTime;

/**
 * One issue report with the names of everything it points to (reporter, laboratory,
 * computer, category, status, priority). Nullable fields: computerId/computerLabel
 * (whole-laboratory problem), priorityId/priority (not set yet), informantName.
 */
public record IssueRecord(
        int reportId, String referenceNo, String title, String description, String informantName,
        int reporterId, String reporterName, String reporterSchoolId, String reporterRole,
        int laboratoryId, String laboratoryName, String roomNumber,
        Integer computerId, String computerLabel,
        int categoryId, String category,
        int statusId, String status,
        Integer priorityId, String priority,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
}
