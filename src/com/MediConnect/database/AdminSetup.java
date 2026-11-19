
package com.MediConnect.database;

import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;

public class AdminSetup extends Application {

    @Override
    public void start(Stage stage) {

        StaffDAO staffDAO = new StaffDAO();

        TextInputDialog nameDialog =
                new TextInputDialog("System Administrator");

        nameDialog.setTitle("Administrator Setup");
        nameDialog.setHeaderText("Create the first administrator");
        nameDialog.setContentText("Full name:");

        String fullName = nameDialog.showAndWait().orElse(null);

        if (fullName == null || fullName.trim().isEmpty()) {
            return;
        }

        PasswordField passwordField = new PasswordField();
        PasswordField confirmField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(new Label("admin"), 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Confirm password:"), 0, 2);
        grid.add(confirmField, 1, 2);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Administrator Credentials");
        dialog.setHeaderText("Choose a secure administrator password");
        dialog.getDialogPane().setContent(grid);

        ButtonType createButton =
                new ButtonType("Create Account", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                createButton, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {

            if (response != createButton) {
                return;
            }

            String password = passwordField.getText();
            String confirmation = confirmField.getText();

            if (password.length() < 8) {
                showMessage("Password must contain at least 8 characters.");
                return;
            }

            if (!password.equals(confirmation)) {
                showMessage("The passwords do not match.");
                return;
            }

            try {
                boolean registered = staffDAO.registerStaff(
                        fullName.trim(),
                        "admin",
                        password,
                        "Administrator"
                );

                if (registered) {
                    showMessage("Administrator account created successfully!");
                } else {
                    showMessage("The username 'admin' already exists.");
                }

            } catch (Exception e) {
                showMessage("Registration failed: " + e.getMessage());
            }
        });
    }

    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Administrator Setup");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
