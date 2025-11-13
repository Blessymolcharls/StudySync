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
import gui.Theme;

public class Dashboard extends JFrame {
    private JFrame currentChildFrame = null;
    private final String userEmail;
    private final String role;
    private final String branch;
    private final Integer semester;

    public Dashboard(String userEmail, String role, String branch, Integer semester) {
        this.userEmail = userEmail;
        this.role = role;
        this.branch = branch;
        this.semester = semester;
        setTitle("📚 Study Portal - Dashboard");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel(new BorderLayout(Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.LIGHT_MINT, 0, getHeight(), Theme.BG_SECONDARY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            Theme.SPACING_LARGE, Theme.SPACING_LARGE, 
            Theme.SPACING_LARGE, Theme.SPACING_LARGE
        ));

        // Header with themed styling
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.DEEP_TEAL);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new Theme.ShadowBorder(),
            BorderFactory.createEmptyBorder(
                Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, 
                Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM
            )
        ));

        JLabel titleLabel = new JLabel("Welcome, " + userEmail + "!");
        titleLabel.setFont(Theme.UI_TITLE);
        titleLabel.setForeground(Theme.TEXT_WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton logoutBtn = Theme.createSecondaryButton("🚪 Logout");
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // PDF Management
        JPanel pdfPanel = createSectionPanel("PDF Management", "pdf");
        JButton uploadBtn = createStyledButton("📄 Upload PDF", "Upload new PDF files");
        JButton viewBtn = createStyledButton("📥 View / Download", "Browse and download PDFs");
        JButton deleteBtn = createStyledButton("🗑️ Delete PDF", "Remove PDF files");

        pdfPanel.add(uploadBtn);
        pdfPanel.add(Box.createRigidArea(new Dimension(0, Theme.SPACING_SMALL)));
        pdfPanel.add(viewBtn);
        if ("teacher".equalsIgnoreCase(role)) {
            pdfPanel.add(Box.createRigidArea(new Dimension(0, Theme.SPACING_SMALL)));
            pdfPanel.add(deleteBtn);
        }

        // Task Management
        JPanel taskPanel = createSectionPanel("Task Management", "task");
        JButton tasksBtn = createStyledButton("📋 Study Tasks", "Manage your study tasks");
        taskPanel.add(tasksBtn);

        // Schedule Manager (both student & teacher)
        JPanel schedulePanel = createSectionPanel("Schedule Manager", "schedule");
        JButton scheduleBtn = createStyledButton("📅 Schedule Manager", "Manage study timetable and calendar");
        schedulePanel.add(scheduleBtn);

        // Only students see Pomodoro Timer
        JPanel pomodoroPanel = null;
        JButton pomodoroBtn = null;
        if ("student".equalsIgnoreCase(role)) {
            pomodoroPanel = createSectionPanel("Pomodoro Timer", "pomodoro");
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

        // Status bar with theme styling
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Theme.DEEP_TEAL);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            new Theme.ShadowBorder(),
            BorderFactory.createEmptyBorder(
                Theme.SPACING_SMALL, Theme.SPACING_MEDIUM,
                Theme.SPACING_SMALL, Theme.SPACING_MEDIUM
            )
        ));
        
        String statusText = "Logged in as: " + userEmail + " (" + role + ")";
        if ("student".equalsIgnoreCase(role) && branch != null) {
            statusText += " - " + branch;
            if (semester != null) {
                statusText += ", Semester " + semester;
            }
        }
        
        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(Theme.UI_FONT);
        statusLabel.setForeground(Theme.TEXT_WHITE);
        statusBar.add(statusLabel, BorderLayout.WEST);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        add(mainPanel);

        // ------------------- Button Actions -------------------
        uploadBtn.addActionListener(e -> FileHandler.uploadFile(this.userEmail, this.branch, this.semester));
        viewBtn.addActionListener(e -> openPDFViewer());      // For viewing & downloading
        deleteBtn.addActionListener(e -> openDeletePDFViewer()); // For deleting only
        tasksBtn.addActionListener(e -> openTaskManager());
        scheduleBtn.addActionListener(e -> openScheduleManager());
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
    private JPanel createSectionPanel(String title, String sectionType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Create title label with theme styling
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.UI_HEADING);
        titleLabel.setForeground(Theme.DEEP_TEAL);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACING_MEDIUM, 0));
        
        // Create content panel with theme styling
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        Theme.styleSectionPanel(contentPanel, sectionType);
        
        // Assemble panel
        panel.setBackground(Theme.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(
            Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM, 
            Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM
        ));
        
        panel.add(titleLabel);
        panel.add(contentPanel);
        
        return contentPanel;
    }

    private JButton createStyledButton(String text, String tooltip) {
        JButton button = Theme.createPrimaryButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, Theme.BUTTON_HEIGHT));
        button.setToolTipText(tooltip);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Theme.DEEP_TEAL_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Theme.DEEP_TEAL);
            }
        });
        
        return button;
    }

    private void closeChildFrame() {
        if (currentChildFrame != null && currentChildFrame.isDisplayable()) {
            currentChildFrame.dispose();
            currentChildFrame = null;
        }
    }

    // ------------------- Open Child Frames -------------------
    private void openTaskManager() {
        closeChildFrame();
        currentChildFrame = new JFrame("📋 Study Tasks");
        currentChildFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        TaskManager taskManager = new TaskManager(this.userEmail, this.role, this.branch, this.semester);
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

    private void openScheduleManager() {
        closeChildFrame();
        currentChildFrame = new JFrame("📅 Schedule Manager");
        currentChildFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ScheduleManager scheduleManager = new ScheduleManager(this.userEmail, this.role);
        currentChildFrame.add(scheduleManager);
        currentChildFrame.setSize(800, 600);
        currentChildFrame.setLocationRelativeTo(this);
        currentChildFrame.setVisible(true);
    }

    private void openPDFViewer() {
        closeChildFrame();
        currentChildFrame = new JFrame("📂 PDF Files");
        currentChildFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.LIGHT_MINT, 0, getHeight(), Theme.BG_SECONDARY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM,
            Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM
        ));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.DEEP_TEAL);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new Theme.ShadowBorder(),
            BorderFactory.createEmptyBorder(
                Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM,
                Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM
            )
        ));

        JButton backBtn = Theme.createSecondaryButton("← Back to Dashboard");
        backBtn.addActionListener(evt -> {
            currentChildFrame.dispose();
            currentChildFrame = null;
        });
        headerPanel.add(backBtn, BorderLayout.WEST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBackground(Theme.BG_PRIMARY);
        FileHandler.listFiles(this.role, filePanel, this.branch, this.semester, false);
        mainPanel.add(filePanel, BorderLayout.CENTER);

        currentChildFrame.add(mainPanel);
        currentChildFrame.setSize(900, 600);
        currentChildFrame.setLocationRelativeTo(this);
        currentChildFrame.setVisible(true);
    }

    private void openDeletePDFViewer() {
        // Check teacher role
        if (!"teacher".equalsIgnoreCase(this.role)) {
            JOptionPane.showMessageDialog(this,
                "Only teachers can access the delete function.",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        closeChildFrame();
        currentChildFrame = new JFrame("🗑️ Delete PDFs");
        currentChildFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.LIGHT_MINT, 0, getHeight(), Theme.BG_SECONDARY);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM,
            Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM
        ));

        // Header with title and back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.DEEP_TEAL);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new Theme.ShadowBorder(),
            BorderFactory.createEmptyBorder(
                Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM,
                Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM
            )
        ));

        JLabel titleLabel = new JLabel("Delete PDF Files");
        titleLabel.setFont(Theme.UI_TITLE);
        titleLabel.setForeground(Theme.TEXT_WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        JButton backBtn = Theme.createSecondaryButton("← Back to Dashboard");
        backBtn.addActionListener(evt -> {
            currentChildFrame.dispose();
            currentChildFrame = null;
        });
        headerPanel.add(backBtn, BorderLayout.WEST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // File Panel with delete mode
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBackground(Theme.BG_PRIMARY);
        FileHandler.listFiles(this.role, filePanel, this.branch, this.semester, true);
        mainPanel.add(filePanel, BorderLayout.CENTER);

        currentChildFrame.add(mainPanel);
        currentChildFrame.setSize(900, 600);
        currentChildFrame.setLocationRelativeTo(this);
        currentChildFrame.setVisible(true);
    }
}
