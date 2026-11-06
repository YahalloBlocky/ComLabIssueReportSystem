package edu.cvsu.labissues.model;

import java.time.LocalDateTime;

/** How a problem was fixed. */
public record ResolutionEntry(int resolutionId, String resolvedByName, String actionTaken,
                              LocalDateTime resolvedAt) {
}
