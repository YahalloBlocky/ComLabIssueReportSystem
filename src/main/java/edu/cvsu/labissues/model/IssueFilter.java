package edu.cvsu.labissues.model;

/** Optional search conditions. A null value means "do not filter on this". priorityId 0 = "not set yet". */
public record IssueFilter(Integer reporterId, Integer statusId, Integer priorityId,
                          Integer categoryId, Integer laboratoryId, String q) {
}
