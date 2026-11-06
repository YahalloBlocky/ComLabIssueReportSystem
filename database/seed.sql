-- =====================================================================
-- SEED DATA  (run AFTER schema.sql)
--  * fixed lookup values (roles, categories, statuses, priorities)
--  * SAMPLE laboratories and computers (placeholders - the Admin can edit them in the system)
--  * demo accounts and sample reports so every screen has something to show
--
-- Demo accounts are stored with PLAIN-TEXT passwords (see README, 'Password storage').
-- ( ADMIN001 / Admin@123   INST001, INST002 / Instructor@123   2026100xx / Student@123 )
-- =====================================================================
USE lab_issue_db;

INSERT INTO roles (role_id, role_name) VALUES
    (1, 'Student'),
    (2, 'Instructor'),
    (3, 'Admin');

INSERT INTO issue_categories (category_id, category_name, description) VALUES
    (1, 'Hardware', 'Broken or missing equipment such as monitor, keyboard, mouse, or system unit'),
    (2, 'Software', 'Program errors, missing programs, or corrupted software'),
    (3, 'Network',  'No internet, slow internet, or no connection to the network'),
    (4, 'Other',    'Any problem that does not fit Hardware, Software, or Network (the reporter describes it)');

INSERT INTO issue_statuses (status_id, status_name) VALUES
    (1, 'Pending'),
    (2, 'In Progress'),
    (3, 'Resolved');

INSERT INTO priority_levels (priority_id, priority_name) VALUES
    (1, 'Low'),
    (2, 'Medium'),
    (3, 'High'),
    (4, 'Critical');

-- SAMPLE laboratories (replace with the real rooms from the Admin > Laboratories page)
INSERT INTO laboratories (laboratory_id, lab_name, building, room_number, is_active) VALUES
    (1, 'Computer Laboratory 1', 'Main Building', 'Room 201', 1),
    (2, 'Computer Laboratory 2', 'Main Building', 'Room 202', 1),
    (3, 'Computer Laboratory 3', 'Main Building', 'Room 203', 1);

