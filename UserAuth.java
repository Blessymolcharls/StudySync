/**
 * User Authentication and Registration Interface
 * 
 * This class provides the login and registration functionality for StudySync.
 * It implements a secure authentication system with role-based access control
 * for teachers and students.
 * 
 * Features:
 * - User login with role validation
 * - New user registration
 * - Email format validation for institutional emails
 * - Password security with show/hide option
 * - Role-specific access control
 * 
 * Email Format Rules:
 * - Teachers: letters@mgits.ac.in
 * - Students: YYbbNNN@mgits.ac.in (YY=year, bb=branch, NNN=number)
 * 
 * Security Features:
 * - Email format validation
 * - Role verification
 * - Password masking
 * 
 * @see Dashboard
 * @see DBConnection
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Map;
import java.util.HashMap;
import gui.Theme;
import gui.ErrorHandler;
import gui.AnimatedLogo;
import db.DBConnection;

public class UserAuth extends JFrame {


    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JComboBox<String> branchBox;
    private JComboBox<Integer> semesterBox;
    private JCheckBox showPassword;
    private JPanel studentDetailsPanel;
    private JButton loginBtn;
    private JButton registerBtn;

    public UserAuth() {
        setTitle("StudySync - Login");
        setSize(440, 1300);  // Reduced height to make form more compact
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create main panel with gradient background
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.DEEP_TEAL, 0, getHeight(), Theme.LIGHT_MINT);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // 0.5cm border (roughly 15 pixels)

        // Create content panel with white background
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_SECONDARY);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new Theme.RoundedBorder(Theme.RADIUS_LARGE, Theme.BORDER_NORMAL),
            BorderFactory.createEmptyBorder(25, 35, 25, 35)
        ));
        
        // Create action buttons with StudySync branding (assign to class fields to avoid shadowing)
        loginBtn = Theme.createPrimaryButton("Login");
        registerBtn = Theme.createSecondaryButton("Register");
        
        // Add hover effect cursor
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Set preferred size for buttons
        Dimension buttonSize = new Dimension(130, 35);
        loginBtn.setPreferredSize(buttonSize);
        registerBtn.setPreferredSize(buttonSize);
        
        // Center align buttons
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);

        // Logo and Title (centered at the top)
        ImageIcon logoIcon = new ImageIcon("Logo.png");
        Image scaledImage = logoIcon.getImage().getScaledInstance(120, 60, Image.SCALE_SMOOTH); // Smaller logo
        AnimatedLogo logoLabel = new AnimatedLogo(new ImageIcon(scaledImage));
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);
        logoLabel.startAnimation();

        // Welcome Text
        JLabel welcomeLabel = new JLabel("Welcome to StudySync");
        welcomeLabel.setFont(Theme.UI_TITLE.deriveFont(18f));  // Slightly smaller title
        welcomeLabel.setForeground(Theme.TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Basic Fields Panel
        JPanel basicFieldsPanel = new JPanel();
        basicFieldsPanel.setLayout(new BoxLayout(basicFieldsPanel, BoxLayout.Y_AXIS));
        basicFieldsPanel.setBackground(Theme.BG_SECONDARY);
        basicFieldsPanel.setAlignmentX(CENTER_ALIGNMENT);
        basicFieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));  // Reduced padding

        // Email field with label
        JLabel emailLabel = createLabel("Email ID");
        emailField = createTextField("");
        emailField.setToolTipText("Enter your institutional email");
        emailField.setMaximumSize(new Dimension(250, 30));
        emailField.setMinimumSize(new Dimension(250, 30));

        // Password field with label
        JLabel passwordLabel = createLabel("Password");
        passwordField = createPasswordField("");
        passwordField.setMaximumSize(new Dimension(250, 30));
        passwordField.setMinimumSize(new Dimension(250, 30));
        
        // Show Password option
        showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(Theme.BG_SECONDARY);
        showPassword.setForeground(Theme.TEXT_SECONDARY);
        showPassword.setFont(Theme.UI_SMALL);
        showPassword.setAlignmentX(CENTER_ALIGNMENT);
        showPassword.setHorizontalAlignment(SwingConstants.CENTER);
        showPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showPassword.addActionListener(this::toggleShowPassword);

        // Role selection with label
        JLabel roleLabel = createLabel("Role");
        roleBox = new JComboBox<>(new String[]{"Select Role", "student", "teacher"});
        roleBox.setSelectedIndex(0);
        styleComboBox(roleBox);
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.INPUT_HEIGHT));

        // Student details panel (initially hidden)
        studentDetailsPanel = new JPanel();
        studentDetailsPanel.setLayout(new BoxLayout(studentDetailsPanel, BoxLayout.Y_AXIS));
        studentDetailsPanel.setBackground(Theme.BG_SECONDARY);
        studentDetailsPanel.setAlignmentX(CENTER_ALIGNMENT);
        studentDetailsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));  // Reduced from 10
        studentDetailsPanel.setVisible(false);

        // Branch selection with label
        JLabel branchLabel = createLabel("Branch");
        branchBox = new JComboBox<>();
        loadBranchesFromDatabase(); // Load branch names from database
        styleComboBox(branchBox);

        // Semester selection with label
        JLabel semesterLabel = createLabel("Semester");
        semesterBox = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});
        styleComboBox(semesterBox);

        // Add components to student details panel
        studentDetailsPanel.add(branchLabel);
        studentDetailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));  // Reduced from 5
        studentDetailsPanel.add(branchBox);
        studentDetailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));  // Reduced from 15
        studentDetailsPanel.add(semesterLabel);
        studentDetailsPanel.add(Box.createRigidArea(new Dimension(0, 4)));  // Reduced from 5
        studentDetailsPanel.add(semesterBox);

        // Create panels for each input field to ensure proper centering
        JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        emailPanel.setBackground(Theme.BG_SECONDARY);
        emailPanel.add(emailField);

        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        passwordPanel.setBackground(Theme.BG_SECONDARY);
        passwordPanel.add(passwordField);

        JPanel showPasswordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        showPasswordPanel.setBackground(Theme.BG_SECONDARY);
        showPasswordPanel.add(showPassword);

        // Add components to basic fields panel
        basicFieldsPanel.add(emailLabel);
        basicFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        basicFieldsPanel.add(emailPanel);
        basicFieldsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        basicFieldsPanel.add(passwordLabel);
        basicFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        basicFieldsPanel.add(passwordPanel);
        basicFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        basicFieldsPanel.add(showPasswordPanel);
        basicFieldsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        basicFieldsPanel.add(roleLabel);
        basicFieldsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        basicFieldsPanel.add(roleBox);

        // Show/hide student details based on role selection
        loginBtn.setVisible(true);
        registerBtn.setVisible(true);
        
        roleBox.addActionListener(e -> {
            boolean isStudent = "student".equals(roleBox.getSelectedItem());
            studentDetailsPanel.setVisible(isStudent);
            loginBtn.setVisible(true);
            registerBtn.setVisible(true);
            pack(); // Dynamically adjust frame size
            panel.revalidate();
            panel.repaint();
        });

        // Attach action listeners to the already-created buttons
        loginBtn.addActionListener(this::onLogin);
        registerBtn.addActionListener(this::onRegister);

        // Button Panel with modern layout
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));  // Reduced from 20
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        // Calculate initial spacing to ensure consistent total height
        int topSpacing = 10;      // Reduced spacing
        int logoSpacing = 8;      // Reduced spacing
        int welcomeSpacing = 15;  // Reduced spacing
        
        // Add all components to main panel with proper spacing
        panel.add(Box.createVerticalStrut(topSpacing));
        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(logoSpacing));
        panel.add(welcomeLabel);
        panel.add(Box.createVerticalStrut(welcomeSpacing));
        panel.add(basicFieldsPanel);
        panel.add(studentDetailsPanel);
        panel.add(Box.createVerticalStrut(15));  // Reduced bottom spacing
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(10));

        // Add main panel to background panel
        backgroundPanel.add(panel, BorderLayout.CENTER);
        add(backgroundPanel);
        setVisible(true);
        pack(); // Adjust frame size to content
    }

    // ✅ Styled text field with StudySync branding
    // ✅ Styled text field with proper visibility and padding
private JTextField createTextField(String placeholder) {
    JTextField field = new JTextField(20);
    field.setPreferredSize(new Dimension(250, 35));
    field.setFont(Theme.UI_FONT);
    field.setBackground(Color.WHITE);
    field.setForeground(Color.BLACK);  // ensure visible text
    field.setCaretColor(Theme.GOLDEN_YELLOW);   // gold cursor for branding

    // Add visible border and internal padding
    field.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Theme.DEEP_TEAL, 2, true),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));

    field.setAlignmentX(CENTER_ALIGNMENT);
    field.setHorizontalAlignment(JTextField.LEFT);

    // Hover highlight
    field.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GOLDEN_YELLOW, 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.DEEP_TEAL, 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }
    });

    return field;
}


    // ✅ Styled password field with StudySync branding
    // ✅ Styled password field with visibility and padding
private JPasswordField createPasswordField(String placeholder) {
    JPasswordField field = new JPasswordField(20);
    field.setPreferredSize(new Dimension(250, 35));
    field.setFont(Theme.UI_FONT);
    field.setBackground(Color.WHITE);
    field.setForeground(Color.BLACK);  // ensure visible text
    field.setCaretColor(Theme.GOLDEN_YELLOW);
    field.setEchoChar('•');

    // Border + padding
    field.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Theme.DEEP_TEAL, 2, true),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));

    field.setAlignmentX(CENTER_ALIGNMENT);
    field.setHorizontalAlignment(JTextField.LEFT);

    // Hover highlight
    field.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.GOLDEN_YELLOW, 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.DEEP_TEAL, 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }
    });

    return field;
}

    
    // ✅ Modern rounded gradient button
    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setAlignmentX(CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(300, 20));
        return label;
    }

    // Helper method to style combo boxes
    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setMaximumSize(new Dimension(250, 30));
        comboBox.setPreferredSize(new Dimension(250, 30));
        comboBox.setFont(Theme.UI_FONT);
        comboBox.setBackground(Theme.BG_SECONDARY);
        comboBox.setForeground(Theme.TEXT_PRIMARY);
        comboBox.setBorder(new Theme.RoundedBorder(8, Theme.DEEP_TEAL));
        comboBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        comboBox.setAlignmentX(CENTER_ALIGNMENT);

        // Style the renderer
        if (comboBox.getRenderer() instanceof JComponent) {
            JComponent renderer = (JComponent) comboBox.getRenderer();
            renderer.setBackground(Theme.BG_SECONDARY);
            renderer.setForeground(Theme.TEXT_PRIMARY);
            renderer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }
    }



    // ActionEvent handlers to avoid unused-lambda-parameter warnings
    private void toggleShowPassword(ActionEvent e) {
        passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•');
    }

    private void onLogin(ActionEvent e) {
        login();
    }

    private void onRegister(ActionEvent e) {
        register();
    }

    // ✅ Database Connection
    private Connection connect() throws Exception {
        return DBConnection.getConnection();
    }

    // ✅ Register logic
    private void register() {
    String email = this.emailField.getText().trim();
    String password = new String(this.passwordField.getPassword());
    String selectedRole = ((String) this.roleBox.getSelectedItem()).toLowerCase();
    
    if (selectedRole.equals("select role")) {
        ErrorHandler.showError(this, "Please select a role");
        return;
    }
    
    String branch = "student".equals(selectedRole) ? (String) this.branchBox.getSelectedItem() : null;
    Integer semester = "student".equals(selectedRole) ? (Integer) this.semesterBox.getSelectedItem() : null;

    if (email.isEmpty() || password.isEmpty() || 
        ("student".equals(selectedRole) && (branch == null || semester == null))) {
        ErrorHandler.showMissingFieldsError(this);
        return;
    }

    
// ✅ STEP 1: Email format check
if ("teacher".equalsIgnoreCase(selectedRole)) {
    // Teachers must have email with only letters before @mgits.ac.in
    if (!email.matches("^[a-zA-Z]+@mgits\\.ac\\.in$")) {
        JOptionPane.showMessageDialog(this,
            "❌ Invalid teacher email format!\n" +
            "Format: Only letters before @mgits.ac.in",
            "Invalid Email",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }
} else if ("student".equalsIgnoreCase(selectedRole)) {
    // Students must use proper format with digits and lowercase letters
    if (!email.matches("^\\d{2}[a-z]{2}\\d{3}@mgits\\.ac\\.in$")) {
        JOptionPane.showMessageDialog(this,
            "❌ Invalid student email format!\n" +
            "Format: 2 digits + 2 lowercase letters + 3 digits + @mgits.ac.in",
            "Invalid Email",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }
}

    

    // ✅ STEP 2: First check if email exists
    try (Connection conn = this.connect()) {
        // Check if email already exists
        String checkSql = "SELECT COUNT(*) FROM users WHERE email = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, email);
        ResultSet rs = checkStmt.executeQuery();
        rs.next();
        if (rs.getInt(1) > 0) {
            ErrorHandler.showDuplicateEmailError(this);
            return;
        }

        // If email doesn't exist, proceed with registration
        String sql = "INSERT INTO users (email, password, role, branch_code, current_semester) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, email);
        stmt.setString(2, password);
        stmt.setString(3, selectedRole);  // Already converted to lowercase
        stmt.setString(4, branch != null ? branchNameToCodeMap.get(branch) : null);
        stmt.setObject(5, semester); // setObject handles null for non-student roles
        stmt.executeUpdate();
        ErrorHandler.showRegistrationSuccess(this);
    } catch (SQLException e) {
        if (e.getMessage().contains("Duplicate entry")) {
            ErrorHandler.showDuplicateEmailError(this);
        } else {
            ErrorHandler.showError(this, "SQL Error", e);
        }
    } catch (Exception e) {
        ErrorHandler.showError(this, "Error occurred", e);
    }
}

    // ✅ Login logic with role restriction
    private boolean validateEmailFormat(String email, String role) {
        if ("teacher".equalsIgnoreCase(role)) {
            if (!email.matches("^[a-zA-Z]+@mgits\\.ac\\.in$")) {
                ErrorHandler.showEmailFormatError(this, "teacher");
                return false;
            }
        } else if ("student".equalsIgnoreCase(role)) {
            if (!email.matches("^\\d{2}[a-z]{2}\\d{3}@mgits\\.ac\\.in$")) {
                ErrorHandler.showEmailFormatError(this, "student");
                return false;
            }
        }
        return true;
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String selectedRole = (String) roleBox.getSelectedItem();

        if (email.isEmpty() || password.isEmpty()) {
            ErrorHandler.showMissingFieldsError(this);
            return;
        }

        // First validate email format
        if (!validateEmailFormat(email, selectedRole)) {
            return;
        }

        try (Connection conn = connect()) {
            String sql = "SELECT u.role, " +
                   "CASE WHEN u.role = 'student' THEN u.branch_code ELSE NULL END as branch_code, " +
                   "CASE WHEN u.role = 'student' THEN COALESCE(b.branch_name, '') ELSE NULL END as branch_name, " +
                   "CASE WHEN u.role = 'student' THEN u.current_semester ELSE NULL END as current_semester " +
                   "FROM users u " +
                   "LEFT JOIN branches b ON u.branch_code = b.branch_code " +
                   "WHERE u.email=? AND u.password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String actualRole = rs.getString("role");
                String branch = rs.getString("branch_name");
                Integer semester = rs.getObject("current_semester", Integer.class);

                if (!selectedRole.equals(actualRole)) {
                    ErrorHandler.showRoleMismatchError(this, actualRole);
                    return;
                }

                ErrorHandler.showLoginSuccess(this, actualRole, branch, semester);
                
                dispose();
                new Dashboard(email, actualRole, branch, semester); // Pass branch/semester to Dashboard
            } else {
                ErrorHandler.showInvalidCredentialsError(this);
            }
        } catch (Exception e) {
            ErrorHandler.showError(this, "Error occurred", e);
        }
    }

    private Map<String, String> branchNameToCodeMap = new HashMap<>();

    private void loadBranchesFromDatabase() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT branch_code, branch_name FROM branches WHERE is_active = TRUE ORDER BY branch_name")) {

            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            branchNameToCodeMap.clear();
            while (rs.next()) {
                String branchName = rs.getString("branch_name");
                String branchCode = rs.getString("branch_code");
                if (branchName != null && branchCode != null) {
                    model.addElement(branchName);
                    branchNameToCodeMap.put(branchName, branchCode);
                }
            }
            branchBox.setModel(model);
        } catch (Exception e) {
            ErrorHandler.showError(this, "Error loading branches", e);
            branchBox.setModel(new DefaultComboBoxModel<>(new String[]{
                "CSE AI", "AIDS", "CSE CY", "CSE",
                "EEE", "ECE", "MECH", "CIVIL"
            }));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UserAuth::new);
    }
}
