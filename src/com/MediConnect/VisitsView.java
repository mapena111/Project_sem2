
package com.MediConnect;

import com.MediConnect.database.VisitDAO;
import com.MediConnect.database.VisitRecord;
import com.MediConnect.database.PatientDAO;
import com.MediConnect.database.PatientRecord;
import com.MediConnect.database.DoctorDAO;
import com.MediConnect.database.DoctorRecord;
import com.MediConnect.database.AppointmentDAO;
import com.MediConnect.database.AppointmentRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class VisitsView extends BorderPane {

    private final VisitDAO visitDAO = new VisitDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    private final TableView<VisitRecord> table = new TableView<>();
    private final ObservableList<VisitRecord> visitList =
            FXCollections.observableArrayList();

    private final TextField searchField = new TextField();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public VisitsView() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f3f7fb;");

        createTable();
        createLayout();
        loadVisits();
    }

    private void createLayout() {

        Label heading = new Label("Visits Management");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label description = new Label(
                "Register and manage patient consultation records."
        );

        searchField.setPromptText(
                "Search by Visit ID, Patient ID, Doctor ID or notes"
        );
        searchField.setPrefWidth(350);

        Button searchButton = new Button("Search");
        Button refreshButton = new Button("Refresh");
        Button addButton = new Button("Register Visit");
        Button editButton = new Button("Edit Notes");

        searchButton.setOnAction(e -> searchVisits());
        refreshButton.setOnAction(e -> loadVisits());
        addButton.setOnAction(e -> showVisitDialog(null));
        editButton.setOnAction(e -> editSelectedVisit());

        HBox searchRow = new HBox(10,
                searchField, searchButton, refreshButton);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        HBox actionRow = new HBox(10, addButton, editButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox topArea = new VBox(10,
                heading, description, searchRow, actionRow);
        topArea.setPadding(new Insets(0, 0, 15, 0));

        setTop(topArea);
        setCenter(table);
    }

    private void createTable() {

        TableColumn<VisitRecord, Number> idColumn =
                new TableColumn<>("Visit ID");
        idColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getVisitID()));

        TableColumn<VisitRecord, Number> patientColumn =
                new TableColumn<>("Patient ID");
        patientColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getPatientID()));

        TableColumn<VisitRecord, Number> doctorColumn =
                new TableColumn<>("Doctor ID");
        doctorColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getDoctorID()));

        TableColumn<VisitRecord, Number> appointmentColumn =
                new TableColumn<>("Appointment ID");
        appointmentColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        data.getValue().getAppointmentID()));

        TableColumn<VisitRecord, String> dateColumn =
                new TableColumn<>("Visit Date");
        dateColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getVisitDate() == null ? "" :
                        data.getValue().getVisitDate().format(dateFormatter)));

        TableColumn<VisitRecord, String> notesColumn =
                new TableColumn<>("Consultation Notes");
        notesColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNotes() == null ? "" :
                        data.getValue().getNotes()));

        idColumn.setPrefWidth(80);
        patientColumn.setPrefWidth(100);
        doctorColumn.setPrefWidth(100);
        appointmentColumn.setPrefWidth(120);
        dateColumn.setPrefWidth(180);
        notesColumn.setPrefWidth(300);

        table.getColumns().addAll(
                idColumn, patientColumn, doctorColumn,
                appointmentColumn, dateColumn, notesColumn);

        table.setItems(visitList);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No visits found."));
    }

    private void loadVisits() {
        try {
            visitList.setAll(visitDAO.getAllVisits());
        } catch (SQLException e) {
            showError("Unable to load visits", e.getMessage());
        }
    }

    private void searchVisits() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadVisits();
            return;
        }

        try {
            visitList.setAll(visitDAO.searchVisits(keyword));
        } catch (SQLException e) {
            showError("Search failed", e.getMessage());
        }
    }

    private void showVisitDialog(VisitRecord existing) {

        try {
            List<PatientRecord> patients = patientDAO.getAllPatients();
            List<DoctorRecord> doctors = doctorDAO.getAllDoctors();

            List<AppointmentRecord> completedAppointments =
                    appointmentDAO.getAllAppointments().stream()
                    .filter(a -> "Completed".equalsIgnoreCase(a.getStatus()))
                    .collect(Collectors.toList());

            if (patients.isEmpty()) {
                showError("No patients available",
                        "Register an active patient first.");
                return;
            }

            if (doctors.isEmpty()) {
                showError("No doctors available",
                        "Register an active doctor first.");
                return;
            }

            ComboBox<PatientRecord> patientCombo =
                    new ComboBox<>(FXCollections.observableArrayList(patients));

            ComboBox<DoctorRecord> doctorCombo =
                    new ComboBox<>(FXCollections.observableArrayList(doctors));

            ComboBox<AppointmentRecord> appointmentCombo =
                    new ComboBox<>(FXCollections.observableArrayList(
                            completedAppointments));

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

            appointmentCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(AppointmentRecord appointment) {
                    return appointment == null ? "Walk-in / No appointment" :
                            "Appointment #" + appointment.getAppointmentID()
                                    + " (Patient " + appointment.getPatientID()
                                    + ", Doctor " + appointment.getDoctorID()
                                    + ")";
                }

                @Override
                public AppointmentRecord fromString(String value) {
                    return null;
                }
            });

            appointmentCombo.setPromptText(
                    "Optional: select completed appointment");

            patientCombo.setPrefWidth(300);
            doctorCombo.setPrefWidth(300);
            appointmentCombo.setPrefWidth(300);

            TextArea notesArea = new TextArea();
            notesArea.setPromptText("Enter consultation notes...");
            notesArea.setPrefRowCount(5);
            notesArea.setWrapText(true);

            appointmentCombo.valueProperty().addListener(
                    (observable, oldValue, newValue) -> {

                if (newValue != null) {
                    PatientRecord selectedPatient =
                            findPatient(patients, newValue.getPatientID());

                    DoctorRecord selectedDoctor =
                            findDoctor(doctors, newValue.getDoctorID());

                    if (selectedPatient != null) {
                        patientCombo.setValue(selectedPatient);
                    }

                    if (selectedDoctor != null) {
                        doctorCombo.setValue(selectedDoctor);
                    }
                }
            });

            if (existing != null) {
                PatientRecord patient =
                        findPatient(patients, existing.getPatientID());

                DoctorRecord doctor =
                        findDoctor(doctors, existing.getDoctorID());

                if (patient != null) {
                    patientCombo.setValue(patient);
                }

                if (doctor != null) {
                    doctorCombo.setValue(doctor);
                }

                notesArea.setText(existing.getNotes() == null
                        ? "" : existing.getNotes());

                if (existing.getAppointmentID() != null) {
                    for (AppointmentRecord appointment :
                            completedAppointments) {

                        if (appointment.getAppointmentID()
                                == existing.getAppointmentID()) {
                            appointmentCombo.setValue(appointment);
                            break;
                        }
                    }
                }
            }

            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(12);
            form.setPadding(new Insets(15));

            form.add(new Label("Patient:"), 0, 0);
            form.add(patientCombo, 1, 0);

            form.add(new Label("Doctor:"), 0, 1);
            form.add(doctorCombo, 1, 1);

            form.add(new Label("Appointment:"), 0, 2);
            form.add(appointmentCombo, 1, 2);

            form.add(new Label("Consultation Notes:"), 0, 3);
            form.add(notesArea, 1, 3);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(existing == null
                    ? "Register Patient Visit" : "Edit Visit Notes");

            dialog.getDialogPane().getButtonTypes().addAll(
                    ButtonType.OK, ButtonType.CANCEL);

            dialog.getDialogPane().setContent(form);

            Button okButton = (Button) dialog.getDialogPane()
                    .lookupButton(ButtonType.OK);

            okButton.addEventFilter(
                    javafx.event.ActionEvent.ACTION, event -> {

                if (existing == null
                        && (patientCombo.getValue() == null
                        || doctorCombo.getValue() == null)) {

                    showError("Incomplete details",
                            "Please select a patient and doctor.");
                    event.consume();
                    return;
                }

                if (notesArea.getText().trim().isEmpty()) {
                    showError("Missing consultation notes",
                            "Please enter the consultation notes.");
                    event.consume();
                    return;
                }

                if (existing == null) {
                    AppointmentRecord selectedAppointment =
                            appointmentCombo.getValue();

                    Integer appointmentID =
                            selectedAppointment == null ? null :
                            selectedAppointment.getAppointmentID();

                    try {
                        int visitID = visitDAO.registerVisit(
                                patientCombo.getValue().getPatientID(),
                                doctorCombo.getValue().getDoctorID(),
                                appointmentID,
                                notesArea.getText().trim());

                        if (visitID <= 0) {
                            showError("Registration failed",
                                    "The visit could not be registered.");
                            event.consume();
                        }

                    } catch (SQLException ex) {
                        showError("Database error", ex.getMessage());
                        event.consume();
                    }

                } else {
                    try {
                        boolean success = visitDAO.updateVisitNotes(
                                existing.getVisitID(),
                                notesArea.getText().trim());

                        if (!success) {
                            showError("Update failed",
                                    "The consultation notes could not be updated.");
                            event.consume();
                        }

                    } catch (SQLException ex) {
                        showError("Database error", ex.getMessage());
                        event.consume();
                    }
                }
            });

            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    loadVisits();

                    showInformation(existing == null
                            ? "Patient visit registered successfully."
                            : "Consultation notes updated successfully.");
                }
            });

        } catch (SQLException e) {
            showError("Unable to load visit details", e.getMessage());
        }
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

    private void editSelectedVisit() {

        VisitRecord selected =
                table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("No visit selected",
                    "Select a visit from the table first.");
            return;
        }

        showVisitDialog(selected);
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