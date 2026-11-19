
package com.MediConnect;

import com.MediConnect.database.DoctorDAO;
import com.MediConnect.database.DoctorRecord;

import java.sql.SQLException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DoctorsView extends VBox {

    private final DoctorDAO doctorDAO = new DoctorDAO();

    private final TableView<DoctorRecord> table = new TableView<>();

    private final ObservableList<DoctorRecord> doctorList =
            FXCollections.observableArrayList();

    private final TextField searchField = new TextField();

    public DoctorsView() {

        setSpacing(12);
        setPadding(new Insets(15));

        Label heading = new Label("Doctors Management");
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        searchField.setPromptText(
                "Search by name, specialization, or ID..."
        );

        Button searchButton = new Button("Search");
        Button refreshButton = new Button("Refresh");
        Button addButton = new Button("Add Doctor");
        Button editButton = new Button("Edit Doctor");
        Button archiveButton = new Button("Archive Doctor");

        HBox searchBar = new HBox(
                10, searchField, searchButton, refreshButton
        );

        HBox actionBar = new HBox(
                10, addButton, editButton, archiveButton
        );

        createTable();

        searchButton.setOnAction(e -> searchDoctors());
        refreshButton.setOnAction(e -> loadDoctors());
        addButton.setOnAction(e -> showAddDoctorDialog());
        editButton.setOnAction(e -> showEditDoctorDialog());
        archiveButton.setOnAction(e -> archiveSelectedDoctor());

        searchField.setOnAction(e -> searchDoctors());

        getChildren().addAll(
                heading,
                searchBar,
                actionBar,
                table
        );

        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        loadDoctors();
    }

    // CREATE TABLE
    private void createTable() {

        TableColumn<DoctorRecord, Number> idColumn =
                new TableColumn<>("Doctor ID");

        idColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getDoctorID()
                )
        );

        TableColumn<DoctorRecord, String> nameColumn =
                new TableColumn<>("Full Name");

        nameColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getFullName()
                )
        );

        TableColumn<DoctorRecord, String> specializationColumn =
                new TableColumn<>("Specialization");

        specializationColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getSpecialization()
                )
        );

        TableColumn<DoctorRecord, String> phoneColumn =
                new TableColumn<>("Phone");

        phoneColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPhone()
                )
        );

        TableColumn<DoctorRecord, String> emailColumn =
                new TableColumn<>("Email");

        emailColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getEmail()
                )
        );

        TableColumn<DoctorRecord, Number> feeColumn =
                new TableColumn<>("Consultation Fee");

        feeColumn.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty(
                        data.getValue().getConsultationFee()
                )
        );

        table.getColumns().addAll(
                idColumn,
                nameColumn,
                specializationColumn,
                phoneColumn,
                emailColumn,
                feeColumn
        );

        table.setItems(doctorList);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }

    // LOAD DOCTORS
    private void loadDoctors() {

        try {
            doctorList.setAll(doctorDAO.getAllDoctors());

        } catch (SQLException e) {
            showError("Failed to load doctors", e.getMessage());
        }
    }

    // SEARCH DOCTORS
    private void searchDoctors() {

        String keyword = searchField.getText().trim();

        try {

            if (keyword.isEmpty()) {
                loadDoctors();
            } else {
                doctorList.setAll(
                        doctorDAO.searchDoctors(keyword)
                );
            }

        } catch (SQLException e) {
            showError("Search failed", e.getMessage());
        }
    }

    // ADD DOCTOR
    private void showAddDoctorDialog() {

        Dialog<DoctorRecord> dialog = new Dialog<>();

        dialog.setTitle("Register Doctor");
        dialog.setHeaderText("Enter doctor details");

        ButtonType saveButton =
                new ButtonType("Save Doctor", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        TextField nameField = new TextField();
        TextField specializationField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField feeField = new TextField();

        nameField.setPromptText("Full name");
        specializationField.setPromptText("Specialization");
        phoneField.setPromptText("Phone number");
        emailField.setPromptText("Email address");
        feeField.setPromptText("Consultation fee");

        VBox form = new VBox(
                10,
                new Label("Full Name"), nameField,
                new Label("Specialization"), specializationField,
                new Label("Phone"), phoneField,
                new Label("Email"), emailField,
                new Label("Consultation Fee"), feeField
        );

        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(button -> {

            if (button == saveButton) {

                String fullName = nameField.getText().trim();
                String specialization =
                        specializationField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String feeText = feeField.getText().trim();

                if (fullName.isEmpty()) {
                    showMessage("Full name is required.");
                    return null;
                }

                if (specialization.isEmpty()) {
                    showMessage("Specialization is required.");
                    return null;
                }

                double fee;

                try {
                    fee = Double.parseDouble(feeText);

                    if (!Double.isFinite(fee) || fee < 0) {
                        showMessage(
                                "Consultation fee must be zero or greater."
                        );
                        return null;
                    }

                } catch (NumberFormatException e) {
                    showMessage("Enter a valid consultation fee.");
                    return null;
                }

                try {

                    boolean success = doctorDAO.registerDoctor(
                            fullName,
                            specialization,
                            emptyToNull(phone),
                            emptyToNull(email),
                            fee
                    );

                    if (success) {
                        showMessage("Doctor registered successfully.");
                        loadDoctors();
                    }

                } catch (SQLException e) {
                    showError("Doctor registration failed", e.getMessage());
                }
            }

            return null;
        });

        dialog.showAndWait();
    }

    // EDIT DOCTOR
    private void showEditDoctorDialog() {

        DoctorRecord selected = table.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showMessage("Please select a doctor to edit.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle("Edit Doctor");
        dialog.setHeaderText("Update doctor details");

        ButtonType saveButton =
                new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        TextField nameField =
                new TextField(selected.getFullName());

        TextField specializationField =
                new TextField(selected.getSpecialization());

        TextField phoneField =
                new TextField(selected.getPhone());

        TextField emailField =
                new TextField(selected.getEmail());

        TextField feeField =
                new TextField(
                        String.valueOf(selected.getConsultationFee())
                );

        VBox form = new VBox(
                10,
                new Label("Full Name"), nameField,
                new Label("Specialization"), specializationField,
                new Label("Phone"), phoneField,
                new Label("Email"), emailField,
                new Label("Consultation Fee"), feeField
        );

        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(button -> {

            if (button == saveButton) {

                String fullName = nameField.getText().trim();
                String specialization =
                        specializationField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();

                if (fullName.isEmpty() || specialization.isEmpty()) {
                    showMessage(
                            "Full name and specialization are required."
                    );
                    return null;
                }

                double fee;

                try {
                    fee = Double.parseDouble(feeField.getText().trim());

                    if (!Double.isFinite(fee) || fee < 0) {
                        showMessage(
                                "Consultation fee must be zero or greater."
                        );
                        return null;
                    }

                } catch (NumberFormatException e) {
                    showMessage("Enter a valid consultation fee.");
                    return null;
                }

                try {

                    boolean success = doctorDAO.updateDoctor(
                            selected.getDoctorID(),
                            fullName,
                            specialization,
                            emptyToNull(phone),
                            emptyToNull(email),
                            fee
                    );

                    if (success) {
                        showMessage("Doctor details updated successfully.");
                        loadDoctors();
                    } else {
                        showMessage("Doctor could not be updated.");
                    }

                } catch (SQLException e) {
                    showError("Update failed", e.getMessage());
                }
            }

            return null;
        });

        dialog.showAndWait();
    }

    // ARCHIVE DOCTOR
    private void archiveSelectedDoctor() {

        DoctorRecord selected = table.getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            showMessage("Please select a doctor to archive.");
            return;
        }

        Alert confirmation = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        confirmation.setTitle("Archive Doctor");
        confirmation.setHeaderText("Archive " + selected.getFullName() + "?");

        confirmation.setContentText(
                "The doctor will no longer appear in the active doctors list. "
                + "Historical records will be preserved."
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                try {

                    boolean success = doctorDAO.archiveDoctor(
                            selected.getDoctorID()
                    );

                    if (success) {
                        showMessage("Doctor archived successfully.");
                        loadDoctors();
                    }

                } catch (SQLException e) {
                    showError("Archiving failed", e.getMessage());
                }
            }
        });
    }

    // HELPERS
    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty()
                ? null : value.trim();
    }

    private void showMessage(String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("MediConnect");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
