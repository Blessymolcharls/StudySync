package gui;

/**
 * TaskManager - Advanced Task Management Interface
 * 
 * This class provides a comprehensive GUI for managing academic tasks in StudySync.
 * It implements role-based task management with different privileges for teachers
 * and students.
 * 
 * Key Features:
 * 1. Task Creation & Management
 *    - Create new tasks with detailed information
 *    - Edit existing task details
 *    - Delete tasks (teachers only)
 *    - Multiple student assignment support
 * 
 * 2. Role-based Access Control
 *    - Teachers: Create, assign, edit, and delete tasks
 *    - Students: View assigned tasks, update status
 *    - Task visibility rules for privacy
 * 
 * 3. Task Filtering & Search
 *    - Filter by status (Pending/In Progress/Completed)
 *    - Search by title, description, or priority
 *    - Sort by various criteria
 * 
 * 4. Visual Features
 *    - Priority-based color coding (High/Medium/Low)
 *    - Status indicators
 *    - User-friendly date selection
 *    - Multi-select assignment interface
 * 
 * 5. Status Management
 *    - Track task progress
 *    - Update completion status
 *    - Monitor due dates
 * 
 * Database Integration:
 * - Uses task_assignments table for multiple assignees
 * - Maintains task history and status
 * - Efficient query optimization
 * 
 * @see DBConnection
 * @see Dashboard
 */

