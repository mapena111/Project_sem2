
package com.MediConnect;

import com.MediConnect.database.Staff;
import com.MediConnect.database.StaffDAO;
import com.MediConnect.database.DashboardDAO;

import java.time.LocalDate;
import java.sql.SQLException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class StaffDashboard {

    private Stage stage;
    private Staff staff;
    private BorderPane mainLayout;
    private VBox contentArea;

    private Button activeModuleButton;

    private DashboardDAO dashboardDAO = new DashboardDAO();
    private StaffDAO staffDAO = new StaffDAO();

    public StaffDashboard(Stage stage, Staff staff) {
        this.stage = stage;
        this.staff = staff;
    }

    public void show() {

        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("app-background");

        // ================= TOP HEADER =================

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.getStyleClass().add("top-header");

        VBox branding = new VBox(3);

        Label appName = new Label("MediConnect");
        appName.getStyleClass().add("brand-title");

        Label subtitle = new Label("HOSPITAL MANAGEMENT SYSTEM");
        subtitle.getStyleClass().add("brand-subtitle");

        branding.getChildren().addAll(appName, subtitle);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        VBox staffInformation = new VBox(4);
        staffInformation.setAlignment(Pos.CENTER_RIGHT);

        Label staffName = new Label(staff.getFullName());
        staffName.getStyleClass().add("staff-name");

        Label staffRole = new Label(
                "Staff Account  |  " + staff.getRole()
        );
        staffRole.getStyleClass().add("staff-role");

        staffInformation.getChildren().addAll(
                staffName,
                staffRole
        );

        header.getChildren().addAll(
                branding,
                headerSpacer,
                staffInformation
        );

        mainLayout.setTop(header);

        // ================= LEFT SIDEBAR =================

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(25, 15, 20, 15));
        sidebar.setPrefWidth(230);
        sidebar.setMinWidth(210);
        sidebar.getStyleClass().add("sidebar");

        Label menuTitle = new Label("MAIN MENU");

        menuTitle.setTextFill(Color.WHITE);
        menuTitle.setFont(
                Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 13)
        );

        menuTitle.setStyle(
                "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 13px;"
        );

        menuTitle.setPadding(new Insets(0, 0, 15, 8));

        sidebar.getChildren().add(menuTitle);

        String[] modules = {
                "Dashboard",
                "Patients",
                "Doctors",
                "Appointments",
                "Visits",
                "Diagnoses",
                "Treatments",
                "Billing",
                "Patient History",
                "Reports"
        };

        // Normal hospital modules
        for (String module : modules) {

            addNavigationButton(sidebar, module);
        }

        // Staff Management is only visible to administrators.
        if ("Administrator".equalsIgnoreCase(staff.getRole())) {

            addNavigationButton(sidebar, "Staff Management");
        }

        // ================= SIDEBAR SPACER =================

        Region spacer = new Region();

        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().add(spacer);

        // ================= CHANGE PASSWORD =================

        Button passwordButton = new Button("CHANGE MY PASSWORD");

        passwordButton.setMaxWidth(Double.MAX_VALUE);
        passwordButton.setPrefHeight(43);
        passwordButton.getStyleClass().add("nav-button");

        passwordButton.setOnAction(event -> changeMyPassword());

        // ================= LOGOUT BUTTON =================

        Button logoutButton = new Button("LOG OUT");

        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setPrefHeight(43);
        logoutButton.getStyleClass().add("logout-button");

        logoutButton.setOnAction(event -> logout());

        // ================= EXIT BUTTON =================

        Button exitButton = new Button("EXIT APPLICATION");

        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setPrefHeight(43);
        exitButton.getStyleClass().add("exit-button");

        exitButton.setOnAction(event -> exitApplication());

        sidebar.getChildren().addAll(
                passwordButton,
                logoutButton,
                exitButton
        );

        mainLayout.setLeft(sidebar);

        // ================= MAIN CONTENT =================

        contentArea = new VBox(20);
        contentArea.setPadding(new Insets(30));
        contentArea.getStyleClass().add("content-area");

        mainLayout.setCenter(contentArea);

        showModule("Dashboard");

        // ================= SCENE =================

        Scene scene = new Scene(mainLayout, 1200, 750);

        java.net.URL stylesheet =
                getClass().getResource("style.css");

        if (stylesheet != null) {
            scene.getStylesheets().add(
                    stylesheet.toExternalForm()
            );
        }

        stage.setTitle("MediConnect - Staff Dashboard");

        stage.setMinWidth(900);
        stage.setMinHeight(600);

        stage.setResizable(true);
        stage.setFullScreen(false);

        stage.setScene(scene);

        stage.setMaximized(true);
        stage.show();
    }

    // ================= ADD NAVIGATION BUTTON =================

    private void addNavigationButton(VBox sidebar, String module) {

        Button moduleButton = new Button(module);

        moduleButton.setMaxWidth(Double.MAX_VALUE);
        moduleButton.setPrefHeight(43);
        moduleButton.setAlignment(Pos.CENTER_LEFT);

        moduleButton.getStyleClass().add("nav-button");

        moduleButton.setOnAction(event -> {

            setActiveNavigation(moduleButton);

            showModule(module);
        });

        sidebar.getChildren().add(moduleButton);

        if (module.equals("Dashboard")) {

            activeModuleButton = moduleButton;

            moduleButton.getStyleClass().add(
                    "nav-button-active"
            );
        }
    }

    // ================= ACTIVE NAVIGATION =================

    private void setActiveNavigation(Button selectedButton) {

        if (activeModuleButton != null) {

            activeModuleButton.getStyleClass().remove(
                    "nav-button-active"
            );
        }

        activeModuleButton = selectedButton;

        if (!activeModuleButton.getStyleClass().contains(
                "nav-button-active")) {

            activeModuleButton.getStyleClass().add(
                    "nav-button-active"
            );
        }
    }

    // ================= DISPLAY SELECTED MODULE =================

    private void showModule(String module) {

        contentArea.getChildren().clear();

        Label heading = new Label(module);
        heading.getStyleClass().add("page-title");

        if (module.equals("Dashboard")) {

            Label description = new Label(
                    "Welcome, " + staff.getFullName()
                    + "! Welcome to the MediConnect staff management system."
            );

            description.getStyleClass().add("welcome-heading");

            Label information = new Label(
                    "Use the navigation menu to manage hospital records, "
                    + "appointments, medical services and billing."
            );

            information.setWrapText(true);
            information.getStyleClass().add("page-subtitle");

            VBox welcomeSection = new VBox(8);

            welcomeSection.getChildren().addAll(
                    description,
                    information
            );

            contentArea.getChildren().addAll(
                    heading,
                    welcomeSection
            );

            try {

                int patients = dashboardDAO.getTotalPatients();
                int doctors = dashboardDAO.getTotalDoctors();
                int appointments = dashboardDAO.getTotalAppointments();
                int staffMembers = dashboardDAO.getTotalStaff();

                HBox statistics = new HBox(18);
                statistics.setAlignment(Pos.CENTER_LEFT);

                statistics.getChildren().addAll(
                        createStatCard("Total Patients", patients),
                        createStatCard("Total Doctors", doctors),
                        createStatCard("Appointments", appointments),
                        createStatCard("Staff Members", staffMembers)
                );

                contentArea.getChildren().add(statistics);

            } catch (SQLException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Unable to load dashboard statistics.",
                        e.getMessage()
                );

                e.printStackTrace();
            }

            contentArea.getChildren().add(
                    createVisitDateSearchSection()
            );

        } else if (module.equals("Patients")) {

            PatientsView patientsView = new PatientsView();

            contentArea.getChildren().add(patientsView);

            VBox.setVgrow(patientsView, Priority.ALWAYS);

        } else if (module.equals("Doctors")) {

            DoctorsView doctorsView = new DoctorsView();

            contentArea.getChildren().add(doctorsView);

            VBox.setVgrow(doctorsView, Priority.ALWAYS);

        } else if (module.equals("Appointments")) {

            AppointmentsView appointmentsView =
                    new AppointmentsView();

            contentArea.getChildren().add(appointmentsView);

            VBox.setVgrow(appointmentsView, Priority.ALWAYS);

        } else if (module.equals("Visits")) {

            VisitsView visitsView = new VisitsView();

            contentArea.getChildren().add(visitsView);

            VBox.setVgrow(visitsView, Priority.ALWAYS);

        } else if (module.equals("Diagnoses")) {

            DiagnosesView diagnosesView =
                    new DiagnosesView();

            contentArea.getChildren().add(diagnosesView);

            VBox.setVgrow(diagnosesView, Priority.ALWAYS);

        } else if (module.equals("Treatments")) {

            TreatmentsView treatmentsView =
                    new TreatmentsView();

            contentArea.getChildren().add(treatmentsView);

            VBox.setVgrow(treatmentsView, Priority.ALWAYS);

        } else if (module.equals("Billing")) {

            BillingView billingView = new BillingView();

            contentArea.getChildren().add(billingView);

            VBox.setVgrow(billingView, Priority.ALWAYS);

        } else if (module.equals("Patient History")) {

            PatientHistoryView patientHistoryView =
                    new PatientHistoryView();

            BorderPane historyScreen =
                    patientHistoryView.getView();

            contentArea.getChildren().add(historyScreen);

            VBox.setVgrow(historyScreen, Priority.ALWAYS);

        } else if (module.equals("Reports")) {

            ReportsView reportsView = new ReportsView();

            contentArea.getChildren().add(reportsView);

            VBox.setVgrow(reportsView, Priority.ALWAYS);

        } else if (module.equals("Staff Management")) {

            // Extra security check before opening staff management.
            if (!"Administrator".equalsIgnoreCase(staff.getRole())) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Access Denied",
                        "Administrator access required.",
                        "You are not authorized to manage staff accounts."
                );

                return;
            }

            StaffView staffView =
                    new StaffView(staff.getStaffID());

            contentArea.getChildren().add(staffView);

            VBox.setVgrow(staffView, Priority.ALWAYS);

        } else {

            Label description = new Label(
                    module + " management will be implemented in the next stages."
            );

            description.setFont(Font.font("Arial", 15));

            contentArea.getChildren().addAll(
                    heading,
                    description
            );
        }
    }

    // ================= DASHBOARD STATISTICS CARD =================

    private VBox createStatCard(String title, int value) {

        VBox card = new VBox(12);

        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(22));
        card.setPrefWidth(210);
        card.setPrefHeight(125);
        card.setMinWidth(150);

        card.getStyleClass().add("stat-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        Label valueLabel = new Label(String.valueOf(value));
        valueLabel.getStyleClass().add("stat-value");

        card.getChildren().addAll(
                titleLabel,
                valueLabel
        );

        return card;
    }

    // ================= PATIENT VISITS BY DATE =================

    private VBox createVisitDateSearchSection() {

        VBox section = new VBox(12);

        section.setPadding(new Insets(20));
        section.setMaxWidth(Double.MAX_VALUE);
        section.getStyleClass().add("card");

        Label sectionHeading =
                new Label("PATIENT VISITS BY DATE");

        sectionHeading.getStyleClass().add("card-heading");

        Label description = new Label(
                "Select a date to find the number of patients who visited the hospital."
        );

        description.setWrapText(true);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPromptText("Select visit date");

        Button searchButton = new Button("SEARCH");
        searchButton.getStyleClass().add("primary-button");
        searchButton.setPrefHeight(38);

        Label resultLabel = new Label(
                "Select a date and click Search."
        );

        resultLabel.setWrapText(true);

        resultLabel.setStyle(
                "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
        );

        HBox searchControls = new HBox(12);

        searchControls.setAlignment(Pos.CENTER_LEFT);

        searchControls.getChildren().addAll(
                datePicker,
                searchButton
        );

        searchButton.setOnAction(event -> {

            LocalDate selectedDate = datePicker.getValue();

            if (selectedDate == null) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Date Required",
                        "Please select a date.",
                        "Choose the date you want to search before continuing."
                );

                return;
            }

            try {

                int patientCount =
                        dashboardDAO.getPatientsVisitedOnDate(selectedDate);

                if (patientCount == 1) {

                    resultLabel.setText(
                            "Result: 1 patient visited on "
                            + selectedDate + "."
                    );

                } else {

                    resultLabel.setText(
                            "Result: " + patientCount
                            + " patients visited on "
                            + selectedDate + "."
                    );
                }

            } catch (SQLException e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Database Error",
                        "Unable to retrieve patient visit information.",
                        e.getMessage()
                );

                e.printStackTrace();
            }
        });

        section.getChildren().addAll(
                sectionHeading,
                description,
                searchControls,
                resultLabel
        );

        return section;
    }

    // ================= CHANGE MY PASSWORD =================

    private void changeMyPassword() {

        PasswordField currentPassword = new PasswordField();
        PasswordField newPassword = new PasswordField();
        PasswordField confirmPassword = new PasswordField();

        currentPassword.setPromptText("Enter current password");
        newPassword.setPromptText("At least 8 characters");
        confirmPassword.setPromptText("Confirm new password");

        VBox fields = new VBox(10);

        fields.setPadding(new Insets(15));

        fields.getChildren().addAll(
                new Label("Current Password"),
                currentPassword,
                new Label("New Password"),
                newPassword,
                new Label("Confirm New Password"),
                confirmPassword
        );

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Change My Password");
        dialog.setHeaderText(
                "Change password for " + staff.getUsername()
        );

        dialog.getDialogPane().setContent(fields);

        ButtonType changeButton = new ButtonType(
                "Change Password",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                changeButton,
                ButtonType.CANCEL
        );

        dialog.showAndWait().ifPresent(response -> {

            if (response != changeButton) {
                return;
            }

            String current = currentPassword.getText();
            String newPass = newPassword.getText();
            String confirmation = confirmPassword.getText();

            if (current.isEmpty()
                    || newPass.isEmpty()
                    || confirmation.isEmpty()) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Missing Information",
                        "Complete all password fields.",
                        null
                );

                return;
            }

            if (newPass.length() < 8) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Weak Password",
                        "New password must contain at least 8 characters.",
                        null
                );

                return;
            }

            if (!newPass.equals(confirmation)) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Password Mismatch",
                        "The new passwords do not match.",
                        "Please confirm your new password correctly."
                );

                return;
            }

            if (current.equals(newPass)) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Choose a Different Password",
                        "Your new password must be different.",
                        null
                );

                return;
            }

            try {

                boolean changed =
                        staffDAO.changeOwnPassword(
                                staff.getStaffID(),
                                current,
                                newPass
                        );

                if (changed) {

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Password Changed",
                            "Your password was changed successfully.",
                            "Use your new password the next time you log in."
                    );

                } else {

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Password Change Failed",
                            "Your current password is incorrect "
                            + "or your account is inactive.",
                            "Check your current password and try again."
                    );
                }

            } catch (Exception e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Password Error",
                        "Unable to change your password.",
                        e.getMessage()
                );

                e.printStackTrace();
            }
        });
    }

    // ================= ALERT HELPER =================

    private void showAlert(
            Alert.AlertType type,
            String title,
            String header,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(header);

        if (message != null) {
            alert.setContentText(message);
        }

        alert.showAndWait();
    }

    // ================= LOG OUT =================

    private void logout() {

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Log Out");

        confirmation.setHeaderText(
                "Are you sure you want to log out?"
        );

        confirmation.setContentText(
                "You will return to the staff login screen."
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                LoginApp loginApp = new LoginApp();

                loginApp.start(stage);
            }
        });
    }

    // ================= EXIT APPLICATION =================

    private void exitApplication() {

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

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
    }
}
