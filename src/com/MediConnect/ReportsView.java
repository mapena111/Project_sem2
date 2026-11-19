
package com.MediConnect;

import com.MediConnect.database.ReceiptDAO;
import com.MediConnect.database.ReceiptRecord;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class ReportsView extends BorderPane {

    private final ReceiptDAO receiptDAO = new ReceiptDAO();

    private final ObservableList<ReceiptRecord> receiptList =
            FXCollections.observableArrayList();

    private TableView<ReceiptRecord> receiptTable;

    private Button previewButton;
    private Button saveButton;
    private Button printButton;

    public ReportsView() {

        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f3f7fb;");

        createHeader();
        createReceiptTable();
        createButtons();

        loadReceipts();
    }

    // CREATE HEADER
    private void createHeader() {

        VBox header = new VBox(8);

        Label title = new Label("Reports Management");

        title.setStyle(
                "-fx-font-size: 25px;"
                + "-fx-font-weight: bold;"
                + "-fx-text-fill: #173b57;"
        );

        Label description = new Label(
                "View, preview, save and print payment receipts."
        );

        header.getChildren().addAll(title, description);

        setTop(header);

        BorderPane.setMargin(
                header,
                new Insets(0, 0, 20, 0)
        );
    }

    // CREATE RECEIPT TABLE
    private void createReceiptTable() {

        receiptTable = new TableView<>();

        receiptTable.setItems(receiptList);

        receiptTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<ReceiptRecord, Integer> paymentColumn =
                new TableColumn<>("Receipt No.");

        paymentColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentID")
        );

        paymentColumn.setCellFactory(column ->
                new javafx.scene.control.TableCell<ReceiptRecord, Integer>() {

                    @Override
                    protected void updateItem(Integer item, boolean empty) {

                        super.updateItem(item, empty);

                        setText(empty || item == null
                                ? null
                                : String.format("REC-%06d", item));
                    }
                }
        );

        TableColumn<ReceiptRecord, String> patientColumn =
                new TableColumn<>("Patient");

        patientColumn.setCellValueFactory(
                new PropertyValueFactory<>("patientName")
        );

        TableColumn<ReceiptRecord, Integer> patientIDColumn =
                new TableColumn<>("Patient ID");

        patientIDColumn.setCellValueFactory(
                new PropertyValueFactory<>("patientID")
        );

        TableColumn<ReceiptRecord, Integer> visitColumn =
                new TableColumn<>("Visit ID");

        visitColumn.setCellValueFactory(
                new PropertyValueFactory<>("visitID")
        );

        TableColumn<ReceiptRecord, BigDecimal> amountColumn =
                new TableColumn<>("Amount Received");

        amountColumn.setCellValueFactory(
                new PropertyValueFactory<>("amountPaid")
        );

        TableColumn<ReceiptRecord, BigDecimal> balanceColumn =
                new TableColumn<>("Remaining Balance");

        balanceColumn.setCellValueFactory(
                new PropertyValueFactory<>("balance")
        );

        TableColumn<ReceiptRecord, String> methodColumn =
                new TableColumn<>("Payment Method");

        methodColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentMethod")
        );

        TableColumn<ReceiptRecord, java.time.LocalDateTime> dateColumn =
                new TableColumn<>("Payment Date");

        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("paymentDate")
        );

        dateColumn.setCellFactory(column ->
                new javafx.scene.control.TableCell<ReceiptRecord,
                        java.time.LocalDateTime>() {

                    private final DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

                    @Override
                    protected void updateItem(
                            java.time.LocalDateTime item,
                            boolean empty) {

                        super.updateItem(item, empty);

                        setText(empty || item == null
                                ? null
                                : formatter.format(item));
                    }
                }
        );

        receiptTable.getColumns().addAll(
                paymentColumn,
                patientColumn,
                patientIDColumn,
                visitColumn,
                amountColumn,
                balanceColumn,
                methodColumn,
                dateColumn
        );

        setCenter(receiptTable);
    }

    // CREATE BUTTONS
    private void createButtons() {

        Button refreshButton = new Button("Refresh Receipts");

        previewButton = new Button("Preview Receipt");
        saveButton = new Button("Save as PDF");
        printButton = new Button("Print Receipt");

        previewButton.setDisable(true);
        saveButton.setDisable(true);
        printButton.setDisable(true);

        receiptTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    boolean noSelection = newValue == null;

                    previewButton.setDisable(noSelection);
                    saveButton.setDisable(noSelection);
                    printButton.setDisable(noSelection);
                });

        refreshButton.setOnAction(event -> loadReceipts());

        previewButton.setOnAction(event -> previewReceipt());

        saveButton.setOnAction(event -> saveReceiptAsPDF());

        printButton.setOnAction(event -> printReceipt());

        HBox buttons = new HBox(
                12,
                refreshButton,
                previewButton,
                saveButton,
                printButton
        );

        buttons.setAlignment(Pos.CENTER_LEFT);

        buttons.setPadding(new Insets(20, 0, 0, 0));

        setBottom(buttons);
    }

    // LOAD RECEIPTS
    private void loadReceipts() {

        List<ReceiptRecord> receipts =
                receiptDAO.getAllReceipts();

        receiptList.setAll(receipts);

        if (receipts.isEmpty()) {

            showInformation(
                    "No Receipts",
                    "No payment receipts were found."
            );
        }
    }

    // GET SELECTED RECEIPT
    private ReceiptRecord getSelectedReceipt() {

        ReceiptRecord selected =
                receiptTable.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showWarning(
                    "Select Receipt",
                    "Please select a receipt from the table."
            );
        }

        return selected;
    }

    // PREVIEW RECEIPT
    private void previewReceipt() {

        ReceiptRecord selected = getSelectedReceipt();

        if (selected == null) {
            return;
        }

        String receiptText =
                "MEDICONNECT HOSPITAL\n"
                + "PAYMENT RECEIPT\n\n"
                + "Receipt Number: REC-"
                + String.format("%06d", selected.getPaymentID())
                + "\nPatient Name: " + safe(selected.getPatientName())
                + "\nPatient ID: " + selected.getPatientID()
                + "\nVisit ID: " + selected.getVisitID()
                + "\nBill ID: " + selected.getBillID()
                + "\n\nBill Total: KSh " + selected.getTotalBill()
                + "\nAmount Received: KSh " + selected.getAmountPaid()
                + "\nRemaining Balance: KSh " + selected.getBalance()
                + "\n\nPayment Method: " + safe(selected.getPaymentMethod())
                + "\nReference Number: " + safe(selected.getReferenceNumber())
                + "\nBill Date: " + selected.getBillDate()
                + "\nPayment Date: " + selected.getPaymentDate()
                + "\n\nThank you for your payment.";

        Alert preview = new Alert(Alert.AlertType.INFORMATION);

        preview.setTitle("Receipt Preview");

        preview.setHeaderText(
                "Receipt REC-"
                + String.format("%06d", selected.getPaymentID())
        );

        preview.setContentText(receiptText);

        preview.getDialogPane().setPrefWidth(450);

        preview.showAndWait();
    }

    // SAVE RECEIPT AS PDF
    private void saveReceiptAsPDF() {

        ReceiptRecord selected = getSelectedReceipt();

        if (selected == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Save Payment Receipt");

        fileChooser.setInitialFileName(
                "Receipt_REC-"
                + String.format("%06d", selected.getPaymentID())
                + ".pdf"
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "PDF Files", "*.pdf"
                )
        );

        File file = fileChooser.showSaveDialog(
                getScene() == null ? null : getScene().getWindow()
        );

        if (file == null) {
            return;
        }

        String path = file.getAbsolutePath();

        if (!path.toLowerCase().endsWith(".pdf")) {
            path += ".pdf";
        }

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);

            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                PDType1Font boldFont = new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                );

                PDType1Font normalFont = new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );

                float y = 760;

                content.beginText();
                content.setFont(boldFont, 20);
                content.newLineAtOffset(155, y);
                content.showText("MEDICONNECT HOSPITAL");
                content.endText();

                y -= 35;

                content.beginText();
                content.setFont(boldFont, 16);
                content.newLineAtOffset(230, y);
                content.showText("PAYMENT RECEIPT");
                content.endText();

                y -= 45;

                String[] lines = {
                        "Receipt Number: REC-" +
                                String.format("%06d", selected.getPaymentID()),
                        "Bill ID: " + selected.getBillID(),
                        "Patient Name: " + safe(selected.getPatientName()),
                        "Patient ID: " + selected.getPatientID(),
                        "Visit ID: " + selected.getVisitID(),
                        "",
                        "PAYMENT DETAILS",
                        "Bill Total: KSh " + selected.getTotalBill(),
                        "Amount Received: KSh " + selected.getAmountPaid(),
                        "Remaining Balance: KSh " + selected.getBalance(),
                        "",
                        "Payment Method: " + safe(selected.getPaymentMethod()),
                        "Reference Number: " + safe(selected.getReferenceNumber()),
                        "Bill Date: " + selected.getBillDate(),
                        "Payment Date: " + selected.getPaymentDate()
                };

                for (String line : lines) {

                    content.beginText();

                    if (line.equals("PAYMENT DETAILS")) {
                        content.setFont(boldFont, 12);
                    } else {
                        content.setFont(normalFont, 11);
                    }

                    content.newLineAtOffset(75, y);

                    content.showText(line);

                    content.endText();

                    y -= 27;
                }

                content.beginText();
                content.setFont(boldFont, 12);
                content.newLineAtOffset(175, y - 20);
                content.showText("Thank you for your payment.");
                content.endText();
            }

            document.save(path);

            showInformation(
                    "Receipt Saved",
                    "Receipt successfully saved to:\n" + path
            );

        } catch (IOException | IllegalArgumentException e) {

            e.printStackTrace();

            showError(
                    "PDF Generation Failed",
                    "Unable to save the receipt.\n" + e.getMessage()
            );
        }
    }

    // PRINT RECEIPT
    private void printReceipt() {

        ReceiptRecord selected = getSelectedReceipt();

        if (selected == null) {
            return;
        }

        PrinterJob printerJob = PrinterJob.createPrinterJob();

        if (printerJob == null) {

            showError(
                    "Printer Error",
                    "No available printer was found."
            );

            return;
        }

        boolean proceed = printerJob.showPrintDialog(
                getScene() == null ? null : getScene().getWindow()
        );

        if (!proceed) {
            return;
        }

        Label hospital = new Label("MEDICONNECT HOSPITAL");

        hospital.setStyle(
                "-fx-font-size: 22px;"
                + "-fx-font-weight: bold;"
        );

        Label heading = new Label("PAYMENT RECEIPT");

        heading.setStyle(
                "-fx-font-size: 17px;"
                + "-fx-font-weight: bold;"
        );

        Label receiptNumber = new Label(
                "Receipt Number: REC-" +
                String.format("%06d", selected.getPaymentID())
        );

        Label patient = new Label(
                "Patient Name: " + safe(selected.getPatientName())
        );

        Label patientID = new Label(
                "Patient ID: " + selected.getPatientID()
        );

        Label visit = new Label(
                "Visit ID: " + selected.getVisitID()
        );

        Label bill = new Label(
                "Bill ID: " + selected.getBillID()
        );

        Label total = new Label(
                "Bill Total: KSh " + selected.getTotalBill()
        );

        Label amount = new Label(
                "Amount Received: KSh " + selected.getAmountPaid()
        );

        Label balance = new Label(
                "Remaining Balance: KSh " + selected.getBalance()
        );

        Label method = new Label(
                "Payment Method: " + safe(selected.getPaymentMethod())
        );

        Label reference = new Label(
                "Reference Number: " + safe(selected.getReferenceNumber())
        );

        Label billDate = new Label(
                "Bill Date: " + selected.getBillDate()
        );

        Label paymentDate = new Label(
                "Payment Date: " + selected.getPaymentDate()
        );

        Label footer = new Label(
                "Thank you for your payment."
        );

        VBox receipt = new VBox(
                15,
                hospital,
                heading,
                new Separator(),
                receiptNumber,
                patient,
                patientID,
                visit,
                bill,
                new Separator(),
                total,
                amount,
                balance,
                new Separator(),
                method,
                reference,
                billDate,
                paymentDate,
                new Separator(),
                footer
        );

        receipt.setPadding(new Insets(35));
        receipt.setAlignment(Pos.TOP_LEFT);
        receipt.setStyle("-fx-background-color: white;");
        receipt.setPrefWidth(500);

        try {

            receipt.applyCss();
            receipt.autosize();
            receipt.layout();

            boolean printed = printerJob.printPage(receipt);

            if (printed) {

                boolean completed = printerJob.endJob();

                if (completed) {

                    showInformation(
                            "Printing Successful",
                            "Receipt REC-" +
                            String.format("%06d", selected.getPaymentID()) +
                            " was sent to the printer."
                    );

                } else {

                    showError(
                            "Printing Failed",
                            "The printer could not complete the print job."
                    );
                }

            } else {

                printerJob.cancelJob();

                showError(
                        "Printing Failed",
                        "The receipt could not be printed."
                );
            }

        } catch (Exception e) {

            printerJob.cancelJob();

            e.printStackTrace();

            showError(
                    "Printing Error",
                    "Unable to print the receipt: " + e.getMessage()
            );
        }
    }

    // HANDLE NULL VALUES
    private String safe(String value) {

        return value == null ? "N/A" : value;
    }

    // INFORMATION ALERT
    private void showInformation(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    // WARNING ALERT
    private void showWarning(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    // ERROR ALERT
    private void showError(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
