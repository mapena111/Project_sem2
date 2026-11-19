
package com.MediConnect;

import com.MediConnect.database.PatientHistoryDAO;
import com.MediConnect.database.PatientHistoryRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PatientHistoryView {

    private final PatientHistoryDAO historyDAO = new PatientHistoryDAO();

    private final ObservableList<PatientHistoryRecord> historyList =
            FXCollections.observableArrayList();

    private TableView<PatientHistoryRecord> table;

    public BorderPane getView() {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        Label heading = new Label("Patient Medical History");
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField patientIDField = new TextField();
        patientIDField.setPromptText("Enter Patient ID");
        patientIDField.setPrefWidth(180);

        Button searchButton = new Button("View History");

        HBox searchBox = new HBox(10, patientIDField, searchButton);
        searchBox.setPadding(new Insets(15, 0, 15, 0));

        VBox topSection = new VBox(5, heading, searchBox);

        table = new TableView<>();
        table.setItems(historyList);

        TableColumn<PatientHistoryRecord, Number> patientIDCol =
                new TableColumn<>("Patient ID");
        patientIDCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getPatientID()));

        TableColumn<PatientHistoryRecord, String> patientNameCol =
                new TableColumn<>("Patient Name");
        patientNameCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPatientName()));

        TableColumn<PatientHistoryRecord, Number> visitIDCol =
                new TableColumn<>("Visit ID");
        visitIDCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getVisitID()));

        TableColumn<PatientHistoryRecord, String> visitDateCol =
                new TableColumn<>("Visit Date");
        visitDateCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getVisitDate()));

        TableColumn<PatientHistoryRecord, String> doctorCol =
                new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDoctorName()));

        TableColumn<PatientHistoryRecord, String> diagnosisCol =
                new TableColumn<>("Diagnosis");
        diagnosisCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDiagnosis()));

        TableColumn<PatientHistoryRecord, String> treatmentCol =
                new TableColumn<>("Treatment");
        treatmentCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getTreatment()));

        TableColumn<PatientHistoryRecord, Number> treatmentCostCol =
                new TableColumn<>("Treatment Cost");
        treatmentCostCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty(
                        data.getValue().getTreatmentCost()));

        TableColumn<PatientHistoryRecord, Number> billCol =
                new TableColumn<>("Bill Amount");
        billCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleDoubleProperty(
                        data.getValue().getBillAmount()));

        TableColumn<PatientHistoryRecord, String> paymentCol =
                new TableColumn<>("Payment Status");
        paymentCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPaymentStatus()));

        table.getColumns().addAll(
                patientIDCol,
                patientNameCol,
                visitIDCol,
                visitDateCol,
                doctorCol,
                diagnosisCol,
                treatmentCol,
                treatmentCostCol,
                billCol,
                paymentCol
        );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY);

        searchButton.setOnAction(e -> searchPatient(patientIDField));

        patientIDField.setOnAction(e -> searchPatient(patientIDField));

        root.setTop(topSection);
        root.setCenter(table);

        return root;
    }

    private void searchPatient(TextField patientIDField) {

        String input = patientIDField.getText().trim();

        if (input.isEmpty()) {
            showAlert("Input Required", "Please enter a Patient ID.");
            return;
        }

        try {

            int patientID = Integer.parseInt(input);

            if (patientID <= 0) {
                showAlert("Invalid ID", "Please enter a valid Patient ID.");
                return;
            }

            historyList.clear();

            historyList.addAll(
                    historyDAO.getPatientHistory(patientID));

            if (historyList.isEmpty()) {
                showAlert("No Records",
                        "No medical history was found for this patient.");
            }

        } catch (NumberFormatException ex) {

            showAlert("Invalid Input",
                    "Patient ID must contain numbers only.");
        }
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