import db.DBConnection;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskManager extends JPanel {
    // ===== Component Declaration =====
    private JTable taskTable;                    // Main table to display tasks
    private DefaultTableModel tableModel;        // Data model for the task table
    private String currentUserEmail;             // Currently logged in user's email
    private String userRole;                     // User's role (teacher/student)
    private JComboBox<String> statusFilter;      // Dropdown for filtering by status
    private JTextField searchField;              // Search box for finding tasks

    /**
     * Constructor: Initializes the Task Management Interface
     * 
     * @param userEmail Email of the logged-in user
     * @param role Role of the user (teacher/student)
     */
    public TaskManager(String userEmail, String role) {
        // Store user information for access control
        this.currentUserEmail = userEmail;
        this.userRole = role;

        // Setup main layout with proper spacing
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Control panel
        JPanel controlPanel = new JPanel(new BorderLayout(5, 0));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchField.setToolTipText("Search tasks...");
        JButton searchBtn = new JButton("🔍 Search");
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusFilter = new JComboBox<>(new String[]{"All", "Pending", "In Progress", "Completed"});
        filterPanel.add(new JLabel("Status: "));
        filterPanel.add(statusFilter);

        controlPanel.add(searchPanel, BorderLayout.WEST);
        controlPanel.add(filterPanel, BorderLayout.EAST);

        // ===== Table Configuration =====
        // Define columns for task properties
        String[] columns = {"ID", "Title", "Description", "Priority", "Status", "Assigned To", "Due Date", "Created By"};
        
        // Create table model with non-editable cells
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent direct editing in table
            }
        };
        
        // Initialize table with custom settings
        taskTable = new JTable(tableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow only one selection
        taskTable.setRowHeight(25); // Increase row height for better readability

        // Color coding based on priority
        taskTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String priority = (String) table.getValueAt(row, 3);
                if (!isSelected) {
                    switch (priority.toUpperCase()) {
                        case "HIGH" -> c.setBackground(new Color(255, 102, 102)); // red
                        case "MEDIUM" -> c.setBackground(new Color(255, 204, 102)); // orange
                        case "LOW" -> c.setBackground(new Color(153, 255, 153)); // green
                        default -> c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(new Color(180, 200, 255));
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskTable);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("➕ New Task");
        JButton editBtn = new JButton("✏️ Edit");
        JButton deleteBtn = new JButton("🗑️ Delete");
        JButton markCompleteBtn = new JButton("✅ Mark Complete");

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        if ("teacher".equalsIgnoreCase(userRole)) buttonPanel.add(deleteBtn);
        buttonPanel.add(markCompleteBtn);

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        refreshTaskList();

        // Actions
        addBtn.addActionListener(e -> showTaskDialog(null));
        editBtn.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row != -1) {
                int taskId = (int) taskTable.getValueAt(row, 0);
                showTaskDialog(taskId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to edit");
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row != -1) {
                int taskId = (int) taskTable.getValueAt(row, 0);
                deleteTask(taskId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to delete");
            }
        });

        markCompleteBtn.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row != -1) {
                int taskId = (int) taskTable.getValueAt(row, 0);
                markTaskComplete(taskId);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to mark complete");
            }
        });

        searchBtn.addActionListener(e -> searchTasks());
        searchField.addActionListener(e -> searchTasks());
        statusFilter.addActionListener(e -> refreshTaskList());
    }

    // ---------------- Load Tasks ----------------
    private void refreshTaskList() {
        tableModel.setRowCount(0);
        String statusFilterValue = (String) statusFilter.getSelectedItem();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT t.* FROM tasks t WHERE 1=1 ";

            if ("student".equalsIgnoreCase(userRole)) {
                // Students see:
                // 1. Tasks they created
                // 2. Tasks assigned to them
                sql += "AND (t.created_by = ? OR t.assigned_to = ?) ";
            } else {
                // Teachers see: 
                // 1. Tasks they created
                sql += "AND t.created_by = ? ";
            }

            if (!"All".equals(statusFilterValue)) {
                sql += "AND t.status = ? ";
            }

            sql += "GROUP BY t.id ORDER BY t.created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);

            int idx = 1;
            if ("student".equalsIgnoreCase(userRole)) {
                stmt.setString(idx++, currentUserEmail); // created_by
                stmt.setString(idx++, currentUserEmail); // assigned_to
            } else {
                stmt.setString(idx++, currentUserEmail); // created_by
            }
            if (!"All".equals(statusFilterValue))
                stmt.setString(idx, statusFilterValue.toLowerCase().replace(" ", "_"));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("status").replace("_", " ").toUpperCase(),
                        rs.getString("assigned_to"),
                        rs.getDate("due_date"),
                        rs.getString("created_by")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage());
        }
    }

    // ---------------- Task Dialog ----------------
    private void showTaskDialog(Integer taskId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                taskId == null ? "New Task" : "Edit Task", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Pending", "In Progress", "Completed"});
        JTextField dueDateField = new JTextField(10);
        JComboBox<String> assigneeBox = new JComboBox<>();
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"HIGH", "MEDIUM", "LOW"});

        if ("student".equalsIgnoreCase(userRole)) {
            // Students can only assign tasks to themselves
            assigneeBox.addItem(currentUserEmail);
            assigneeBox.setSelectedIndex(0);
            assigneeBox.setEnabled(false);
        } else {
            loadUsers(assigneeBox);
        }


        // Add fields
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; panel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(descArea), gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; panel.add(priorityBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; panel.add(statusBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Due Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; panel.add(dueDateField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Assign To:"), gbc);
        gbc.gridx = 1; panel.add(assigneeBox, gbc);

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveBtn); buttonPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; panel.add(buttonPanel, gbc);

        // Load existing task
        if (taskId != null) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT * FROM tasks WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, taskId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    titleField.setText(rs.getString("title"));
                    descArea.setText(rs.getString("description"));
                    priorityBox.setSelectedItem(rs.getString("priority").toUpperCase());
                    statusBox.setSelectedItem(rs.getString("status").replace("_", " "));
                    Date dueDate = rs.getDate("due_date");
                    if (dueDate != null)
                        dueDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(dueDate));
                    
                    // Load assigned user
                    String assignedTo = rs.getString("assigned_to");
                    if (assignedTo != null) {
                        assigneeBox.setSelectedItem(assignedTo);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog, "Error loading task: " + e.getMessage());
            }
        }

        // Save action
        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();
            String priority = priorityBox.getSelectedItem().toString();
            String status = statusBox.getSelectedItem().toString().toLowerCase().replace(" ", "_");
            String dueDate = dueDateField.getText().trim();
            String assignee = (String) assigneeBox.getSelectedItem();
            
            if (title.isEmpty() || description.isEmpty() || dueDate.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields");
                return;
            }

            if (assignee == null) {
                JOptionPane.showMessageDialog(dialog, "Please select an assignee");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql;
                
                if (taskId == null) {
                    sql = "INSERT INTO tasks (title, description, priority, status, created_by, due_date, assigned_to) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    stmt.setString(3, priority);
                    stmt.setString(4, status);
                    stmt.setString(5, currentUserEmail);
                    stmt.setString(6, dueDate);
                    stmt.setString(7, assignee);
                    stmt.executeUpdate();
                } else {
                    sql = "UPDATE tasks SET title=?, description=?, priority=?, status=?, due_date=?, assigned_to=? WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    stmt.setString(3, priority);
                    stmt.setString(4, status);
                    stmt.setString(5, dueDate);
                    stmt.setString(6, assignee);
                    stmt.setInt(7, taskId);
                    stmt.executeUpdate();
                }
                
                dialog.dispose();
                refreshTaskList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error saving task: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void loadUsers(JComboBox<String> assigneeBox) {
        try (Connection conn = DBConnection.getConnection()) {
            assigneeBox.removeAllItems();
            String sql = "SELECT email FROM users";
            if ("teacher".equalsIgnoreCase(userRole)) {
                sql += " WHERE role = 'student'";
            }
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                assigneeBox.addItem(rs.getString("email"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
    }

    private void deleteTask(int taskId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this task?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM tasks WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, taskId);
                stmt.executeUpdate();
                refreshTaskList();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting task: " + e.getMessage());
            }
        }
    }

    private void markTaskComplete(int taskId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE tasks SET status='completed', completed_at=CURRENT_TIMESTAMP WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, taskId);
            stmt.executeUpdate();
            refreshTaskList();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage());
        }
    }

