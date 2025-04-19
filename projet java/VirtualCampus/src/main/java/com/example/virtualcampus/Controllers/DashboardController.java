package com.example.virtualcampus.Controllers;

import com.example.virtualcampus.Model.Batiment;
import com.example.virtualcampus.Model.Personne;
import com.example.virtualcampus.dao.BatimentDAO;
import com.example.virtualcampus.dao.PersonneDAO;
import com.example.virtualcampus.dao.RessourceDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DashboardController {
    @FXML private PieChart populationChart;
    @FXML private BarChart<String, Number> occupationChart;
    @FXML private ProgressBar wifiProgress;
    @FXML private ProgressBar electricityProgress;
    @FXML private ProgressBar waterProgress;
    @FXML private ProgressBar spaceProgress;
    @FXML private ProgressBar satisfactionProgress;
    @FXML private Label wifiLabel;
    @FXML private Label electricityLabel;
    @FXML private Label waterLabel;
    @FXML private Label spaceLabel;
    @FXML private Label satisfactionLabel;

    private final PersonneDAO personneDAO = new PersonneDAO();
    private final BatimentDAO batimentDAO = new BatimentDAO();
    private final RessourceDAO ressourceDAO = new RessourceDAO();
    private Timeline updateTimeline;

    @FXML
    public void initialize() {
        setupCharts();
        setupResourceMonitors();
        setupSatisfactionMonitor();
        startAutoUpdate();
    }

    private void setupCharts() {
        try {
            // Population Chart
            ObservableList<PieChart.Data> populationData = FXCollections.observableArrayList();
            Map<String, Integer> populationCounts = getPopulationCounts();

            populationCounts.forEach((type, count) -> {
                PieChart.Data slice = new PieChart.Data(type, count);
                populationData.add(slice);
            });

            populationChart.setData(populationData);
            populationChart.setTitle("Population Distribution");

            // Add hover effect and click handlers
            populationData.forEach(data -> {
                data.getNode().setOnMouseEntered(e -> {
                    data.getNode().setStyle("-fx-pie-color: derive(" + data.getNode().getStyle() + ", 20%);");
                });
                data.getNode().setOnMouseExited(e -> {
                    data.getNode().setStyle("");
                });
            });

            // Building Chart
            XYChart.Series<String, Number> buildingSeries = new XYChart.Series<>();
            buildingSeries.setName("Buildings by Type");

            Map<String, Integer> buildingCounts = getBuildingCounts();
            buildingCounts.forEach((type, count) -> {
                buildingSeries.getData().add(new XYChart.Data<>(type, count));
            });

            occupationChart.getData().clear();
            occupationChart.getData().add(buildingSeries);
            occupationChart.setTitle("Building Distribution");

            // Style the charts
            populationChart.setStyle("-fx-pie-color: #2196F3, #4CAF50, #FFC107;");
            occupationChart.setAnimated(true);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error appropriately
        }
    }

    private void setupResourceMonitors() {
        try {
            List<RessourceDAO.Resource> resources = ressourceDAO.getAllResources();

            for (RessourceDAO.Resource resource : resources) {
                double progress = resource.getUsageRatio();
                String percentage = String.format("%.1f%%", progress * 100);
                String status = getResourceStatus(progress);

                updateResourceUI(resource.getType(), progress, percentage, status);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error appropriately
        }
    }

    private void setupSatisfactionMonitor() {
        try {
            double averageSatisfaction = personneDAO.getAverageSatisfaction();
            double normalizedSatisfaction = averageSatisfaction / 100.0; // Convert from 0-100 scale to 0-1 for ProgressBar

            String percentage = String.format("%.1f%%", averageSatisfaction);
            String status = getSatisfactionStatus(averageSatisfaction);
            String color = getSatisfactionColor(averageSatisfaction);

            satisfactionProgress.setProgress(normalizedSatisfaction);
            satisfactionProgress.setStyle("-fx-accent: " + color + ";");
            satisfactionLabel.setText(percentage + "\n" + status);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error appropriately
        }
    }

    private String getSatisfactionStatus(double satisfaction) {
        if (satisfaction >= 80) return "EXCELLENT";
        if (satisfaction >= 60) return "GOOD";
        if (satisfaction >= 40) return "AVERAGE";
        if (satisfaction >= 20) return "LOW";
        return "CRITICAL";
    }

    private String getSatisfactionColor(double satisfaction) {
        if (satisfaction >= 80) return "#4CAF50"; // Green for excellent
        if (satisfaction >= 60) return "#8BC34A"; // Light green for good
        if (satisfaction >= 40) return "#FFC107"; // Yellow for average
        if (satisfaction >= 20) return "#FF9800"; // Orange for low
        return "#F44336"; // Red for critical
    }

    private void updateResourceUI(String type, double progress, String percentage, String status) {
        String color = getStatusColor(progress);

        switch (type) {
            case "WIFI":
                wifiProgress.setProgress(progress);
                wifiProgress.setStyle("-fx-accent: " + color + ";");
                wifiLabel.setText(percentage + "\n" + status);
                break;
            case "ELECTRICITY":
                electricityProgress.setProgress(progress);
                electricityProgress.setStyle("-fx-accent: " + color + ";");
                electricityLabel.setText(percentage + "\n" + status);
                break;
            case "WATER":
                waterProgress.setProgress(progress);
                waterProgress.setStyle("-fx-accent: " + color + ";");
                waterLabel.setText(percentage + "\n" + status);
                break;
            case "SPACE":
                spaceProgress.setProgress(progress);
                spaceProgress.setStyle("-fx-accent: " + color + ";");
                spaceLabel.setText(percentage + "\n" + status);
                break;
        }
    }

    private String getStatusColor(double progress) {
        if (progress >= 0.9) return "#f44336"; // Red for critical
        if (progress >= 0.7) return "#ff9800"; // Orange for warning
        return "#4caf50"; // Green for normal
    }

    private String getResourceStatus(double progress) {
        if (progress >= 0.9) return "CRITICAL";
        if (progress >= 0.7) return "WARNING";
        return "NORMAL";
    }

    private Map<String, Integer> getPopulationCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        List<Personne> etudiants = personneDAO.findByType(Personne.TypePersonne.ETUDIANT);
        List<Personne> professeurs = personneDAO.findByType(Personne.TypePersonne.PROFESSEUR);

        counts.put("Students", etudiants.size());
        counts.put("Professors", professeurs.size());

        return counts;
    }

    private Map<String, Integer> getBuildingCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        List<Batiment> batiments = batimentDAO.getAllBatiments();

        for (Batiment batiment : batiments) {
            counts.merge(batiment.getType(), 1, Integer::sum);
        }

        return counts;
    }

    private void startAutoUpdate() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            setupCharts();
            setupResourceMonitors();
            setupSatisfactionMonitor();
        }));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();
    }

    public void stopAutoUpdate() {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
    }
}