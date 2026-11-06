package edu.cvsu.labissues.model;

/** A laboratory with its counters, for the Admin > Laboratories page. */
public record Laboratory(int laboratoryId, String labName, String building, String roomNumber,
                         boolean active, int computerCount, int reportCount) {
}
