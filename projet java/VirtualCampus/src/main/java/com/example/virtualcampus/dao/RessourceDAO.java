package com.example.virtualcampus.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RessourceDAO {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/virtualcampus";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public List<Resource> getAllResources() throws SQLException {
        List<Resource> resources = new ArrayList<>();
        String query = "SELECT type, current_consumption, capacity FROM resources";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                double currentPercentage = rs.getDouble("current_consumption");
                double capacity = rs.getDouble("capacity");
                double actualConsumption = (currentPercentage / 100.0) * capacity;

                resources.add(new Resource(
                        rs.getString("type"),
                        actualConsumption,
                        capacity,
                        currentPercentage
                ));
            }
        }
        return resources;
    }

    public void updateConsumption(String type, double newConsumption) throws SQLException {
        String query = "UPDATE resources SET current_consumption = ? WHERE type = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setDouble(1, newConsumption);
            stmt.setString(2, type);
            stmt.executeUpdate();
        }
    }

    public void reduceConsumptionByPercentage(double percentage) throws SQLException {
        String query = "UPDATE resources SET current_consumption = current_consumption * ? " +
                "WHERE type != 'SPACE'";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setDouble(1, (100 - percentage) / 100);
            stmt.executeUpdate();
        }
    }

    public double getAverageUsage() throws SQLException {
        String query = "SELECT AVG(current_consumption) as avg_usage FROM resources WHERE type != 'SPACE'";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("avg_usage");
            }
            return 0;
        }
    }

    public List<ResourceUsage> getResourceUsageReport() throws SQLException {
        List<ResourceUsage> report = new ArrayList<>();
        String query = "SELECT type, current_consumption, capacity, " +
                "(current_consumption) as usage_percent " +
                "FROM resources ORDER BY current_consumption DESC";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                report.add(new ResourceUsage(
                        rs.getString("type"),
                        (rs.getDouble("current_consumption") / 100.0) * rs.getDouble("capacity"),
                        rs.getDouble("capacity"),
                        rs.getDouble("current_consumption")
                ));
            }
        }
        return report;
    }

    public static class Resource {
        private final String type;
        private final double currentConsumption;
        private final double capacity;
        private final double currentPercentage;

        public Resource(String type, double currentConsumption, double capacity, double currentPercentage) {
            this.type = type;
            this.currentConsumption = currentConsumption;
            this.capacity = capacity;
            this.currentPercentage = currentPercentage;
        }

        public String getType() { return type; }
        public double getCurrentConsumption() { return currentConsumption; }
        public double getCapacity() { return capacity; }
        public double getUsageRatio() { return currentPercentage / 100.0; }
        public double getCurrentPercentage() { return currentPercentage; }
    }

    public static class ResourceUsage extends Resource {
        public ResourceUsage(String type, double currentConsumption, double capacity, double usagePercent) {
            super(type, currentConsumption, capacity, usagePercent);
        }
    }
}