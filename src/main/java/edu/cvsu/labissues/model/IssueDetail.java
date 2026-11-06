package edu.cvsu.labissues.model;

import java.util.List;

/** Everything shown when a report is opened: the report, its status history and its resolution(s). */
public record IssueDetail(IssueRecord issue, List<HistoryEntry> history, List<ResolutionEntry> resolutions) {
}
