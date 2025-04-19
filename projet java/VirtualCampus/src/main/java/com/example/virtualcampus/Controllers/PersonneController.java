package com.example.virtualcampus.Controllers;

import com.example.virtualcampus.Model.Personne;
import com.example.virtualcampus.Model.Etudiant;
import com.example.virtualcampus.Model.Professeur;
import com.example.virtualcampus.dao.PersonneDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.sql.SQLException;
import java.time.LocalDate;

public class PersonneController {
    @FXML private TableView<Personne> personneTable;
    @FXML private TableColumn<Personne, String> idColumn;
    @FXML private TableColumn<Personne, String> typeColumn;
    @FXML private TableColumn<Personne, String> nomColumn;
    @FXML private TableColumn<Personne, String> prenomColumn;
    @FXML private TableColumn<Personne, String> emailColumn;
    @FXML private TableColumn<Personne, Integer> satisfactionColumn;
    @FXML private TableColumn<Personne, LocalDate> dateInscriptionColumn;
    @FXML private TableColumn<Personne, Integer> batimentIdColumn;

    // Student specific columns
    @FXML private TableColumn<Etudiant, Integer> niveauColumn;
    @FXML private TableColumn<Etudiant, String> filiereColumn;
    @FXML private TableColumn<Etudiant, Integer> heuresCoursColumn;

    // Professor specific columns
    @FXML private TableColumn<Professeur, String> matiereColumn;
    @FXML private TableColumn<Professeur, Boolean> disponibleColumn;
    @FXML private TableColumn<Professeur, Boolean> greveColumn;
    @FXML private TableColumn<Professeur, Integer> heuresEnseignementColumn;

    @FXML private TextField idField;
    @FXML private ComboBox<Personne.TypePersonne> typeField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private Slider satisfactionSlider;
    @FXML private DatePicker dateInscriptionPicker;
    @FXML private TextField batimentIdField;
    @FXML private VBox specificInfoPane;
    @FXML private ComboBox<String> filterType;
    @FXML private TextField searchField;
    @FXML private BorderPane formPane;
    @FXML private Label formTitle;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private PersonneDAO personneDAO = new PersonneDAO();
    private ObservableList<Personne> personneList = FXCollections.observableArrayList();
    private boolean isAddingNew = false;

    @FXML
    public void initialize() {
        configureTableColumns();
        configureComboBoxes();
        setupTableSelectionListener();
        setupFilterListener();
        setupTypeFieldListener();
        setupFormStyles();
        hideForm();
        refreshTable();
    }

    private void setupFormStyles() {
        if (formPane != null) {
            formPane.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 5;");
            formPane.setPadding(new Insets(15));
        }

        if (saveButton != null) {
            saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        }

        if (cancelButton != null) {
            cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        }
    }

