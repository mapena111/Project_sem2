
package com.MediConnect;

import com.MediConnect.database.PatientDAO;
import com.MediConnect.database.PatientRecord;

import java.sql.SQLException;
import java.time.LocalDate;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PatientsView extends BorderPane {

    private final PatientDAO patientDAO = new PatientDAO();

    private final TableView<PatientRecord> patientTable =
            new TableView<>();

    private final ObservableList<PatientRecord> patientList =
            FXCollections.observableArrayList();

    private final TextField searchField = new TextField();

    public PatientsView() {

        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f3f7fb;");

        Label heading = new Label("Patient Management");
        heading.setStyle(
                "-fx-font-size: 24px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #173b57;"
        );

        Label description = new Label(
                "Register, view, search and manage patient records."
        );

        searchField.setPromptText(
                "Search by patient name, ID or phone number..."
        );

        searchField.setPrefWidth(350);
        searchField.setMaxWidth(Double.MAX_VALUE);

        Button searchButton = new Button("SEARCH");
        searchButton.setOnAction(event -> searchPatients());

        Button refreshButton = new Button("REFRESH");
        refreshButton.setOnAction(event -> {
            searchField.clear();
            loadPatients();
        });

        Button addButton = new Button("ADD PATIENT");
        addButton.setStyle(
                "-fx-background-color: #1677b8;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
        );
        addButton.setOnAction(event -> showAddPatientDialog());

        Button editButton = new Button("EDIT PATIENT");
        editButton.setOnAction(event -> editSelectedPatient());

        Button deleteButton = new Button("DELETE PATIENT");
        deleteButton.setStyle(
                "-fx-background-color: #d9534f;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
        );
        deleteButton.setOnAction(event -> archiveSelectedPatient());

        // FIRST ROW: SEARCH AND REFRESH
        HBox searchArea = new HBox(
                10, searchField, searchButton, refreshButton
        );

        searchArea.setAlignment(Pos.CENTER_LEFT);
        searchArea.setMaxWidth(Double.MAX_VALUE);

        // SECOND ROW: PATIENT ACTION BUTTONS
        HBox actionArea = new HBox(
                12, addButton, editButton, deleteButton
        );

        actionArea.setAlignment(Pos.CENTER_LEFT);
        actionArea.setPadding(new Insets(5, 0, 10, 0));

        // COMPLETE TOOLBAR
        VBox toolbar = new VBox(8);
        toolbar.getChildren().addAll(searchArea, actionArea);

        // TABLE COLUMNS

        TableColumn<PatientRecord, String> idColumn =
                new TableColumn<>("Patient ID");

        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(data.getValue().getPatientID())
                )
        );

        TableColumn<PatientRecord, String> nameColumn =
                new TableColumn<>("Full Name");

        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getFullName()
                )
        );

        TableColumn<PatientRecord, String> genderColumn =
                new TableColumn<>("Gender");

        genderColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getGender() == null
                                ? ""
                                : data.getValue().getGender()
                )
        );

        TableColumn<PatientRecord, String> dobColumn =
                new TableColumn<>("Date of Birth");

        dobColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getDateOfBirth() == null
                                ? ""
                                : data.getValue().getDateOfBirth().toString()
                )
        );

        TableColumn<PatientRecord, String> phoneColumn =
                new TableColumn<>("Phone");

        phoneColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getPhone() == null
                                ? ""
                                : data.getValue().getPhone()
                )
        );

        TableColumn<PatientRecord, String> emailColumn =
                new TableColumn<>("Email");

        emailColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getEmail() == null
                                ? ""
                                : data.getValue().getEmail()
                )
        );

        TableColumn<PatientRecord, String> addressColumn =
                new TableColumn<>("Address");

        addressColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getAddress() == null
                                ? ""
                                : data.getValue().getAddress()
                )
        );

        patientTable.getColumns().addAll(
                idColumn, nameColumn, genderColumn,
                dobColumn, phoneColumn, emailColumn, addressColumn
        );

        patientTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        patientTable.setItems(patientList);

        // TOP AREA
        VBox topArea = new VBox(10);
        topArea.getChildren().addAll(
                heading, description, toolbar
        );

        setTop(topArea);
        setCenter(patientTable);

        loadPatients();
    }

    // REGISTER A PATIENT
    private void showAddPatientDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Register Patient");
        dialog.setHeaderText("Enter the patient's details");

        ButtonType registerButton = new ButtonType(
                "Register Patient", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                registerButton, ButtonType.CANCEL
        );

        TextField nameField = new TextField();
        nameField.setPromptText("Enter full name");

        ComboBox<String> genderField = new ComboBox<>();
        genderField.getItems().addAll("Male", "Female", "Other");
        genderField.setPromptText("Select gender");

        DatePicker dobField = createDatePicker();

        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter email address");

        TextField addressField = new TextField();
        addressField.setPromptText("Enter residential address");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Gender:"), 0, 1);
        grid.add(genderField, 1, 1);
        grid.add(new Label("Date of Birth:"), 0, 2);
        grid.add(dobField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Address:"), 0, 5);
        grid.add(addressField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {

            if (response == registerButton) {

                String fullName = nameField.getText().trim();
                String gender = genderField.getValue();
                LocalDate dob = dobField.getValue();

                if (fullName.isEmpty() || gender == null || dob == null) {
                    showMessage(Alert.AlertType.WARNING,
                            "Incomplete Information",
                            "Full name, gender and date of birth are required.");
                    return;
                }

                try {

                    int patientID = patientDAO.registerPatient(
                            fullName, gender,
                            java.sql.Date.valueOf(dob),
                            emptyToNull(phoneField.getText()),
                            emptyToNull(emailField.getText()),
                            emptyToNull(addressField.getText())
                    );

                    showMessage(Alert.AlertType.INFORMATION,
                            "Registration Successful",
                            "Patient registered successfully!\nPatient ID: "
                            + patientID);

                    searchField.clear();
                    loadPatients();

                } catch (SQLException e) {
                    showError("Patient registration failed.", e);
                }
            }
        });
    }

    // EDIT SELECTED PATIENT
    private void editSelectedPatient() {

        PatientRecord selected =
                patientTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage(Alert.AlertType.WARNING,
                    "No Patient Selected",
                    "Please select a patient to edit.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Edit Patient");
        dialog.setHeaderText(
                "Update details for " + selected.getFullName()
        );

        ButtonType saveButton = new ButtonType(
                "Save Changes", ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton, ButtonType.CANCEL
        );

        TextField nameField = new TextField(selected.getFullName());

        ComboBox<String> genderField = new ComboBox<>();
        genderField.getItems().addAll("Male", "Female", "Other");
        genderField.setValue(selected.getGender());

        DatePicker dobField = createDatePicker();
        dobField.setValue(
                selected.getDateOfBirth() == null
                        ? null
                        : selected.getDateOfBirth().toLocalDate()
        );

        TextField phoneField = new TextField(selected.getPhone());
        TextField emailField = new TextField(selected.getEmail());
        TextField addressField = new TextField(selected.getAddress());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Gender:"), 0, 1);
        grid.add(genderField, 1, 1);
        grid.add(new Label("Date of Birth:"), 0, 2);
        grid.add(dobField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Address:"), 0, 5);
        grid.add(addressField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {

            if (response == saveButton) {

                String fullName = nameField.getText().trim();
                String gender = genderField.getValue();
                LocalDate dob = dobField.getValue();

                if (fullName.isEmpty() || gender == null || dob == null) {
                    showMessage(Alert.AlertType.WARNING,
                            "Incomplete Information",
                            "Full name, gender and date of birth are required.");
                    return;
                }

                try {

                    boolean updated = patientDAO.updatePatient(
                            selected.getPatientID(),
                            fullName,
                            gender,
                            java.sql.Date.valueOf(dob),
                            emptyToNull(phoneField.getText()),
                            emptyToNull(emailField.getText()),
                            emptyToNull(addressField.getText())
                    );

                    if (updated) {
                        showMessage(Alert.AlertType.INFORMATION,
                                "Update Successful",
                                "Patient details updated successfully.");
                    } else {
                        showMessage(Alert.AlertType.WARNING,
                                "Update Not Completed",
                                "The patient could not be updated.");
                    }

                    loadPatients();

                } catch (SQLException e) {
                    showError("Patient update failed.", e);
                }
            }
        });
    }

    // ARCHIVE SELECTED PATIENT
    private void archiveSelectedPatient() {

        PatientRecord selected =
                patientTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage(Alert.AlertType.WARNING,
                    "No Patient Selected",
                    "Please select a patient to archive.");
            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Archive Patient");
        confirmation.setHeaderText(
                "Archive " + selected.getFullName() + "?"
        );

        confirmation.setContentText(
                "This patient will disappear from the active patient list, "
                + "but their appointments and medical history will be preserved."
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                try {

                    boolean archived = patientDAO.archivePatient(
                            selected.getPatientID()
                    );

                    if (archived) {
                        showMessage(Alert.AlertType.INFORMATION,
                                "Patient Archived",
                                "The patient has been archived successfully.");
                    } else {
                        showMessage(Alert.AlertType.WARNING,
                                "Archive Not Completed",
                                "The patient could not be archived.");
                    }

                    searchField.clear();
                    loadPatients();

                } catch (SQLException e) {
                    showError("Patient archiving failed.", e);
                }
            }
        });
    }

    // DATE PICKER WITH FUTURE DATES DISABLED
    private DatePicker createDatePicker() {

        DatePicker picker = new DatePicker();
        picker.setPromptText("Select date of birth");

        picker.setDayCellFactory(datePicker -> new DateCell() {

            @Override
            public void updateItem(LocalDate date, boolean empty) {

                super.updateItem(date, empty);

                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #eeeeee;");
                }
            }
        });

        return picker;
    }

    // LOAD ACTIVE PATIENTS
    private void loadPatients() {

        try {
            patientList.setAll(patientDAO.getAllPatients());
        } catch (SQLException e) {
            showError("Unable to load patients.", e);
        }
    }

    // SEARCH ACTIVE PATIENTS
    private void searchPatients() {

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadPatients();
            return;
        }

        try {
            patientList.setAll(patientDAO.searchPatients(keyword));
        } catch (SQLException e) {
            showError("Patient search failed.", e);
        }
    }

    // CONVERT EMPTY INPUTS TO NULL
    private String emptyToNull(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    // DISPLAY MESSAGES
    private void showMessage(
            Alert.AlertType type, String title, String message) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // DISPLAY ERRORS
    private void showError(String message, Exception e) {

        showMessage(Alert.AlertType.ERROR,
                "Patient Management Error",
                message + "\n" + e.getMessage());

        e.printStackTrace();
    }
}
