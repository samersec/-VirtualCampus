package com.example.virtualcampus.dao;

import com.example.virtualcampus.Model.Batiment;
import javafx.scene.control.Alert;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BatimentDAO {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/virtualcampus";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static final Map<String, Integer> SPACE_ALLOCATION = new HashMap<>() {{
        put("Bibliotheque", 10);
        put("Salle", 5);
        put("Cafeteria", 8);
    }};

    private static final Map<String, Integer> SATISFACTION_IMPACT = new HashMap<>() {{
        put("Bibliotheque", 7);
        put("Salle", 8);
        put("Cafeteria", 6);
    }};

    public List<Batiment> getAllBatiments() {
        List<Batiment> batiments = new ArrayList<>();
        String query = "SELECT * FROM batiments";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Batiment batiment = new Batiment(
                        resultSet.getInt("id"),
                        resultSet.getString("nom"),
                        resultSet.getString("type"),
                        resultSet.getInt("capacite"),
                        resultSet.getDouble("conso_wifi"),
                        resultSet.getDouble("conso_elec"),
                        resultSet.getDouble("conso_eau"),
                        resultSet.getInt("impact_satisfaction")
                );
                batiments.add(batiment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching batiments: " + e.getMessage());
        }
        return batiments;
    }

    private ResourceInfo getResourceInfo(Connection connection, String resourceType) throws SQLException {
        String query = "SELECT current_consumption, capacity FROM resources WHERE type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, resourceType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double currentPercentage = rs.getDouble("current_consumption");
                double capacity = rs.getDouble("capacity");
                double actualConsumption = (currentPercentage / 100.0) * capacity;
                return new ResourceInfo(currentPercentage, capacity, actualConsumption);
            }
        }
        throw new SQLException("Resource not found: " + resourceType);
    }

    private boolean checkResourceAvailability(Connection connection, String resourceType, double additionalConsumption) throws SQLException {
        String query = "SELECT current_consumption, capacity FROM resources WHERE type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, resourceType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double currentPercentage = rs.getDouble("current_consumption");
                double capacity = rs.getDouble("capacity");
                double currentActual = (currentPercentage / 100.0) * capacity;
                double newActual = currentActual + additionalConsumption;
                return (newActual <= capacity);
            }
        }
        throw new SQLException("Resource not found: " + resourceType);
    }

    private void updateResource(Connection connection, String resourceType, double amount) throws SQLException {
        String query = "SELECT current_consumption, capacity FROM resources WHERE type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, resourceType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double currentPercentage = rs.getDouble("current_consumption");
                double capacity = rs.getDouble("capacity");
                double currentActual = (currentPercentage / 100.0) * capacity;
                double newActual = currentActual + amount;
                double newPercentage = (newActual / capacity) * 100.0;

                // Ensure percentage stays within bounds
                newPercentage = Math.max(0.0, Math.min(100.0, newPercentage));

                String updateQuery = "UPDATE resources SET current_consumption = ? WHERE type = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setDouble(1, newPercentage);
                    updateStmt.setString(2, resourceType);
                    updateStmt.executeUpdate();
                }
            }
        }
    }

    private static class ResourceInfo {
        final double currentPercentage;
        final double capacity;
        final double actualConsumption;

        ResourceInfo(double currentPercentage, double capacity, double actualConsumption) {
            this.currentPercentage = currentPercentage;
            this.capacity = capacity;
            this.actualConsumption = actualConsumption;
        }
    }

    public void addBatiment(Batiment batiment) {
        if (batiment.getNom() == null || batiment.getNom().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name cannot be empty!");
            return;
        }

        int spaceAllocation = SPACE_ALLOCATION.getOrDefault(batiment.getType(), 5);
        int satisfactionImpact = SATISFACTION_IMPACT.getOrDefault(batiment.getType(), 5);
        batiment.setImpactSatisfaction(satisfactionImpact);

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false);

            // Check resource availability
            if (!checkResourceAvailability(connection, "SPACE", spaceAllocation)) {
                throw new SQLException("Insufficient space available");
            }
            if (!checkResourceAvailability(connection, "WATER", batiment.getEauConsommation())) {
                ResourceInfo info = getResourceInfo(connection, "WATER");
                throw new SQLException(String.format("Insufficient water resources. Available: %.2f, Requested: %.2f",
                        info.capacity - info.actualConsumption, batiment.getEauConsommation()));
            }
            if (!checkResourceAvailability(connection, "ELECTRICITY", batiment.getElectriciteConsommation())) {
                throw new SQLException("Insufficient electricity resources");
            }
            if (!checkResourceAvailability(connection, "WIFI", batiment.getWifiConsommation())) {
                throw new SQLException("Insufficient WiFi resources");
            }

            String query = "INSERT INTO batiments (nom, type, capacite, conso_wifi, conso_elec, conso_eau, impact_satisfaction) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, batiment.getNom());
                pstmt.setString(2, batiment.getType());
                pstmt.setInt(3, batiment.getCapacite());
                pstmt.setDouble(4, batiment.getWifiConsommation());
                pstmt.setDouble(5, batiment.getElectriciteConsommation());
                pstmt.setDouble(6, batiment.getEauConsommation());
                pstmt.setInt(7, batiment.getImpactSatisfaction());

                pstmt.executeUpdate();

                updateResource(connection, "SPACE", spaceAllocation);
                updateResource(connection, "WATER", batiment.getEauConsommation());
                updateResource(connection, "ELECTRICITY", batiment.getElectriciteConsommation());
                updateResource(connection, "WIFI", batiment.getWifiConsommation());

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        batiment.setId(generatedKeys.getInt(1));
                    }
                }
            }

            connection.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Building added successfully and all resources updated!");

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.ERROR, "Error", "Error adding building: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateBatiment(Batiment batiment) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false);

            String getCurrentQuery = "SELECT type, conso_eau, conso_elec, conso_wifi FROM batiments WHERE id = ?";
            double oldWater = 0, oldElectricity = 0, oldWifi = 0;
            String oldType = "";

            try (PreparedStatement stmt = connection.prepareStatement(getCurrentQuery)) {
                stmt.setInt(1, batiment.getId());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    oldType = rs.getString("type");
                    oldWater = rs.getDouble("conso_eau");
                    oldElectricity = rs.getDouble("conso_elec");
                    oldWifi = rs.getDouble("conso_wifi");
                }
            }

            int oldSpace = SPACE_ALLOCATION.getOrDefault(oldType, 5);
            int newSpace = SPACE_ALLOCATION.getOrDefault(batiment.getType(), 5);
            int spaceAdjustment = newSpace - oldSpace;

            // Check resource availability for changes
            if (spaceAdjustment > 0 && !checkResourceAvailability(connection, "SPACE", spaceAdjustment)) {
                throw new SQLException("Insufficient space for new building type");
            }
            if (!checkResourceAvailability(connection, "WATER", batiment.getEauConsommation() - oldWater)) {
                ResourceInfo info = getResourceInfo(connection, "WATER");
                throw new SQLException(String.format("Insufficient water resources. Available: %.2f, Requested change: %.2f",
                        info.capacity - info.actualConsumption, batiment.getEauConsommation() - oldWater));
            }
            if (!checkResourceAvailability(connection, "ELECTRICITY", batiment.getElectriciteConsommation() - oldElectricity)) {
                throw new SQLException("Insufficient electricity resources");
            }
            if (!checkResourceAvailability(connection, "WIFI", batiment.getWifiConsommation() - oldWifi)) {
                throw new SQLException("Insufficient WiFi resources");
            }

            batiment.setImpactSatisfaction(SATISFACTION_IMPACT.getOrDefault(batiment.getType(), 5));

            String query = "UPDATE batiments SET nom=?, type=?, capacite=?, conso_wifi=?, conso_elec=?, conso_eau=?, " +
                    "impact_satisfaction=? WHERE id=?";

            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, batiment.getNom());
                pstmt.setString(2, batiment.getType());
                pstmt.setInt(3, batiment.getCapacite());
                pstmt.setDouble(4, batiment.getWifiConsommation());
                pstmt.setDouble(5, batiment.getElectriciteConsommation());
                pstmt.setDouble(6, batiment.getEauConsommation());
                pstmt.setInt(7, batiment.getImpactSatisfaction());
                pstmt.setInt(8, batiment.getId());

                pstmt.executeUpdate();

                if (spaceAdjustment != 0) {
                    updateResource(connection, "SPACE", spaceAdjustment);
                }
                updateResource(connection, "WATER", batiment.getEauConsommation() - oldWater);
                updateResource(connection, "ELECTRICITY", batiment.getElectriciteConsommation() - oldElectricity);
                updateResource(connection, "WIFI", batiment.getWifiConsommation() - oldWifi);
            }

            connection.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Building updated successfully and all resources adjusted!");

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.ERROR, "Error", "Error updating building: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void deleteBatiment(int id) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false);

            String getCurrentQuery = "SELECT type, conso_eau, conso_elec, conso_wifi FROM batiments WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(getCurrentQuery)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String type = rs.getString("type");
                    int spaceAllocation = SPACE_ALLOCATION.getOrDefault(type, 5);

                    updateResource(connection, "SPACE", -spaceAllocation);
                    updateResource(connection, "WATER", -rs.getDouble("conso_eau"));
                    updateResource(connection, "ELECTRICITY", -rs.getDouble("conso_elec"));
                    updateResource(connection, "WIFI", -rs.getDouble("conso_wifi"));
                }
            }

            String query = "DELETE FROM batiments WHERE id=?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            connection.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Building deleted successfully and resources freed!");

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.ERROR, "Error", "Error deleting building: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}