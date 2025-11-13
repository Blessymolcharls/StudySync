package gui;

import javax.swing.*;
import java.awt.*;

public class ErrorHandler {
    // Error icons
    private static final String WARNING_ICON = "⚠";
    private static final String ERROR_ICON = "❌";
    private static final String SUCCESS_ICON = "✅";
    
    // Show warning message
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            WARNING_ICON + " " + message,
            "Warning",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    // Show error message
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            ERROR_ICON + " " + message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    // Show error message with detailed technical info
    public static void showError(Component parent, String message, Exception e) {
        String detailedMessage = ERROR_ICON + " " + message + "\n\nTechnical details:\n" + e.getMessage();
        JOptionPane.showMessageDialog(
            parent,
            detailedMessage,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    // Show success message
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(
            parent,
            SUCCESS_ICON + " " + message,
            "Success",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    // Show validation error for email format
    public static void showEmailFormatError(Component parent, String role) {
        String message;
        if ("teacher".equalsIgnoreCase(role)) {
            message = "Invalid teacher email format!\n" +
                     "Format: Only letters before @mgits.ac.in";
        } else {
            message = "Invalid student email format!\n" +
                     "Format: 2 digits + 2 lowercase letters + 3 digits + @mgits.ac.in";
        }
        showError(parent, message);
    }
    
    // Show missing fields error
    public static void showMissingFieldsError(Component parent) {
        showWarning(parent, "Please fill all required fields!");
    }
    
    // Show duplicate email error
    public static void showDuplicateEmailError(Component parent) {
        showWarning(parent, "Email already exists in the system!");
    }
    
    // Show invalid credentials error
    public static void showInvalidCredentialsError(Component parent) {
        showError(parent, "Invalid credentials!");
    }
    
    // Show role mismatch error
    public static void showRoleMismatchError(Component parent, String actualRole) {
        showError(parent, "Role mismatch! You are registered as " + actualRole + ".");
    }
    
    // Show success message with user details
    public static void showLoginSuccess(Component parent, String role, String branch, Integer semester) {
        String message = "Login successful as " + role;
        if ("student".equals(role) && branch != null && semester != null) {
            message += "\nBranch: " + branch + ", Semester: " + semester;
        }
        showSuccess(parent, message);
    }
    
    // Show registration success
    public static void showRegistrationSuccess(Component parent) {
        showSuccess(parent, "Registration successful!");
    }
}