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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskManager extends JPanel {
    // ===== Component Declaration =====
    private JTable taskTable;                    // Main table to display tasks
    private DefaultTableModel tableModel;        // Data model for the task table
    private String currentUserEmail;             // Currently logged in user's email
    private String userRole;                     // User's role (teacher/student)
    private String userBranch;                   // User's branch (for students)
    private Integer userSemester;                // User's semester (for students)
    private JComboBox<String> statusFilter;      // Dropdown for filtering by status
    private JComboBox<String> branchFilter;      // Dropdown for filtering by branch
    private JComboBox<Integer> semesterFilter;   // Dropdown for filtering by semester
    private JTextField searchField;              // Search box for finding tasks

    /**
     * Constructor: Initializes the Task Management Interface
     * 
     * @param userEmail Email of the logged-in user
     * @param role Role of the user (teacher/student)
     * @param branch User's branch (for students)
     * @param semester User's semester (for students)
     */
    public TaskManager(String userEmail, String role, String branch, Integer semester) {
        // Store user information for access control
        this.currentUserEmail = userEmail;
        this.userRole = role;

        // Setup main layout with proper spacing
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Control panel
        JPanel controlPanel = new JPanel(new BorderLayout(5, 0));
        
        // Search panel on the left
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchField.setToolTipText("Search tasks...");
        JButton searchBtn = new JButton("🔍 Search");
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // Filter panel using GridBagLayout for better spacing
        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.insets = new Insets(0, 5, 0, 5);
        filterGbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Status filter with expanded width
        statusFilter = new JComboBox<>(new String[]{"All", "Pending", "In Progress", "Completed"});
        filterGbc.weightx = 0;
        filterPanel.add(new JLabel("Status: "), filterGbc);
        filterGbc.weightx = 1.0;
        filterPanel.add(statusFilter, filterGbc);

        // Store branch and semester for filtering
        if ("teacher".equalsIgnoreCase(role)) {
            // Initialize filters but don't add them to UI
            branchFilter = new JComboBox<>();
            branchFilter.addItem("All Branches");
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT branch_code, branch_name FROM branches WHERE is_active = TRUE ORDER BY branch_name";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    branchFilter.addItem(rs.getString("branch_name"));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading branches: " + e.getMessage());
            }

            semesterFilter = new JComboBox<>();
            semesterFilter.addItem(0); // 0 means "All Semesters"
            for (int i = 1; i <= 8; i++) {
                semesterFilter.addItem(i);
            }
        } else {
            // For students, set their branch and semester
            this.userBranch = branch;
            this.userSemester = semester;
        }

        // Use GridBagLayout for better control over component sizes
        JPanel mainControlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.HORIZONTAL;
        mainGbc.weightx = 0.7; // Give more space to search
        mainGbc.gridx = 0;
        mainControlPanel.add(searchPanel, mainGbc);
        
        mainGbc.weightx = 0.3; // Less space for status filter
        mainGbc.gridx = 1;
        mainControlPanel.add(filterPanel, mainGbc);
        
        controlPanel.add(mainControlPanel, BorderLayout.CENTER);

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
        String branchFilterValue = "teacher".equalsIgnoreCase(userRole) ? 
            (String) branchFilter.getSelectedItem() : userBranch;
        Integer semesterFilterValue = "teacher".equalsIgnoreCase(userRole) ? 
            (Integer) semesterFilter.getSelectedItem() : userSemester;

        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT t.*, b.branch_name " +
                "FROM tasks t " +
                "LEFT JOIN branches b ON t.branch_code = b.branch_code " +
                "WHERE 1=1 ");

            if ("student".equalsIgnoreCase(userRole)) {
                // Students see:
                // 1. Tasks they created
                // 2. Tasks assigned to their branch and semester (but only if created by teachers)
                sql.append("AND (t.created_by = ? OR (t.branch_code = (SELECT branch_code FROM branches WHERE branch_name = ?) " +
                          "AND t.semester = ? AND EXISTS (SELECT 1 FROM users u WHERE u.email = t.created_by AND u.role = 'teacher'))) ");
            } else {
                // Teachers see filtered tasks
                sql.append("AND t.created_by = ? ");
                if (!"All Branches".equals(branchFilterValue)) {
                    sql.append("AND t.branch_code = (SELECT branch_code FROM branches WHERE branch_name = ?) ");
                }
                if (semesterFilterValue != 0) {
                    sql.append("AND t.semester = ? ");
                }
            }

            if (!"All".equals(statusFilterValue)) {
                sql.append("AND t.status = ? ");
            }

            sql.append("ORDER BY t.created_at DESC");
            PreparedStatement stmt = conn.prepareStatement(sql.toString());

            int idx = 1;
            if ("student".equalsIgnoreCase(userRole)) {
                stmt.setString(idx++, currentUserEmail);
                stmt.setString(idx++, userBranch);
                stmt.setInt(idx++, userSemester);
            } else {
                stmt.setString(idx++, currentUserEmail);
                if (!"All Branches".equals(branchFilterValue)) {
                    stmt.setString(idx++, branchFilterValue);
                }
                if (semesterFilterValue != 0) {
                    stmt.setInt(idx++, semesterFilterValue);
                }
            }
            if (!"All".equals(statusFilterValue)) {
                stmt.setString(idx, statusFilterValue.toLowerCase().replace(" ", "_"));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String priority = rs.getString("priority");
                String status = rs.getString("status").replace("_", " ").toUpperCase();
                String branchName = rs.getString("branch_name");
                int semester = rs.getInt("semester");
                String assignedTo = branchName + " (Sem " + semester + ")";
                Date dueDate = rs.getDate("due_date");
                String createdBy = rs.getString("created_by");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dueDateStr = dueDate != null ? sdf.format(dueDate) : "";
                
                tableModel.addRow(new Object[]{
                    id, title, description, priority, status, assignedTo, dueDateStr, createdBy
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
        dialog.setSize(450, 500); // Increased size for better spacing
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); // Increased vertical spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        JTextField titleField = new JTextField(20);
        JTextArea descArea = new JTextArea(3, 20);
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Pending", "In Progress", "Completed"});
        JTextField dueDateField = new JTextField(10);
        JComboBox<String> priorityBox = new JComboBox<>(new String[]{"HIGH", "MEDIUM", "LOW"});
        
        // Branch and semester selection for teachers
        JComboBox<String> branchSelect = new JComboBox<>();
        JComboBox<Integer> semesterSelect = new JComboBox<>();

        // Configure branch and semester for task assignment
        if ("teacher".equalsIgnoreCase(userRole)) {
            // Load branches
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT branch_code, branch_name FROM branches WHERE is_active = TRUE ORDER BY branch_name";
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    branchSelect.addItem(rs.getString("branch_name"));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(panel, "Error loading branches: " + e.getMessage());
            }

            // Add semesters 1-8
            for (int i = 1; i <= 8; i++) {
                semesterSelect.addItem(i);
            }
        } else {
            // Students can only create tasks for their own branch/semester
            branchSelect.addItem(userBranch);
            branchSelect.setEnabled(false);
            semesterSelect.addItem(userSemester);
            semesterSelect.setEnabled(false);
        }

        // Configure component sizes
        Dimension fieldSize = new Dimension(250, 25);
        titleField.setPreferredSize(fieldSize);
        priorityBox.setPreferredSize(fieldSize);
        statusBox.setPreferredSize(fieldSize);
        dueDateField.setPreferredSize(fieldSize);
        
        // Add fields with proper spacing
        gbc.weightx = 0.0; gbc.gridwidth = 1;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(titleField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descArea);
        scrollPane.setPreferredSize(new Dimension(250, 100));
        panel.add(scrollPane, gbc);

        // Priority
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        panel.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(priorityBox, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(statusBox, gbc);

        // Due Date
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        panel.add(new JLabel("Due Date (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(dueDateField, gbc);

        // Branch and Semester for all users
        gbc.gridy = 5;
        
        // Branch selection
        gbc.gridx = 0; gbc.weightx = 0.0;
        panel.add(new JLabel("Branch:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        branchSelect.setPreferredSize(fieldSize);
        panel.add(branchSelect, gbc);

        // Semester selection
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.0;
        panel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        semesterSelect.setPreferredSize(fieldSize);
        panel.add(semesterSelect, gbc);

        // Button panel with proper spacing
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 8, 5); // Add extra top padding for buttons
        panel.add(buttonPanel, gbc);

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
                    
                    // Branch and semester selection for existing task
                    String branchCode = rs.getString("branch_code");
                    int semester = rs.getInt("semester");
                    
                    // Set branch selection
                    String sql2 = "SELECT branch_name FROM branches WHERE branch_code = ?";
                    PreparedStatement stmt2 = conn.prepareStatement(sql2);
                    stmt2.setString(1, branchCode);
                    ResultSet rs2 = stmt2.executeQuery();
                    if (rs2.next()) {
                        branchSelect.setSelectedItem(rs2.getString("branch_name"));
                    }
                    
                    // Set semester selection
                    semesterSelect.setSelectedItem(semester);
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
            
            if (title.isEmpty() || description.isEmpty() || dueDate.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql;
                
                // Get branch and semester for the task
                String taskBranch = "teacher".equalsIgnoreCase(userRole) ? 
                    (String) branchSelect.getSelectedItem() : userBranch;
                Integer taskSemester = "teacher".equalsIgnoreCase(userRole) ? 
                    (Integer) semesterSelect.getSelectedItem() : userSemester;
                
                if (taskId == null) {
                    // First, get the branch_code for the selected branch name
                    String branchCodeSql = "SELECT branch_code FROM branches WHERE branch_name = ?";
                    PreparedStatement branchStmt = conn.prepareStatement(branchCodeSql);
                    branchStmt.setString(1, taskBranch);
                    ResultSet branchRs = branchStmt.executeQuery();
                    String branchCode = branchRs.next() ? branchRs.getString("branch_code") : null;

                    sql = "INSERT INTO tasks (title, description, priority, status, created_by, due_date, " +
                          "branch_code, semester) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    stmt.setString(3, priority);
                    stmt.setString(4, status);
                    stmt.setString(5, currentUserEmail);
                    stmt.setString(6, dueDate);
                    stmt.setString(7, branchCode);
                    stmt.setInt(8, taskSemester);
                    stmt.executeUpdate();
                } else {
                    // First, get the branch_code for the selected branch name
                    String branchCodeSql = "SELECT branch_code FROM branches WHERE branch_name = ?";
                    PreparedStatement branchStmt = conn.prepareStatement(branchCodeSql);
                    branchStmt.setString(1, taskBranch);
                    ResultSet branchRs = branchStmt.executeQuery();
                    String branchCode = branchRs.next() ? branchRs.getString("branch_code") : null;

                    sql = "UPDATE tasks SET title=?, description=?, priority=?, status=?, due_date=?, " +
                          "branch_code=?, semester=? WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    stmt.setString(3, priority);
                    stmt.setString(4, status);
                    stmt.setString(5, dueDate);
                    stmt.setString(6, branchCode);
                    stmt.setInt(7, taskSemester);
                    stmt.setInt(8, taskId);
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

    // Removed updateStudentList method as tasks are now assigned by branch/semester

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
            String sql = "SELECT t.*, b.branch_name FROM tasks t " +
                "LEFT JOIN branches b ON t.branch_code = b.branch_code " +
                "WHERE 1=1 ";

            if ("student".equalsIgnoreCase(userRole)) {
                // Students see:
                // 1. Tasks they created
                // 2. Tasks assigned to their branch and semester (but only if created by teachers)
                sql += "AND (created_by = ? OR (branch_code = (SELECT branch_code FROM branches WHERE branch_name = ?) " +
                       "AND semester = ? AND EXISTS (SELECT 1 FROM users u WHERE u.email = t.created_by AND u.role = 'teacher'))) ";
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
                stmt.setString(idx++, userBranch);      // branch_name
                stmt.setInt(idx++, userSemester);       // semester
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
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String priority = rs.getString("priority");
                String status = rs.getString("status").replace("_", " ").toUpperCase();
                String branchName = rs.getString("branch_name");
                int semester = rs.getInt("semester");
                String assignedTo = branchName + " (Sem " + semester + ")";
                Date dueDate = rs.getDate("due_date");
                String createdBy = rs.getString("created_by");
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dueDateStr = dueDate != null ? sdf.format(dueDate) : "";
                
                tableModel.addRow(new Object[]{
                    id, title, description, priority, status, assignedTo, dueDateStr, createdBy
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Could not fetch tasks: " + e.getMessage());
        }
    }
    public static void showTasks(String userEmail, String role, String branch, Integer semester) {
        SwingUtilities.invokeLater(() -> {
            TaskManager tm = new TaskManager(userEmail, role, branch, semester);
            tm.setVisible(true);
        });
    }
}
