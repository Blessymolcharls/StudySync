import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.nio.file.*;
import java.util.*;
import gui.Theme;

/**
 * Unified File Management System for StudySync
 * 
 * Features:
 * - Universal file upload for all users
 * - Theme-consistent UI components
 * - Hierarchical file organization
 * - Simple and intuitive interface
 * 
 * Organization:
 * Group → Branch → Semester → Subject
 */
public class FileHandler {
    // Database connection settings
    private static final String URL = "jdbc:mariadb://localhost:3306/pdfshare";
    private static final String USER = "pdfshare";
    private static final String PASS = "toor";

    // File upload dialog components
    private static final File[] selectedFile = new File[1];
    private static JDialog uploadDialog;
    private static JTextField subjectNameField;
    private static JTextField courseCodeField;
    private static JLabel selectedFileLabel;
    private static JButton uploadBtn;
    private static JComboBox<String> branchBox;
    private static JComboBox<Integer> semesterBox;
    private static JComboBox<String> groupBox;

    // Store the current file list table for refreshing
    private static JTable currentFileTable;
    private static String currentUserRole;
    private static String currentUserBranch;
    private static Integer currentUserSemester;
    private static boolean isDeleteMode; // Track whether we're in delete mode
    // Removed unused fields
    
    // Constants
    private static final Color DELETE_BUTTON_COLOR = new Color(220, 53, 69); // Red color for delete buttons