// ---------------- Search Tasks ----------------
    private void searchTasks() {
        String keyword = searchField.getText().trim().toLowerCase();
        String statusFilterValue = (String) statusFilter.getSelectedItem();
        tableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM tasks WHERE 1=1 ";

            if ("student".equalsIgnoreCase(userRole)) {
                // Students see:
                // 1. Tasks they created
                // 2. Tasks assigned to them
                sql += "AND (created_by = ? OR assigned_to = ?) ";
            } else {
                // Teachers see: 
                // 1. Tasks they created
                sql += "AND created_by = ? ";
            }

            // Apply search filter
            if (!keyword.isEmpty()) {
                sql += "AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ? OR LOWER(priority) LIKE ? OR LOWER(status) LIKE ?)";
            }

            // Apply status filter
            if (!"All".equals(statusFilterValue)) {
                sql += " AND status = ?";
            }

            sql += " ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);

            int idx = 1;
            if ("student".equalsIgnoreCase(userRole)) {
                stmt.setString(idx++, currentUserEmail); // created_by
                stmt.setString(idx++, currentUserEmail); // assigned_to
            } else {
                stmt.setString(idx++, currentUserEmail); // created_by
            }

            if (!keyword.isEmpty()) {
                String searchValue = "%" + keyword + "%";
                stmt.setString(idx++, searchValue);
                stmt.setString(idx++, searchValue);
                stmt.setString(idx++, searchValue);
                stmt.setString(idx++, searchValue);
            }
            if (!"All".equals(statusFilterValue))
                stmt.setString(idx++, statusFilterValue.toLowerCase().replace(" ", "_"));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("priority"),
                        rs.getString("status").replace("_", " ").toUpperCase(),
                        rs.getString("assigned_to"),
                        rs.getDate("due_date"),
                        rs.getString("created_by")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error searching tasks: " + e.getMessage());
        }
    }
    public static void showTasks(String userEmail, String role) {
        SwingUtilities.invokeLater(() -> {
            TaskManager tm = new TaskManager(userEmail, role);
            tm.setVisible(true);
        });
    }
}
