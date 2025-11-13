-- StudySync Database Schema
-- Generated: 2025-11-04
-- Compatible with MariaDB 10.4+

-- Create the database
DROP DATABASE IF EXISTS pdfshare;
CREATE DATABASE pdfshare;
USE pdfshare;

-- Drop tables in reverse dependency order
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS study_sessions;
DROP TABLE IF EXISTS pomodoro_settings;
DROP TABLE IF EXISTS file_comments;
DROP TABLE IF EXISTS file_access;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS study_groups;
DROP TABLE IF EXISTS branches;
SET FOREIGN_KEY_CHECKS = 1;

-- Branches table
CREATE TABLE branches (
    branch_code VARCHAR(10) PRIMARY KEY,
    branch_name VARCHAR(50) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_branch_name (branch_name)
) ENGINE=InnoDB;

-- Study Groups table
CREATE TABLE study_groups (
    group_code VARCHAR(10) PRIMARY KEY,
    group_name VARCHAR(50) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_group_name (group_name)
) ENGINE=InnoDB;

-- Insert required branches first
INSERT INTO branches (branch_code, branch_name, description) VALUES 
('CSE-AI', 'CSE AI', 'Computer Science and Engineering - Artificial Intelligence'),
('AIDS', 'AIDS', 'Artificial Intelligence and Data Science'),
('CSE-CY', 'CSE CY', 'Computer Science and Engineering - Cybersecurity'),
('CSE', 'CSE', 'Computer Science and Engineering'),
('EEE', 'EEE', 'Electrical and Electronics Engineering'),
('ECE', 'ECE', 'Electronics and Communication Engineering'),
('MECH', 'MECH', 'Mechanical Engineering'),
('CIVIL', 'CIVIL', 'Civil Engineering');

-- Insert study groups
INSERT INTO study_groups (group_code, group_name, description) VALUES
('GRP-A', 'Group A', 'First Study Group'),
('GRP-B', 'Group B', 'Second Study Group'),
('GRP-C', 'Group C', 'Third Study Group');

