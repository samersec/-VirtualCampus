package com.example.virtualcampus.Controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class HelloController {
    @FXML private StackPane contentPane;
    @FXML private Button dashboardBtn;
    @FXML private Button batimentBtn;
    @FXML private Button etudiantBtn;
    @FXML private Button professeurBtn;
    @FXML private Button personnelBtn;
    @FXML private Button startBtn;
    @FXML private Button stopBtn;
    @FXML private Button historyBtn;
    @FXML private Label simulationStatusLabel;

    private SimulationController simulationController;

    @FXML
    public void initialize() {
        simulationController = new SimulationController();
        switchToDashboard();

        // Configure simulation buttons
        startBtn.setDisable(false);
        stopBtn.setDisable(true);

        // Initialize simulation status label
        if (simulationStatusLabel != null) {
            simulationStatusLabel.setText("Simulation inactive");
            simulationStatusLabel.setTextFill(Color.GRAY);
        }

        // Customize buttons
        if (startBtn != null) {
            startBtn.getStyleClass().add("start-button");
            startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        }

        if (stopBtn != null) {
            stopBtn.getStyleClass().add("stop-button");
            stopBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        }

        if (historyBtn != null) {
            historyBtn.getStyleClass().add("history-button");
            historyBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        }
    }

    private void resetButtonStyles() {
        String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER_LEFT;";
        dashboardBtn.setStyle(defaultStyle);
        batimentBtn.setStyle(defaultStyle);
        etudiantBtn.setStyle(defaultStyle);
        professeurBtn.setStyle(defaultStyle);
    }

    private void setActiveButton(Button button) {
        resetButtonStyles();
        button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-alignment: CENTER_LEFT;");
    }

    private void loadView(String fxmlPath) {
        try {
            contentPane.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().add(view);

            // Add fade-in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), view);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToDashboard() {
        setActiveButton(dashboardBtn);
        loadView("/com/example/virtualcampus/dashboardView.fxml");
    }

    @FXML
    private void switchToBatiment() {
        setActiveButton(batimentBtn);
        loadView("/com/example/virtualcampus/batimentView.fxml");
    }

    @FXML
    private void switchToPersonne() {
        setActiveButton(etudiantBtn);
        loadView("/com/example/virtualcampus/personneView.fxml");
    }

    @FXML
    private void switchToProfesseur() {
        setActiveButton(professeurBtn);
        loadView("/com/example/virtualcampus/ressourceView.fxml");
    }

    @FXML
    private void handleStartSimulation() {
        simulationController.startSimulation();
        startBtn.setDisable(true);
        stopBtn.setDisable(false);

        if (simulationStatusLabel != null) {
            simulationStatusLabel.setText("Simulation en cours...");
            simulationStatusLabel.setTextFill(Color.GREEN);

            // Add animation to status label
            FadeTransition pulse = new FadeTransition(Duration.millis(800), simulationStatusLabel);
            pulse.setFromValue(0.7);
            pulse.setToValue(1.0);
            pulse.setCycleCount(FadeTransition.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();
        }
    }

    @FXML
    private void handleStopSimulation() {
        simulationController.stopSimulation();
        startBtn.setDisable(false);
        stopBtn.setDisable(true);

        if (simulationStatusLabel != null) {
            simulationStatusLabel.setText("Simulation arrêtée");
            simulationStatusLabel.setTextFill(Color.RED);

            // Stop any running animations
            FadeTransition fadeToNormal = new FadeTransition(Duration.millis(300), simulationStatusLabel);
            fadeToNormal.setToValue(1.0);
            fadeToNormal.play();
        }
    }

    @FXML
    private void handleSaveSimulation() {
        simulationController.saveSimulationState();
    }

    @FXML
    private void handleResetSimulation() {
        simulationController.resetSimulation();
        startBtn.setDisable(false);
        stopBtn.setDisable(true);

        if (simulationStatusLabel != null) {
            simulationStatusLabel.setText("Simulation réinitialisée");
            simulationStatusLabel.setTextFill(Color.GRAY);
        }
    }

    @FXML
    private void handleViewHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/virtualcampus/simulationHistoryView.fxml"));
            Parent root = loader.load();

            Stage historyStage = new Stage();
            historyStage.initModality(Modality.APPLICATION_MODAL);
            historyStage.setTitle("Historique de la Simulation");
            historyStage.setScene(new Scene(root));

            historyStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}