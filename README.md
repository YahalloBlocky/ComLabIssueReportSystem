# Computer Laboratory Issue Reporting System

DCIT 24 Information Management final project (Cavite State University).
Java 17+ / Spring Boot 3.5 / Spring `JdbcTemplate` / MySQL 8 + plain HTML, CSS and JavaScript.

## What it does
* **Student / Instructor** - register, report a laboratory or computer problem (Instructors may add an *informant name*), see their own reports and their status history.
* **Admin (laboratory technician)** - sees every report, sets the **priority**, moves the status **Pending -> In Progress -> Resolved** (Resolved needs the action taken), manages laboratories and computer units, sees the dashboard and the user list.

The database follows the group's ERD: `roles, users, laboratories, computer_units, issue_categories, issue_statuses, priority_levels, issue_reports, status_history, resolutions`.

## Set up (once)
1. Install **JDK 17 or newer**, **Maven 3.9+**, and **MySQL 8.0.16+** (MySQL Workbench is handy).
2. In MySQL Workbench open and run, in this order:
   1. `database/schema.sql`  (creates database `lab_issue_db`; **drops it first if it exists**)
   2. `database/seed.sql`    (labs, computers, statuses, priorities, demo users and sample reports)
3. Tell the app your MySQL login with **environment variables** (never type the password into a file):

   Windows (PowerShell):
   ```
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_mysql_password"
   ```
   macOS / Linux:
   ```
   export DB_USERNAME=root
   export DB_PASSWORD=your_mysql_password
   ```
   Optional: `DB_URL` (default `jdbc:mysql://localhost:3306/lab_issue_db?...`).

## Run
In the folder that contains `pom.xml`:
```
mvn spring-boot:run
```
Wait for `Started LabIssuesApplication`, then open **http://localhost:8080**.
Check the database connection at http://localhost:8080/api/health (should say `"database":"UP"`).

## Demo accounts (not shown on the login page)
| Role | ID number | Password |
|---|---|---|
| Admin | ADMIN001 | Admin@123 |
| Instructor | INST001 / INST002 | Instructor@123 |
| Student | 202610001 / 202610002 / 202610003 | Student@123 |

## Password storage
By request, passwords are stored as **plain text** so they can be read in MySQL Workbench (`SELECT school_id, password FROM users;`). This is fine for a class demo with fake accounts, but **not safe for real users**: anyone who can open the database sees every password.
To switch to hashed (BCrypt) passwords, set the environment variable `PASSWORD_MODE=bcrypt` before starting the app and use only accounts created in that mode (the demo accounts then need re-seeding via `DemoAccountInitializer` after setting their password column to `PENDING_FIRST_RUN_HASH`).
If you created the database with the older seed, run `database/upgrade_plain_passwords.sql` once.

## Troubleshooting
| Problem | Fix |
|---|---|
| `Access denied for user` | `DB_USERNAME` / `DB_PASSWORD` not set in the **same** terminal that runs Maven. |
| `Unknown database 'lab_issue_db'` | Run `schema.sql` then `seed.sql`. |
| Login says wrong password for demo users | Start the app once after `seed.sql`; check the log for "Demo accounts are ready". |
| Port 8080 busy | `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` |
| `release version 17 not supported` | Your JDK is older than 17. |
| Quick API check | With the app running: `scripts/smoke-test.sh` |

## Important rules built in
* Status only moves forward; Resolved is final; resolving writes a `resolutions` row; every change writes `status_history`.
* Only the Admin sets priority. Students/Instructors can only see their own reports. Roles are enforced on the server (401 / 403), not just by hiding buttons.
* A computer is optional on a report (whole-laboratory problem). A person cannot file the same unresolved problem twice (409).
* Laboratories/computers that already have reports cannot be deleted - make them inactive.

## Honest verification notes
| Item | Status |
|---|---|
| Java sources type-check (`javac`) | Verified against hand-written stubs of the Spring/Jakarta APIs, **not** the real libraries |
| Frontend (login, register form rules, report form, my reports, admin tabs, resolve rule) | Tested in a real browser against a mock API |
| Real Spring Boot start-up, real MySQL, SQL scripts | **Not run** by the author of this package (no Maven Central / MySQL access when it was built). The first run on your machine is the real test. If something fails, copy the error message - most problems are the database login or step 2. |

## Known design note
The Database Design paper says a computer unit is mandatory on a report, while the ERD and business rules allow it to be empty. The system follows the ERD (optional). Change by making `computer_id` `NOT NULL` in `schema.sql` and validating it in `CreateIssueRequest`.

More: `LEARNING_GUIDE.md` (how the code is organised), `docs/API.md`, `docs/DATABASE.md`.
