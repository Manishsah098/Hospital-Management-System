package com.smartcare.util;

import com.smartcare.exception.DatabaseException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reusable Singleton & Factory for managing JDBC Database Connections in SmartCare.
 * Provides connection pooling support, schema initialization, and transaction management.
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/smartcare_hospital?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    static {
        // Load MySQL Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found in classpath!", e);
        }

        refreshConfiguration();
    }

    public static void refreshConfiguration() {
        dbUrl = ConfigLoader.get("db.url", DEFAULT_URL);
        dbUser = ConfigLoader.get("db.user", DEFAULT_USER);
        dbPassword = ConfigLoader.get("db.password", DEFAULT_PASSWORD);
    }

    /**
     * Gets a new JDBC Connection to the SmartCare database.
     * @return Connection object
     * @throws DatabaseException if connection fails
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to MySQL database at: " + dbUrl, e);
            throw new DatabaseException("Cannot connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Tests whether database connection is active and reachable.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes AutoCloseable resources safely without throwing checked exceptions.
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable res : resources) {
            if (res != null) {
                try {
                    res.close();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error closing resource: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Rolls back a transaction safely.
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Transaction rollback failed: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Helper to check if tables exist and initialize database if required.
     */
    public static synchronized void initializeDatabaseIfRequired() {
        // Check if database exists by connecting to base MySQL server first if needed
        String baseUrl = dbUrl.replaceAll("/smartcare_hospital.*", "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        
        try (Connection serverConn = DriverManager.getConnection(baseUrl, dbUser, dbPassword);
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS smartcare_hospital CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            LOGGER.info("Verified database 'smartcare_hospital' existence.");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Notice while checking base database: " + e.getMessage());
        }

        // Now check if 'users' table exists in smartcare_hospital
        try (Connection conn = getConnection()) {
            boolean tablesExist = false;
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "users", null)) {
                if (rs.next()) {
                    tablesExist = true;
                }
            }

            if (!tablesExist) {
                LOGGER.info("Tables not found. Initializing database schema and seed data...");
                executeSqlScript(conn, "database/schema.sql");
                LOGGER.info("Database schema initialized successfully.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Auto-initialization check skipped or failed: " + e.getMessage());
        }
    }

    /**
     * Executes SQL script file from filesystem or resource stream.
     */
    public static void executeSqlScript(Connection conn, String scriptPath) {
        try {
            InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream(scriptPath);
            if (is == null) {
                // Try direct file path
                java.io.File file = new java.io.File(scriptPath);
                if (file.exists()) {
                    is = new java.io.FileInputStream(file);
                }
            }

            if (is == null) {
                LOGGER.warning("Could not find SQL script: " + scriptPath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                 Statement stmt = conn.createStatement()) {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("--") || line.startsWith("/*") || line.isEmpty()) {
                        continue;
                    }
                    sql.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String statementToRun = sql.toString().trim();
                        // Remove trailing semicolon
                        statementToRun = statementToRun.substring(0, statementToRun.length() - 1);
                        if (!statementToRun.equalsIgnoreCase("USE smartcare_hospital")) {
                            stmt.execute(statementToRun);
                        }
                        sql.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed executing SQL script: " + scriptPath, e);
        }
    }
}
