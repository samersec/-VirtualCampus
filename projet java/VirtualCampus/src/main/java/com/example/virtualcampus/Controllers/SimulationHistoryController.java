package com.example.virtualcampus.Controllers;

import com.example.virtualcampus.Model.SimulationEvent;
import com.example.virtualcampus.Model.SimulationState;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class SimulationHistoryController {
    @FXML private TableView<SimulationEvent> historyTable;
    @FXML private TableColumn<SimulationEvent, String> dateColumn;
    @FXML private TableColumn<SimulationEvent, String> eventColumn;
    @FXML private TableColumn<SimulationEvent, Number> impactColumn;
    @FXML private TableColumn<SimulationEvent, Number> satisfactionColumn;

    @FXML
    public void initialize() {
        // Configure columns
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        ));

        eventColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getType().getTitle())
        );

        impactColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getType().getSatisfactionImpact())
        );

        satisfactionColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(0) // This will be updated with actual satisfaction
        );

        // Load history data
        loadHistoryData();
    }

    private void loadHistoryData() {
        SimulationController controller = new SimulationController();
        SimulationState state = controller.loadSimulationState();

        if (state != null && state.getEventHistory() != null) {
            ObservableList<SimulationEvent> events = FXCollections.observableArrayList(state.getEventHistory());
            historyTable.setItems(events);
        }
    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter l'historique");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write CSV header
                writer.println("Date,Événement,Impact,Satisfaction");

                // Write data
                for (SimulationEvent event : historyTable.getItems()) {
                    writer.printf("%s,%s,%d,%d%n",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                            event.getType().getTitle(),
                            event.getType().getSatisfactionImpact(),
                            0 // Actual satisfaction value
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) historyTable.getScene().getWindow()).close();
    }
}