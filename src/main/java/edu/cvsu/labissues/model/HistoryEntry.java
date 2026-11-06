package edu.cvsu.labissues.model;

import java.time.LocalDateTime;

/** One line of the status history timeline. oldStatus is null on the first line. */
public record HistoryEntry(int historyId, String oldStatus, String newStatus,
                           String changedByName, String changedByRole,
                           String remarks, LocalDateTime changedAt) {
}
