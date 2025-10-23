import java.sql.*;

/**
 * Database Connection Tester
 * 
 * Utility class to verify:
 * 1. Database connectivity
 * 2. Credentials validation
 * 3. Driver availability (MariaDB/MySQL)
 * 4. Connection string format
 */
public class DBTest {
    /**
     * Tests database connection with provided credentials
     * Supports both MariaDB and MySQL drivers
     */
    public static void main(String[] args) {
        // Connection parameters with SSL and timezone settings
        String jdbcURL = "jdbc:mariadb://localhost:3306/pdfshare?useSSL=false&serverTimezone=UTC";
        String dbUser = "pdfshare";
        String dbPassword = "toor";

        try {
            // Try MariaDB driver first, fall back to MySQL if not available
            try {
                Class.forName("org.mariadb.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.out.println("MariaDB driver not found, falling back to MySQL driver...");
                Class.forName("com.mysql.cj.jdbc.Driver");
            }
            Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
            System.out.println("✅ Connection successful!");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
