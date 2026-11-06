# Database
10 tables, see `database/schema.sql`. Key points:
* `users.role_id -> roles`; `issue_reports.reporter_id -> users` (reporter is a Student or Instructor).
* Order of `issue_statuses.status_id` (Pending 1, In Progress 2, Resolved 3) and `priority_levels.priority_id` (Low..Critical) is the order used by the app - no separate sort column.
* `issue_reports(computer_id, laboratory_id)` has a composite foreign key to `computer_units(computer_id, laboratory_id)`, so a report can never name a computer from a different laboratory. `computer_id` may be NULL.
* `priority_id` is NULL until the Admin sets it.
* `status_history` keeps every change (first row: old status NULL -> Pending). `resolutions` keeps how a report was fixed.
* Reference number `ICL-00001` is calculated from `report_id` in SQL (not stored).