-- 10 sample computers per laboratory: PC-01 ... PC-10
INSERT INTO computer_units (laboratory_id, unit_label, is_active)
SELECT l.laboratory_id, CONCAT('PC-', LPAD(n.i, 2, '0')), 1
FROM laboratories l
JOIN (SELECT 1 AS i UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n
ORDER BY l.laboratory_id, n.i;

-- Demo users (fictional people)
INSERT INTO users (user_id, role_id, school_id, first_name, last_name, email, password) VALUES
    (1, 3, 'ADMIN001',  'Edmund',  'Martines',  'edmundmartines@gmail.com',      '12345678'),
    (2, 2, 'INST001',   'Dan',   'Dandandan',      'dan4lyfe@gmail.com','DanlovesJovy'),
    (6, 1, '202610003', 'Rene',   'Baterbonia',     'renebaterbonia@gmail.com',    'Seacuts123');

-- ---------------------------------------------------------------------
-- Sample reports. Every report has a first history row (Pending).
-- ---------------------------------------------------------------------
-- Report 1: Resolved, High  (student Ana, Lab 1 / PC-03)
INSERT INTO issue_reports (report_id, reporter_id, laboratory_id, computer_id, category_id, status_id, priority_id, title, description, informant_name, created_at)
VALUES (1, 4, 1, (SELECT computer_id FROM computer_units WHERE laboratory_id = 1 AND unit_label = 'PC-03'), 1, 3, 3,
        'Monitor shows no display', 'The monitor stays black after pressing the power button. The system unit lights are on.', NULL,
        NOW() - INTERVAL 6 DAY);
INSERT INTO status_history (report_id, old_status_id, new_status_id, changed_by, remarks, changed_at) VALUES
    (1, NULL, 1, 4, 'Report submitted', NOW() - INTERVAL 6 DAY),
    (1, 1, 2, 1, 'Checking the cables', NOW() - INTERVAL 5 DAY),
    (1, 2, 3, 1, 'Fixed', NOW() - INTERVAL 4 DAY);
INSERT INTO resolutions (report_id, resolved_by, action_taken, resolved_at) VALUES
    (1, 1, 'Replaced the loose VGA cable and tested the monitor.', NOW() - INTERVAL 4 DAY);

-- Report 2: In Progress, Medium  (student Ben, Lab 2 / PC-05)
INSERT INTO issue_reports (report_id, reporter_id, laboratory_id, computer_id, category_id, status_id, priority_id, title, description, informant_name, created_at)
VALUES (2, 5, 2, (SELECT computer_id FROM computer_units WHERE laboratory_id = 2 AND unit_label = 'PC-05'), 2, 2, 2,
        'Java IDE will not open', 'The Java IDE closes right after it starts. This happens every time on this computer.', NULL,
        NOW() - INTERVAL 3 DAY);
INSERT INTO status_history (report_id, old_status_id, new_status_id, changed_by, remarks, changed_at) VALUES
    (2, NULL, 1, 5, 'Report submitted', NOW() - INTERVAL 3 DAY),
    (2, 1, 2, 1, 'Reinstalling the IDE', NOW() - INTERVAL 2 DAY);

-- Report 3: Pending, no priority yet  (instructor Maria, whole Lab 1, told by a student)
INSERT INTO issue_reports (report_id, reporter_id, laboratory_id, computer_id, category_id, status_id, priority_id, title, description, informant_name, created_at)
VALUES (3, 2, 1, NULL, 3, 1, NULL,
        'No internet connection in the laboratory', 'None of the computers can open websites during the class. The network icon shows no connection.', 'Juan Dela Cruz (sample)',
        NOW() - INTERVAL 1 DAY);
INSERT INTO status_history (report_id, old_status_id, new_status_id, changed_by, remarks, changed_at) VALUES
    (3, NULL, 1, 2, 'Report submitted', NOW() - INTERVAL 1 DAY);

-- Report 4: Pending, no priority yet  (student Carla, Lab 3 / PC-02)
INSERT INTO issue_reports (report_id, reporter_id, laboratory_id, computer_id, category_id, status_id, priority_id, title, description, informant_name, created_at)
VALUES (4, 6, 3, (SELECT computer_id FROM computer_units WHERE laboratory_id = 3 AND unit_label = 'PC-02'), 1, 1, NULL,
        'Keyboard keys are not working', 'The letters E and R do not type. Other keys are fine.', NULL,
        NOW() - INTERVAL 5 HOUR);
INSERT INTO status_history (report_id, old_status_id, new_status_id, changed_by, remarks, changed_at) VALUES
    (4, NULL, 1, 6, 'Report submitted', NOW() - INTERVAL 5 HOUR);

-- Report 5: Resolved, Critical  (instructor Jose, whole Lab 2)
INSERT INTO issue_reports (report_id, reporter_id, laboratory_id, computer_id, category_id, status_id, priority_id, title, description, informant_name, created_at)
VALUES (5, 3, 2, NULL, 3, 3, 4,
        'Network switch is down', 'The whole laboratory lost the connection before the exam. The switch lights are off.', NULL,
        NOW() - INTERVAL 8 DAY);
INSERT INTO status_history (report_id, old_status_id, new_status_id, changed_by, remarks, changed_at) VALUES
    (5, NULL, 1, 3, 'Report submitted', NOW() - INTERVAL 8 DAY),
    (5, 1, 2, 1, 'On my way to the laboratory', NOW() - INTERVAL 8 DAY + INTERVAL 1 HOUR),
    (5, 2, 3, 1, 'Network restored', NOW() - INTERVAL 8 DAY + INTERVAL 3 HOUR);
INSERT INTO resolutions (report_id, resolved_by, action_taken, resolved_at) VALUES
    (5, 1, 'Restarted the network switch and replaced its power adapter.', NOW() - INTERVAL 8 DAY + INTERVAL 3 HOUR);

-- Report 6: Pending, Low  (student Ana, Lab 2 / PC-08)
INSERT INTO issue_reports (report_id, reporter_id, laboratory_id, computer_id, category_id, status_id, priority_id, title, description, informant_name, created_at)
VALUES (6, 4, 2, (SELECT computer_id FROM computer_units WHERE laboratory_id = 2 AND unit_label = 'PC-08'), 2, 1, 1,
        'Wallpaper and display settings keep resetting', 'The display settings return to the default after each restart.', NULL,
        NOW() - INTERVAL 2 HOUR);
INSERT INTO status_history (report_id, old_status_id, new_status_id, changed_by, remarks, changed_at) VALUES
    (6, NULL, 1, 4, 'Report submitted', NOW() - INTERVAL 2 HOUR);
