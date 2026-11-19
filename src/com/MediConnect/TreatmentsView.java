
package com.MediConnect;

import com.MediConnect.database.TreatmentDAO;
import com.MediConnect.database.TreatmentRecord;
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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TreatmentsView extends VBox {

    private final TreatmentDAO treatmentDAO = new TreatmentDAO();
    private final VisitDAO visitDAO = new VisitDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    private final TableView<TreatmentRecord> treatmentTable =
            new TableView<>();

    private final ObservableList<TreatmentRecord> treatmentList =
            FXCollections.observableArrayList();

    private final TextField searchField = new TextField();

    private final Map<Integer, String> patientNames = new HashMap<>();
    private final Map<Integer, String> doctorNames = new HashMap<>();

    public TreatmentsView() {

        setSpacing(15);
        setPadding(new Insets(20));

        createTable();
        loadNames();

        Label title = new Label("Treatments Management");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitle = new Label(
                "Register, search and manage patient treatments."
        );

        searchField.setPromptText(
                "Search by treatment ID, visit ID or description..."
        );
        searchField.setPrefWidth(350);

        Button searchButton = new Button("Search");
        Button refreshButton = new Button("Refresh");
        Button addButton = new Button("Register Treatment");
        Button editButton = new Button("Edit Treatment");

        searchButton.setOnAction(e -> searchTreatments());
        refreshButton.setOnAction(e -> loadTreatments());
        addButton.setOnAction(e -> showRegisterDialog());
        editButton.setOnAction(e -> showEditDialog());

        HBox searchBar = new HBox(
                10, searchField, searchButton, refreshButton
        );

        HBox actionBar = new HBox(10, addButton, editButton);

        VBox heading = new VBox(5, title, subtitle);

        getChildren().addAll(
                heading,
                searchBar,
                actionBar,
                treatmentTable
        );

        VBox.setVgrow(treatmentTable, Priority.ALWAYS);

        loadTreatments();
    }

    // Create the treatments table
    private void createTable() {

        TableColumn<TreatmentRecord, String> idColumn =
                new TableColumn<>("Treatment ID");

        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(data.getValue().getTreatmentID())
                ));

        TableColumn<TreatmentRecord, String> visitColumn =
                new TableColumn<>("Visit ID");

        visitColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(data.getValue().getVisitID())
                ));

        TableColumn<TreatmentRecord, String> descriptionColumn =
                new TableColumn<>("Treatment Description");

        descriptionColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getTreatmentDescription()
                ));

        TableColumn<TreatmentRecord, String> costColumn =
                new TableColumn<>("Treatment Cost");

        costColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getTreatmentCost() == null
                                ? "0.00"
                                : data.getValue().getTreatmentCost()
                                        .toPlainString()
                ));

        idColumn.setPrefWidth(100);
        visitColumn.setPrefWidth(100);
        descriptionColumn.setPrefWidth(350);
        costColumn.setPrefWidth(120);

        treatmentTable.getColumns().addAll(
                idColumn,
                visitColumn,
                descriptionColumn,
                costColumn
        );

        treatmentTable.setItems(treatmentList);

        treatmentTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        treatmentTable.setPlaceholder(
                new Label("No treatments found.")
        );
    }

    // Load patient and doctor names
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

        } catch (SQLException e) {

            e.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Unable to load patient and doctor details."
            );
        }
    }

    // Retrieve all treatments
    private void loadTreatments() {

        searchField.clear();

        treatmentList.setAll(
                treatmentDAO.getAllTreatments()
        );
    }

    // Search treatments
    private void searchTreatments() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadTreatments();
            return;
        }

        treatmentList.setAll(
                treatmentDAO.searchTreatments(keyword)
        );
    }

    // Register a new treatment
    private void showRegisterDialog() {

        List<VisitRecord> visits;

        try {
            visits = visitDAO.getAllVisits();

        } catch (SQLException e) {

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
                    "Register a patient visit before adding a treatment."
            );

            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Register Treatment");
        dialog.setHeaderText("Enter the treatment details");

        ButtonType saveButton = new ButtonType(
                "Save Treatment",
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
                "Enter the treatment description..."
        );

        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);

        TextField costField = new TextField();

        costField.setPromptText("Enter treatment cost");

        VBox content = new VBox(
                10,
                new Label("Select Visit:"),
                visitComboBox,
                new Label("Treatment Description:"),
                descriptionArea,
                new Label("Treatment Cost:"),
                costField
        );

        content.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(content);

        Button save = (Button) dialog.getDialogPane()
                .lookupButton(saveButton);

        save.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    String description =
                            descriptionArea.getText().trim();

                    String costText = costField.getText().trim();

                    if (visitComboBox.getValue() == null
                            || description.isEmpty()
                            || costText.isEmpty()) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Missing Information",
                                "Please complete all treatment fields."
                        );

                        event.consume();
                        return;
                    }

                    try {

                        BigDecimal cost = new BigDecimal(costText);

                        if (cost.compareTo(BigDecimal.ZERO) < 0) {
                            throw new NumberFormatException();
                        }

                    } catch (NumberFormatException ex) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Invalid Cost",
                                "Enter a valid non-negative treatment cost."
                        );

                        event.consume();
                    }
                }
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButton) {

            int visitID = visitComboBox.getValue().getVisitID();

            String description = descriptionArea.getText().trim();

            BigDecimal cost = new BigDecimal(
                    costField.getText().trim()
            );

            int treatmentID = treatmentDAO.registerTreatment(
                    visitID,
                    description,
                    cost
            );

            if (treatmentID > 0) {

                loadTreatments();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Treatment registered successfully.\n"
                                + "Treatment ID: " + treatmentID
                );

            } else {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Registration Failed",
                        "The treatment could not be registered."
                );
            }
        }
    }

    // Edit an existing treatment
    private void showEditDialog() {

        TreatmentRecord selectedTreatment =
                treatmentTable.getSelectionModel().getSelectedItem();

        if (selectedTreatment == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Treatment Selected",
                    "Please select a treatment to edit."
            );

            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Edit Treatment");

        dialog.setHeaderText(
                "Editing Treatment #"
                        + selectedTreatment.getTreatmentID()
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
                selectedTreatment.getTreatmentDescription()
        );

        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);

        TextField costField = new TextField(
                selectedTreatment.getTreatmentCost() == null
                        ? "0.00"
                        : selectedTreatment.getTreatmentCost().toPlainString()
        );

        VBox content = new VBox(
                10,
                new Label("Treatment Description:"),
                descriptionArea,
                new Label("Treatment Cost:"),
                costField
        );

        content.setPadding(new Insets(15));

        dialog.getDialogPane().setContent(content);

        Button save = (Button) dialog.getDialogPane()
                .lookupButton(saveButton);

        save.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    if (descriptionArea.getText().trim().isEmpty()
                            || costField.getText().trim().isEmpty()) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Missing Information",
                                "Please complete all treatment fields."
                        );

                        event.consume();
                        return;
                    }

                    try {

                        BigDecimal cost = new BigDecimal(
                                costField.getText().trim()
                        );

                        if (cost.compareTo(BigDecimal.ZERO) < 0) {
                            throw new NumberFormatException();
                        }

                    } catch (NumberFormatException ex) {

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Invalid Cost",
                                "Enter a valid non-negative treatment cost."
                        );

                        event.consume();
                    }
                }
        );

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButton) {

            boolean updated = treatmentDAO.updateTreatment(
                    selectedTreatment.getTreatmentID(),
                    descriptionArea.getText().trim(),
                    new BigDecimal(costField.getText().trim())
            );

            if (updated) {

                loadTreatments();

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Success",
                        "Treatment updated successfully."
                );

            } else {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Update Failed",
                        "The treatment could not be updated."
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
