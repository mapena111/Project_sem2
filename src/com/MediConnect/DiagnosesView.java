
package com.MediConnect;

import com.MediConnect.database.DiagnosisDAO;
import com.MediConnect.database.DiagnosisRecord;
import com.MediConnect.database.VisitDAO;
import com.MediConnect.database.VisitRecord;
import com.MediConnect.database.PatientDAO;
import com.MediConnect.database.PatientRecord;
import com.MediConnect.database.DoctorDAO;
import com.MediConnect.database.DoctorRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DiagnosesView extends VBox {

    private final DiagnosisDAO diagnosisDAO = new DiagnosisDAO();
    private final VisitDAO visitDAO = new VisitDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private final TableView<DiagnosisRecord> diagnosisTable = new TableView<>();

    private final ObservableList<DiagnosisRecord> diagnosisList =
            FXCollections.observableArrayList();

    private final TextField searchField = new TextField();

    private final Map<Integer, String> patientNames = new HashMap<>();
    private final Map<Integer, String> doctorNames = new HashMap<>();

    public DiagnosesView() {

        setSpacing(15);
        setPadding(new Insets(20));

        createTable();
        loadNames();

        Label title = new Label("Diagnoses Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label(
                "Register, search and manage patient diagnoses."
        );

        searchField.setPromptText("Search by diagnosis ID, visit ID or description...");
        searchField.setPrefWidth(350);

        Button searchButton = new Button("Search");
        Button refreshButton = new Button("Refresh");
        Button addButton = new Button("Register Diagnosis");
        Button editButton = new Button("Edit Diagnosis");

        searchButton.setOnAction(e -> searchDiagnoses());
        refreshButton.setOnAction(e -> loadDiagnoses());
        addButton.setOnAction(e -> showRegisterDialog());
        editButton.setOnAction(e -> showEditDialog());

        HBox searchBar = new HBox(10,
                searchField, searchButton, refreshButton);

        HBox actionBar = new HBox(10, addButton, editButton);

        VBox heading = new VBox(5, title, subtitle);

        getChildren().addAll(
                heading,
                searchBar,
                actionBar,
                diagnosisTable
        );

        VBox.setVgrow(diagnosisTable, Priority.ALWAYS);

        loadDiagnoses();
    }

    // Create the diagnoses table
    private void createTable() {

        TableColumn<DiagnosisRecord, String> idColumn =
                new TableColumn<>("Diagnosis ID");

        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(data.getValue().getDiagnosisID())
                ));

        TableColumn<DiagnosisRecord, String> visitColumn =
                new TableColumn<>("Visit ID");

        visitColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(data.getValue().getVisitID())
                ));

        TableColumn<DiagnosisRecord, String> descriptionColumn =
                new TableColumn<>("Diagnosis Description");

        descriptionColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getDiagnosisDescription()
                ));

        idColumn.setPrefWidth(120);
        visitColumn.setPrefWidth(120);
        descriptionColumn.setPrefWidth(450);

        diagnosisTable.getColumns().addAll(
                idColumn,
                visitColumn,
                descriptionColumn
        );

        diagnosisTable.setItems(diagnosisList);
        diagnosisTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        diagnosisTable.setPlaceholder(
                new Label("No diagnoses found.")
        );
    }

    // Load patient and doctor names for displaying visits

