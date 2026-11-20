
package com.MediConnect;

import com.MediConnect.database.Staff;
import com.MediConnect.database.StaffDAO;

import java.sql.SQLException;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StaffView extends VBox {

    private final StaffDAO staffDAO = new StaffDAO();
    private final int administratorID;

    private TableView<Staff> staffTable;

    public StaffView(int administratorID) {

        this.administratorID = administratorID;

        setSpacing(15);
        setPadding(new Insets(10));

        createStaffTable();
        createButtons();

        loadStaff();
    }

    // ================= STAFF TABLE =================

    private void createStaffTable() {

        staffTable = new TableView<>();

        TableColumn<Staff, String> idColumn =
                new TableColumn<>("Staff ID");

        idColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        String.valueOf(data.getValue().getStaffID())
                )
        );

        TableColumn<Staff, String> nameColumn =
                new TableColumn<>("Full Name");

        nameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getFullName()
                )
        );

        TableColumn<Staff, String> usernameColumn =
                new TableColumn<>("Username");

        usernameColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getUsername()
                )
        );

        TableColumn<Staff, String> roleColumn =
                new TableColumn<>("Role");

        roleColumn.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(
                        data.getValue().getRole()
                )
        );

        TableColumn<Staff, String> statusColumn =
                new TableColumn<>("Account Status");

        statusColumn.setCellValueFactory(data -> {

            String status = data.getValue().getStatus();

            return new ReadOnlyStringWrapper(
                    status == null ? "Inactive" : status
            );
        });

        staffTable.getColumns().addAll(
                idColumn,
                nameColumn,
                usernameColumn,
                roleColumn,
                statusColumn
        );

        staffTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        staffTable.setPrefHeight(450);

        getChildren().add(staffTable);
    }

    // ================= ACTION BUTTONS =================

    private void createButtons() {

        Button registerButton = new Button("Register Staff");
        Button updateButton = new Button("Update Details");
        Button statusButton = new Button("Activate / Deactivate");
        Button refreshButton = new Button("Refresh");

        registerButton.setOnAction(event -> registerStaff());

        updateButton.setOnAction(event -> updateStaff());

        statusButton.setOnAction(event -> changeStaffStatus());

        refreshButton.setOnAction(event -> loadStaff());

        HBox buttons = new HBox(10);

        buttons.getChildren().addAll(
                registerButton,
                updateButton,
                statusButton,
                refreshButton
        );

        getChildren().add(buttons);
    }

    // ================= LOAD STAFF =================

    private void loadStaff() {

        try {

            List<Staff> staffMembers =
                    staffDAO.getAllStaff(administratorID);

            staffTable.setItems(
                    FXCollections.observableArrayList(staffMembers)
            );

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Database Error",
                    "Unable to load staff accounts.",
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }

    // ================= REGISTER STAFF =================

    private void registerStaff() {

        TextField nameField = new TextField();
        TextField usernameField = new TextField();

        PasswordField passwordField = new PasswordField();

        nameField.setPromptText("Enter full name");
        usernameField.setPromptText("Choose username");
        passwordField.setPromptText("At least 8 characters");

        GridPane grid = new GridPane();

        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);

        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Register Staff");
        dialog.setHeaderText("Create a new staff account");

        dialog.getDialogPane().setContent(grid);

        ButtonType registerButton = new ButtonType(
                "Register",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                registerButton,
                ButtonType.CANCEL
        );

        dialog.showAndWait().ifPresent(response -> {

            if (response != registerButton) {
                return;
            }

            String fullName = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (fullName.isEmpty()
                    || username.isEmpty()
                    || password.isEmpty()) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "Missing Information",
                        "Complete all registration fields.",
                        "Full name, username and password are required."
                );

                return;
            }

            try {

                boolean registered =
                        staffDAO.registerStaffByAdmin(
                                administratorID,
                                fullName,
                                username,
                                password
                        );

                if (registered) {

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Registration Successful",
                            "Staff account created successfully.",
                            "The new account is active."
                    );

                    loadStaff();

                } else {

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Username Unavailable",
                            "The account could not be created.",
                            "This username may already be in use."
                    );
                }

            } catch (Exception e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Registration Error",
                        "Unable to register staff account.",
                        e.getMessage()
                );

                e.printStackTrace();
            }
        });
    }

    // ================= UPDATE STAFF DETAILS =================

    private void updateStaff() {

        Staff selectedStaff =
                staffTable.getSelectionModel().getSelectedItem();

        if (selectedStaff == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Staff Selected",
                    "Select a staff member first.",
                    "Choose an account from the table."
            );

            return;
        }

        TextField nameField =
                new TextField(selectedStaff.getFullName());

        TextField usernameField =
                new TextField(selectedStaff.getUsername());

        GridPane grid = new GridPane();

        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Username:"), 0, 1);
        grid.add(usernameField, 1, 1);

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle("Update Staff Details");
        dialog.setHeaderText(
                "Update account: " + selectedStaff.getUsername()
        );

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType(
                "Save Changes",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton,
                ButtonType.CANCEL
        );

        dialog.showAndWait().ifPresent(response -> {

            if (response != saveButton) {
                return;
            }

            try {

                boolean updated =
                        staffDAO.updateStaffDetails(
                                administratorID,
                                selectedStaff.getStaffID(),
                                nameField.getText().trim(),
                                usernameField.getText().trim()
                        );

                if (updated) {

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Update Successful",
                            "Staff details updated successfully.",
                            null
                    );

                    loadStaff();

                } else {

                    showAlert(
                            Alert.AlertType.WARNING,
                            "Update Failed",
                            "The details could not be updated.",
                            "The username may already exist."
                    );
                }

            } catch (Exception e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Update Error",
                        "Unable to update staff details.",
                        e.getMessage()
                );

                e.printStackTrace();
            }
        });
    }

    // ================= ACTIVATE / DEACTIVATE =================

    private void changeStaffStatus() {

        Staff selectedStaff =
                staffTable.getSelectionModel().getSelectedItem();

        if (selectedStaff == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Staff Selected",
                    "Select a staff member first.",
                    "Choose an account from the table."
            );

            return;
        }

        if (selectedStaff.getStaffID() == administratorID
                && "Active".equalsIgnoreCase(selectedStaff.getStatus())) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Account Protection",
                    "You cannot deactivate your own account.",
                    "Log in using another administrator account if you "
                    + "need to change this account's status."
            );

            return;
        }

        boolean currentlyActive =
                "Active".equalsIgnoreCase(selectedStaff.getStatus());

        boolean newStatus = !currentlyActive;

        String action = newStatus ? "activate" : "deactivate";

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Confirm Account Status");
        confirmation.setHeaderText(
                "Are you sure you want to " + action + " this account?"
        );

        confirmation.setContentText(
                "Staff member: " + selectedStaff.getFullName()
                + "\nUsername: " + selectedStaff.getUsername()
        );

        confirmation.showAndWait().ifPresent(response -> {

            if (response != ButtonType.OK) {
                return;
            }

            try {

                staffDAO.setStaffStatus(
                        administratorID,
                        selectedStaff.getStaffID(),
                        newStatus
                );

                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Account Updated",
                        "Staff account " + action + "d successfully.",
                        null
                );

                loadStaff();

            } catch (Exception e) {

                showAlert(
                        Alert.AlertType.ERROR,
                        "Status Update Failed",
                        "Unable to change account status.",
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
}
