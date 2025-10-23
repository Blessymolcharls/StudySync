import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.sql.*;

/**
 * PDF File Management System
 * 
 * Handles all file operations in the application:
 * 1. File Upload - Store PDFs in database
 * 2. File Download - Retrieve PDFs from database
 * 3. File Listing - Display available PDFs
 * 4. File Deletion - Remove PDFs (teacher only)
 * 
 * Security Features:
 * - Role-based access control
 * - File ownership tracking
 * - Secure file storage in database
 * - Protected file operations
 */
public class FileHandler {
    // Database connection settings
    private static final String URL = "jdbc:mariadb://localhost:3306/pdfshare";
    private static final String USER = "pdfshare";
    private static final String PASS = "toor";

    // 🟢 Upload file
    public static void uploadFile(String uploadedBy) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                 FileInputStream fis = new FileInputStream(file)) {
                Class.forName("org.mariadb.jdbc.Driver");
                String sql = "INSERT INTO files (filename, filedata, uploaded_by, upload_time) VALUES (?, ?, ?, NOW())";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, file.getName());
                stmt.setBinaryStream(2, fis, (int) file.length());
                stmt.setString(3, uploadedBy);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "✅ File uploaded!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "❌ Upload failed: " + ex.getMessage());
            }
        }
    }

        // 🟢 List files
    public static void listFiles(String role, JPanel containerPanel) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            Class.forName("org.mariadb.jdbc.Driver");

            String sql = "SELECT id, filename, uploaded_by, upload_time FROM files";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Filename", "Uploader", "Uploaded At"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("filename"),
                        rs.getString("uploaded_by"),
                        rs.getTimestamp("upload_time")
                });
            }

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowHeight(25);

            // Style the table
            table.setShowGrid(true);
            table.setGridColor(Color.LIGHT_GRAY);
            table.setIntercellSpacing(new Dimension(1, 1));
            
            // Set column widths
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(1).setPreferredWidth(200);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);
            table.getColumnModel().getColumn(3).setPreferredWidth(150);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Create buttons with improved styling
            JButton downloadBtn = new JButton("🔽 Download Selected");
            JButton deleteBtn = new JButton("🗑 Delete Selected");
            
            downloadBtn.setFocusPainted(false);
            deleteBtn.setFocusPainted(false);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
            buttonPanel.add(downloadBtn);
            if ("teacher".equalsIgnoreCase(role)) {
                buttonPanel.add(deleteBtn);
            }

            // Add components to the container panel
            containerPanel.setLayout(new BorderLayout(5, 5));
            containerPanel.add(scrollPane, BorderLayout.CENTER);
            containerPanel.add(buttonPanel, BorderLayout.SOUTH);

            // Button actions
            downloadBtn.addActionListener(e -> {
                Object __ = e.getSource();
                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(containerPanel, "⚠️ Please select a file to download!");
                    return;
                }
                int fileId = (int) table.getValueAt(row, 0);
                String filename = (String) table.getValueAt(row, 1);
                downloadFile(fileId, filename);
            });
            deleteBtn.addActionListener(e -> {
                Object __ = e.getSource();
                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(containerPanel, "⚠️ Please select a file to delete!");
                    return;
                }
                int fileId = (int) table.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(containerPanel, 
                    "Delete file permanently?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteFile(fileId);
                    ((DefaultTableModel) table.getModel()).removeRow(row);
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Could not fetch files: " + e.getMessage());
        }
    }

    // 🟢 Download file
    public static void downloadFile(int id, String filename) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(filename));
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File saveFile = chooser.getSelectedFile();
            try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                String sql = "SELECT filedata FROM files WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    try (InputStream in = rs.getBinaryStream("filedata");
                         OutputStream out = new FileOutputStream(saveFile)) {
                        in.transferTo(out);
                        JOptionPane.showMessageDialog(null, "✅ File downloaded!");
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "❌ Download failed: " + e.getMessage());
            }
        }
    }

    // 🟢 Delete file (teacher)
    public static void deleteFilePrompt() {
        String input = JOptionPane.showInputDialog("Enter File ID to delete:");
        if (input == null || input.isBlank()) return;
        try {
            int id = Integer.parseInt(input);
            deleteFile(id);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "⚠️ Invalid ID!");
        }
    }

    public static void deleteFile(int id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "DELETE FROM files WHERE id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0)
                JOptionPane.showMessageDialog(null, "🗑 File deleted!");
            else
                JOptionPane.showMessageDialog(null, "⚠️ File not found!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Delete failed: " + e.getMessage());
        }
    }
}
