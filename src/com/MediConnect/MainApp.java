package com.MediConnect;

import com.MediConnect.module.*;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainApp extends Application {

    private appointment_manager appointmentManager;
    private patient currentPatient;

    @Override
    public void start(Stage primaryStage) {
        appointmentManager = new appointment_manager();
        initializeSampleData();

        TabPane tabPane = new TabPane();

        Tab appointmentTab = createAppointmentTab();
        Tab profileTab = createProfileTab();
        Tab doctorsTab = createDoctorsTab();

        tabPane.getTabs().addAll(appointmentTab, profileTab, doctorsTab);

        Scene scene = new Scene(tabPane, 800, 600);
        primaryStage.setTitle("MediConnect - Patient Portal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeSampleData() {
        doctor doc1 = new doctor("Dr. Smith", "Cardiology", "Mon, Wed");
        doctor doc2 = new doctor("Dr. Jones", "Pediatrics", "Tue, Thu");
        doctor doc3 = new doctor("Dr. Brown", "General Practice", "Available Tue, Thu");

        appointmentManager.addDoctor(doc1);
        appointmentManager.addDoctor(doc2);
        appointmentManager.addDoctor(doc3);

        currentPatient = new patient("John Doe", "P100", "No significant history.");
        appointmentManager.addPatient(currentPatient);
    }

    private Tab createAppointmentTab() {
        Tab tab = new Tab("Appointments");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Book an Appointment");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<doctor> doctorSelect = new ComboBox<>();
        doctorSelect.getItems().addAll(appointmentManager.getDoctors());
        doctorSelect.setPromptText("Select Doctor");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select Date");

        TextField timeField = new TextField();
        timeField.setPromptText("Enter Time (e.g., 10:00 AM)");

        Button bookButton = new Button("Confirm Booking");
        bookButton.setStyle("-fx-background-color: #0076a3; -fx-text-fill: white;");

        Label statusLabel = new Label();

        bookButton.setOnAction(e -> {
            doctor selectedDoctor = doctorSelect.getValue();
            String selectedDate = datePicker.getValue() != null ? datePicker.getValue().toString() : null;
            String time = timeField.getText();

            if (selectedDoctor != null && selectedDate != null && !time.isEmpty()) {
                appointment newApp = new appointment(selectedDoctor, currentPatient, selectedDate, time, "Confirmed");
                appointmentManager.scheduleAppointment(newApp);
                statusLabel.setText("Appointment confirmed with " + selectedDoctor.getName() + " on " + selectedDate + " at " + time);
                statusLabel.setTextFill(Color.GREEN);
            } else {
                statusLabel.setText("Please select a doctor, date, and enter a time.");
                statusLabel.setTextFill(Color.RED);
            }
        });

        ListView<appointment> appointmentList = new ListView<>();
        appointmentList.getItems().addAll(appointmentManager.getAppointments());

        Button refreshButton = new Button("Refresh Appointments");
        refreshButton.setOnAction(e -> {
            appointmentList.getItems().setAll(appointmentManager.getAppointments());
        });

        layout.getChildren().addAll(
                title, new Label("Select Doctor:"), doctorSelect,
                new Label("Select Date:"), datePicker,
                new Label("Enter Time:"), timeField,
                bookButton, statusLabel,
                new Label("Your Appointments:"), appointmentList, refreshButton
        );

        tab.setContent(layout);
        return tab;
    }

    private Tab createProfileTab() {
        Tab tab = new Tab("Patient Profile");
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Name:"), 0, 0);
        TextField nameField = new TextField(currentPatient.getName());
        grid.add(nameField, 1, 0);

        grid.add(new Label("Patient ID:"), 0, 1);
        TextField idField = new TextField(currentPatient.getId());
        idField.setEditable(false);
        grid.add(idField, 1, 1);

        grid.add(new Label("Medical History:"), 0, 2);
        TextArea historyArea = new TextArea(currentPatient.getMedicalHistory());
        historyArea.setPrefRowCount(3);
        grid.add(historyArea, 1, 2);

        Button saveButton = new Button("Save Profile");
        saveButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profile Saved");
            alert.setHeaderText(null);
            alert.setContentText("Profile information has been updated.");
            alert.showAndWait();
        });
        grid.add(saveButton, 1, 3);

        tab.setContent(grid);
        return tab;
    }

    private Tab createDoctorsTab() {
        Tab tab = new Tab("Doctors");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label title = new Label("Available Specialists");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<doctor> doctorList = new ListView<>();
        doctorList.getItems().addAll(appointmentManager.getDoctors());

        layout.getChildren().addAll(title, doctorList);
        tab.setContent(layout);
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}