-- Users table
CREATE TABLE users (
    email VARCHAR(100) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role ENUM('student', 'teacher') NOT NULL,
    branch_code VARCHAR(10) NULL,
    current_semester INT NULL,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_code) REFERENCES branches(branch_code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

DELIMITER //

CREATE TRIGGER users_before_insert BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    IF NEW.current_semester IS NOT NULL AND (NEW.current_semester < 1 OR NEW.current_semester > 8) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Current semester must be between 1 and 8';
    END IF;
    
    IF (NEW.role = 'student' AND (NEW.branch_code IS NULL OR NEW.current_semester IS NULL)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Students must have branch and semester';
    END IF;
    
    IF (NEW.role = 'teacher' AND (NEW.branch_code IS NOT NULL OR NEW.current_semester IS NOT NULL)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Teachers cannot have branch or semester';
    END IF;
END//

CREATE TRIGGER users_before_update BEFORE UPDATE ON users
FOR EACH ROW
BEGIN
    IF NEW.current_semester IS NOT NULL AND (NEW.current_semester < 1 OR NEW.current_semester > 8) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Current semester must be between 1 and 8';
    END IF;
    
    IF (NEW.role = 'student' AND (NEW.branch_code IS NULL OR NEW.current_semester IS NULL)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Students must have branch and semester';
    END IF;
    
    IF (NEW.role = 'teacher' AND (NEW.branch_code IS NOT NULL OR NEW.current_semester IS NOT NULL)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Teachers cannot have branch or semester';
    END IF;
END//

DELIMITER ;

-- Default users for testing
INSERT INTO users (email, password, role, branch_code, current_semester) VALUES 
('teacher@pdfshare.com', 'teach123', 'teacher', NULL, NULL),
('student@pdfshare.com', 'stud123', 'student', 'CSE-AI', 3);

-- Subjects table
CREATE TABLE subjects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    group_code VARCHAR(10) NOT NULL,
    semester INT NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_subject_semester CHECK (semester BETWEEN 1 AND 8),
    UNIQUE KEY uk_subject_unique (branch_code, semester, course_code),
    FOREIGN KEY (branch_code) REFERENCES branches(branch_code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    FOREIGN KEY (group_code) REFERENCES study_groups(group_code)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(email)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Files table
CREATE TABLE files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_tag_id VARCHAR(20) NOT NULL UNIQUE,
    filename VARCHAR(255) NOT NULL,
    filedata LONGBLOB NOT NULL,
    subject_id INT NOT NULL,
    uploaded_by VARCHAR(100) NOT NULL,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_size BIGINT GENERATED ALWAYS AS (LENGTH(filedata)) STORED,
    is_deleted BOOLEAN DEFAULT FALSE,
    delete_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(email)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- File Access Control
CREATE TABLE file_access (
    file_id INT,
    user_id VARCHAR(100),
    access_type ENUM('read', 'write', 'owner') DEFAULT 'read',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (file_id, user_id),
    FOREIGN KEY (file_id) REFERENCES files(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(email)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(email)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- File Comments
CREATE TABLE file_comments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT NOT NULL,
    user_id VARCHAR(100) NOT NULL,
    comment_text TEXT NOT NULL,
    comment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_private BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES files(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(email)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Tasks table
CREATE TABLE tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('pending', 'in_progress', 'completed') DEFAULT 'pending',
    priority ENUM('low', 'medium', 'high') DEFAULT 'medium',
    created_by VARCHAR(100) NOT NULL,
    assigned_to VARCHAR(100),
    branch_code VARCHAR(10),
    semester INT,
    due_date DATE,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_task_semester CHECK (semester IS NULL OR semester BETWEEN 1 AND 8),
    FOREIGN KEY (created_by) REFERENCES users(email)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(email)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    FOREIGN KEY (branch_code) REFERENCES branches(branch_code)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Study Schedules
CREATE TABLE study_schedules (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(100) NOT NULL,
    subject_id INT NOT NULL,
    schedule_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(email)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_time_order CHECK (end_time > start_time)
) ENGINE=InnoDB;

-- Pomodoro Settings
CREATE TABLE pomodoro_settings (
    user_id VARCHAR(100) PRIMARY KEY,
    focus_duration INT DEFAULT 25,
    break_duration INT DEFAULT 5,
    long_break_duration INT DEFAULT 15,
    sessions_until_long_break INT DEFAULT 4,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_focus_duration CHECK (focus_duration BETWEEN 1 AND 120),
    CONSTRAINT chk_break_duration CHECK (break_duration BETWEEN 1 AND 60),
    CONSTRAINT chk_long_break_duration CHECK (long_break_duration BETWEEN 1 AND 120),
    CONSTRAINT chk_sessions CHECK (sessions_until_long_break BETWEEN 1 AND 10),
    FOREIGN KEY (user_id) REFERENCES users(email)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Study Sessions
CREATE TABLE study_sessions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(100) NOT NULL,
    subject_id INT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_minutes INT GENERATED ALWAYS AS 
        (TIMESTAMPDIFF(MINUTE, start_time, IFNULL(end_time, start_time))) STORED,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(email)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Create optimized indexes
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_branch_sem ON users(branch_code, current_semester);
CREATE INDEX idx_subjects_filters ON subjects(branch_code, semester, group_code);
CREATE INDEX idx_files_uploaded ON files(uploaded_by, upload_time);
CREATE INDEX idx_files_subject ON files(subject_id);
CREATE INDEX idx_file_comments_user ON file_comments(user_id, comment_time);
CREATE INDEX idx_tasks_assigned ON tasks(assigned_to, status);
CREATE INDEX idx_tasks_filters ON tasks(branch_code, semester, due_date);
CREATE INDEX idx_schedules_user ON study_schedules(user_id, schedule_date);
CREATE INDEX idx_sessions_user ON study_sessions(user_id, start_time);
