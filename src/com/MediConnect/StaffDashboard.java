
package com.MediConnect;

import com.MediConnect.database.Staff;
import com.MediConnect.database.DashboardDAO;

import java.sql.SQLException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
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

        Label staffRole = new Label("Staff Account  |  " + staff.getRole());
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
        menuTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 13));

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

        for (String module : modules) {

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
                moduleButton.getStyleClass().add("nav-button-active");
            }
        }

        // ================= SIDEBAR SPACER =================

        Region spacer = new Region();

        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().add(spacer);

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

        // Load the application stylesheet when available.
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

        // Open maximized, while preserving normal window controls.
        stage.setMaximized(true);
        stage.show();
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

                Alert alert = new Alert(Alert.AlertType.ERROR);

                alert.setTitle("Database Error");
                alert.setHeaderText(
                        "Unable to load dashboard statistics"
                );
                alert.setContentText(e.getMessage());

                alert.showAndWait();

                e.printStackTrace();
            }

        } else if (module.equals("Patients")) {

            PatientsView patientsView = new PatientsView();

            contentArea.getChildren().add(patientsView);

            VBox.setVgrow(
                    patientsView,
                    Priority.ALWAYS
            );

        } else if (module.equals("Doctors")) {

            DoctorsView doctorsView = new DoctorsView();

            contentArea.getChildren().add(doctorsView);

            VBox.setVgrow(
                    doctorsView,
                    Priority.ALWAYS
            );

        } else if (module.equals("Appointments")) {

            AppointmentsView appointmentsView =
                    new AppointmentsView();

            contentArea.getChildren().add(appointmentsView);

            VBox.setVgrow(
                    appointmentsView,
                    Priority.ALWAYS
            );

        } else if (module.equals("Visits")) {

            VisitsView visitsView = new VisitsView();

            contentArea.getChildren().add(visitsView);

            VBox.setVgrow(
                    visitsView,
                    Priority.ALWAYS
            );

        } else if (module.equals("Diagnoses")) {

            DiagnosesView diagnosesView =
                    new DiagnosesView();

            contentArea.getChildren().add(diagnosesView);

            VBox.setVgrow(
                    diagnosesView,
                    Priority.ALWAYS
            );

        } else if (module.equals("Treatments")) {

            TreatmentsView treatmentsView =
                    new TreatmentsView();

            contentArea.getChildren().add(treatmentsView);

            VBox.setVgrow(
                    treatmentsView,
                    Priority.ALWAYS
            );

        } else if (module.equals("Billing")) {

            BillingView billingView = new BillingView();

            contentArea.getChildren().add(billingView);

            VBox.setVgrow(
                    billingView,
                    Priority.ALWAYS
            );

        } else if (module.equals("Patient History")) {

            PatientHistoryView patientHistoryView =
                    new PatientHistoryView();

            BorderPane historyScreen =
                    patientHistoryView.getView();

            contentArea.getChildren().add(historyScreen);

            VBox.setVgrow(
                    historyScreen,
                    Priority.ALWAYS
            );

        } else if (module.equals("Reports")) {

            ReportsView reportsView = new ReportsView();

            contentArea.getChildren().add(reportsView);

            VBox.setVgrow(
                    reportsView,
                    Priority.ALWAYS
            );

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
