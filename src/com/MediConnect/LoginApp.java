
package com.MediConnect;

import com.MediConnect.database.StaffDAO;
import com.MediConnect.database.Staff;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginApp extends Application {

    private final StaffDAO staffDAO = new StaffDAO();

    @Override
    public void start(Stage stage) {

        // MAIN BACKGROUND
        VBox mainLayout = new VBox(18);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(40));
        mainLayout.setStyle(
                "-fx-background-color: #f1f5f9;"
        );

        // APPLICATION BRANDING
        Label title = new Label("MediConnect");
        title.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 34));
        title.setTextFill(Color.web("#1677b8"));

        Label subtitle = new Label("Hospital Management System");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#64748b"));

        // LOGIN CARD
        VBox loginCard = new VBox(16);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(35));
        loginCard.setMaxWidth(390);
        loginCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 14;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 18, 0, 0, 5);"
        );

        // LOGIN HEADING
        Label loginHeading = new Label("Staff Login");
        loginHeading.setFont(
                Font.font("Segoe UI", FontWeight.BOLD, 24)
        );
        loginHeading.setTextFill(Color.web("#1e293b"));

        Label instructionLabel = new Label(
                "Sign in to access the MediConnect dashboard"
        );
        instructionLabel.setFont(Font.font("Segoe UI", 12));
        instructionLabel.setTextFill(Color.web("#64748b"));
        instructionLabel.setWrapText(true);

        // USERNAME FIELD
        Label usernameLabel = new Label("Username");
        usernameLabel.setMaxWidth(Double.MAX_VALUE);
        usernameLabel.setFont(
                Font.font("Segoe UI", FontWeight.BOLD, 12)
        );
        usernameLabel.setTextFill(Color.web("#334155"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(43);
        usernameField.setMaxWidth(Double.MAX_VALUE);
        usernameField.setStyle(
                "-fx-background-radius: 6;" +
                "-fx-border-radius: 6;" +
                "-fx-border-color: #cbd5e1;" +
                "-fx-padding: 10;"
        );

        // PASSWORD FIELD
        Label passwordLabel = new Label("Password");
        passwordLabel.setMaxWidth(Double.MAX_VALUE);
        passwordLabel.setFont(
                Font.font("Segoe UI", FontWeight.BOLD, 12)
        );
        passwordLabel.setTextFill(Color.web("#334155"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(43);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setStyle(
                "-fx-background-radius: 6;" +
                "-fx-border-radius: 6;" +
                "-fx-border-color: #cbd5e1;" +
                "-fx-padding: 10;"
        );

        // MESSAGE LABEL
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.web("#dc2626"));
        messageLabel.setWrapText(true);

        // LOGIN BUTTON
        Button loginButton = new Button("LOGIN");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(44);
        loginButton.setFont(
                Font.font("Segoe UI", FontWeight.BOLD, 13)
        );
        loginButton.setStyle(
                "-fx-background-color: #1677b8;" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );

        loginButton.setOnAction(event -> {

            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText(
                        "Please enter both username and password."
                );
                return;
            }

            try {
                boolean authenticated =
                        staffDAO.authenticateStaff(username, password);

                if (authenticated) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Login Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Welcome to MediConnect!");

                    alert.showAndWait();

                    messageLabel.setText("");

                } else {
                    messageLabel.setText(
                            "Invalid username or password, or account inactive."
                    );
                }

                Staff loggedInStaff =
                        staffDAO.getAuthenticatedStaff(username, password);

                if (loggedInStaff != null) {

                    messageLabel.setText("");

                    StaffDashboard dashboard =
                            new StaffDashboard(stage, loggedInStaff);

                    dashboard.show();

                } else {

                    messageLabel.setText(
                            "Invalid username or password, or account inactive."
                    );
                }

            } catch (SQLException e) {
                messageLabel.setText(
                        "Unable to connect to the database."
                );
                e.printStackTrace();
            }
        });

        // EXIT BUTTON
        Button exitButton = new Button("EXIT");
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setPrefHeight(42);
        exitButton.setFont(
                Font.font("Segoe UI", FontWeight.BOLD, 13)
        );
        exitButton.setStyle(
                "-fx-background-color: #e2e8f0;" +
                "-fx-text-fill: #334155;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );

        exitButton.setOnAction(event -> {

            Alert confirmation = new Alert(
                    Alert.AlertType.CONFIRMATION
            );

            confirmation.setTitle("Exit MediConnect");
            confirmation.setHeaderText(
                    "Are you sure you want to exit?"
            );
            confirmation.setContentText(
                    "The application will close."
            );

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stage.close();
                }
            });
        });

        // ASSEMBLE LOGIN CARD
        loginCard.getChildren().addAll(
                loginHeading,
                instructionLabel,
                new Separator(),
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                messageLabel,
                loginButton,
                exitButton
        );

        // FOOTER
        Label footer = new Label(
                "Authorized staff access only"
        );
        footer.setFont(Font.font("Segoe UI", 11));
        footer.setTextFill(Color.web("#94a3b8"));

        // ASSEMBLE MAIN SCREEN
        mainLayout.getChildren().addAll(
                title,
                subtitle,
                loginCard,
                footer
        );

        Scene scene = new Scene(mainLayout, 520, 650);

        stage.setTitle("MediConnect - Staff Login");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
