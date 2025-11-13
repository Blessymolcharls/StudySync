package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

/**
 * Database Initialization Utility
 * 
 * This class handles database schema creation and updates
 * Executes SQL scripts to set up necessary tables and relationships
 */
public class DBInit {
    /**
     * Initialize or update the database schema
     * 
     * @throws Exception if initialization fails
     */
    public static void initializeDatabase() throws Exception {
        // Create a backup of the current schema if it exists
        backupCurrentSchema();
        
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction
            
            // Read the schema file
            StringBuilder schema = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader("db/schema.sql"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("--")) {  // Skip comments
                        schema.append(line).append("\n");
                    }
                }
            }

            // Split and execute SQL statements
            String[] statements = schema.toString().split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    sql = sql.trim();
                    if (!sql.isEmpty()) {
                        try {
                            stmt.execute(sql);
                        } catch (SQLException e) {
                            System.err.println("Error executing SQL: " + sql);
                            throw e;
                        }
                    }
                }
            }
            
            conn.commit();  // Commit transaction
            System.out.println("Database schema updated successfully");
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();  // Rollback on error
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            System.err.println("Error initializing database: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    private static void backupCurrentSchema() {
        try (Connection conn = DBConnection.getConnection()) {
            String backupFile = "db/schema.sql.backup";
            
            // Generate schema dump
            StringBuilder dump = new StringBuilder();
            dump.append("-- StudySync Database Schema Backup\n");
            dump.append("-- Generated: ").append(new java.util.Date()).append("\n\n");
            
            // Get table list
            ResultSet tables = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                
                // Get table creation SQL
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + tableName);
                    if (rs.next()) {
                        dump.append("\n").append(rs.getString(2)).append(";\n");
                    }
                }
            }
            
            // Write backup file
            java.nio.file.Files.write(
                java.nio.file.Paths.get(backupFile),
                dump.toString().getBytes()
            );
            
            System.out.println("Schema backup created successfully: " + backupFile);
        } catch (Exception e) {
            System.err.println("Warning: Could not create schema backup: " + e.getMessage());
            // Continue without backup - don't throw
        }
    }

    public static void main(String[] args) {
        try {
            initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}