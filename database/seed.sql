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

