-- Drop existing tables if they exist
DROP TABLE IF EXISTS task_assignments;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    email VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create tasks table
CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_by VARCHAR(255) NOT NULL,
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (created_by) REFERENCES users(email)
);

-- Create task_assignments table for multiple assignees
CREATE TABLE task_assignments (
    task_id INT,
    assigned_to VARCHAR(255),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    completed_at TIMESTAMP NULL,
    PRIMARY KEY (task_id, assigned_to),
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(email)
);

-- Add indexes for better performance
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_created_by ON tasks(created_by);
CREATE INDEX idx_task_assignments_assigned_to ON task_assignments(assigned_to);

-- Migration: Move existing assignments to new table
INSERT INTO task_assignments (task_id, assigned_to, status, completed_at)
SELECT id, assigned_to, status, completed_at
FROM tasks 
WHERE assigned_to IS NOT NULL;

-- Remove old assigned_to column from tasks table
ALTER TABLE tasks DROP COLUMN assigned_to;