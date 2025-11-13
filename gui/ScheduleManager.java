package gui;

import db.DBConnection;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Schedule Management System - Visual Calendar Interface
 * 
 * This class provides a comprehensive calendar-based task visualization and 
 * management system for the StudySync application. It offers an intuitive
 * way to view and manage academic tasks across a monthly calendar view.
 * 
 * Visual Components:
 * 1. Calendar Display
 *    - Monthly grid view with date cells
 *    - Task indicators within date cells
 *    - Priority-based color coding
 *    - Status indicators (Pending/In Progress/Completed)
 * 
 * 2. Navigation Features
 *    - Month-to-month navigation
 *    - Current month/year display
 *    - Interactive date selection
 *    - Task detail popups
 * 
 * 3. Task Visualization
 *    - Color-coded priority indicators
 *    - Status-based text formatting
 *    - Multiple task support per day
 *    - Task ownership indication
 * 
 * Role-Based Features:
 * - Teachers: View all assigned and created tasks
 * - Students: View personal and assigned tasks
 * - Task detail access control
 * 
 * Integration Points:
 * - Database: Tasks and assignments
 * - TaskManager: Task creation and updates
 * - User Authentication: Role-based access
 * 
 * @see TaskManager
 * @see DBConnection
 * @see Task
 */
public class ScheduleManager extends JPanel {
    // Calendar UI Components
    private JPanel calendarPanel;         // Calendar grid display
    private JLabel monthLabel;            // Current month/year display
    private JButton prevBtn, nextBtn;     // Month navigation buttons
    private Calendar currentCalendar;     // Tracks current display date
    private Map<String, List<Task>> tasksMap; // Stores tasks by date (yyyy-MM-dd)

    private String currentUserEmail;
    private String currentUserRole; // "teacher" or "student"

