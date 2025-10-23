-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS pdfshare;
USE pdfshare;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS file_access;
DROP TABLE IF EXISTS file_comments;
DROP TABLE IF EXISTS files;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS tasks;

-- Users table (from UserAuth.java)
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('student', 'teacher') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Files table (from FileHandler.java)
CREATE TABLE files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    filedata LONGBLOB NOT NULL,
    uploaded_by VARCHAR(100) NOT NULL,  -- References users(email)
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_size BIGINT,
    mime_type VARCHAR(100),
    description TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    delete_time TIMESTAMP NULL,
    FOREIGN KEY (uploaded_by) REFERENCES users(email)
);

-- File access permissions
CREATE TABLE file_access (
    file_id INT,
    user_id INT,
    access_type ENUM('read', 'write', 'owner') DEFAULT 'read',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    granted_by INT,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (granted_by) REFERENCES users(user_id),
    PRIMARY KEY (file_id, user_id)
);

-- File comments/annotations
CREATE TABLE file_comments (
    comment_id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT,
    user_id INT,
    comment_text TEXT NOT NULL,
    comment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    page_number INT,  -- For PDF page-specific comments
    is_private BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ✅ Tasks table with Priority Support
CREATE TABLE tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status ENUM('pending', 'in_progress', 'completed') DEFAULT 'pending',
    priority ENUM('low', 'medium', 'high') DEFAULT 'medium',
    created_by VARCHAR(100),
    assigned_to VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date DATE,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (created_by) REFERENCES users(email),
    FOREIGN KEY (assigned_to) REFERENCES users(email)
);

-- Create indexes for better performance
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_file_upload_time ON files(upload_time);
CREATE INDEX idx_file_uploaded_by ON files(uploaded_by);
CREATE INDEX idx_file_comments_time ON file_comments(comment_time);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_assigned_to ON tasks(assigned_to);
CREATE INDEX idx_task_due_date ON tasks(due_date);
CREATE INDEX idx_task_priority ON tasks(priority);

-- Insert default admin user (change password in production)
INSERT INTO users (email, password, role) 
VALUES ('blessy', 'yourpassword', 'teacher');

-- Grant necessary permissions (adjust based on your DB user)
GRANT ALL PRIVILEGES ON pdfshare.* TO 'pdfshare'@'localhost';
FLUSH PRIVILEGES;
