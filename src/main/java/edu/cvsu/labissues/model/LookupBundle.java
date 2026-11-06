package edu.cvsu.labissues.model;

import java.util.List;

/** All dropdown data in one response (the browser asks once). */
public record LookupBundle(List<Option> categories, List<Option> statuses, List<Option> priorities,
                           List<LabOption> laboratories) {
}
