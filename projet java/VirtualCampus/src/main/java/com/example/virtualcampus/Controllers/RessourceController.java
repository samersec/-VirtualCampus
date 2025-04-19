package com.example.virtualcampus.Controllers;

import com.example.virtualcampus.dao.RessourceDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import java.sql.SQLException;
import java.util.List;

public class RessourceController {
    private final RessourceDAO RessourceDAO = new RessourceDAO();

    @FXML private ProgressBar electricityProgress;
    @FXML private ProgressBar waterProgress;
    @FXML private ProgressBar wifiProgress;
    @FXML private ProgressBar spaceProgress;

    @FXML private Label electricityLabel;
    @FXML private Label waterLabel;
    @FXML private Label wifiLabel;
    @FXML private Label spaceLabel;

    @FXML private BarChart<String, Number> resourceChart;

    @FXML private Button optimizeBtn;
    @FXML private Button calculateBtn;
    @FXML private Button reportBtn;

    @FXML
    private void initialize() {
        updateResourceDisplay();

        optimizeBtn.setOnAction(e -> optimizeResources());
        calculateBtn.setOnAction(e -> calculateConsumption());
        reportBtn.setOnAction(e -> generateReport());
    }

    private String getStatusColor(double percentage) {
        if (percentage >= 0.7 || percentage < 0.1) {  // CRITICAL (≥70% or <10%)
            return "-fx-accent: #ff0000;";  // Red
        } else if (percentage < 0.4) {      // OPTIMIZED (0-39%)
            return "-fx-accent: #00bfff;";  // Blue
        } else {                            // NORMAL (40-69%)
            return "-fx-accent: #00ff00;";  // Green
        }
    }
    private void updateResourceDisplay() {
        try {
            List<RessourceDAO.Resource> resources = RessourceDAO.getAllResources();

            for (RessourceDAO.Resource resource : resources) {
                double remaining = resource.getCapacity() - resource.getCurrentConsumption();
                String displayText = String.format("%.1f/%.1f (%.1f left)",
                        resource.getCurrentConsumption(),
                        resource.getCapacity(),
                        remaining);
                double usageRatio = resource.getUsageRatio();
                String colorStyle = getStatusColor(usageRatio);

                switch (resource.getType()) {
                    case "ELECTRICITY":
                        electricityProgress.setProgress(usageRatio);
                        electricityProgress.setStyle(colorStyle);
                        electricityLabel.setText(displayText);
                        break;
                    case "WATER":
                        waterProgress.setProgress(usageRatio);
                        waterProgress.setStyle(colorStyle);
                        waterLabel.setText(displayText);
                        break;
                    case "WIFI":
                        wifiProgress.setProgress(usageRatio);
                        wifiProgress.setStyle(colorStyle);
                        wifiLabel.setText(displayText);
                        break;
                    case "SPACE":
                        spaceProgress.setProgress(usageRatio);
                        spaceProgress.setStyle(colorStyle);
                        spaceLabel.setText(displayText);
                        break;
                }
            }

            updateResourceChart();

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to update resource display: " + e.getMessage());
        }
    }

    private void updateResourceChart() {
        resourceChart.getData().clear();

        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName("Current Usage");

        XYChart.Series<String, Number> capacitySeries = new XYChart.Series<>();
        capacitySeries.setName("Total Capacity");

        try {
            List<RessourceDAO.Resource> resources = RessourceDAO.getAllResources();

            for (RessourceDAO.Resource resource : resources) {
                currentSeries.getData().add(new XYChart.Data<>(
                        resource.getType(),
                        resource.getCurrentConsumption()));
                capacitySeries.getData().add(new XYChart.Data<>(
                        resource.getType(),
                        resource.getCapacity()));
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to update chart: " + e.getMessage());
        }

        resourceChart.getData().addAll(currentSeries, capacitySeries);
    }

    @FXML
    private void optimizeResources() {
        try {
            RessourceDAO.reduceConsumptionByPercentage(10);
            updateResourceDisplay();
            showAlert(AlertType.INFORMATION, "Optimization Complete",
                    "Resources have been optimized. Consumption reduced by 10%");
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to optimize resources: " + e.getMessage());
        }
    }

    @FXML
    private void calculateConsumption() {
        try {
            double avgUsage = RessourceDAO.getAverageUsage();
            showAlert(AlertType.INFORMATION, "Resource Usage",
                    String.format("Average resource usage: %.1f%%", avgUsage));
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to calculate consumption: " + e.getMessage());
        }
    }

    @FXML
    private void generateReport() {
        try {
            List<RessourceDAO.ResourceUsage> reportData = RessourceDAO.getResourceUsageReport();
            StringBuilder report = new StringBuilder();
            report.append("Resource Usage Report:\n\n");

            for (RessourceDAO.ResourceUsage usage : reportData) {
                report.append(String.format("%s:\n", usage.getType()))
                        .append(String.format("  Usage: %.1f / %.1f\n",
                                usage.getCurrentConsumption(),
                                usage.getCapacity()))
                        .append(String.format("  Percentage: %.1f%%\n\n",
                                usage.getCurrentPercentage()));
            }

            showAlert(AlertType.INFORMATION, "Resource Report", report.toString());
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Failed to generate report: " + e.getMessage());
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}