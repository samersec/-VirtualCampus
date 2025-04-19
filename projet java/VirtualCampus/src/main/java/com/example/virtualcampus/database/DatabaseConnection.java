// File: src/main/java/com/example/virtualcampus/util/DatabaseConnection.java
package com.example.virtualcampus.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection;

    // Database configuration - modify these for your environment
    private static final String URL = "jdbc:mysql://localhost:3306/virtualcampus";
    private static final String USER = "root";
    private static final String PASSWORD = "password"; // Change to your DB password

    private DatabaseConnection() {} // Private constructor to prevent instantiation

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Create the connection
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                connection.setAutoCommit(true); // Enable auto-commit by default
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }
        return connection;
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}