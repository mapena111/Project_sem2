
package com.MediConnect;

import com.MediConnect.database.AppointmentDAO;
import com.MediConnect.database.AppointmentRecord;
import com.MediConnect.database.PatientDAO;
import com.MediConnect.database.PatientRecord;
import com.MediConnect.database.DoctorDAO;
import com.MediConnect.database.DoctorRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppointmentsView extends BorderPane {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private final TableView<AppointmentRecord> table = new TableView<>();
    private final ObservableList<AppointmentRecord> appointmentList =
            FXCollections.observableArrayList();

    private final TextField searchField = new TextField();

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("hh:mm a");

    public AppointmentsView() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f3f7fb;");

        createTable();
        createLayout();
        loadAppointments();
    }

    private void createLayout() {

        Label heading = new Label("Appointments Management");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label description = new Label(
                "Book, view, update and manage patient appointments."
        );

        searchField.setPromptText("Search appointment, patient, doctor ID or status");
        searchField.setPrefWidth(350);

        Button searchButton = new Button("Search");
        Button refreshButton = new Button("Refresh");
        Button bookButton = new Button("Book Appointment");
        Button editButton = new Button("Edit Appointment");
        Button completeButton = new Button("Mark Completed");
        Button cancelButton = new Button("Cancel Appointment");

        searchButton.setOnAction(e -> searchAppointments());
        refreshButton.setOnAction(e -> loadAppointments());
        bookButton.setOnAction(e -> showAppointmentDialog(null));
        editButton.setOnAction(e -> editSelectedAppointment());
        completeButton.setOnAction(e -> changeStatus("Completed"));
        cancelButton.setOnAction(e -> changeStatus("Cancelled"));

        HBox searchRow = new HBox(10,
                searchField, searchButton, refreshButton);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        HBox actionRow = new HBox(10,
                bookButton, editButton, completeButton, cancelButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox topArea = new VBox(10,
                heading, description, searchRow, actionRow);
        topArea.setPadding(new Insets(0, 0, 15, 0));

        setTop(topArea);
        setCenter(table);
    }

    private void createTable() {

        TableColumn<AppointmentRecord, Number> idColumn =
                new TableColumn<>("Appointment ID");
        idColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getAppointmentID()));

        TableColumn<AppointmentRecord, Number> patientColumn =
                new TableColumn<>("Patient ID");
        patientColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getPatientID()));

        TableColumn<AppointmentRecord, Number> doctorColumn =
                new TableColumn<>("Doctor ID");
        doctorColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getDoctorID()));

        TableColumn<AppointmentRecord, LocalDate> dateColumn =
                new TableColumn<>("Date");
        dateColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getAppointmentDate()));

        TableColumn<AppointmentRecord, LocalTime> timeColumn =
                new TableColumn<>("Time");
        timeColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getAppointmentTime()));

        timeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                setText(empty || time == null
                        ? null : time.format(timeFormatter));
            }
        });

        TableColumn<AppointmentRecord, String> statusColumn =
                new TableColumn<>("Status");
        statusColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getStatus()));

        idColumn.setPrefWidth(110);
        patientColumn.setPrefWidth(100);
        doctorColumn.setPrefWidth(100);
        dateColumn.setPrefWidth(130);
        timeColumn.setPrefWidth(130);
        statusColumn.setPrefWidth(130);

        table.getColumns().addAll(
                idColumn, patientColumn, doctorColumn,
                dateColumn, timeColumn, statusColumn);

        table.setItems(appointmentList);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No appointments found."));
    }

    private void loadAppointments() {
        try {
            appointmentList.setAll(
                    appointmentDAO.getAllAppointments());
        } catch (SQLException e) {
            showError("Unable to load appointments", e.getMessage());
        }
    }

    private void searchAppointments() {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadAppointments();
            return;
        }

        try {
            appointmentList.setAll(
                    appointmentDAO.searchAppointments(keyword));
        } catch (SQLException e) {
            showError("Search failed", e.getMessage());
        }
    }

    private void showAppointmentDialog(AppointmentRecord existing) {

        try {
            List<PatientRecord> patients = patientDAO.getAllPatients();
            List<DoctorRecord> doctors = doctorDAO.getAllDoctors();

            if (patients.isEmpty()) {
                showError("No patients available",
                        "Register an active patient before booking.");
                return;
            }

            if (doctors.isEmpty()) {
                showError("No doctors available",
                        "Register an active doctor before booking.");
                return;
            }

            ComboBox<PatientRecord> patientCombo =
                    new ComboBox<>(FXCollections.observableArrayList(patients));

            ComboBox<DoctorRecord> doctorCombo =
                    new ComboBox<>(FXCollections.observableArrayList(doctors));

            patientCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(PatientRecord patient) {
                    return patient == null ? "" :
                            patient.getPatientID() + " - "
                                    + patient.getFullName();
                }

                @Override
                public PatientRecord fromString(String value) {
                    return null;
                }
            });

            doctorCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(DoctorRecord doctor) {
                    return doctor == null ? "" :
                            doctor.getDoctorID() + " - "
                                    + doctor.getFullName();
                }

                @Override
                public DoctorRecord fromString(String value) {
                    return null;
                }
            });

            patientCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(PatientRecord patient,
                        boolean empty) {
                    super.updateItem(patient, empty);
                    setText(empty || patient == null ? "" :
                            patient.getPatientID() + " - "
                                    + patient.getFullName());
                }
            });

            doctorCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(DoctorRecord doctor,
                        boolean empty) {
                    super.updateItem(doctor, empty);
                    setText(empty || doctor == null ? "" :
                            doctor.getDoctorID() + " - "
                                    + doctor.getFullName());
                }
            });

            DatePicker datePicker = new DatePicker();
            datePicker.setValue(LocalDate.now());

            ComboBox<LocalTime> timeCombo =
                    new ComboBox<>(FXCollections.observableArrayList(
                            generateTimes()));

            timeCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(LocalTime time) {
                    return time == null ? "" :
                            time.format(timeFormatter);
                }

                @Override
                public LocalTime fromString(String value) {
                    return null;
                }
            });

            timeCombo.getSelectionModel().selectFirst();

            if (existing != null) {
                patientCombo.setValue(findPatient(
                        patients, existing.getPatientID()));
                doctorCombo.setValue(findDoctor(
                        doctors, existing.getDoctorID()));
                datePicker.setValue(existing.getAppointmentDate());
                timeCombo.setValue(existing.getAppointmentTime());
            }

            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(12);
            form.setPadding(new Insets(15));

            form.add(new Label("Patient:"), 0, 0);
            form.add(patientCombo, 1, 0);

            form.add(new Label("Doctor:"), 0, 1);
            form.add(doctorCombo, 1, 1);

            form.add(new Label("Date:"), 0, 2);
            form.add(datePicker, 1, 2);

            form.add(new Label("Time:"), 0, 3);
            form.add(timeCombo, 1, 3);

            patientCombo.setPrefWidth(260);
            doctorCombo.setPrefWidth(260);
            datePicker.setPrefWidth(260);
            timeCombo.setPrefWidth(260);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(existing == null
                    ? "Book Appointment" : "Edit Appointment");

            dialog.getDialogPane().getButtonTypes().addAll(
                    ButtonType.OK, ButtonType.CANCEL);

            dialog.getDialogPane().setContent(form);

            Button okButton = (Button) dialog.getDialogPane()
                    .lookupButton(ButtonType.OK);

            okButton.addEventFilter(
                    javafx.event.ActionEvent.ACTION, event -> {

                if (patientCombo.getValue() == null
                        || doctorCombo.getValue() == null
                        || datePicker.getValue() == null
                        || timeCombo.getValue() == null) {

                    showError("Incomplete details",
                            "Please select a patient, doctor, date and time.");
                    event.consume();
                    return;
                }

                if (datePicker.getValue().isBefore(LocalDate.now())) {
                    showError("Invalid date",
                            "Appointments cannot be booked in the past.");
                    event.consume();
                    return;
                }

                try {
                    boolean success;

                    if (existing == null) {
                        success = appointmentDAO.bookAppointment(
                                patientCombo.getValue().getPatientID(),
                                doctorCombo.getValue().getDoctorID(),
                                datePicker.getValue(),
                                timeCombo.getValue());
                    } else {
                        success = appointmentDAO.updateAppointment(
                                existing.getAppointmentID(),
                                patientCombo.getValue().getPatientID(),
                                doctorCombo.getValue().getDoctorID(),
                                datePicker.getValue(),
                                timeCombo.getValue());
                    }

                    if (!success) {
                        showError("Operation failed",
                                "The appointment could not be saved.");
                        event.consume();
                        return;
                    }

                } catch (SQLException ex) {
                    showError("Database error", ex.getMessage());
                    event.consume();
                }
            });

            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    loadAppointments();
                    showInformation(existing == null
                            ? "Appointment booked successfully."
                            : "Appointment updated successfully.");
                }
            });

        } catch (SQLException e) {
            showError("Unable to load booking details", e.getMessage());
        }
    }

    private List<LocalTime> generateTimes() {
        List<LocalTime> times = new ArrayList<>();

        for (int hour = 8; hour <= 17; hour++) {
            times.add(LocalTime.of(hour, 0));
        }

        return times;
    }

    private PatientRecord findPatient(List<PatientRecord> patients,
            int patientID) {
        for (PatientRecord patient : patients) {
            if (patient.getPatientID() == patientID) {
                return patient;
            }
        }
        return null;
    }

    private DoctorRecord findDoctor(List<DoctorRecord> doctors,
            int doctorID) {
        for (DoctorRecord doctor : doctors) {
            if (doctor.getDoctorID() == doctorID) {
                return doctor;
            }
        }
        return null;
    }

    private void editSelectedAppointment() {
        AppointmentRecord selected =
                table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("No appointment selected",
                    "Select an appointment from the table first.");
            return;
        }

        if ("Completed".equalsIgnoreCase(selected.getStatus())
                || "Cancelled".equalsIgnoreCase(selected.getStatus())) {
            showError("Appointment cannot be edited",
                    "Completed or cancelled appointments cannot be edited.");
            return;
        }

        showAppointmentDialog(selected);
    }

    private void changeStatus(String status) {
        AppointmentRecord selected =
                table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("No appointment selected",
                    "Select an appointment from the table first.");
            return;
        }

        if ("Completed".equalsIgnoreCase(selected.getStatus())
                || "Cancelled".equalsIgnoreCase(selected.getStatus())) {
            showError("Appointment already finalized",
                    "This appointment is already completed or cancelled.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Appointment Status");
        confirmation.setHeaderText("Change appointment status?");
        confirmation.setContentText(
                "Appointment ID: " + selected.getAppointmentID()
                        + "\nNew status: " + status);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (appointmentDAO.updateAppointmentStatus(
                            selected.getAppointmentID(), status)) {

                        loadAppointments();
                        showInformation(
                                "Appointment status changed to " + status + ".");
                    }
                } catch (SQLException e) {
                    showError("Unable to update status", e.getMessage());
                }
            }
        });
    }

    private void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("MediConnect");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("MediConnect");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
