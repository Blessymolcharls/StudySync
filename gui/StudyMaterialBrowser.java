package gui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import db.DBConnection;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class StudyMaterialBrowser extends JPanel {
    private JComboBox<BranchItem> branchCombo;
    private JComboBox<GroupItem> groupCombo;
    private JComboBox<Integer> semesterCombo;
    private JComboBox<SubjectItem> subjectCombo;
    private JPanel fileListPanel;
    private String currentUserRole;
    
    
    public StudyMaterialBrowser(String userRole, String userEmail, String defaultBranch, Integer defaultSemester) {
        this.currentUserRole = userRole;
        
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create filter panel
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(Theme.BG_PRIMARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Initialize combos
        branchCombo = new JComboBox<>();
        groupCombo = new JComboBox<>();
        semesterCombo = new JComboBox<>();
        subjectCombo = new JComboBox<>();
        
        // Style combos
        Theme.styleComboBox(branchCombo);
        Theme.styleComboBox(groupCombo);
        Theme.styleComboBox(semesterCombo);
        Theme.styleComboBox(subjectCombo);
        
        // Add components to filter panel
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(createLabelWithIcon("🏫", "Branch:"), gbc);
        gbc.gridx = 1;
        filterPanel.add(branchCombo, gbc);
        
        gbc.gridx = 2;
        filterPanel.add(createLabelWithIcon("👥", "Group:"), gbc);
        gbc.gridx = 3;
        filterPanel.add(groupCombo, gbc);
        
        gbc.gridx = 4;
        filterPanel.add(createLabelWithIcon("📚", "Semester:"), gbc);
        gbc.gridx = 5;
        filterPanel.add(semesterCombo, gbc);
        
        gbc.gridx = 6;
        filterPanel.add(createLabelWithIcon("📖", "Subject:"), gbc);
        gbc.gridx = 7;
        filterPanel.add(subjectCombo, gbc);
        
        // File list panel
        fileListPanel = new JPanel(new BorderLayout());
        fileListPanel.setBackground(Theme.BG_PRIMARY);
        
        // Add panels to main panel
        add(filterPanel, BorderLayout.NORTH);
        add(fileListPanel, BorderLayout.CENTER);
        
        // Load data
        try {
            loadBranches(defaultBranch);
            loadSemesters(defaultSemester);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error initializing data: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
        
        // Add listeners
        branchCombo.addActionListener(e -> {
            loadGroups();
            updateFileList();
        });
        
        groupCombo.addActionListener(e -> {
            loadSubjects();
            updateFileList();
        });
        
        semesterCombo.addActionListener(e -> {
            loadSubjects();
            updateFileList();
        });
        
        subjectCombo.addActionListener(e -> updateFileList());
    }
    
    private JLabel createLabelWithIcon(String icon, String text) {
        JLabel label = new JLabel(icon + " " + text);
        label.setFont(new Font("Dialog", Font.PLAIN, 14));
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }
    
    private void loadBranches(String defaultBranch) throws Exception {
        branchCombo.removeAllItems();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT branch_code, branch_name FROM branches WHERE is_active = TRUE ORDER BY branch_name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                BranchItem item = new BranchItem(
                    rs.getString("branch_code"),
                    rs.getString("branch_name")
                );
                branchCombo.addItem(item);
                if (defaultBranch != null && defaultBranch.equals(item.code)) {
                    branchCombo.setSelectedItem(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if ("student".equalsIgnoreCase(currentUserRole) && defaultBranch != null) {
            branchCombo.setEnabled(false);
        }
        
        loadGroups();
    }
    
    private void loadGroups() {
        groupCombo.removeAllItems();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT DISTINCT g.group_code, g.group_name " +
                        "FROM study_groups g " +
                        "JOIN subjects s ON s.group_code = g.group_code " +
                        "WHERE s.branch_code = ? AND g.is_active = TRUE " +
                        "ORDER BY g.group_name";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, ((BranchItem)branchCombo.getSelectedItem()).code);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                groupCombo.addItem(new GroupItem(
                    rs.getString("group_code"),
                    rs.getString("group_name")
                ));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading groups: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
        
        loadSubjects();
    }
    
    private void loadSemesters(Integer defaultSemester) {
        semesterCombo.removeAllItems();
        for (int i = 1; i <= 8; i++) {
            semesterCombo.addItem(i);
        }
        
        if (defaultSemester != null) {
            semesterCombo.setSelectedItem(defaultSemester);
            if ("student".equalsIgnoreCase(currentUserRole)) {
                semesterCombo.setEnabled(false);
            }
        }
        
        loadSubjects();
    }
    
    private void loadSubjects() {
        subjectCombo.removeAllItems();
        
        BranchItem selectedBranch = (BranchItem)branchCombo.getSelectedItem();
        GroupItem selectedGroup = (GroupItem)groupCombo.getSelectedItem();
        Integer selectedSemester = (Integer)semesterCombo.getSelectedItem();
        
        if (selectedBranch != null && selectedGroup != null && selectedSemester != null) {
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "SELECT id, name, course_code FROM subjects " +
                            "WHERE branch_code = ? AND group_code = ? AND semester = ? " +
                            "ORDER BY name";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, selectedBranch.code);
                stmt.setString(2, selectedGroup.code);
                stmt.setInt(3, selectedSemester);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    subjectCombo.addItem(new SubjectItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("course_code")
                    ));
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error loading subjects: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }
        }
        
        updateFileList();
    }
    
    private void updateFileList() {
        fileListPanel.removeAll();
        
        SubjectItem selectedSubject = (SubjectItem)subjectCombo.getSelectedItem();
        if (selectedSubject == null) {
            fileListPanel.add(new JLabel("Select filters to view files"), BorderLayout.CENTER);
            fileListPanel.revalidate();
            fileListPanel.repaint();
            return;
        }
        
        // Create table model
        String[] columns = {
            "File Tag ID", "File Name", "Uploaded By", "Upload Date", "Actions"
        };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT f.file_tag_id, f.filename, f.uploaded_by, f.upload_time " +
                        "FROM files f " +
                        "WHERE f.subject_id = ? AND f.is_deleted = FALSE " +
                        "ORDER BY f.upload_time DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, selectedSubject.id);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("file_tag_id"),
                    rs.getString("filename"),
                    rs.getString("uploaded_by"),
                    rs.getTimestamp("upload_time"),
                    createActionPanel(
                        rs.getString("file_tag_id"),
                        rs.getString("filename")
                    )
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading files: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
            
            // Add error message to panel
            fileListPanel.add(new JLabel("Error loading files. Please try again."), BorderLayout.CENTER);
            fileListPanel.revalidate();
            fileListPanel.repaint();
            return;
        }
        
        // Create and configure table
        JTable fileTable = new JTable(model);
        fileTable.setBackground(Theme.BG_PRIMARY);
        fileTable.setForeground(Theme.TEXT_PRIMARY);
        fileTable.setGridColor(Theme.BORDER_COLOR);
        fileTable.getTableHeader().setBackground(Theme.DEEP_TEAL);
        fileTable.getTableHeader().setForeground(Theme.TEXT_WHITE);
        
        // Set column widths
        int[] columnWidths = {100, 300, 150, 150, 100};
        for (int i = 0; i < columnWidths.length; i++) {
            fileTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        
        // Configure action column
        fileTable.getColumnModel().getColumn(4).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                return (JPanel) value;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        
        fileListPanel.add(scrollPane, BorderLayout.CENTER);
        fileListPanel.revalidate();
        fileListPanel.repaint();
    }
    
    private JPanel createActionPanel(String fileTagId, String filename) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(Theme.BG_PRIMARY);
        
        // View button
        JButton viewBtn = createActionButton("👁️", "View");
        viewBtn.addActionListener(e -> {
            try {
                // Get file data from database
                String sql = "SELECT filedata, filename FROM files WHERE file_tag_id = ?";
                Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, fileTagId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    byte[] fileData = rs.getBytes("filedata");
                    String tempFileName = rs.getString("filename");
                    
                    // Create temporary file
                    Path tempFile = Files.createTempFile("studysync_", tempFileName);
                    Files.write(tempFile, fileData);
                    
                    // Open file with default system application
                    Desktop.getDesktop().open(tempFile.toFile());
                    
                    // Schedule file for deletion when JVM exits
                    tempFile.toFile().deleteOnExit();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error viewing file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
        panel.add(viewBtn);
        
        // Download button
        JButton downloadBtn = createActionButton("⬇️", "Download");
        downloadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(filename));
            int result = chooser.showSaveDialog(this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    // Get file data from database
                    String sql = "SELECT filedata FROM files WHERE file_tag_id = ?";
                    Connection conn = DBConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, fileTagId);
                    ResultSet rs = stmt.executeQuery();
                    
                    if (rs.next()) {
                        byte[] fileData = rs.getBytes("filedata");
                        File saveFile = chooser.getSelectedFile();
                        Files.write(saveFile.toPath(), fileData);
                        JOptionPane.showMessageDialog(this, "File downloaded successfully!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Error downloading file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
        panel.add(downloadBtn);
        
        // Delete button (teachers only)
        if ("teacher".equalsIgnoreCase(currentUserRole)) {
            JButton deleteBtn = createActionButton("🗑️", "Delete");
            deleteBtn.setForeground(new Color(220, 53, 69)); // Red color
            deleteBtn.addActionListener(e -> {
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this file?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        Connection conn = DBConnection.getConnection();
                        conn.setAutoCommit(false);
                        
                        try {
                            // First, get the file ID
                            String idSql = "SELECT id FROM files WHERE file_tag_id = ?";
                            PreparedStatement idStmt = conn.prepareStatement(idSql);
                            idStmt.setString(1, fileTagId);
                            ResultSet rs = idStmt.executeQuery();
                            
                            if (rs.next()) {
                                // Mark file as deleted
                                String updateSql = "UPDATE files SET is_deleted = TRUE, delete_time = CURRENT_TIMESTAMP WHERE id = ?";
                                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                                updateStmt.setInt(1, rs.getInt("id"));
                                updateStmt.executeUpdate();
                                
                                conn.commit();
                                updateFileList();
                            }
                        } catch (Exception ex) {
                            conn.rollback();
                            throw ex;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Error deleting file: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            });
            panel.add(deleteBtn);
        }
        
        return panel;
    }
    
    private JButton createActionButton(String icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Dialog", Font.PLAIN, 14));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(30, 25));
        btn.setBackground(Theme.BG_SECONDARY);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    // Helper classes for combo box items
    private static class BranchItem {
        String code;
        String name;
        
        BranchItem(String code, String name) {
            this.code = code;
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    private static class GroupItem {
        String code;
        String name;
        
        GroupItem(String code, String name) {
            this.code = code;
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    private static class SubjectItem {
        int id;
        String name;
        String courseCode;
        
        SubjectItem(int id, String name, String courseCode) {
            this.id = id;
            this.name = name;
            this.courseCode = courseCode;
        }
        
        @Override
        public String toString() {
            return name + " (" + courseCode + ")";
        }
    }
}