    /**
     * Show the file upload dialog with Theme styling
     */
    public static void uploadFile(String uploadedBy, String userBranch, Integer userSemester) {
        // Reset state
        selectedFile[0] = null;
        
        // Create dialog
        uploadDialog = new JDialog();
        uploadDialog.setTitle("Upload Study Material");
        uploadDialog.setModal(true);
        uploadDialog.setSize(500, 600);
        
        // Main panel with theme styling
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Theme.BG_PRIMARY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Theme.BG_PRIMARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Initialize components
        JLabel titleLabel = new JLabel("Upload Study Material");
        titleLabel.setFont(Theme.UI_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        groupBox = new JComboBox<>();
        branchBox = new JComboBox<>();
        semesterBox = new JComboBox<>();
        subjectNameField = new JTextField();
        courseCodeField = new JTextField();
        selectedFileLabel = new JLabel("No file selected");
        
        // Apply theme styling
        Theme.styleComboBox(groupBox);
        Theme.styleComboBox(branchBox);
        Theme.styleComboBox(semesterBox);
        Theme.styleTextField(subjectNameField);
        Theme.styleTextField(courseCodeField);
        
        // Style labels
        JLabel[] labels = {
            new JLabel("Group:"),
            new JLabel("Branch:"),
            new JLabel("Semester:"),
            new JLabel("Subject Name:"),
            new JLabel("Course Code:"),
            new JLabel("File:")
        };
        for (JLabel label : labels) {
            Theme.styleLabel(label);
        }

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // Load study groups
            String groupSql = "SELECT group_name FROM study_groups WHERE is_active = TRUE ORDER BY group_name";
            PreparedStatement groupStmt = conn.prepareStatement(groupSql);
            ResultSet groupRs = groupStmt.executeQuery();
            while (groupRs.next()) {
                groupBox.addItem(groupRs.getString("group_name"));
            }
            
            // Load branches
            String branchSql = "SELECT branch_name FROM branches WHERE is_active = TRUE ORDER BY branch_name";
            PreparedStatement branchStmt = conn.prepareStatement(branchSql);
            ResultSet branchRs = branchStmt.executeQuery();
            while (branchRs.next()) {
                branchBox.addItem(branchRs.getString("branch_name"));
            }

            // Set default branch for students
            if (userBranch != null) {
                branchBox.setSelectedItem(userBranch);
                branchBox.setEnabled(false);
            }

            // Add semesters 1-8
            for (int i = 1; i <= 8; i++) {
                semesterBox.addItem(i);
            }

            // Set default semester for students
            if (userSemester != null) {
                semesterBox.setSelectedItem(userSemester);
                semesterBox.setEnabled(false);
            }

            // Layout components
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
            formPanel.add(titleLabel, gbc);
            
            gbc.gridwidth = 1; gbc.gridy++;
            formPanel.add(labels[0], gbc); // Group
            gbc.gridx = 1;
            formPanel.add(groupBox, gbc);
            
            gbc.gridx = 0; gbc.gridy++;
            formPanel.add(labels[1], gbc); // Branch
            gbc.gridx = 1;
            formPanel.add(branchBox, gbc);
            
            gbc.gridx = 0; gbc.gridy++;
            formPanel.add(labels[2], gbc); // Semester
            gbc.gridx = 1;
            formPanel.add(semesterBox, gbc);
            
            gbc.gridx = 0; gbc.gridy++;
            formPanel.add(labels[3], gbc); // Subject Name
            gbc.gridx = 1;
            formPanel.add(subjectNameField, gbc);
            
            gbc.gridx = 0; gbc.gridy++;
            formPanel.add(labels[4], gbc); // Course Code
            gbc.gridx = 1;
            formPanel.add(courseCodeField, gbc);
            
            // File selection
            gbc.gridx = 0; gbc.gridy++;
            formPanel.add(labels[5], gbc);
            
            JPanel filePanel = new JPanel(new BorderLayout(10, 0));
            filePanel.setBackground(Theme.BG_PRIMARY);
            
            selectedFileLabel.setForeground(Theme.TEXT_SECONDARY);
            filePanel.add(selectedFileLabel, BorderLayout.CENTER);
            
            JButton selectFileBtn = Theme.createSecondaryButton("Choose PDF");
            selectFileBtn.setPreferredSize(new Dimension(120, 35));
            filePanel.add(selectFileBtn, BorderLayout.EAST);
            
            gbc.gridx = 1;
            formPanel.add(filePanel, gbc);
            
            // Buttons
            gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            buttonPanel.setBackground(Theme.BG_PRIMARY);
            
            uploadBtn = Theme.createPrimaryButton("Upload");
            JButton cancelBtn = Theme.createSecondaryButton("Cancel");
            uploadBtn.setEnabled(false);
            
            buttonPanel.add(uploadBtn);
            buttonPanel.add(cancelBtn);
            
            gbc.insets = new Insets(20, 8, 8, 8);
            formPanel.add(buttonPanel, gbc);
            
            // Add form to main panel
            mainPanel.add(formPanel, BorderLayout.CENTER);
            uploadDialog.add(mainPanel);

            // File selection action
            selectFileBtn.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select PDF File");
                fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
                
                int result = fileChooser.showOpenDialog(uploadDialog);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile[0] = fileChooser.getSelectedFile();
                    selectedFileLabel.setText(selectedFile[0].getName());
                    selectedFileLabel.setForeground(Theme.TEXT_PRIMARY);
                    uploadBtn.setEnabled(true);
                }
            });

            // Upload action
            uploadBtn.addActionListener(e -> {
                if (validateForm()) {
                    try {
                        uploadSelectedFile(uploadedBy);
                        JOptionPane.showMessageDialog(uploadDialog, "File uploaded successfully!");
                        uploadDialog.dispose();
                        // Refresh file list if it exists
                        if (currentFileTable != null) {
                            refreshFileList();
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(uploadDialog,
                            "Upload failed: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            cancelBtn.addActionListener(e -> uploadDialog.dispose());

            // Show dialog
            uploadDialog.pack();
            uploadDialog.setLocationRelativeTo(null);
            uploadDialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    private static boolean validateForm() {
        if (selectedFile[0] == null) {
            JOptionPane.showMessageDialog(uploadDialog,
                "Please select a PDF file",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (subjectNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(uploadDialog,
                "Please enter a subject name",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (courseCodeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(uploadDialog,
                "Please enter a course code",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private static void uploadSelectedFile(String uploadedBy) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            conn.setAutoCommit(false);
            try {
                // Get branch_code for selected branch
                String branchCodeSql = "SELECT branch_code FROM branches WHERE branch_name = ?";
                PreparedStatement branchStmt = conn.prepareStatement(branchCodeSql);
                branchStmt.setString(1, (String) branchBox.getSelectedItem());
                ResultSet branchRs = branchStmt.executeQuery();
                
                if (!branchRs.next()) {
                    throw new Exception("Selected branch not found");
                }
                String branchCode = branchRs.getString("branch_code");

                // Get group_code for selected group
                String groupCodeSql = "SELECT group_code FROM study_groups WHERE group_name = ?";
                PreparedStatement groupStmt = conn.prepareStatement(groupCodeSql);
                groupStmt.setString(1, (String) groupBox.getSelectedItem());
                ResultSet groupRs = groupStmt.executeQuery();
                
                if (!groupRs.next()) {
                    throw new Exception("Selected group not found");
                }
                String groupCode = groupRs.getString("group_code");

                // Create or get subject
                String subjectSql = "INSERT INTO subjects (name, course_code, branch_code, semester, group_code, created_by) " +
                                  "VALUES (?, ?, ?, ?, ?, ?) " +
                                  "ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)";
                
                PreparedStatement subjectStmt = conn.prepareStatement(subjectSql, Statement.RETURN_GENERATED_KEYS);
                subjectStmt.setString(1, subjectNameField.getText().trim());
                subjectStmt.setString(2, courseCodeField.getText().trim());
                subjectStmt.setString(3, branchCode);
                subjectStmt.setInt(4, (Integer) semesterBox.getSelectedItem());
                subjectStmt.setString(5, groupCode);
                subjectStmt.setString(6, uploadedBy);
                subjectStmt.executeUpdate();

                ResultSet subjectRs = subjectStmt.getGeneratedKeys();
                if (!subjectRs.next()) {
                    throw new Exception("Failed to create subject");
                }
                int subjectId = subjectRs.getInt(1);

                // Generate file tag ID
                String fileTagId = generateFileTagId(branchCode, (Integer) semesterBox.getSelectedItem());
                
                // Upload file
                byte[] fileData = Files.readAllBytes(selectedFile[0].toPath());
                String fileSql = "INSERT INTO files (file_tag_id, filename, filedata, subject_id, uploaded_by) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement fileStmt = conn.prepareStatement(fileSql);
                fileStmt.setString(1, fileTagId);
                fileStmt.setString(2, selectedFile[0].getName());
                fileStmt.setBytes(3, fileData);
                fileStmt.setInt(4, subjectId);
                fileStmt.setString(5, uploadedBy);
                fileStmt.executeUpdate();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * List files in a hierarchical tree view with Group -> Semester -> Subject organization
     */
    public static void listFiles(String userRole, JPanel containerPanel, String userBranch, Integer userSemester, boolean deleteMode) {
        // Store parameters for refresh functionality
        currentUserRole = userRole;
        currentUserBranch = userBranch;
        currentUserSemester = userSemester;
        isDeleteMode = deleteMode;

        // Create main panel with split pane for tree and table
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(250); // Width of the tree panel

        // Create tree for navigation
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Study Materials");
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        JTree fileTree = new JTree(treeModel);
        fileTree.setBackground(Theme.BG_PRIMARY);
        fileTree.setForeground(Theme.TEXT_PRIMARY);
        
        // Add selection listener to the tree
        fileTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
            if (node != null && node.isLeaf()) {
                String nodeStr = node.getUserObject().toString();
                // Filter the table to show only files for the selected subject
                if (currentFileTable != null) {
                    TableRowSorter<TableModel> sorter = new TableRowSorter<>(currentFileTable.getModel());
                    currentFileTable.setRowSorter(sorter);
                    RowFilter<Object, Object> subjectFilter = new RowFilter<Object, Object>() {
                        @Override
                        public boolean include(Entry<?, ?> entry) {
                            return entry.getStringValue(2).equals(nodeStr); // Column 2 is Subject
                        }
                    };
                    sorter.setRowFilter(subjectFilter);
                }
            } else {
                // Clear filter when a non-leaf node is selected
                if (currentFileTable != null) {
                    currentFileTable.setRowSorter(null);
                }
            }
        });
        
        // Create detail panel for file list
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(Theme.BG_PRIMARY);
        
        // Create card layout for file lists
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Theme.BG_PRIMARY);
        
        Map<String, DefaultMutableTreeNode> groupNodes = new HashMap<>();
        Map<String, DefaultMutableTreeNode> semesterNodes = new HashMap<>();
        Map<String, DefaultMutableTreeNode> subjectNodes = new HashMap<>();
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // Build the tree structure query
            StringBuilder structureSQL = new StringBuilder(
                "SELECT DISTINCT g.group_name, s.semester, s.name as subject_name, s.id as subject_id, b.branch_name " +
                "FROM subjects s " +
                "JOIN branches b ON s.branch_code = b.branch_code " +
                "JOIN study_groups g ON s.group_code = g.group_code " +
                "JOIN files f ON f.subject_id = s.id " +
                "WHERE f.is_deleted = FALSE");

            // No filters for students - all files are accessible
            structureSQL.append(" ORDER BY g.group_name, s.semester, s.name");

            try (PreparedStatement stmt = conn.prepareStatement(structureSQL.toString())) {
                // No parameters needed for the query - all files are visible
                
                // Execute query and build tree
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String groupName = rs.getString("group_name");
                    int semester = rs.getInt("semester");
                    String subjectName = rs.getString("subject_name");
                    int subjectId = rs.getInt("subject_id");
                    
                    // Create/get group node
                    String branchName = rs.getString("branch_name");
                    
                    // Create/get group node (Study Materials -> Group)
                    String groupKey = groupName;
                    DefaultMutableTreeNode groupNode = groupNodes.computeIfAbsent(groupKey, k -> {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode(groupName);
                        root.add(node);
                        return node;
                    });
                    
                    // Create/get semester node (Group -> Semester)
                    String semesterKey = groupKey + "-" + semester + "-" + branchName;
                    DefaultMutableTreeNode semesterNode = semesterNodes.computeIfAbsent(semesterKey, k -> {
                        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Semester " + semester + " (" + branchName + ")");
                        groupNode.add(node);
                        return node;
                    });
                    
                    // Create subject node (Semester -> Subject)
                    String subjectKey = semesterKey + "-" + subjectId;
                    if (!subjectNodes.containsKey(subjectKey)) {
                        DefaultMutableTreeNode subjectNode = new DefaultMutableTreeNode(subjectName);
                        semesterNode.add(subjectNode);
                        subjectNodes.put(subjectKey, subjectNode);
                    }
                    
                    // Create a panel for this subject's files
                    JPanel filePanel = createFileListPanel(subjectId, userRole);
                    cardPanel.add(filePanel, subjectKey);
                }
            }

            // Retrieve full file list for the table (separate query)
            String filesSql = "SELECT f.id, f.filename, s.name as subject_name, s.course_code, b.branch_name, s.semester, g.group_name, f.uploaded_by, f.upload_time " +
                              "FROM files f " +
                              "JOIN subjects s ON f.subject_id = s.id " +
                              "JOIN branches b ON s.branch_code = b.branch_code " +
                              "JOIN study_groups g ON s.group_code = g.group_code " +
                              "WHERE f.is_deleted = FALSE " +
                              "ORDER BY f.upload_time DESC";
            PreparedStatement filesStmt = conn.prepareStatement(filesSql);
            ResultSet rs = filesStmt.executeQuery();

        // Create split pane for tree and table (reuse existing mainSplitPane and fileTree)
        JScrollPane treeScroll = new JScrollPane(fileTree);
        mainSplitPane.setLeftComponent(treeScroll);
        
        // Create table model with non-editable cells except actions
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == getColumnCount() - 1; // Only action column is editable
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == getColumnCount() - 1 ? JPanel.class : Object.class;
            }
        };            // Define columns
            model.addColumn("ID"); // Hidden column for internal use
            model.addColumn("File Name");
            model.addColumn("Subject");
            model.addColumn("Course Code");
            model.addColumn("Branch");
            model.addColumn("Semester");
            model.addColumn("Group");
            model.addColumn("Uploaded By");
            model.addColumn("Upload Date");
            model.addColumn("Actions");

            // Populate table
            while (rs.next()) {
                int fileId = rs.getInt("id");
                String filename = rs.getString("filename");
                
                model.addRow(new Object[]{
                    fileId,
                    filename,
                    rs.getString("subject_name"),
                    rs.getString("course_code"),
                    rs.getString("branch_name"),
                    rs.getInt("semester"),
                    rs.getString("group_name"),
                    rs.getString("uploaded_by"),
                    rs.getTimestamp("upload_time"),
                    createActionPanel(fileId, filename, userRole)
                });
            }

            // Create and configure table
            currentFileTable = new JTable(model);
            currentFileTable.setFillsViewportHeight(true);
            currentFileTable.setBackground(Theme.BG_PRIMARY);
            currentFileTable.setForeground(Theme.TEXT_PRIMARY);
            currentFileTable.setGridColor(Theme.BORDER_COLOR);
            currentFileTable.getTableHeader().setBackground(Theme.DEEP_TEAL);
            currentFileTable.getTableHeader().setForeground(Theme.TEXT_WHITE);
            currentFileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // Create bottom action button based on mode
            JButton actionButton;
            if (isDeleteMode && "teacher".equalsIgnoreCase(userRole)) {
                // Delete button for teachers in delete mode
                actionButton = Theme.createSecondaryButton("Delete Selected");
                actionButton.setIcon(new JLabel("🗑️").getIcon());
                actionButton.setBackground(DELETE_BUTTON_COLOR);
                actionButton.setForeground(Color.WHITE);
                
                actionButton.addActionListener(e -> {
                    int row = currentFileTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = currentFileTable.convertRowIndexToModel(row);
                        int fileId = (int) model.getValueAt(modelRow, 0);
                        String filename = (String) model.getValueAt(modelRow, 1);
                        
                        int confirm = JOptionPane.showConfirmDialog(null,
                            "Are you sure you want to delete this file?\nThis action cannot be undone.",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                            
                        if (confirm == JOptionPane.YES_OPTION) {
                            deleteFile(fileId);
                        }
                    }
                });
            } else {
                // Download button for everyone else
                actionButton = Theme.createPrimaryButton("Download Selected");
                actionButton.setIcon(new JLabel("⬇️").getIcon());
                
                actionButton.addActionListener(e -> {
                    int row = currentFileTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = currentFileTable.convertRowIndexToModel(row);
                        int fileId = (int) model.getValueAt(modelRow, 0);
                        String filename = (String) model.getValueAt(modelRow, 1);
                        downloadFile(fileId, filename);
                    }
                });
            }
            
            // Create bottom panel for action buttons
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            bottomPanel.setBackground(Theme.BG_PRIMARY);
            
            // Common button configuration
            actionButton.setEnabled(false);
            actionButton.setIconTextGap(10);
            actionButton.setPreferredSize(new Dimension(200, 40));
            bottomPanel.add(actionButton);
            
            // Update button state based on selection
            final JButton finalActionButton = actionButton;
            currentFileTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    boolean hasSelection = currentFileTable.getSelectedRow() != -1;
                    finalActionButton.setEnabled(hasSelection);
                    
                    if (hasSelection) {
                        int modelRow = currentFileTable.convertRowIndexToModel(currentFileTable.getSelectedRow());
                        String filename = (String) model.getValueAt(modelRow, 1);
                        finalActionButton.setToolTipText((isDeleteMode && "teacher".equalsIgnoreCase(userRole) ? 
                            "Delete " : "Download ") + filename);
                    }
                }
            });
            
            // Hide ID column
            currentFileTable.removeColumn(currentFileTable.getColumnModel().getColumn(0));

            // Determine action column index (last visible column)
            int actionColIndex = currentFileTable.getColumnModel().getColumnCount() - 1;

            // Custom renderer for action buttons
            currentFileTable.getColumnModel().getColumn(actionColIndex).setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    if (value instanceof JPanel) {
                        return (JPanel) value;
                    }
                    return new JLabel();
                }
            });
            
            // Custom editor to handle button clicks
            currentFileTable.getColumnModel().getColumn(actionColIndex).setCellEditor(
                new DefaultCellEditor(new JCheckBox()) {
                    private JPanel panel;
                    
                    @Override
                    public Component getTableCellEditorComponent(JTable table, Object value,
                            boolean isSelected, int row, int column) {
                        if (value instanceof JPanel) {
                            panel = (JPanel) value;
                            return panel;
                        }
                        return new JPanel();
                    }
                    
                    @Override
                    public Object getCellEditorValue() {
                        return panel;
                    }
                    
                    @Override
                    public boolean stopCellEditing() {
                        super.stopCellEditing();
                        return true;
                    }
                });

            // Set column widths
            int[] columnWidths = {200, 150, 100, 100, 80, 80, 150, 150, 100};
            for (int i = 0; i < columnWidths.length; i++) {
                currentFileTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
            }

            // Create scroll pane and main panels with themed styling
            JScrollPane scrollPane = new JScrollPane(currentFileTable);
            scrollPane.getViewport().setBackground(Theme.BG_PRIMARY);
            scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));

            // Create right panel with table and buttons
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setBackground(Theme.BG_PRIMARY);
            rightPanel.add(scrollPane, BorderLayout.CENTER);
            
            // Add action button to the bottom of the right panel
            rightPanel.add(bottomPanel, BorderLayout.SOUTH);
            
            // Update button state based on selection
            currentFileTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    boolean hasSelection = currentFileTable.getSelectedRow() != -1;
                    actionButton.setEnabled(hasSelection);
                    
                    if (hasSelection) {
                        int modelRow = currentFileTable.convertRowIndexToModel(currentFileTable.getSelectedRow());
                        String filename = (String) model.getValueAt(modelRow, 1);
                        actionButton.setToolTipText((isDeleteMode && "teacher".equalsIgnoreCase(userRole) ? 
                            "Delete " : "Download ") + filename);
                    }
                }
            });
            
            // Add to the right side of the split pane
            mainSplitPane.setRightComponent(rightPanel);

            // Update container panel
            containerPanel.removeAll();
            containerPanel.setLayout(new BorderLayout());
            containerPanel.add(mainSplitPane, BorderLayout.CENTER);
            containerPanel.revalidate();
            containerPanel.repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading files: " + e.getMessage());
        }
    }

    private static JPanel createActionPanel(int fileId, String filename, String userRole) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(Theme.BG_PRIMARY);
        
        // Create single View button
        JButton viewBtn = Theme.createPrimaryButton("👁️ View");
        viewBtn.setPreferredSize(new Dimension(80, 15));
        viewBtn.setFont(new Font("Dialog", Font.PLAIN, 12));
        viewBtn.setToolTipText("View PDF");
        viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add view action
        viewBtn.addActionListener(e -> viewFile(fileId, filename));
        
        panel.add(viewBtn);
        return panel;
    }
    
    // Helper method to view PDF files
    private static void viewFile(int fileId, String filename) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "SELECT filedata FROM files WHERE id = ? AND is_deleted = FALSE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                byte[] fileData = rs.getBytes("filedata");
                Path tempFile = Files.createTempFile("studysync_", "_" + filename);
                Files.write(tempFile, fileData);
                Desktop.getDesktop().open(tempFile.toFile());
                tempFile.toFile().deleteOnExit();
            } else {
                throw new Exception("File not found or has been deleted");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Error viewing file: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JPanel createFileListPanel(int subjectId, String userRole) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_PRIMARY);
        
        DefaultListModel<FileItem> listModel = new DefaultListModel<>();
        JList<FileItem> fileList = new JList<>(listModel);
        fileList.setCellRenderer(new FileListCellRenderer());
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "SELECT id, filename, uploaded_by, upload_time FROM files WHERE subject_id = ? AND is_deleted = FALSE ORDER BY upload_time DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, subjectId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                FileItem item = new FileItem(
                    rs.getInt("id"),
                    rs.getString("filename"),
                    rs.getString("uploaded_by"),
                    rs.getTimestamp("upload_time")
                );
                listModel.addElement(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static class FileItem {
        @SuppressWarnings("unused")
        final int id;  // Used for database operations
        final String filename;
        final String uploadedBy;
        final Timestamp uploadTime;
        
        FileItem(int id, String filename, String uploadedBy, Timestamp uploadTime) {
            this.id = id;
            this.filename = filename;
            this.uploadedBy = uploadedBy;
            this.uploadTime = uploadTime;
        }
        
        @Override
        public String toString() {
            return filename;
        }
    }
    
    private static class FileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof FileItem) {
                FileItem item = (FileItem) value;
                label.setIcon(new JLabel("📄").getIcon());
                label.setText(item.filename);
                label.setFont(new Font("Dialog", Font.PLAIN, 12));
                label.setToolTipText(String.format("Uploaded by: %s on %s", 
                    item.uploadedBy, item.uploadTime));
            }
            
            if (!isSelected) {
                label.setBackground(Theme.BG_PRIMARY);
                label.setForeground(Theme.TEXT_PRIMARY);
            }
            
            return label;
        }
    }
    
    // Removed showFileActions method as context menus are no longer used
    
    private static void refreshFileList() {
        // We don't need this anymore as the table is updated directly in deleteFile method
        // Only refresh if there's some other reason than deletion
        if (currentFileTable != null) {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                String filesSql = "SELECT f.id, f.filename, s.name as subject_name, s.course_code, " +
                                "b.branch_name, s.semester, g.group_name, f.uploaded_by, f.upload_time " +
                                "FROM files f " +
                                "JOIN subjects s ON f.subject_id = s.id " +
                                "JOIN branches b ON s.branch_code = b.branch_code " +
                                "JOIN study_groups g ON s.group_code = g.group_code " +
                                "WHERE f.is_deleted = FALSE " +
                                "ORDER BY f.upload_time DESC";
                PreparedStatement stmt = conn.prepareStatement(filesSql);
                ResultSet rs = stmt.executeQuery();
                
                DefaultTableModel model = (DefaultTableModel) currentFileTable.getModel();
                model.setRowCount(0); // Clear existing rows
                
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("filename"),
                        rs.getString("subject_name"),
                        rs.getString("course_code"),
                        rs.getString("branch_name"),
                        rs.getInt("semester"),
                        rs.getString("group_name"),
                        rs.getString("uploaded_by"),
                        rs.getTimestamp("upload_time"),
                        createActionPanel(rs.getInt("id"), rs.getString("filename"), currentUserRole)
                    });
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, 
                    "Error refreshing file list: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Show delete confirmation dialog
     */
    public static void deleteFilePrompt() {
        JOptionPane.showMessageDialog(null,
            "Please use the delete button in the file list to remove files.",
            "Delete Files",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // Generate unique file tag ID
    private static String generateFileTagId(String branchCode, int semester) throws SQLException {
        String prefix = branchCode + "_S" + semester + "_";
        int sequence = 1;
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            // Get the highest sequence number for this branch and semester
            String sql = "SELECT file_tag_id FROM files " +
                        "WHERE file_tag_id LIKE ? " +
                        "ORDER BY file_tag_id DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String lastId = rs.getString("file_tag_id");
                sequence = Integer.parseInt(lastId.substring(lastId.lastIndexOf("_") + 1)) + 1;
            }
        }
        
        return prefix + String.format("%03d", sequence);
    }

    // Download file
    public static void downloadFile(int id, String filename) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(filename));
        int result = chooser.showSaveDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File saveFile = chooser.getSelectedFile();
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                String sql = "SELECT filedata FROM files WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Files.write(saveFile.toPath(), rs.getBytes("filedata"));
                    JOptionPane.showMessageDialog(null, "File downloaded successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "File not found in database.",
                        "Download Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Download failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Delete file (teacher only)
    public static void deleteFile(int id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            conn.setAutoCommit(false);
            try {
                // Get file info before deletion for tree update
                String fileInfoSql = "SELECT f.id, f.subject_id FROM files f WHERE f.id = ? AND f.is_deleted = FALSE";
                PreparedStatement fileInfoStmt = conn.prepareStatement(fileInfoSql);
                fileInfoStmt.setInt(1, id);
                ResultSet fileInfoRs = fileInfoStmt.executeQuery();
                
                if (!fileInfoRs.next()) {
                    throw new Exception("File not found or already deleted");
                }
                
                // Perform the deletion
                String sql = "UPDATE files SET is_deleted = TRUE, delete_time = CURRENT_TIMESTAMP WHERE id = ? AND is_deleted = FALSE";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    
                    // Update UI - only update the file list, no tree rebuild needed
                    if (currentFileTable != null) {
                        DefaultTableModel model = (DefaultTableModel) currentFileTable.getModel();
                        for (int i = 0; i < model.getRowCount(); i++) {
                            if ((int)model.getValueAt(i, 0) == id) {
                                model.removeRow(i);
                                break;
                            }
                        }
                    }
                    
                    JOptionPane.showMessageDialog(null, "File deleted successfully!");
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(null, 
                        "File not found or already deleted!",
                        "Delete Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Delete failed: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete the currently selected file (teacher only)
     */
    public static void deleteSelectedFile(String userRole) {
        if (!"teacher".equalsIgnoreCase(userRole)) {
            JOptionPane.showMessageDialog(null,
                "Only teachers can delete files.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (currentFileTable == null || currentFileTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(null,
                "Please select a file to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = currentFileTable.getSelectedRow();
        int modelRow = currentFileTable.convertRowIndexToModel(row);
        DefaultTableModel model = (DefaultTableModel) currentFileTable.getModel();

        int fileId = (int) model.getValueAt(modelRow, 0);
        String filename = (String) model.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(null,
            "Are you sure you want to delete the file '" + filename + "'?\nThis action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            deleteFile(fileId);
        }
    }
}