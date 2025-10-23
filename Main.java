/**
 * StudySync - Academic Task Management System
 * Entry point for the application
 * 
 * This is the main application class that initializes the StudySync system.
 * StudySync is designed to facilitate academic task management and collaboration
 * between teachers and students.
 * 
 * Key Features:
 * - PDF Management: Upload, view, and download study materials
 * - Task Management: Create, track, and complete academic tasks
 * - User Authentication: Separate interfaces for teachers and students
 * - Study Schedule Planning: Organize study sessions and deadlines
 * - Pomodoro Timer: Implement focused study sessions
 * 
 * Architecture:
 * - GUI: Swing-based user interface
 * - Database: MariaDB for data persistence
 * - Authentication: Role-based access control
 * 
 * @version 1.0
 * @since 2025-10-22
 */
public class Main {
    /**
     * Main entry point - Launches the application login window
     * Uses SwingUtilities.invokeLater for thread safety in GUI creation
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new UserAuth());
    }
}

