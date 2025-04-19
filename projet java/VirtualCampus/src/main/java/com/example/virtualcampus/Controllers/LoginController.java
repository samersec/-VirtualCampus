package com.example.virtualcampus.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validateLogin(username, password)) {
            try {
                // Load the FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/virtualcampus/hello-view.fxml"));
                Parent mainView = loader.load();

                // Get the current stage
                Stage stage = (Stage) usernameField.getScene().getWindow();

                // Create new scene
                Scene scene = new Scene(mainView);

                // Add stylesheet if it exists
                try {
                    String css = getClass().getResource("style.css").toExternalForm();
                    scene.getStylesheets().add(css);
                } catch (NullPointerException e) {
                    System.err.println("Stylesheet not found: style.css");
                }

                // Set the new scene
                stage.setScene(scene);
                stage.setMaximized(true);

            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading application view.");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                showError("Error in application configuration. Check FXML file.");
            } catch (Exception e) {
                e.printStackTrace();
                showError("An unexpected error occurred.");
            }
        } else {
            showError("Invalid username or password.");
        }
    }

    private boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM USER WHERE username = ? AND password = ?";

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/virtualcampus",
                "root",
                "root");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next(); // Returns true if a matching user was found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database connection error.");
            return false;
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}