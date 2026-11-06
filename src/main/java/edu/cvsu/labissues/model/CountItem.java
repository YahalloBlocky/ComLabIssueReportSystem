package edu.cvsu.labissues.model;

/** A label with a number, for dashboard charts (for example "Pending" -> 4). */
public record CountItem(String label, long count) {
}
