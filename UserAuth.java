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

public class UserAuth extends JFrame {
    // ✅ Database credentials
    private static final String URL = "jdbc:mariadb://localhost:3306/pdfshare";
    private static final String USER = "pdfshare";
    private static final String PASS = "toor";

    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;
    private JCheckBox showPassword;

    public UserAuth() {
        setTitle("📘 StudySync Login / Register");
        setSize(420, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 25, 25));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        // Title
        JLabel title = new JLabel("📘 StudySync");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(0, 191, 255));
        title.setAlignmentX(CENTER_ALIGNMENT);

        // Fields
        emailField = createTextField("Email");
        passwordField = createPasswordField("Password");

        // Show Password
        showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(new Color(25, 25, 25));
        showPassword.setForeground(Color.WHITE);
        showPassword.addActionListener(this::toggleShowPassword);

        // Role dropdown
        roleBox = new JComboBox<>(new String[]{"student", "teacher"});
        roleBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        roleBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Buttons
        JButton loginBtn = createButton("Login", new Color(0, 123, 255), new Color(0, 150, 255));
        JButton registerBtn = createButton("Register", new Color(40, 167, 69), new Color(60, 190, 90));

        loginBtn.addActionListener(this::onLogin);
        registerBtn.addActionListener(this::onRegister);

        // Button Panel (Side-by-side)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(25, 25, 25));
        buttonPanel.setLayout(new GridLayout(1, 2, 15, 0)); // side-by-side layout
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        // Add components
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(emailField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(showPassword);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(roleBox);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(buttonPanel);

        add(panel);
        setVisible(true);
    }

    // ✅ Styled text field
    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return field;
    }

    // ✅ Styled password field
    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return field;
    }

    // ✅ Modern rounded gradient button
    // ✅ Modern rounded gradient button
    private JButton createButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, baseColor.brighter(), 0, getHeight(), baseColor.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            public void mouseExited(MouseEvent e) { button.setBackground(baseColor); }
        });

        return button;
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
        Class.forName("org.mariadb.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // ✅ Register logic
    private void register() {
    String email = this.emailField.getText().trim();
    String password = new String(this.passwordField.getPassword());
    String role = (String) this.roleBox.getSelectedItem();

    if (email.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "⚠ Please fill all fields!");
        return;
    }

    
// ✅ STEP 1: Email format check
if ("teacher".equalsIgnoreCase(role)) {
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
} else if ("student".equalsIgnoreCase(role)) {
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
            JOptionPane.showMessageDialog(this, "⚠ Email already exists!");
            return;
        }

        // If email doesn't exist, proceed with registration
        String sql = "INSERT INTO users (email, password, role) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, email);
        stmt.setString(2, password);
        stmt.setString(3, role);
        stmt.executeUpdate();
        JOptionPane.showMessageDialog(this, "✅ Registered successfully!");
    } catch (SQLException e) {
        if (e.getMessage().contains("Duplicate entry")) {
            JOptionPane.showMessageDialog(this, "⚠ Email already exists!");
        } else {
            JOptionPane.showMessageDialog(this, "❌ SQL Error: " + e.getMessage());
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
    }
}

    // ✅ Login logic with role restriction
    private boolean validateEmailFormat(String email, String role) {
        if ("teacher".equalsIgnoreCase(role)) {
            if (!email.matches("^[a-zA-Z]+@mgits\\.ac\\.in$")) {
                JOptionPane.showMessageDialog(this,
                    "❌ Invalid teacher email format!\n" +
                    "Format: Only letters before @mgits.ac.in",
                    "Invalid Email Format",
                    JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        } else if ("student".equalsIgnoreCase(role)) {
            if (!email.matches("^\\d{2}[a-z]{2}\\d{3}@mgits\\.ac\\.in$")) {
                JOptionPane.showMessageDialog(this,
                    "❌ Invalid student email format!\n" +
                    "Format: 2 digits + 2 lowercase letters + 3 digits + @mgits.ac.in",
                    "Invalid Email Format",
                    JOptionPane.ERROR_MESSAGE
                );
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
            JOptionPane.showMessageDialog(this, "⚠ Please enter credentials!");
            return;
        }

        // First validate email format
        if (!validateEmailFormat(email, selectedRole)) {
            return;
        }

        try (Connection conn = connect()) {
            String sql = "SELECT role FROM users WHERE email=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String actualRole = rs.getString("role");

                if (!selectedRole.equals(actualRole)) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Role mismatch! You are registered as " + actualRole + ".");
                    return;
                }

                JOptionPane.showMessageDialog(this, "✅ Login successful as " + actualRole);
                dispose();
                new Dashboard(email, actualRole); // Create your Dashboard class
            } else {
                JOptionPane.showMessageDialog(this, "❌ Invalid credentials!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UserAuth::new);
    }
}
