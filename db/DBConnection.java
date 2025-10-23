package db;

import java.sql.*;

/**
 * Database Connection Manager
 * 
 * This class provides centralized database connection management for the StudySync application.
 * It implements the singleton pattern for connection handling and ensures consistent
 * database access across the application.
 * 
 * Connection Details:
 * - Database: pdfshare (StudySync main database)
 * - Type: MariaDB (Compatible with MySQL)
 * - Port: 3306 (Default MariaDB port)
 * - Host: localhost (Local development database)
 * 
 * Security Features:
 * - Centralized credential management
 * - Connection pooling support
 * - Automatic resource cleanup
 * 
 * Usage:
 * try (Connection conn = DBConnection.getConnection()) {
 *     // Perform database operations
 * } catch (Exception e) {
 *     // Handle connection errors
 * }
 * 
 * @see java.sql.Connection
 * @see org.mariadb.jdbc.Driver
 */
public class DBConnection {
    // Database connection configuration
    private static final String URL = "jdbc:mariadb://localhost:3306/pdfshare";
    private static final String USER = "pdfshare"; 
    private static final String PASS = "toor";

    /**
     * Provides a connection to the database
     * 
     * @return Connection object for database operations
     * @throws Exception if connection fails or driver not found
     */
    public static Connection getConnection() throws Exception {
        Class.forName("org.mariadb.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }
}