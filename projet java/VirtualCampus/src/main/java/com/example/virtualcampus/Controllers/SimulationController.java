package com.example.virtualcampus.Controllers;

import com.example.virtualcampus.Model.Personne;
import com.example.virtualcampus.Model.SimulationEvent;
import com.example.virtualcampus.Model.SimulationState;
import com.example.virtualcampus.dao.PersonneDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationController {
    private final SimulationState state;
    private Timeline eventTimeline;
    private final Random random;
    private final PersonneDAO personneDAO;
    private final List<SimulationEvent> eventHistory;
    private static final String SAVE_FILE_PATH = "simulation_save.dat";

    public SimulationController() {
        this.personneDAO = new PersonneDAO();
        this.random = new Random();
        this.eventHistory = new ArrayList<>();

        // Try to restore saved state or create new one
        SimulationState loadedState = loadSimulationState();
        this.state = loadedState != null ? loadedState : new SimulationState();

        // Configure event timeline to trigger events randomly between 5-15 seconds
        this.eventTimeline = new Timeline(
                new KeyFrame(Duration.seconds(getRandomTimeInterval()), event -> {
                    triggerRandomEvent();
                    // Reset with a new random interval after each event
                    eventTimeline.setDelay(Duration.seconds(getRandomTimeInterval()));
                })
        );
        eventTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    // Generate random time interval between 5-15 seconds
    private double getRandomTimeInterval() {
        return 5 + random.nextDouble() * 10; // Between 5-15 seconds
    }

    public void startSimulation() {
        if (!state.isRunning()) {
            state.setRunning(true);
            eventTimeline.play();
            showAlert("Simulation démarrée", "La simulation du campus est en cours.", Alert.AlertType.INFORMATION);
        }
    }

    public void stopSimulation() {
        if (state.isRunning()) {
            state.setRunning(false);
            eventTimeline.stop();
            showEndSimulationSummary();
            saveSimulationState();
        }
    }

    private void triggerRandomEvent() {
        if (!state.isRunning()) return;

        // Randomly select an event with weighted probability
        SimulationEvent.EventType selectedEvent = getRandomEventType();
        SimulationEvent event = new SimulationEvent(selectedEvent);

        // Add to history
        eventHistory.add(event);

        // Apply event effects
        applyEventEffects(event);

        // Save state after each event
        saveSimulationState();

        // Show event notification
        Platform.runLater(() -> showEventNotification(event));
    }

    private SimulationEvent.EventType getRandomEventType() {
        SimulationEvent.EventType[] events = SimulationEvent.EventType.values();

        // Simple weighted probability - can be adjusted for more complex logic
        int totalWeight = 0;
        int[] weights = new int[events.length];

        for (int i = 0; i < events.length; i++) {
            // Assign weights - negative events more common when satisfaction is high
            // Positive events more common when satisfaction is low (balancing mechanism)
            if (events[i].getSatisfactionImpact() < 0) {
                weights[i] = 50 + (state.getGlobalSatisfaction() / 5); // More likely when satisfaction is high
            } else {
                weights[i] = 50 + ((100 - state.getGlobalSatisfaction()) / 5); // More likely when satisfaction is low
            }
            totalWeight += weights[i];
        }

        // Pick based on weights
        int randomValue = random.nextInt(totalWeight);
        int runningTotal = 0;

        for (int i = 0; i < events.length; i++) {
            runningTotal += weights[i];
            if (randomValue < runningTotal) {
                return events[i];
            }
        }

        // Fallback
        return events[random.nextInt(events.length)];
    }

    private void applyEventEffects(SimulationEvent event) {
        try {
            switch (event.getType()) {
                case GREVE_PROFS:
                    // Update professors' strike status and satisfaction
                    personneDAO.updateAllProfesseursGreveStatus(true);
                    personneDAO.updateTypeSatisfaction(Personne.TypePersonne.PROFESSEUR, event.getType().getSatisfactionImpact());
                    state.adjustSatisfaction(event.getType().getSatisfactionImpact());
                    break;

                case COUPURE_WIFI:
                    // Affect all persons' satisfaction
                    personneDAO.updateGlobalSatisfaction(state.getGlobalSatisfaction() + event.getType().getSatisfactionImpact());
                    state.setWifiWorking(false);
                    state.adjustSatisfaction(event.getType().getSatisfactionImpact());

                    // Schedule WiFi repair
                    Timeline wifiRepairTimeline = new Timeline(
                            new KeyFrame(Duration.seconds(20 + random.nextInt(10)), e -> {
                                state.setWifiWorking(true);
                                Platform.runLater(() -> showAlert(
                                        "Wi-Fi Réparé",
                                        "Le réseau Wi-Fi a été réparé et fonctionne à nouveau.",
                                        Alert.AlertType.INFORMATION
                                ));
                            })
                    );
                    wifiRepairTimeline.play();
                    break;

                case CAFETERIA_PROBLEME:
                    // Affect student satisfaction more than others
                    personneDAO.updateTypeSatisfaction(Personne.TypePersonne.ETUDIANT, event.getType().getSatisfactionImpact());
                    personneDAO.updateTypeSatisfaction(Personne.TypePersonne.PROFESSEUR, event.getType().getSatisfactionImpact() / 2);
                    state.setCafeteriaOpen(false);
                    state.adjustSatisfaction(event.getType().getSatisfactionImpact());

                    // Schedule cafeteria reopening
                    Timeline cafeteriaReopenTimeline = new Timeline(
                            new KeyFrame(Duration.seconds(15 + random.nextInt(10)), e -> {
                                state.setCafeteriaOpen(true);
                                Platform.runLater(() -> showAlert(
                                        "Cafétéria Rouverte",
                                        "La cafétéria a été nettoyée et est à nouveau ouverte.",
                                        Alert.AlertType.INFORMATION
                                ));
                            })
                    );
                    cafeteriaReopenTimeline.play();
                    break;

                case EXAMENS_STRESS:
                    // Mainly affects students
                    personneDAO.updateTypeSatisfaction(Personne.TypePersonne.ETUDIANT, event.getType().getSatisfactionImpact());
                    state.adjustSatisfaction(event.getType().getSatisfactionImpact());

                    // Schedule exam end
                    Timeline examEndTimeline = new Timeline(
                            new KeyFrame(Duration.seconds(30 + random.nextInt(15)), e -> {
                                personneDAO.updateTypeSatisfaction(Personne.TypePersonne.ETUDIANT,
                                        Math.abs(event.getType().getSatisfactionImpact()) / 2);
                                state.adjustSatisfaction(Math.abs(event.getType().getSatisfactionImpact()) / 2);
                                Platform.runLater(() -> showAlert(
                                        "Fin des Examens",
                                        "La période d'examens est terminée. Le niveau de stress diminue.",
                                        Alert.AlertType.INFORMATION
                                ));
                            })
                    );
                    examEndTimeline.play();
                    break;

                case PORTES_OUVERTES:
                    // Positive effect on everyone
                    personneDAO.updateGlobalSatisfaction(state.getGlobalSatisfaction() + event.getType().getSatisfactionImpact());
                    state.setVisitorCount(state.getVisitorCount() + random.nextInt(50) + 20);
                    state.adjustSatisfaction(event.getType().getSatisfactionImpact());
                    break;
            }

            // Add event to history
            state.addEvent(event);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'application des effets: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showEventNotification(SimulationEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Événement Campus");
        alert.setHeaderText(event.getType().getTitle());

        // Create a custom content area
        VBox content = new VBox(10);

        Text description = new Text(event.getType().getDescription());
        description.setWrappingWidth(400);

        Text impact = new Text("Impact sur la satisfaction: " + event.getType().getSatisfactionImpact() + "%");
        if (event.getType().getSatisfactionImpact() < 0) {
            impact.setFill(Color.RED);
        } else {
            impact.setFill(Color.GREEN);
        }
        impact.setFont(Font.font("System", FontWeight.BOLD, 12));

        Text satisfaction = new Text("Satisfaction globale: " + state.getGlobalSatisfaction() + "%");

        Text timestamp = new Text("Heure: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        timestamp.setFont(Font.font("System", 10));

        Label statusLabel = new Label("État des installations:");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Text wifiStatus = new Text("Wi-Fi: " + (state.isWifiWorking() ? "Fonctionnel ✓" : "En panne ✗"));
        if (!state.isWifiWorking()) wifiStatus.setFill(Color.RED);

        Text cafeteriaStatus = new Text("Cafétéria: " + (state.isCafeteriaOpen() ? "Ouverte ✓" : "Fermée ✗"));
        if (!state.isCafeteriaOpen()) cafeteriaStatus.setFill(Color.RED);

        Text visitorCount = new Text("Visiteurs: " + state.getVisitorCount());

        content.getChildren().addAll(
                description,
                impact,
                satisfaction,
                timestamp,
                statusLabel,
                wifiStatus,
                cafeteriaStatus,
                visitorCount
        );

        alert.getDialogPane().setContent(content);

        // Make dialog closeable with OK button
        alert.getButtonTypes().setAll(ButtonType.OK);

        alert.show();
    }

    private void showEndSimulationSummary() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fin de la Simulation");
        alert.setHeaderText("Résumé de la Simulation");

        VBox content = new VBox(10);

        Text durationText = new Text("Durée de la simulation: " +
                (eventHistory.size() > 0 ?
                        ((System.currentTimeMillis() - eventHistory.get(0).getTimestamp()) / 1000) + " secondes" :
                        "0 secondes"));

        Text eventsText = new Text("Nombre d'événements: " + eventHistory.size());

        Text satisfactionText = new Text("Satisfaction finale: " + state.getGlobalSatisfaction() + "%");

        content.getChildren().addAll(durationText, eventsText, satisfactionText);

        // Add event history if any
        if (!eventHistory.isEmpty()) {
            Text historyTitle = new Text("Historique des événements:");
            historyTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
            content.getChildren().add(historyTitle);

            // Add the last 5 events (or fewer if less occurred)
            int startIndex = Math.max(0, eventHistory.size() - 5);
            for (int i = startIndex; i < eventHistory.size(); i++) {
                SimulationEvent event = eventHistory.get(i);
                Text eventText = new Text((i + 1) + ". " + event.getType().getTitle());
                content.getChildren().add(eventText);
            }
        }

        alert.getDialogPane().setContent(content);
        alert.show();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    // Save simulation state to file
    public void saveSimulationState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE_PATH))) {
            oos.writeObject(state);
            System.out.println("Simulation state saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving simulation state: " + e.getMessage());
        }
    }

    // Load simulation state from file
    public SimulationState loadSimulationState() {
        File saveFile = new File(SAVE_FILE_PATH);
        if (!saveFile.exists()) {
            System.out.println("No saved simulation state found");
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            SimulationState loadedState = (SimulationState) ois.readObject();
            System.out.println("Simulation state loaded successfully");
            return loadedState;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Error loading simulation state: " + e.getMessage());
            return null;
        }
    }

    // Force reset of simulation state
    public void resetSimulation() {
        this.state.reset();
        this.eventHistory.clear();
        saveSimulationState();
        showAlert("Simulation Réinitialisée", "L'état de la simulation a été remis à zéro.", Alert.AlertType.INFORMATION);
    }
}