package db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;

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
        try (Connection conn = DBConnection.getConnection()) {
            // Read the schema file
            StringBuilder schema = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader("db/schema.sql"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    schema.append(line).append("\n");
                }
            }

            // Split and execute SQL statements
            String[] statements = schema.toString().split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String sql : statements) {
                    if (!sql.trim().isEmpty()) {
                        stmt.execute(sql.trim());
                    }
                }
            }

            System.out.println("Database schema updated successfully");
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
            throw e;
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