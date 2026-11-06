# Learning guide: how this project is built

## The layers (follow one request from top to bottom)
Browser (`static/*.html, js/*.js`) -> **Controller** (HTTP in/out) -> **Service** (business rules) -> **Repository** (SQL with `JdbcTemplate`) -> **MySQL**.

| Layer | Package | Job |
|---|---|---|
| controller | `edu.cvsu.labissues.controller` | Reads the request, checks `@Valid`, calls a service, returns JSON. No SQL here. |
| service | `...service` | Rules: forward-only status, duplicate check, informant only for Instructors, `@Transactional` for multi-step changes. |
| repository | `...repository` | Only SQL. Every value uses a `?` placeholder, so SQL injection is not possible. |
| model / dto | `...model`, `...dto` | Records: model = what we send, dto = what we receive (with validation annotations). |
| security | `...security` | `AuthInterceptor` + `@RequireRole` give 401/403 before a controller runs. |
| exception | `...exception` | `ApiException` + `GlobalExceptionHandler` turn errors into clean JSON. |

## Example: Admin marks a report Resolved
1. `admin.js` sends `PATCH /api/admin/issues/5`.
2. `AuthInterceptor` checks the session and that the role is Admin.
3. `AdminController.update` -> `IssueService.adminUpdate`.
4. The service loads the report, refuses if already Resolved or going backwards, requires `actionTaken`.
5. In **one transaction**: `UPDATE issue_reports`, `INSERT status_history`, `INSERT resolutions`.
6. The updated report comes back as JSON and the page refreshes the list.

## Ideas to understand / defend
* **BCrypt**: passwords are hashed with a random salt; we can check a password but never read it back.
* **Why the server checks roles**: anyone can edit the HTML/JS in their browser, so hidden buttons are not security.
* **Composite foreign key**: stops a report mixing Lab 1 with a computer from Lab 2.
* **`<=>`**: NULL-safe equals used for the duplicate check because `computer_id` can be NULL.
* **Status history vs. status**: `issue_reports.status_id` is the current state; `status_history` is the diary of every change (who, when, why).