    public ScheduleManager(String userEmail, String role) {
        this.currentUserEmail = userEmail;
        this.currentUserRole = role.toLowerCase();
        this.currentCalendar = Calendar.getInstance();
        this.tasksMap = new HashMap<>();

        setLayout(new BorderLayout(10, 10));

        // ----- Top panel -----
        JPanel topPanel = new JPanel(new BorderLayout());
        prevBtn = new JButton("<");
        nextBtn = new JButton(">");
        monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 16));

        topPanel.add(prevBtn, BorderLayout.WEST);
        topPanel.add(monthLabel, BorderLayout.CENTER);
        topPanel.add(nextBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ----- Calendar panel -----
        calendarPanel = new JPanel(new GridLayout(0, 7, 5, 5));
        add(calendarPanel, BorderLayout.CENTER);

        // Button actions
        prevBtn.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, -1);
            refreshCalendar();
        });

        nextBtn.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, 1);
            refreshCalendar();
        });

        // Initial load
        loadTasks();
        refreshCalendar();
    }

    // ---------------- TASK CLASS ----------------
    public class Task {
        int id;
        String title;
        String status;
        java.util.Date dueDate;
        Priority priority;
        String createdBy;

        public Task(int id, String title, String status, java.util.Date dueDate,
                    Priority priority, String createdBy) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.dueDate = dueDate;
            this.priority = priority;
            this.createdBy = createdBy;
        }

        public Priority getPriority() {
            return priority;
        }

        public Color getPriorityColor() {
            return switch (priority) {
                case HIGH -> Color.RED;
                case MEDIUM -> Color.ORANGE;
                case LOW -> Color.GREEN;
            };
        }
    }

    // ---------------- PRIORITY ENUM ----------------
    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    // ---------------- LOAD TASKS ----------------
    private void loadTasks() {
        tasksMap.clear();
        try (Connection conn = DBConnection.getConnection()) {
            // Build query based on user role
            StringBuilder sql = new StringBuilder(
                "SELECT t.*, b.branch_name " +
                "FROM tasks t " +
                "LEFT JOIN branches b ON t.branch_code = b.branch_code " +
                "WHERE 1=1 ");

            if (currentUserRole.equals("teacher")) {
                // Teachers see tasks they created
                sql.append("AND created_by = ? ");
            } else {
                // Students see tasks for their branch and semester
                sql.append("AND (created_by = ? OR (t.semester = " +
                         "(SELECT current_semester FROM users WHERE email = ?) " +
                         "AND t.branch_code = (SELECT branch_code FROM users WHERE email = ?))) ");
            }
            
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            if (currentUserRole.equals("teacher")) {
                stmt.setString(paramIndex++, currentUserEmail);
            } else {
                stmt.setString(paramIndex++, currentUserEmail); // for created_by
                stmt.setString(paramIndex++, currentUserEmail); // for semester check
                stmt.setString(paramIndex++, currentUserEmail); // for branch check
            }
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String status = rs.getString("status");
                String priorityStr = rs.getString("priority");
                String createdBy = rs.getString("created_by");

                java.sql.Date sqlDueDate = rs.getDate("due_date");
                java.util.Date dueDate = null;
                if (sqlDueDate != null) dueDate = new java.util.Date(sqlDueDate.getTime());

                String key = dueDate != null ? sdf.format(dueDate) : "";
                Priority priority = Priority.LOW;
                if (priorityStr != null) {
                    try {
                        priority = Priority.valueOf(priorityStr.toUpperCase());
                    } catch (IllegalArgumentException ignored) {}
                }

                Task task = new Task(id, title, status, dueDate, priority, createdBy);

                // Role-based visibility
                boolean shouldShow = false;
                if (currentUserRole.equals("teacher")) {
                    // Teachers see tasks they created
                    shouldShow = createdBy.equals(currentUserEmail);
                } else if (currentUserRole.equals("student")) {
                    // Students see tasks they created AND tasks assigned to their branch/semester
                    shouldShow = true; // We've already filtered by branch/semester in SQL query
                }
                
                if (shouldShow && !key.isEmpty()) {
                    tasksMap.computeIfAbsent(key, k -> new ArrayList<>()).add(task);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage());
        }
    }

    // ---------------- REFRESH CALENDAR ----------------
    private void refreshCalendar() {
        calendarPanel.removeAll();

        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMMM yyyy");
        monthLabel.setText(sdfMonth.format(currentCalendar.getTime()));

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel lbl = new JLabel(day, JLabel.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 12));
            calendarPanel.add(lbl);
        }

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i < firstDayOfWeek; i++) calendarPanel.add(new JLabel(""));

        SimpleDateFormat sdfKey = new SimpleDateFormat("yyyy-MM-dd");

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            String key = sdfKey.format(cal.getTime());

            JPanel dayPanel = new JPanel(new BorderLayout());
            dayPanel.setBorder(new LineBorder(Color.GRAY));
            JLabel dayLabel = new JLabel(String.valueOf(day), JLabel.CENTER);
            dayPanel.add(dayLabel, BorderLayout.NORTH);

            if (tasksMap.containsKey(key)) {
                JPanel tasksPanel = new JPanel();
                tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
                for (Task t : tasksMap.get(key)) {
                    JLabel taskLabel = new JLabel(t.title + " [" + t.status.toUpperCase() + "]");
                    taskLabel.setOpaque(true);
                    taskLabel.setBorder(new LineBorder(t.getPriorityColor(), 2));

                    switch (t.status.toLowerCase()) {
                        case "pending" -> taskLabel.setForeground(Color.RED);
                        case "completed" -> taskLabel.setForeground(Color.GREEN.darker());
                        case "in_progress" -> taskLabel.setForeground(Color.ORANGE.darker());
                        default -> taskLabel.setForeground(Color.BLACK);
                    }

                    tasksPanel.add(taskLabel);
                }
                dayPanel.add(tasksPanel, BorderLayout.CENTER);
            }

            dayPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showTasksDialog(key);
                }
            });

            calendarPanel.add(dayPanel);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    // ---------------- TASK DIALOG ----------------
    private void showTasksDialog(String dateKey) {
        List<Task> dayTasks = tasksMap.getOrDefault(dateKey, new ArrayList<>());
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Tasks on " + dateKey, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Task t : dayTasks) {
            listModel.addElement(t.title + " [" + t.status.toUpperCase() + "] (" + t.priority + ") - by " + t.createdBy);
        }

        JList<String> taskList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(taskList);

        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    // ---------------- SHOW FRAME ----------------
    public static void showSchedule(String userEmail, String role) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Schedule Manager");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(800, 600);
            frame.add(new ScheduleManager(userEmail, role));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
