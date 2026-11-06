package edu.cvsu.labissues.model;

/** A computer unit with the number of reports made about it, for the Admin > Computers page. */
public record ComputerUnit(int computerId, int laboratoryId, String unitLabel, String assetTag,
                           boolean active, int reportCount) {
}