    private void configureTableColumns() {
        // Configure common columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        satisfactionColumn.setCellValueFactory(new PropertyValueFactory<>("satisfaction"));
        dateInscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        batimentIdColumn.setCellValueFactory(new PropertyValueFactory<>("batimentId"));

        // Configure student-specific columns
        niveauColumn.setCellValueFactory(cellData -> {
            Personne personne = cellData.getValue();
            if (personne instanceof Etudiant) {
                return new SimpleIntegerProperty(((Etudiant) personne).getNiveau()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject(); // Default value for non-students
        });

        filiereColumn.setCellValueFactory(cellData -> {
            Personne personne = cellData.getValue();
            if (personne instanceof Etudiant) {
                return new SimpleStringProperty(((Etudiant) personne).getFiliere());
            }
            return new SimpleStringProperty(""); // Default empty string for non-students
        });

        heuresCoursColumn.setCellValueFactory(cellData -> {
            Personne personne = cellData.getValue();
            if (personne instanceof Etudiant) {
                return new SimpleIntegerProperty(((Etudiant) personne).getHeuresCours()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject(); // Default value for non-students
        });

        // Configure professor-specific columns
        matiereColumn.setCellValueFactory(cellData -> {
            Personne personne = cellData.getValue();
            if (personne instanceof Professeur) {
                return new SimpleStringProperty(((Professeur) personne).getMatiereEnseignee());
            }
            return new SimpleStringProperty(""); // Default empty string for non-professors
        });

        disponibleColumn.setCellValueFactory(cellData -> {
            Personne personne = cellData.getValue();
            if (personne instanceof Professeur) {
                return new SimpleBooleanProperty(((Professeur) personne).isDisponible());
            }
            return new SimpleBooleanProperty(false); // Default value for non-professors
        });

        greveColumn.setCellValueFactory(cellData -> {
            Personne personne = cellData.getValue();
            if (personne instanceof Professeur) {
                return new SimpleBooleanProperty(((Professeur) personne).isEnGreve());
            }
            return new SimpleBooleanProperty(false); // Default value for non-professors
        });

        heuresEnseignementColumn.setCellValueFactory(cellData -> {
            Personne personne = cellData.getValue();
            if (personne instanceof Professeur) {
                return new SimpleIntegerProperty(((Professeur) personne).getHeuresEnseignement()).asObject();
            }
            return new SimpleIntegerProperty(0).asObject(); // Default value for non-professors
        });

        personneTable.setItems(personneList);
    }

    private void configureComboBoxes() {
        typeField.getItems().setAll(Personne.TypePersonne.values());
        filterType.getItems().addAll("All", "ETUDIANT", "PROFESSEUR");
        filterType.setValue("All");
    }

    private void setupTableSelectionListener() {
        personneTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!isAddingNew) {
                        showPersonneDetails(newValue);
                    }
                });
    }

    private void setupTypeFieldListener() {
        typeField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateSpecificFields(newValue);
            }
        });
    }

    private void setupFilterListener() {
        filterType.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if ("All".equals(newValue)) {
                    personneList.setAll(personneDAO.findAll());
                    showAllColumns();
                } else {
                    Personne.TypePersonne type = Personne.TypePersonne.valueOf(newValue);
                    personneList.setAll(personneDAO.findByType(type));
                    showSpecificColumns(type);
                }
                applySearch();
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to filter data: " + e.getMessage());
            }
        });
    }

    private void showAllColumns() {
        // Show common columns
        idColumn.setVisible(true);
        typeColumn.setVisible(true);
        nomColumn.setVisible(true);
        prenomColumn.setVisible(true);
        emailColumn.setVisible(true);
        satisfactionColumn.setVisible(true);
        dateInscriptionColumn.setVisible(true);
        batimentIdColumn.setVisible(true);

        // Show all specific columns
        if (niveauColumn != null) {
            niveauColumn.setVisible(true);
            filiereColumn.setVisible(true);
            heuresCoursColumn.setVisible(true);
        }
        if (matiereColumn != null) {
            matiereColumn.setVisible(true);
            disponibleColumn.setVisible(true);
            greveColumn.setVisible(true);
            heuresEnseignementColumn.setVisible(true);
        }
    }

    private void showSpecificColumns(Personne.TypePersonne type) {
        // Show common columns
        idColumn.setVisible(true);
        typeColumn.setVisible(true);
        nomColumn.setVisible(true);
        prenomColumn.setVisible(true);
        emailColumn.setVisible(true);
        satisfactionColumn.setVisible(true);
        dateInscriptionColumn.setVisible(true);
        batimentIdColumn.setVisible(true);

        // Show/hide specific columns based on type
        if (niveauColumn != null) {
            boolean isStudent = type == Personne.TypePersonne.ETUDIANT;
            niveauColumn.setVisible(isStudent);
            filiereColumn.setVisible(isStudent);
            heuresCoursColumn.setVisible(isStudent);
        }
        if (matiereColumn != null) {
            boolean isProfessor = type == Personne.TypePersonne.PROFESSEUR;
            matiereColumn.setVisible(isProfessor);
            disponibleColumn.setVisible(isProfessor);
            greveColumn.setVisible(isProfessor);
            heuresEnseignementColumn.setVisible(isProfessor);
        }
    }

    private void refreshTable() {
        try {
            personneList.setAll(personneDAO.findAll());
            personneTable.setItems(personneList);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void showPersonneDetails(Personne personne) {
        clearFields();
        if (personne != null) {
            populateCommonFields(personne);
            populateSpecificFields(personne);
        }
    }

    private void populateCommonFields(Personne personne) {
        idField.setText(personne.getId());
        typeField.setValue(personne.getType());
        nomField.setText(personne.getNom());
        prenomField.setText(personne.getPrenom());
        emailField.setText(personne.getEmail());
        satisfactionSlider.setValue(personne.getSatisfaction());
        dateInscriptionPicker.setValue(personne.getDateInscription());
        batimentIdField.setText(String.valueOf(personne.getBatimentId()));
    }

    private void populateSpecificFields(Personne personne) {
        specificInfoPane.getChildren().clear();

        if (personne instanceof Etudiant) {
            Etudiant etudiant = (Etudiant) personne;
            addSpecificField("Niveau", String.valueOf(etudiant.getNiveau()));
            addSpecificField("Filière", etudiant.getFiliere());
            addSpecificField("Heures de cours", String.valueOf(etudiant.getHeuresCours()));
        } else if (personne instanceof Professeur) {
            Professeur professeur = (Professeur) personne;
            addSpecificField("Matière enseignée", professeur.getMatiereEnseignee());
            addSpecificField("Disponible", String.valueOf(professeur.isDisponible()));
            addSpecificField("En grève", String.valueOf(professeur.isEnGreve()));
            addSpecificField("Heures d'enseignement", String.valueOf(professeur.getHeuresEnseignement()));
        }
    }

    private void updateSpecificFields(Personne.TypePersonne type) {
        specificInfoPane.getChildren().clear();

        switch (type) {
            case ETUDIANT:
                addSpecificField("Niveau", "1");
                addSpecificField("Filière", "");
                addSpecificField("Heures de cours", "0");
                break;
            case PROFESSEUR:
                addSpecificField("Matière enseignée", "");
                addSpecificField("Disponible", "true");
                addSpecificField("En grève", "false");
                addSpecificField("Heures d'enseignement", "0");
                break;
        }
    }

    private void addSpecificField(String label, String value) {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5));

        Label fieldLabel = new Label(label + ":");
        fieldLabel.setStyle("-fx-font-weight: bold;");
        TextField fieldValue = new TextField(value);
        fieldValue.setEditable(true);
        fieldValue.setPromptText(label);

        grid.add(fieldLabel, 0, 0);
        grid.add(fieldValue, 1, 0);

        // Add some styling
        grid.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
        fieldValue.setStyle("-fx-background-radius: 3;");

        specificInfoPane.getChildren().add(grid);
    }

    private void clearFields() {
        idField.clear();
        typeField.setValue(null);
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        satisfactionSlider.setValue(50);
        dateInscriptionPicker.setValue(null);
        batimentIdField.clear();
        specificInfoPane.getChildren().clear();
        idField.setDisable(false);
        typeField.setDisable(false);
    }

    private void hideForm() {
        if (formPane != null) {
            formPane.setVisible(false);
            formPane.setManaged(false);
        }
    }

    private void showForm() {
        if (formPane != null) {
            formPane.setVisible(true);
            formPane.setManaged(true);

            // Add a nice fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), formPane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    @FXML
    private void handleAdd() {
        isAddingNew = true;
        clearFields();
        showForm();

        if (formTitle != null) {
            formTitle.setText("Ajouter une nouvelle personne");
        }

        // Auto-generate ID based on selected type
        try {
            // Default to ETUDIANT
            Personne.TypePersonne defaultType = Personne.TypePersonne.ETUDIANT;
            typeField.setValue(defaultType);

            String prefix = defaultType == Personne.TypePersonne.ETUDIANT ? "ETD" : "PRO";
            int nextId = personneDAO.getMaxIdNumber(prefix) + 1;
            idField.setText(String.format("%s%03d", prefix, nextId));
            idField.setDisable(true);

            // Set default values
            dateInscriptionPicker.setValue(LocalDate.now());
            batimentIdField.setText("1"); // Default building ID
            satisfactionSlider.setValue(50);

            // Update specific fields based on the selected type
            updateSpecificFields(defaultType);

            // Setup listener for type changes to update ID accordingly
            typeField.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && isAddingNew) {
                    try {
                        String newPrefix = newValue == Personne.TypePersonne.ETUDIANT ? "ETD" : "PRO";
                        int newNextId = personneDAO.getMaxIdNumber(newPrefix) + 1;
                        idField.setText(String.format("%s%03d", newPrefix, newNextId));
                    } catch (SQLException e) {
                        showAlert("Database Error", "Failed to generate ID: " + e.getMessage());
                    }
                }
            });

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to generate ID: " + e.getMessage());
            idField.setText("ID_AUTO");
        }
    }

    @FXML
    private void handleEdit() {
        Personne selected = personneTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            isAddingNew = false;
            showForm();
            if (formTitle != null) {
                formTitle.setText("Modifier une personne");
            }
            idField.setDisable(true);
            typeField.setDisable(true);
        } else {
            showAlert("No Selection", "Please select a person to edit.");
        }
    }

    @FXML
    private void handleDelete() {
        Personne selected = personneTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Supprimer une personne");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette personne ?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                try {
                    personneDAO.delete(selected.getId());
                    refreshTable();
                    clearFields();
                    hideForm();
                } catch (SQLException e) {
                    showAlert("Database Error", "Failed to delete: " + e.getMessage());
                }
            }
        } else {
            showAlert("No Selection", "Please select a person to delete.");
        }
    }

    @FXML
    private void handleSave() {
        try {
            Personne personne = createPersonneFromFields();
            if (personne == null) return;

            if (!isAddingNew) {
                personneDAO.update(personne);
                showSuccessMessage("Personne mise à jour avec succès");
            } else {
                personneDAO.create(personne);
                showSuccessMessage("Nouvelle personne ajoutée avec succès");
                isAddingNew = false;
            }
            refreshTable();
            clearFields();
            hideForm();
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to save: " + e.getMessage());
        }
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Personne createPersonneFromFields() {
        if (!validateFields()) return null;

        Personne personne;
        if (typeField.getValue() == Personne.TypePersonne.ETUDIANT) {
            personne = new Etudiant();
            populateEtudiantFields((Etudiant) personne);
        } else {
            personne = new Professeur();
            populateProfesseurFields((Professeur) personne);
        }

        populateCommonPersonneFields(personne);
        return personne;
    }

    private void populateCommonPersonneFields(Personne personne) {
        personne.setId(idField.getText());
        personne.setType(typeField.getValue());
        personne.setNom(nomField.getText());
        personne.setPrenom(prenomField.getText());
        personne.setEmail(emailField.getText());
        personne.setSatisfaction((int) satisfactionSlider.getValue());
        personne.setDateInscription(dateInscriptionPicker.getValue());
        personne.setBatimentId(Integer.parseInt(batimentIdField.getText()));
    }

    private void populateEtudiantFields(Etudiant etudiant) {
        for (javafx.scene.Node node : specificInfoPane.getChildren()) {
            if (node instanceof GridPane) {
                GridPane grid = (GridPane) node;
                Label label = (Label) grid.getChildren().get(0);
                TextField field = (TextField) grid.getChildren().get(1);

                switch (label.getText().replace(":", "")) {
                    case "Niveau":
                        etudiant.setNiveau(Integer.parseInt(field.getText()));
                        break;
                    case "Filière":
                        etudiant.setFiliere(field.getText());
                        break;
                    case "Heures de cours":
                        etudiant.setHeuresCours(Integer.parseInt(field.getText()));
                        break;
                }
            }
        }
    }

    private void populateProfesseurFields(Professeur professeur) {
        for (javafx.scene.Node node : specificInfoPane.getChildren()) {
            if (node instanceof GridPane) {
                GridPane grid = (GridPane) node;
                Label label = (Label) grid.getChildren().get(0);
                TextField field = (TextField) grid.getChildren().get(1);

                switch (label.getText().replace(":", "")) {
                    case "Matière enseignée":
                        professeur.setMatiereEnseignee(field.getText());
                        break;
                    case "Disponible":
                        professeur.setDisponible(Boolean.parseBoolean(field.getText()));
                        break;
                    case "En grève":
                        professeur.setEnGreve(Boolean.parseBoolean(field.getText()));
                        break;
                    case "Heures d'enseignement":
                        professeur.setHeuresEnseignement(Integer.parseInt(field.getText()));
                        break;
                }
            }
        }
    }

    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        if (nomField.getText().isEmpty()) {
            errorMessage.append("Le nom est requis.\n");
            nomField.setStyle("-fx-border-color: red;");
        } else {
            nomField.setStyle("");
        }

        if (prenomField.getText().isEmpty()) {
            errorMessage.append("Le prénom est requis.\n");
            prenomField.setStyle("-fx-border-color: red;");
        } else {
            prenomField.setStyle("");
        }

        if (emailField.getText().isEmpty()) {
            errorMessage.append("L'email est requis.\n");
            emailField.setStyle("-fx-border-color: red;");
        } else {
            emailField.setStyle("");
        }

        if (typeField.getValue() == null) {
            errorMessage.append("Le type est requis.\n");
            typeField.setStyle("-fx-border-color: red;");
        } else {
            typeField.setStyle("");
        }

        if (dateInscriptionPicker.getValue() == null) {
            errorMessage.append("La date d'inscription est requise.\n");
            dateInscriptionPicker.setStyle("-fx-border-color: red;");
        } else {
            dateInscriptionPicker.setStyle("");
        }

        if (batimentIdField.getText().isEmpty()) {
            errorMessage.append("L'ID du bâtiment est requis.\n");
            batimentIdField.setStyle("-fx-border-color: red;");
        } else {
            batimentIdField.setStyle("");
        }

        if (errorMessage.length() > 0) {
            showAlert("Validation Error", errorMessage.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        clearFields();
        hideForm();
        isAddingNew = false;

        // Deselect the current selection in the table
        personneTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        applySearch();
    }

    private void applySearch() {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<Personne> filteredList = personneList.filtered(personne ->
                personne.getNom().toLowerCase().contains(searchText) ||
                        personne.getPrenom().toLowerCase().contains(searchText) ||
                        personne.getEmail().toLowerCase().contains(searchText)
        );
        personneTable.setItems(filteredList);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}