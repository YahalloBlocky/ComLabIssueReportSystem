package edu.cvsu.labissues.model;

import java.util.List;

/** Everything the Admin dashboard needs, all counted from MySQL. */
public record DashboardStats(long totalReports, long openReports, long resolvedReports, long unprioritizedReports,
                             List<CountItem> byStatus, List<CountItem> byPriority,
                             List<CountItem> byCategory, List<CountItem> byLaboratory,
                             List<IssueRecord> recentReports) {
}
