
package com.MediConnect;

import com.MediConnect.database.BillDAO;
import com.MediConnect.database.BillItemDAO;
import com.MediConnect.database.BillItemRecord;
import com.MediConnect.database.BillRecord;
import com.MediConnect.database.PatientDAO;
import com.MediConnect.database.PatientRecord;
import com.MediConnect.database.VisitDAO;
import com.MediConnect.database.VisitRecord;
import com.MediConnect.database.PaymentDAO;
import com.MediConnect.database.PaymentRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingView extends VBox {

    private final BillDAO billDAO = new BillDAO();
    private final BillItemDAO billItemDAO = new BillItemDAO();
    private final VisitDAO visitDAO = new VisitDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    private final TableView<BillRecord> billTable = new TableView<>();

    private final ObservableList<BillRecord> billList =
            FXCollections.observableArrayList();

    private final Map<Integer, String> patientNames = new HashMap<>();

    public BillingView() {

        setSpacing(15);
        setPadding(new Insets(20));

        Label heading = new Label("Billing Management");
        heading.setStyle(
                "-fx-font-size: 22px; -fx-font-weight: bold;"
        );

        Label description = new Label(
                "Create bills, manage charges and record patient payments."
        );

        createBillTable();

        Button createBillButton = new Button("Create Bill");
        Button addItemButton = new Button("Add Bill Item");
        Button viewItemsButton = new Button("View Bill Items");
        Button recordPaymentButton = new Button("Record Payment");
        Button paymentHistoryButton = new Button("Payment History");
        Button refreshButton = new Button("Refresh Bills");

        // Keep the buttons visible by allowing them to wrap onto new rows.
        FlowPane buttonArea = new FlowPane();
        buttonArea.setHgap(10);
        buttonArea.setVgap(10);
        buttonArea.setPadding(new Insets(5, 0, 5, 0));

        buttonArea.getChildren().addAll(
                createBillButton,
                addItemButton,
                viewItemsButton,
                recordPaymentButton,
                paymentHistoryButton,
                refreshButton
        );

        createBillButton.setOnAction(e -> showCreateBillDialog());
        addItemButton.setOnAction(e -> showAddItemDialog());
        viewItemsButton.setOnAction(e -> showBillItems());
        recordPaymentButton.setOnAction(e -> showRecordPaymentDialog());
        paymentHistoryButton.setOnAction(e -> showPaymentHistory());
        refreshButton.setOnAction(e -> loadBills());

        getChildren().addAll(
                heading,
                description,
                buttonArea,
                billTable
        );

        VBox.setVgrow(billTable, Priority.ALWAYS);

        loadPatientNames();
        loadBills();

       
    }

    // CREATE THE BILL TABLE
    private void createBillTable() {

        TableColumn<BillRecord, Integer> billIDColumn =
                new TableColumn<>("Bill ID");
        billIDColumn.setCellValueFactory(
                new PropertyValueFactory<>("billID")
        );

        TableColumn<BillRecord, Integer> patientIDColumn =
                new TableColumn<>("Patient ID");
        patientIDColumn.setCellValueFactory(
                new PropertyValueFactory<>("patientID")
        );

        TableColumn<BillRecord, Integer> visitIDColumn =
                new TableColumn<>("Visit ID");
        visitIDColumn.setCellValueFactory(
                new PropertyValueFactory<>("visitID")
        );

        TableColumn<BillRecord, Object> dateColumn =
                new TableColumn<>("Bill Date");
        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("billDate")
        );

        TableColumn<BillRecord, BigDecimal> totalColumn =
                new TableColumn<>("Total Amount");
        totalColumn.setCellValueFactory(
                new PropertyValueFactory<>("totalAmount")
        );

        TableColumn<BillRecord, BigDecimal> paidColumn =
                new TableColumn<>("Amount Paid");
        paidColumn.setCellValueFactory(
                new PropertyValueFactory<>("amountPaid")
        );

        TableColumn<BillRecord, BigDecimal> balanceColumn =
                new TableColumn<>("Balance");
        balanceColumn.setCellValueFactory(
                new PropertyValueFactory<>("balance")
        );

        TableColumn<BillRecord, String> statusColumn =
                new TableColumn<>("Payment Status");
        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentStatus")
        );

        billTable.getColumns().addAll(
                billIDColumn,
                patientIDColumn,
                visitIDColumn,
                dateColumn,
                totalColumn,
                paidColumn,
                balanceColumn,
                statusColumn
        );

        billTable.setItems(billList);

        billTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    // LOAD PATIENT NAMES
    private void loadPatientNames() {

        try {

            patientNames.clear();

            List<PatientRecord> patients =
                    patientDAO.getAllPatients();

            for (PatientRecord patient : patients) {

                patientNames.put(
                        patient.getPatientID(),
                        patient.getFullName()
                );
            }

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Loading Error",
                    "Unable to load patient details: " + e.getMessage()
            );
        }
    }

    // LOAD ALL BILLS
    private void loadBills() {

        billList.setAll(billDAO.getAllBills());
    }

    // CREATE A NEW BILL
    private void showCreateBillDialog() {

        ComboBox<VisitRecord> visitComboBox = new ComboBox<>();
        visitComboBox.setPromptText("Select an existing visit");
        visitComboBox.setPrefWidth(350);

        Label patientLabel = new Label(
                "Patient: Select a visit first"
        );

        try {

            List<VisitRecord> visits = visitDAO.getAllVisits();

            visitComboBox.setItems(
                    FXCollections.observableArrayList(visits)
            );

            visitComboBox.setConverter(new StringConverter<VisitRecord>() {

                @Override
                public String toString(VisitRecord visit) {

                    if (visit == null) {
                        return "";
                    }

                    String patientName = patientNames.getOrDefault(
                            visit.getPatientID(),
                            "Patient ID " + visit.getPatientID()
                    );

                    String visitDate = visit.getVisitDate() == null
                            ? "Date unavailable"
                            : visit.getVisitDate().toLocalDate().toString();

                    return "Visit #" + visit.getVisitID()
                            + " - " + patientName
                            + " - " + visitDate;
                }

                @Override
                public VisitRecord fromString(String value) {
                    return null;
                }
            });

        } catch (SQLException e) {

            showAlert(
                    Alert.AlertType.ERROR,
                    "Loading Error",
                    "Unable to load visits: " + e.getMessage()
            );

            return;
        }

        visitComboBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {

                    if (newValue != null) {

                        String patientName = patientNames.getOrDefault(
                                newValue.getPatientID(),
                                "Patient ID " + newValue.getPatientID()
                        );

                        patientLabel.setText("Patient: " + patientName);

                    } else {

                        patientLabel.setText(
                                "Patient: Select a visit first"
                        );
                    }
                }
        );

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Existing Visit:"), 0, 0);
        grid.add(visitComboBox, 1, 0);
        grid.add(patientLabel, 1, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Bill");
        dialog.setHeaderText(
                "Select the visit associated with this bill."
        );

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        Button okButton = (Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);

        okButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    if (visitComboBox.getValue() == null) {

                        event.consume();

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Visit Required",
                                "You must select an existing visit."
                        );
                    }
                }
        );

        dialog.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                VisitRecord selectedVisit = visitComboBox.getValue();

                int billID = billDAO.createBill(
                        selectedVisit.getPatientID(),
                        selectedVisit.getVisitID()
                );

                if (billID > 0) {

                    loadBills();

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Bill Created",
                            "Bill created successfully.\n\n"
                                    + "Bill ID: " + billID
                                    + "\nVisit ID: "
                                    + selectedVisit.getVisitID()
                    );

                } else {

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Creation Failed",
                            "Unable to create the bill."
                    );
                }
            }
        });
    }

    // ADD AN ITEM TO AN EXISTING BILL
    private void showAddItemDialog() {

        BillRecord selectedBill =
                billTable.getSelectionModel().getSelectedItem();

        if (selectedBill == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Bill Selected",
                    "Please select a bill from the table first."
            );

            return;
        }

        if (billItemDAO.hasPayments(selectedBill.getBillID())) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Bill Locked",
                    "This bill already has a recorded payment. "
                            + "New items cannot be added."
            );

            return;
        }

        TextField descriptionField = new TextField();
        TextField amountField = new TextField();

        descriptionField.setPromptText("Enter charge description");
        amountField.setPromptText("Enter amount");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Description:"), 0, 0);
        grid.add(descriptionField, 1, 0);
        grid.add(new Label("Amount:"), 0, 1);
        grid.add(amountField, 1, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Bill Item");
        dialog.setHeaderText(
                "Adding charges to Bill #" + selectedBill.getBillID()
        );

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        Button okButton = (Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);

        okButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    String description =
                            descriptionField.getText().trim();

                    String amountText =
                            amountField.getText().trim();

                    if (description.isEmpty() || amountText.isEmpty()) {

                        event.consume();

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Missing Information",
                                "Please enter both description and amount."
                        );

                        return;
                    }

                    if (description.length() > 200) {

                        event.consume();

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Invalid Description",
                                "Description cannot exceed 200 characters."
                        );

                        return;
                    }

                    try {

                        BigDecimal amount = new BigDecimal(amountText);

                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                            event.consume();

                            showAlert(
                                    Alert.AlertType.WARNING,
                                    "Invalid Amount",
                                    "Amount must be greater than zero."
                            );
                        }

                    } catch (NumberFormatException ex) {

                        event.consume();

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Invalid Amount",
                                "Please enter a valid monetary amount."
                        );
                    }
                }
        );

        dialog.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                String description =
                        descriptionField.getText().trim();

                BigDecimal amount =
                        new BigDecimal(amountField.getText().trim());

                boolean added = billItemDAO.addBillItem(
                        selectedBill.getBillID(),
                        description,
                        amount
                );

                if (added) {

                    BigDecimal newTotal =
                            billItemDAO.calculateBillTotal(
                                    selectedBill.getBillID()
                            );

                    boolean updated = billDAO.updateBillTotal(
                            selectedBill.getBillID(),
                            newTotal
                    );

                    loadBills();

                    if (updated) {

                        showAlert(
                                Alert.AlertType.INFORMATION,
                                "Bill Item Added",
                                "Charge added successfully.\n\n"
                                        + "Updated bill total: " + newTotal
                        );

                    } else {

                        showAlert(
                                Alert.AlertType.ERROR,
                                "Total Update Failed",
                                "The charge was saved, but the bill total "
                                        + "could not be updated. Please "
                                        + "refresh and check the bill."
                        );
                    }

                } else {

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Operation Failed",
                            "Unable to add the bill item."
                    );
                }
            }
        });
    }

    // RECORD A PAYMENT
    private void showRecordPaymentDialog() {

        BillRecord selectedBill =
                billTable.getSelectionModel().getSelectedItem();

        if (selectedBill == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Bill Selected",
                    "Please select a bill before recording a payment."
            );

            return;
        }

        BigDecimal outstandingBalance = selectedBill.getBalance();

        if (outstandingBalance == null
                || outstandingBalance.compareTo(BigDecimal.ZERO) <= 0) {

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Bill Already Paid",
                    "This bill has no outstanding balance."
            );

            return;
        }

        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount received");

        ComboBox<String> methodComboBox = new ComboBox<>();

        methodComboBox.setItems(FXCollections.observableArrayList(
                "Cash",
                "M-Pesa",
                "Card",
                "Bank Transfer"
        ));

        methodComboBox.setPromptText("Select payment method");
        methodComboBox.setMaxWidth(Double.MAX_VALUE);

        TextField referenceField = new TextField();
        referenceField.setPromptText("Transaction reference (optional)");

        Label balanceLabel = new Label(
                "Outstanding Balance: " + outstandingBalance
        );

        Label billLabel = new Label(
                "Bill ID: " + selectedBill.getBillID()
        );

        Label patientLabel = new Label(
                "Patient: " + patientNames.getOrDefault(
                        selectedBill.getPatientID(),
                        "Patient ID " + selectedBill.getPatientID()
                )
        );

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.add(billLabel, 0, 0, 2, 1);
        grid.add(patientLabel, 0, 1, 2, 1);
        grid.add(balanceLabel, 0, 2, 2, 1);

        grid.add(new Label("Amount Received:"), 0, 3);
        grid.add(amountField, 1, 3);

        grid.add(new Label("Payment Method:"), 0, 4);
        grid.add(methodComboBox, 1, 4);

        grid.add(new Label("Reference Number:"), 0, 5);
        grid.add(referenceField, 1, 5);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record Patient Payment");
        dialog.setHeaderText(
                "Enter the payment details for Bill #"
                        + selectedBill.getBillID()
        );

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL
        );

        Button okButton = (Button) dialog.getDialogPane()
                .lookupButton(ButtonType.OK);

        okButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> {

                    String amountText = amountField.getText().trim();

                    if (amountText.isEmpty()
                            || methodComboBox.getValue() == null) {

                        event.consume();

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Missing Information",
                                "Enter the amount and select a payment method."
                        );

                        return;
                    }

                    try {

                        BigDecimal amount = new BigDecimal(amountText);

                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                            event.consume();

                            showAlert(
                                    Alert.AlertType.WARNING,
                                    "Invalid Amount",
                                    "Payment amount must be greater than zero."
                            );

                            return;
                        }

                        if (amount.compareTo(outstandingBalance) > 0) {

                            event.consume();

                            showAlert(
                                    Alert.AlertType.WARNING,
                                    "Amount Exceeds Balance",
                                    "The payment cannot exceed the outstanding "
                                            + "balance of " + outstandingBalance
                            );
                        }

                    } catch (NumberFormatException ex) {

                        event.consume();

                        showAlert(
                                Alert.AlertType.WARNING,
                                "Invalid Amount",
                                "Please enter a valid monetary amount."
                        );
                    }
                }
        );

        dialog.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                BigDecimal amount =
                        new BigDecimal(amountField.getText().trim());

                String paymentMethod = methodComboBox.getValue();

                String referenceNumber =
                        referenceField.getText().trim();

                if (referenceNumber.isEmpty()) {
                    referenceNumber = null;
                }

                boolean recorded = paymentDAO.recordPayment(
                        selectedBill.getBillID(),
                        amount,
                        paymentMethod,
                        referenceNumber
                );

                if (recorded) {

                    loadBills();

                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Payment Recorded",
                            "Payment recorded successfully.\n\n"
                                    + "Bill ID: " + selectedBill.getBillID()
                                    + "\nAmount Received: " + amount
                                    + "\nPayment Method: " + paymentMethod
                                    + "\nReference: "
                                    + (referenceNumber == null
                                            ? "Not provided"
                                            : referenceNumber)
                    );

                } else {

                    showAlert(
                            Alert.AlertType.ERROR,
                            "Payment Failed",
                            "Unable to record the payment. "
                                    + "Please check the amount and try again."
                    );
                }
            }
        });
    }

    // VIEW PAYMENT HISTORY FOR A SELECTED BILL
    private void showPaymentHistory() {

        BillRecord selectedBill =
                billTable.getSelectionModel().getSelectedItem();

        if (selectedBill == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Bill Selected",
                    "Please select a bill to view its payment history."
            );

            return;
        }

        List<PaymentRecord> payments =
                paymentDAO.getPaymentsByBillID(selectedBill.getBillID());

        if (payments.isEmpty()) {

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "No Payments Found",
                    "No payments have been recorded for Bill #"
                            + selectedBill.getBillID() + "."
            );

            return;
        }

        TableView<PaymentRecord> paymentTable = new TableView<>();

        TableColumn<PaymentRecord, Integer> paymentIDColumn =
                new TableColumn<>("Payment ID");
        paymentIDColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentID")
        );

        TableColumn<PaymentRecord, BigDecimal> amountColumn =
                new TableColumn<>("Amount Received");
        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("amount")
        );

        TableColumn<PaymentRecord, String> methodColumn =
                new TableColumn<>("Payment Method");
        methodColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentMethod")
        );

        TableColumn<PaymentRecord, LocalDateTime> dateColumn =
                new TableColumn<>("Payment Date");
        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentDate")
        );

        TableColumn<PaymentRecord, String> referenceColumn =
                new TableColumn<>("Reference Number");
        referenceColumn.setCellValueFactory(
                new PropertyValueFactory<>("referenceNumber")
        );

        paymentTable.getColumns().addAll(
                paymentIDColumn,
                amountColumn,
                methodColumn,
                dateColumn,
                referenceColumn
        );

        paymentTable.setItems(
                FXCollections.observableArrayList(payments)
        );

        paymentTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        Label heading = new Label(
                "Payment History for Bill #" + selectedBill.getBillID()
        );

        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox content = new VBox(
                12,
                heading,
                paymentTable
        );

        content.setPadding(new Insets(10));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Payment History");
        dialog.setHeaderText(null);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(750, 400);

        dialog.getDialogPane().getButtonTypes().add(
                ButtonType.CLOSE
        );

        dialog.showAndWait();
    }

    // VIEW, EDIT AND DELETE ITEMS BELONGING TO A SELECTED BILL
    private void showBillItems() {

        BillRecord selectedBill =
                billTable.getSelectionModel().getSelectedItem();

        if (selectedBill == null) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "No Bill Selected",
                    "Please select a bill first."
            );

            return;
        }

        int billID = selectedBill.getBillID();

        TableView<BillItemRecord> itemTable = new TableView<>();

        TableColumn<BillItemRecord, Integer> itemIDColumn =
                new TableColumn<>("Item ID");
        itemIDColumn.setCellValueFactory(
                new PropertyValueFactory<>("billItemID")
        );

        TableColumn<BillItemRecord, String> descriptionColumn =
                new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(
                new PropertyValueFactory<>("description")
        );

        TableColumn<BillItemRecord, BigDecimal> amountColumn =
                new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("amount")
        );

        itemTable.getColumns().addAll(
                itemIDColumn,
                descriptionColumn,
                amountColumn
        );

        itemTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        itemTable.setPrefHeight(300);
        itemTable.setPrefWidth(600);

        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label statusLabel = new Label();

        // Refresh the items and total shown in the window.
        Runnable refreshItems = () -> {

            List<BillItemRecord> items =
                    billItemDAO.getItemsByBillID(billID);

            itemTable.setItems(
                    FXCollections.observableArrayList(items)
            );

            BigDecimal total = billItemDAO.calculateBillTotal(billID);

            totalLabel.setText("Bill Total: " + total);

            loadBills();
        };

        boolean billHasPayments = billItemDAO.hasPayments(billID);

        Button editButton = new Button("Edit Selected Item");
        Button deleteButton = new Button("Delete Selected Item");

        // Wrap the item-management buttons as well.
        FlowPane actionButtons = new FlowPane();
        actionButtons.setHgap(10);
        actionButtons.setVgap(10);
        actionButtons.getChildren().addAll(
                editButton,
                deleteButton
        );

        if (billHasPayments) {

            statusLabel.setText(
                    "This bill has recorded payments. Its items are locked."
            );

            statusLabel.setStyle("-fx-text-fill: #b45309;");

            editButton.setDisable(true);
            deleteButton.setDisable(true);

        } else {

            statusLabel.setText(
                    "Select an item to edit or delete it."
            );
        }

        // EDIT SELECTED ITEM
        editButton.setOnAction(e -> {

            BillItemRecord selectedItem =
                    itemTable.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "No Item Selected",
                        "Please select a bill item to edit."
                );

                return;
            }

            TextField descriptionField = new TextField(
                    selectedItem.getDescription()
            );

            TextField amountField = new TextField(
                    selectedItem.getAmount().toString()
            );

            GridPane editGrid = new GridPane();
            editGrid.setHgap(12);
            editGrid.setVgap(15);
            editGrid.setPadding(new Insets(20));

            editGrid.add(new Label("Description:"), 0, 0);
            editGrid.add(descriptionField, 1, 0);

            editGrid.add(new Label("Amount:"), 0, 1);
            editGrid.add(amountField, 1, 1);

            Dialog<ButtonType> editDialog = new Dialog<>();
            editDialog.setTitle("Edit Bill Item");
            editDialog.setHeaderText(
                    "Correct the details of Bill Item #"
                            + selectedItem.getBillItemID()
            );

            editDialog.getDialogPane().setContent(editGrid);

            editDialog.getDialogPane().getButtonTypes().addAll(
                    ButtonType.OK,
                    ButtonType.CANCEL
            );

            Button editOKButton = (Button) editDialog.getDialogPane()
                    .lookupButton(ButtonType.OK);

            editOKButton.addEventFilter(
                    javafx.event.ActionEvent.ACTION,
                    event -> {

                        String description =
                                descriptionField.getText().trim();

                        String amountText =
                                amountField.getText().trim();

                        if (description.isEmpty() || amountText.isEmpty()) {

                            event.consume();

                            showAlert(
                                    Alert.AlertType.WARNING,
                                    "Missing Information",
                                    "Please enter both description and amount."
                            );

                            return;
                        }

                        if (description.length() > 200) {

                            event.consume();

                            showAlert(
                                    Alert.AlertType.WARNING,
                                    "Invalid Description",
                                    "Description cannot exceed 200 characters."
                            );

                            return;
                        }

                        try {

                            BigDecimal amount = new BigDecimal(amountText);

                            if (amount.compareTo(BigDecimal.ZERO) <= 0) {

                                event.consume();

                                showAlert(
                                        Alert.AlertType.WARNING,
                                        "Invalid Amount",
                                        "Amount must be greater than zero."
                                );
                            }

                        } catch (NumberFormatException ex) {

                            event.consume();

                            showAlert(
                                    Alert.AlertType.WARNING,
                                    "Invalid Amount",
                                    "Please enter a valid monetary amount."
                            );
                        }
                    }
            );

            editDialog.showAndWait().ifPresent(response -> {

                if (response == ButtonType.OK) {

                    String description =
                            descriptionField.getText().trim();

                    BigDecimal amount =
                            new BigDecimal(amountField.getText().trim());

                    boolean updated = billItemDAO.updateBillItem(
                            selectedItem.getBillItemID(),
                            description,
                            amount
                    );

                    if (updated) {

                        refreshItems.run();

                        showAlert(
                                Alert.AlertType.INFORMATION,
                                "Item Updated",
                                "Bill item updated successfully. "
                                        + "The bill total has been recalculated."
                        );

                    } else {

                        showAlert(
                                Alert.AlertType.ERROR,
                                "Update Failed",
                                "The item could not be updated. "
                                        + "Check whether the bill has a payment."
                        );
                    }
                }
            });
        });

        // DELETE SELECTED ITEM
        deleteButton.setOnAction(e -> {

            BillItemRecord selectedItem =
                    itemTable.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {

                showAlert(
                        Alert.AlertType.WARNING,
                        "No Item Selected",
                        "Please select a bill item to delete."
                );

                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Delete Bill Item?");
            confirmation.setContentText(
                    "Are you sure you want to delete:\n\n"
                            + selectedItem.getDescription()
                            + "\nAmount: " + selectedItem.getAmount()
            );

            confirmation.showAndWait().ifPresent(response -> {

                if (response == ButtonType.OK) {

                    boolean deleted = billItemDAO.deleteBillItem(
                            selectedItem.getBillItemID()
                    );

                    if (deleted) {

                        refreshItems.run();

                        showAlert(
                                Alert.AlertType.INFORMATION,
                                "Item Deleted",
                                "Bill item deleted successfully. "
                                        + "The bill total has been recalculated."
                        );

                    } else {

                        showAlert(
                                Alert.AlertType.ERROR,
                                "Deletion Failed",
                                "The item could not be deleted. "
                                        + "Check whether the bill has a payment."
                        );
                    }
                }
            });
        });

        refreshItems.run();

        VBox content = new VBox(
                12,
                itemTable,
                totalLabel,
                statusLabel,
                actionButtons
        );

        content.setPadding(new Insets(10));
        VBox.setVgrow(itemTable, Priority.ALWAYS);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Bill Items");
        dialog.setHeaderText(
                "Charges for Bill #" + billID
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(650, 480);

        dialog.getDialogPane().getButtonTypes().add(
                ButtonType.CLOSE
        );

        dialog.showAndWait();
    }

    // DISPLAY ALERT MESSAGES
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
