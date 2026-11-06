# API (all JSON, session cookie login)
Errors: `{status, error, message, fieldErrors}`. 401 = not logged in, 403 = wrong role, 409 = business-rule conflict.

| Method & path | Who | Purpose |
|---|---|---|
| POST /api/auth/login | public | `{schoolId, password}` |
| POST /api/auth/register | public | Student or Instructor only |
| POST /api/auth/logout, GET /api/auth/me | logged in | |
| GET /api/health | public | database check |
| GET /api/lookups | logged in | categories, statuses, priorities, active labs |
| GET /api/laboratories/{id}/computers | logged in | active computers of a lab |
| POST /api/issues | Student, Instructor | submit a report |
| GET /api/issues/mine | Student, Instructor | own reports |
| GET /api/issues/{id} | owner or Admin | report + history + resolution |
| GET /api/admin/issues?statusId&priorityId(0=not set)&categoryId&laboratoryId&q | Admin | search all |
| PATCH /api/admin/issues/{id} | Admin | `{statusId, priorityId, remarks, actionTaken}` |
| GET /api/admin/dashboard, /api/admin/users | Admin | |
| GET/POST /api/admin/laboratories, PUT/DELETE /{id}, PATCH /{id}/active | Admin | |
| GET /api/admin/laboratories/{id}/computers, POST /api/admin/computers, PUT/DELETE /api/admin/computers/{id}, PATCH .../active | Admin | |
