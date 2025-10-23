/**
 * Dashboard - Main Application Interface
 * 
 * This class represents the main user interface of the StudySync application.
 * It provides access to all major features through a clean, intuitive GUI.
 * The dashboard adapts its interface based on the user's role (teacher/student).
 * 
 * Features:
 * 1. PDF Management
 *    - Upload PDFs (all users)
 *    - View/Download PDFs (all users)
 *    - Delete PDFs (teachers only)
 * 
 * 2. Task Management
 *    - Create and manage study tasks
 *    - Track task progress
 *    - Role-specific task views
 * 
 * UI Components:
 * - Header with user info and logout
 * - Section panels for different features
 * - Status bar showing current user and role
 * - Responsive layout with GridBagLayout
 * 
 * Window Management:
 * - Single child window policy
 * - Automatic cleanup of disposed windows
 * - Centralized window positioning
 * 
 * @see TaskManager
 * @see FileHandler
 * @see UserAuth
 */

import javax.swing.*;
import java.awt.*;
import gui.TaskManager;
import gui.PomodoroTimer;
import gui.ScheduleManager;

public class Dashboard extends JFrame {
    private JFrame currentChildFrame = null;

    public Dashboard(String userEmail, String role) {
        setTitle("📚 Study Portal - Dashboard");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("Welcome, " + userEmail + "!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("🚪 Logout");
        logoutBtn.setFocusPainted(false);
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // PDF Management
        JPanel pdfPanel = createSectionPanel("PDF Management", new Color(240, 248, 255));
        JButton uploadBtn = createStyledButton("📄 Upload PDF", "Upload new PDF files");
        JButton viewBtn = createStyledButton("📥 View / Download", "Browse and download PDFs");
        JButton deleteBtn = createStyledButton("🗑️ Delete PDF", "Remove PDF files");

        pdfPanel.add(uploadBtn);
        pdfPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        pdfPanel.add(viewBtn);
        if ("teacher".equalsIgnoreCase(role)) {
            pdfPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            pdfPanel.add(deleteBtn);
        }

        // Task Management
        JPanel taskPanel = createSectionPanel("Task Management", new Color(245, 245, 255));
        JButton tasksBtn = createStyledButton("📋 Study Tasks", "Manage your study tasks");
        taskPanel.add(tasksBtn);

        // Schedule Manager (both student & teacher)
        JPanel schedulePanel = createSectionPanel("Schedule Manager", new Color(240, 255, 245));
        JButton scheduleBtn = createStyledButton("📅 Schedule Manager", "Manage study timetable and calendar");
        schedulePanel.add(scheduleBtn);

        // Only students see Pomodoro Timer
        JPanel pomodoroPanel = null;
        JButton pomodoroBtn = null;
        if ("student".equalsIgnoreCase(role)) {
            pomodoroPanel = createSectionPanel("Pomodoro Timer", new Color(255, 250, 240));
            pomodoroBtn = createStyledButton("⏱️ Pomodoro Timer", "Start focused study sessions");
            pomodoroPanel.add(pomodoroBtn);
        }

        // Add panels to content
        gbc.gridy = 0;
        contentPanel.add(pdfPanel, gbc);
        gbc.gridy = 1;
        contentPanel.add(taskPanel, gbc);
        gbc.gridy = 2;
        contentPanel.add(schedulePanel, gbc);
        if (pomodoroPanel != null) {
            gbc.gridy = 3;
            contentPanel.add(pomodoroPanel, gbc);
        }

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel statusLabel = new JLabel("Logged in as: " + userEmail + " (" + role + ")");
        statusBar.add(statusLabel, BorderLayout.WEST);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        add(mainPanel);

        // ------------------- Button Actions -------------------
        uploadBtn.addActionListener(e -> FileHandler.uploadFile(userEmail));
        viewBtn.addActionListener(e -> openPDFViewer(role));
        deleteBtn.addActionListener(e -> FileHandler.deleteFilePrompt());
        tasksBtn.addActionListener(e -> openTaskManager(userEmail, role));
        scheduleBtn.addActionListener(e -> openScheduleManager(userEmail, role));
        if (pomodoroBtn != null)
            pomodoroBtn.addActionListener(e -> openPomodoroTimer());

        logoutBtn.addActionListener(e -> {
            closeChildFrame();
            dispose();
            new UserAuth();
        });

        setVisible(true);
    }

    // ------------------- Helper Methods -------------------
    private JPanel createSectionPanel(String title, Color bgColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(bgColor);
        return panel;
    }

    private JButton createStyledButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        return button;
    }

    private void closeChildFrame() {
        if (currentChildFrame != null && currentChildFrame.isDisplayable()) {
            currentChildFrame.dispose();
            currentChildFrame = null;
        }
    }

    // ------------------- Open Child Frames -------------------
    private void openTaskManager(String userEmail, String role) {
        closeChildFrame();
        currentChildFrame = new JFrame("📋 Study Tasks");
        currentChildFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        TaskManager taskManager = new TaskManager(userEmail, role);
        currentChildFrame.add(taskManager);
        currentChildFrame.setSize(800, 600);
        currentChildFrame.setLocationRelativeTo(this);
        currentChildFrame.setVisible(true);
    }

    private void openPomodoroTimer() {
        closeChildFrame();
        currentChildFrame = new JFrame("⏱️ Pomodoro Timer");
        currentChildFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        PomodoroTimer pomodoro = new PomodoroTimer();
        currentChildFrame.add(pomodoro);
        currentChildFrame.setSize(400, 300);
        currentChildFrame.setLocationRelativeTo(this);
        currentChildFrame.setVisible(true);
    }

    private void openScheduleManager(String userEmail, String role) {
        closeChildFrame();
        currentChildFrame = new JFrame("📅 Schedule Manager");
        currentChildFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ScheduleManager scheduleManager = new ScheduleManager(userEmail, role);
        currentChildFrame.add(scheduleManager);
        currentChildFrame.setSize(800, 600);
        currentChildFrame.setLocationRelativeTo(this);
        currentChildFrame.setVisible(true);
    }

    private void openPDFViewer(String role) {
        closeChildFrame();
        currentChildFrame = new JFrame("📂 PDF Files");
        currentChildFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel filePanel = new JPanel(new BorderLayout());
        JButton backBtn = new JButton("← Back to Dashboard");
        backBtn.addActionListener(evt -> {
            currentChildFrame.dispose();
            currentChildFrame = null;
        });
        filePanel.add(backBtn, BorderLayout.NORTH);
        FileHandler.listFiles(role, filePanel);
        currentChildFrame.add(filePanel);

        currentChildFrame.setSize(700, 500);
        currentChildFrame.setLocationRelativeTo(this);
        currentChildFrame.setVisible(true);
    }
}
