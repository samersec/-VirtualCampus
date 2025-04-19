package com.example.virtualcampus.Controllers;

import com.example.virtualcampus.Model.SimulationEvent;
import com.example.virtualcampus.Model.SimulationState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SimulationViewController {

    @FXML private ProgressBar satisfactionBar;
    @FXML private Label satisfactionLabel;
    @FXML private Label wifiStatusLabel;
    @FXML private Label cafeteriaStatusLabel;
    @FXML private Label visitorCountLabel;
    @FXML private VBox eventLogContainer;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button saveButton;
    @FXML private Button resetButton;

    private SimulationController simulationController;
    private Timeline updateTimer;

    @FXML
    public void initialize() {
        simulationController = new SimulationController();

        // Initialize UI with current state
        updateUI(simulationController.loadSimulationState());

        // Set up timer to update UI every second
        updateTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    SimulationState state = simulationController.loadSimulationState();
                    if (state != null) {
                        updateUI(state);
                    }
                })
        );
        updateTimer.setCycleCount(Timeline.INDEFINITE);
        updateTimer.play();
    }

    private void updateUI(SimulationState state) {
        if (state == null) {
            state = new SimulationState(); // Use default state if none loaded
        }

        // Update satisfaction
        double satisfactionValue = state.getGlobalSatisfaction() / 100.0;
        satisfactionBar.setProgress(satisfactionValue);
        satisfactionLabel.setText(state.getGlobalSatisfaction() + "%");

        // Set color based on satisfaction level
        if (satisfactionValue < 0.3) {
            satisfactionLabel.setTextFill(Color.RED);
        } else if (satisfactionValue < 0.7) {
            satisfactionLabel.setTextFill(Color.ORANGE);
        } else {
            satisfactionLabel.setTextFill(Color.GREEN);
        }

        // Update WiFi status
        wifiStatusLabel.setText(state.isWifiWorking() ? "Fonctionnel" : "En panne");
        wifiStatusLabel.setTextFill(state.isWifiWorking() ? Color.GREEN : Color.RED);

        // Update cafeteria status
        cafeteriaStatusLabel.setText(state.isCafeteriaOpen() ? "Ouverte" : "Fermée");
        cafeteriaStatusLabel.setTextFill(state.isCafeteriaOpen() ? Color.GREEN : Color.RED);

        // Update visitor count
        visitorCountLabel.setText(String.valueOf(state.getVisitorCount()));

        // Update button states
        startButton.setDisable(state.isRunning());
        stopButton.setDisable(!state.isRunning());

        // Potentially update event log
        List<SimulationEvent> events = state.getEventHistory();
        if (events != null && !events.isEmpty()) {
            updateEventLog(events);
        }
    }

    private void updateEventLog(List<SimulationEvent> events) {
        // Clear existing log entries if there are more than 10
        if (eventLogContainer.getChildren().size() > 10) {
            eventLogContainer.getChildren().clear();
        }

        // We'll display the last event if it's not already in the log
        if (!events.isEmpty()) {
            SimulationEvent latestEvent = events.get(events.size() - 1);

            // Check if this event is already in the log
            boolean eventExists = false;
            for (javafx.scene.Node node : eventLogContainer.getChildren()) {
                if (node instanceof Text) {
                    Text text = (Text) node;
                    if (text.getText().contains(latestEvent.getType().getTitle())) {
                        eventExists = true;
                        break;
                    }
                }
            }

            if (!eventExists) {
                // Create event entry
                Text eventText = new Text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                        " - " + latestEvent.getType().getTitle() +
                        " (" + latestEvent.getType().getSatisfactionImpact() + "%)");

                // Style based on impact
                if (latestEvent.getType().getSatisfactionImpact() < 0) {
                    eventText.setFill(Color.RED);
                } else {
                    eventText.setFill(Color.GREEN);
                }

                eventText.setFont(Font.font("System", FontWeight.NORMAL, 12));

                // Add to log (at the top)
                eventLogContainer.getChildren().add(0, eventText);
            }
        }
    }

    @FXML
    private void handleStartSimulation() {
        simulationController.startSimulation();
        updateUI(simulationController.loadSimulationState());
    }

    @FXML
    private void handleStopSimulation() {
        simulationController.stopSimulation();
        updateUI(simulationController.loadSimulationState());
    }

    @FXML
    private void handleSaveSimulation() {
        simulationController.saveSimulationState();

        // Add a save confirmation to event log
        Text saveText = new Text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                " - Simulation sauvegardée");
        saveText.setFill(Color.BLUE);
        saveText.setFont(Font.font("System", FontWeight.BOLD, 12));
        eventLogContainer.getChildren().add(0, saveText);
    }

    @FXML
    private void handleResetSimulation() {
        simulationController.resetSimulation();
        updateUI(simulationController.loadSimulationState());

        // Clear event log
        eventLogContainer.getChildren().clear();

        // Add a reset confirmation to event log
        Text resetText = new Text(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                " - Simulation réinitialisée");
        resetText.setFill(Color.ORANGE);
        resetText.setFont(Font.font("System", FontWeight.BOLD, 12));
        eventLogContainer.getChildren().add(resetText);
    }
}