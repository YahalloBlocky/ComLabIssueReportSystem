-- =====================================================================
-- Computer Laboratory Issue Reporting System  -  DATABASE SCHEMA
-- DCIT 24 Information Management  -  final design (10 tables)
--
-- Matches the group's ERD:
--   ROLE, USER, LABORATORY, COMPUTER_UNIT, ISSUE_CATEGORY, ISSUE_STATUS,
--   PRIORITY_LEVEL, ISSUE_REPORT, STATUS_HISTORY, RESOLUTION
--
-- WARNING: this script DROPS and re-creates the whole database.
-- Run it again any time you want to go back to a clean start.
-- Requires MySQL 8.0.16 or newer (so CHECK constraints are enforced).
-- =====================================================================

DROP DATABASE IF EXISTS lab_issue_db;
CREATE DATABASE lab_issue_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE lab_issue_db;

-- ---------------------------------------------------------------------
-- ROLE : Student, Instructor, Admin (the Admin is the laboratory technician)
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    role_id    INT          NOT NULL AUTO_INCREMENT,
    role_name  VARCHAR(30)  NOT NULL,
    PRIMARY KEY (role_id),
    UNIQUE KEY uq_roles_name (role_name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- USER : everyone who logs in. "password" stores a BCrypt hash, never the real password.
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id     INT           NOT NULL AUTO_INCREMENT,
    role_id     INT           NOT NULL,
    school_id   VARCHAR(20)   NOT NULL,
    first_name  VARCHAR(50)   NOT NULL,
    last_name   VARCHAR(50)   NOT NULL,
    email       VARCHAR(100)  NOT NULL,
    password    VARCHAR(255)  NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_school_id (school_id),
    UNIQUE KEY uq_users_email (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (role_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- LABORATORY : a computer laboratory room
-- ---------------------------------------------------------------------
CREATE TABLE laboratories (
    laboratory_id  INT          NOT NULL AUTO_INCREMENT,
    lab_name       VARCHAR(50)  NOT NULL,
    building       VARCHAR(50)  NOT NULL,
    room_number    VARCHAR(20)  NOT NULL,
    is_active      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (laboratory_id),
    UNIQUE KEY uq_lab_room (building, room_number),
    CONSTRAINT ck_lab_active CHECK (is_active IN (0, 1))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- COMPUTER_UNIT : one computer inside a laboratory
-- uq_computer_in_lab lets ISSUE_REPORT prove that a chosen computer really
-- belongs to the chosen laboratory (composite foreign key below).
-- ---------------------------------------------------------------------
CREATE TABLE computer_units (
    computer_id    INT          NOT NULL AUTO_INCREMENT,
    laboratory_id  INT          NOT NULL,
    unit_label     VARCHAR(30)  NOT NULL,
    asset_tag      VARCHAR(50)  NULL,
    is_active      TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (computer_id),
    UNIQUE KEY uq_unit_label_per_lab (laboratory_id, unit_label),
    UNIQUE KEY uq_asset_tag (asset_tag),
    UNIQUE KEY uq_computer_in_lab (computer_id, laboratory_id),
    CONSTRAINT fk_computer_lab FOREIGN KEY (laboratory_id) REFERENCES laboratories (laboratory_id),
    CONSTRAINT ck_computer_active CHECK (is_active IN (0, 1))
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Lookup tables: ISSUE_CATEGORY, ISSUE_STATUS, PRIORITY_LEVEL
-- The ORDER of status_id (1,2,3) and priority_id (1..4) is the order of the steps.
-- ---------------------------------------------------------------------
CREATE TABLE issue_categories (
    category_id    INT           NOT NULL AUTO_INCREMENT,
    category_name  VARCHAR(50)   NOT NULL,
    description    VARCHAR(255)  NULL,
    PRIMARY KEY (category_id),
    UNIQUE KEY uq_category_name (category_name)
) ENGINE=InnoDB;

CREATE TABLE issue_statuses (
    status_id    INT          NOT NULL AUTO_INCREMENT,
    status_name  VARCHAR(30)  NOT NULL,
    PRIMARY KEY (status_id),
    UNIQUE KEY uq_status_name (status_name)
) ENGINE=InnoDB;

CREATE TABLE priority_levels (
    priority_id    INT          NOT NULL AUTO_INCREMENT,
    priority_name  VARCHAR(30)  NOT NULL,
    PRIMARY KEY (priority_id),
    UNIQUE KEY uq_priority_name (priority_name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- ISSUE_REPORT : one reported problem (the ticket)
--   computer_id   is NULL when the problem affects the whole laboratory
--   priority_id   is NULL until the Admin sets it
--   informant_name is optional (an instructor can write who told them)
-- ---------------------------------------------------------------------
CREATE TABLE issue_reports (
    report_id       INT           NOT NULL AUTO_INCREMENT,
    reporter_id     INT           NOT NULL,
    laboratory_id   INT           NOT NULL,
    computer_id     INT           NULL,
    category_id     INT           NOT NULL,
    status_id       INT           NOT NULL,
    priority_id     INT           NULL,
    title           VARCHAR(150)  NOT NULL,
    description     TEXT          NOT NULL,
    informant_name  VARCHAR(100)  NULL,
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (report_id),
    KEY idx_reports_reporter (reporter_id),
    KEY idx_reports_status (status_id),
    KEY idx_reports_lab (laboratory_id),
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id)   REFERENCES users (user_id),
    CONSTRAINT fk_report_lab      FOREIGN KEY (laboratory_id) REFERENCES laboratories (laboratory_id),
    -- If computer_id is filled in, that computer must belong to the same laboratory.
    CONSTRAINT fk_report_computer FOREIGN KEY (computer_id, laboratory_id)
        REFERENCES computer_units (computer_id, laboratory_id),
    CONSTRAINT fk_report_category FOREIGN KEY (category_id)   REFERENCES issue_categories (category_id),
    CONSTRAINT fk_report_status   FOREIGN KEY (status_id)     REFERENCES issue_statuses (status_id),
    CONSTRAINT fk_report_priority FOREIGN KEY (priority_id)   REFERENCES priority_levels (priority_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- STATUS_HISTORY : one row for every status change (rows are only added)
-- old_status_id is NULL for the first row, written when the report is submitted.
-- ---------------------------------------------------------------------
CREATE TABLE status_history (
    history_id     INT           NOT NULL AUTO_INCREMENT,
    report_id      INT           NOT NULL,
    old_status_id  INT           NULL,
    new_status_id  INT           NOT NULL,
    changed_by     INT           NOT NULL,
    remarks        VARCHAR(255)  NULL,
    changed_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (history_id),
    KEY idx_history_report (report_id),
    CONSTRAINT fk_history_report FOREIGN KEY (report_id)     REFERENCES issue_reports (report_id),
    CONSTRAINT fk_history_old    FOREIGN KEY (old_status_id) REFERENCES issue_statuses (status_id),
    CONSTRAINT fk_history_new    FOREIGN KEY (new_status_id) REFERENCES issue_statuses (status_id),
    CONSTRAINT fk_history_user   FOREIGN KEY (changed_by)    REFERENCES users (user_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- RESOLUTION : how a problem was fixed (written by the Admin)
-- ---------------------------------------------------------------------
CREATE TABLE resolutions (
    resolution_id  INT       NOT NULL AUTO_INCREMENT,
    report_id      INT       NOT NULL,
    resolved_by    INT       NOT NULL,
    action_taken   TEXT      NOT NULL,
    resolved_at    DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (resolution_id),
    KEY idx_resolution_report (report_id),
    CONSTRAINT fk_resolution_report FOREIGN KEY (report_id)   REFERENCES issue_reports (report_id),
    CONSTRAINT fk_resolution_user   FOREIGN KEY (resolved_by) REFERENCES users (user_id)
) ENGINE=InnoDB;