private void loadNames() {

    patientNames.clear();
    doctorNames.clear();

    try {

        List<PatientRecord> patients = patientDAO.getAllPatients();

        for (PatientRecord patient : patients) {
            patientNames.put(
                    patient.getPatientID(),
                    patient.getFullName()
            );
        }

        List<DoctorRecord> doctors = doctorDAO.getAllDoctors();

        for (DoctorRecord doctor : doctors) {
            doctorNames.put(
                    doctor.getDoctorID(),
                    doctor.getFullName()
            );
        }

    } catch (java.sql.SQLException e) {

        e.printStackTrace();

        showAlert(
                Alert.AlertType.ERROR,
                "Database Error",
                "Unable to load patient and doctor details."
        );
    }
}


    // Retrieve all diagnoses
    private void loadDiagnoses() {

        searchField.clear();

        diagnosisList.setAll(
                diagnosisDAO.getAllDiagnoses()
        );
    }

    // Search diagnoses
    private void searchDiagnoses() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadDiagnoses();
            return;
        }

        diagnosisList.setAll(
                diagnosisDAO.searchDiagnoses(keyword)
        );
    }

    // Register a new diagnosis
    private void showRegisterDialog() {

    	List<VisitRecord> visits;

    	try {
    	    visits = visitDAO.getAllVisits();

    	} catch (java.sql.SQLException e) {

    	    e.printStackTrace();

    	    showAlert(
    	            Alert.AlertType.ERROR,
    	            "Database Error",
    	            "Unable to retrieve patient visits."
    	    );

    	    return;
    	}

        if (visits.isEmpty()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "No Visits Available",
                    "Register a patient visit before adding a diagnosis."
            );
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Register Diagnosis");
        dialog.setHeaderText("Enter the diagnosis details");

        ButtonType saveButton = new ButtonType(
                "Save Diagnosis",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        ComboBox<VisitRecord> visitComboBox =
                new ComboBox<>(
                        FXCollections.observableArrayList(visits)
                );

        visitComboBox.setPromptText("Select patient visit");
        visitComboBox.setMaxWidth(Double.MAX_VALUE);

        visitComboBox.setConverter(new StringConverter<>() {

            @Override
            public String toString(VisitRecord visit) {

                if (visit == null) {
                    return "";
                }

                String patientName = patientNames.getOrDefault(
                        visit.getPatientID(),
                        "Patient #" + visit.getPatientID()
                );

                String doctorName = doctorNames.getOrDefault(
                        visit.getDoctorID(),
                        "Doctor #" + visit.getDoctorID()
                );

                return "Visit #" + visit.getVisitID()
                        + " - " + patientName
                        + " - Dr. " + doctorName;
            }

            @Override
            public VisitRecord fromString(String value) {
                return null;
            }
        });

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText(
                "Enter the patient's diagnosis..."
        );
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(5);

        VBox content = new VBox(
                10,
                new Label("Select Visit:"),
                visitComboBox,
                new Label("Diagnosis Description:"),
                descriptionArea
        );

        content.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(content);

        Button save = (Button) dialog.getDialogPane()
                .lookupButton(saveButton);

        save.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    if (visitComboBox.getValue() == null
                            || descriptionArea.getText().trim().isEmpty()) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Missing Information",
                                "Please select a visit and enter a diagnosis."
                        );

                        event.consume();
                    }
                }
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButton) {

            int visitID = visitComboBox.getValue().getVisitID();

            int diagnosisID = diagnosisDAO.registerDiagnosis(
                    visitID,
                    descriptionArea.getText().trim()
            );

            if (diagnosisID > 0) {

                loadDiagnoses();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Diagnosis registered successfully.\nDiagnosis ID: "
                                + diagnosisID
                );

            } else {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Registration Failed",
                        "The diagnosis could not be registered."
                );
            }
        }
    }

    // Edit an existing diagnosis
    private void showEditDialog() {

        DiagnosisRecord selectedDiagnosis =
                diagnosisTable.getSelectionModel().getSelectedItem();

        if (selectedDiagnosis == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Diagnosis Selected",
                    "Please select a diagnosis to edit."
            );

            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Edit Diagnosis");
        dialog.setHeaderText(
                "Editing Diagnosis #" + selectedDiagnosis.getDiagnosisID()
        );

        ButtonType saveButton = new ButtonType(
                "Save Changes",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        TextArea descriptionArea = new TextArea(
                selectedDiagnosis.getDiagnosisDescription()
        );

        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(5);

        VBox content = new VBox(
                10,
                new Label("Diagnosis Description:"),
                descriptionArea
        );

        content.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(content);

        Button save = (Button) dialog.getDialogPane()
                .lookupButton(saveButton);

        save.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    if (descriptionArea.getText().trim().isEmpty()) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Missing Information",
                                "Diagnosis description cannot be empty."
                        );

                        event.consume();
                    }
                }
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButton) {

            boolean updated = diagnosisDAO.updateDiagnosis(
                    selectedDiagnosis.getDiagnosisID(),
                    descriptionArea.getText().trim()
            );

            if (updated) {

                loadDiagnoses();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Diagnosis updated successfully."
                );

            } else {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Update Failed",
                        "The diagnosis could not be updated."
                );
            }
        }
    }

    // Display JavaFX alerts
    private void showAlert(
            Alert.AlertType type,
            String title,
            String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
