package com.MediConnect;



import com.MediConnect.module.*;



import javafx.application.Application;

import javafx.beans.property.SimpleStringProperty;

import javafx.geometry.Insets;

import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.*;

import javafx.scene.layout.*;


import javafx.stage.Stage;



import java.time.LocalDate;

import java.time.LocalTime;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;

import java.util.List;



public class MainApp extends Application {



    private appointment_manager appointmentManager;

    private patient currentPatient;



    private BorderPane root;

    private VBox content;



    private TextField patientNameField;

    private ComboBox<doctor> doctorSelect;

    private DatePicker datePicker;

    private ComboBox<String> timeSelect;



    private TableView<appointment> appointmentTable;

    // Keeps the dashboard appointment count up to date.
    private Label appointmentsCountLabel;



    @Override

    public void start(Stage primaryStage) {



        appointmentManager = new appointment_manager();



        initializeSampleData();



        root = new BorderPane();

        root.setStyle("-fx-background-color: #F4F7FB;");



        content = new VBox(20);

        content.setPadding(new Insets(25));



        root.setLeft(createSidebar());

        root.setCenter(content);



        // Important: this displays the dashboard when the app starts.

        showDashboard();



        Scene scene = new Scene(root, 1100, 700);



        // Load CSS only if the file exists.

        java.net.URL css = getClass().getResource("style.css");



        if (css != null) {

            scene.getStylesheets().add(css.toExternalForm());

        }



        primaryStage.setTitle("MediConnect - Patient Portal");

        primaryStage.setMinWidth(900);

        primaryStage.setMinHeight(600);

        primaryStage.setScene(scene);

        primaryStage.show();

    }



    // DISPLAY ALERT MESSAGES

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // SAMPLE DATA



    private void initializeSampleData() {



        appointmentManager.addDoctor(

                new doctor("Dr. Smith", "Cardiology", "Mon, Wed")

        );



        appointmentManager.addDoctor(

                new doctor("Dr. Jones", "Pediatrics", "Tue, Thu")

        );



        appointmentManager.addDoctor(

                new doctor("Dr. Brown", "General Practice", "Tue, Thu")

        );



        currentPatient = new patient(

                "", "P100", "No significant history."

        );



        appointmentManager.addPatient(currentPatient);

    }



    // SIDEBAR



    private VBox createSidebar() {



        VBox menu = new VBox(15);

        menu.setPadding(new Insets(25, 15, 25, 15));

        menu.setPrefWidth(220);

        menu.setStyle("-fx-background-color: #123B67;");



        Label logo = new Label("♥ MediConnect");

        logo.setStyle(

                "-fx-text-fill: white;" +

                "-fx-font-size: 23px;" +

                "-fx-font-weight: bold;"

        );



        Label subtitle = new Label("PATIENT PORTAL");

        subtitle.setStyle("-fx-text-fill: #A8D7F5;");



        Separator separator = new Separator();



        Button dashboardButton = menuButton("Dashboard");

        Button appointmentsButton = menuButton("Appointments");

        Button doctorsButton = menuButton("Doctors");

        Button profileButton = menuButton("Patient Profile");



        dashboardButton.setOnAction(e -> showDashboard());

        appointmentsButton.setOnAction(e -> showAppointments());

        doctorsButton.setOnAction(e -> showDoctors());

        profileButton.setOnAction(e -> showProfile());



        Region spacer = new Region();

        VBox.setVgrow(spacer, Priority.ALWAYS);



        Label footer = new Label("Better Care. Better Life.");

        footer.setStyle("-fx-text-fill: #A8D7F5;");



        menu.getChildren().addAll(

                logo,

                subtitle,

                separator,

                dashboardButton,

                appointmentsButton,

                doctorsButton,

                profileButton,

                spacer,

                footer

        );



        return menu;

    }



    private Button menuButton(String text) {



        Button button = new Button(text);



        button.setMaxWidth(Double.MAX_VALUE);

        button.setAlignment(Pos.CENTER_LEFT);



        button.setStyle(

                "-fx-background-color: transparent;" +

                "-fx-text-fill: white;" +

                "-fx-font-size: 14px;" +

                "-fx-padding: 12 15;" +

                "-fx-cursor: hand;"

        );



        button.setOnMouseEntered(e ->

                button.setStyle(

                        "-fx-background-color: #245782;" +

                        "-fx-text-fill: white;" +

                        "-fx-font-size: 14px;" +

                        "-fx-padding: 12 15;" +

                        "-fx-background-radius: 8;" +

                        "-fx-cursor: hand;"

                )

        );



        button.setOnMouseExited(e ->

                button.setStyle(

                        "-fx-background-color: transparent;" +

                        "-fx-text-fill: white;" +

                        "-fx-font-size: 14px;" +

                        "-fx-padding: 12 15;" +

                        "-fx-cursor: hand;"

                )

        );



        return button;

    }



    // PAGE HEADER



    private VBox createHeader(String title, String subtitle) {



        VBox header = new VBox(5);



        Label pageTitle = new Label(title);

        pageTitle.getStyleClass().add("page-title");

        pageTitle.setStyle(

                "-fx-font-size: 27px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #123B67;"

        );



        Label pageSubtitle = new Label(subtitle);

        pageSubtitle.getStyleClass().add("page-subtitle");

        pageSubtitle.setStyle(

                "-fx-font-size: 14px;" +

                "-fx-text-fill: #64748B;"

        );



        header.getChildren().addAll(pageTitle, pageSubtitle);



        return header;

    }



    // DASHBOARD



    private void showDashboard() {



        content.getChildren().clear();



        content.getChildren().add(

                createHeader(

                        "Dashboard",

                        "Welcome to MediConnect Patient Portal"

                )

        );



        HBox statistics = new HBox(15);



        statistics.getChildren().addAll(

                createStatCard(

                        "Registered Doctors",

                        String.valueOf(

                                appointmentManager.getDoctors().size()

                        )

                ),

                createStatCard(

                        "Your Appointments",

                        String.valueOf(

                                appointmentManager.getAppointments().size()

                        )

                ),

                createStatCard(

                        "Patient ID",

                        currentPatient.getId()

                )

        );



        content.getChildren().add(statistics);



        HBox mainSection = new HBox(20);



        VBox bookingCard = createBookingCard();

        VBox appointmentsCard = createAppointmentsCard();



        HBox.setHgrow(bookingCard, Priority.ALWAYS);

        HBox.setHgrow(appointmentsCard, Priority.ALWAYS);



        mainSection.getChildren().addAll(

                bookingCard,

                appointmentsCard

        );



        content.getChildren().add(mainSection);

    }



    private VBox createStatCard(String heading, String value) {



        VBox card = new VBox(12);

        card.getStyleClass().add("card");

        card.setPadding(new Insets(20));

        card.setStyle(

                "-fx-background-color: white;" +

                "-fx-background-radius: 12;" +

                "-fx-border-color: #E2E8F0;" +

                "-fx-border-radius: 12;"

        );



        Label title = new Label(heading);

        title.getStyleClass().add("card-heading");

        title.setStyle(

                "-fx-font-size: 13px;" +

                "-fx-text-fill: #64748B;"

        );



        Label number = new Label(value);

        if (heading.equals("Your Appointments")) {
            appointmentsCountLabel = number;
        }

        number.setStyle(

                "-fx-font-size: 26px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #087BA8;"

        );



        card.getChildren().addAll(title, number);



        HBox.setHgrow(card, Priority.ALWAYS);



        return card;

    }



    // BOOKING FORM



    private VBox createBookingCard() {



        VBox card = new VBox(12);

        card.getStyleClass().add("card");

        card.setPadding(new Insets(20));

        card.setStyle(

                "-fx-background-color: white;" +

                "-fx-background-radius: 12;" +

                "-fx-border-color: #E2E8F0;" +

                "-fx-border-radius: 12;"

        );



        Label heading = new Label("Book an Appointment");

        heading.setStyle(

                "-fx-font-size: 18px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #123B67;"

        );



        patientNameField = new TextField();

        patientNameField.setPromptText("Enter patient name");

        patientNameField.setMaxWidth(Double.MAX_VALUE);



        doctorSelect = new ComboBox<>();

        doctorSelect.getItems().setAll(

                appointmentManager.getDoctors()

        );

        doctorSelect.setPromptText("Select doctor");

        doctorSelect.setMaxWidth(Double.MAX_VALUE);



        datePicker = new DatePicker();

        datePicker.setPromptText("Select appointment date");

        datePicker.setMaxWidth(Double.MAX_VALUE);



        datePicker.setDayCellFactory(picker -> new DateCell() {

            @Override

            public void updateItem(LocalDate date, boolean empty) {

                super.updateItem(date, empty);



                if (!empty && date != null &&

                        date.isBefore(LocalDate.now())) {

                    setDisable(true);

                }

            }

        });



        timeSelect = new ComboBox<>();

        timeSelect.getItems().setAll(generateTimes());

        timeSelect.setPromptText("Select appointment time");

        timeSelect.setMaxWidth(Double.MAX_VALUE);



        Button bookButton = new Button("Confirm Booking");

        bookButton.getStyleClass().add("primary-button");

        bookButton.setMaxWidth(Double.MAX_VALUE);

        bookButton.setStyle(

                "-fx-background-color: #087BA8;" +

                "-fx-text-fill: white;" +

                "-fx-font-weight: bold;" +

                "-fx-padding: 11;" +

                "-fx-background-radius: 8;" +

                "-fx-cursor: hand;"

        );






        bookButton.setOnAction(e -> {



            String name = patientNameField.getText().trim();



            doctor selectedDoctor = doctorSelect.getValue();

            LocalDate selectedDate = datePicker.getValue();

            String selectedTime = timeSelect.getValue();



            if (name.isEmpty() || selectedDoctor == null

                    || selectedDate == null || selectedTime == null) {



                showAlert(Alert.AlertType.WARNING, "Incomplete Booking",
                        "Please complete all booking fields.");

                return;

            }



            if (selectedDate.isBefore(LocalDate.now())) {



                showAlert(Alert.AlertType.WARNING, "Invalid Date",
                        "Please select a valid appointment date.");

                return;

            }



            // Use the same patient record for every appointment.
            // A patient's ID remains the same across all their bookings.
            currentPatient.setName(name);

            appointment newAppointment = new appointment(

                    selectedDoctor,

                    currentPatient,

                    selectedDate.toString(),

                    selectedTime,

                    "Confirmed"

            );



            appointmentManager.scheduleAppointment(newAppointment);



            showAlert(Alert.AlertType.INFORMATION, "Appointment Confirmed",
                    "Your appointment has been booked successfully.\n\n" +
                    "Patient: " + name + "\n" +
                    "Patient ID: " + currentPatient.getId() + "\n" +
                    "Doctor: " + selectedDoctor.getName() + "\n" +
                    "Date: " + selectedDate + "\n" +
                    "Time: " + selectedTime);



            patientNameField.clear();

            doctorSelect.setValue(null);

            datePicker.setValue(null);

            timeSelect.setValue(null);



            // Refresh the existing table without rebuilding the dashboard.

            refreshAppointmentTable();

            // Update the dashboard statistic immediately after booking.
            if (appointmentsCountLabel != null) {
                appointmentsCountLabel.setText(
                        String.valueOf(appointmentManager.getAppointments().size())
                );
            }

        });



        card.getChildren().addAll(

                heading,

                new Label("Patient Name"),

                patientNameField,

                new Label("Doctor"),

                doctorSelect,

                new Label("Appointment Date"),

                datePicker,

                new Label("Appointment Time"),

                timeSelect,

                bookButton

        );



        return card;

    }



    // GENERATE APPOINTMENT TIMES



    private List<String> generateTimes() {



        List<String> times = new ArrayList<>();



        LocalTime time = LocalTime.of(8, 0);

        LocalTime end = LocalTime.of(17, 0);



        DateTimeFormatter formatter =

                DateTimeFormatter.ofPattern("h:mm a");



        while (!time.isAfter(end)) {



            times.add(time.format(formatter));

            time = time.plusMinutes(30);

        }



        return times;

    }



    // APPOINTMENTS TABLE



    private VBox createAppointmentsCard() {



        VBox card = new VBox(15);

        card.getStyleClass().add("card");

        card.setPadding(new Insets(20));

        card.setStyle(

                "-fx-background-color: white;" +

                "-fx-background-radius: 12;" +

                "-fx-border-color: #E2E8F0;" +

                "-fx-border-radius: 12;"

        );



        Label heading = new Label("Your Appointments");

        heading.setStyle(

                "-fx-font-size: 18px;" +

                "-fx-font-weight: bold;" +

                "-fx-text-fill: #123B67;"

        );



        appointmentTable = new TableView<>();



        TableColumn<appointment, String> patientColumn =

                new TableColumn<>("Patient");



        patientColumn.setCellValueFactory(data ->

                new SimpleStringProperty(

                        data.getValue().getPatient().getName()

                )

        );



        TableColumn<appointment, String> doctorColumn =

                new TableColumn<>("Doctor");



        doctorColumn.setCellValueFactory(data ->

                new SimpleStringProperty(

                        data.getValue().getDoctor().getName()

                )

        );



        TableColumn<appointment, String> dateColumn =

                new TableColumn<>("Date");



        dateColumn.setCellValueFactory(data ->

                new SimpleStringProperty(

                        data.getValue().getDate()

                )

        );



        TableColumn<appointment, String> timeColumn =

                new TableColumn<>("Time");



        timeColumn.setCellValueFactory(data ->

                new SimpleStringProperty(

                        data.getValue().getTime()

                )

        );



        appointmentTable.getColumns().addAll(

                patientColumn,

                doctorColumn,

                dateColumn,

                timeColumn

        );



        appointmentTable.setColumnResizePolicy(

                TableView.CONSTRAINED_RESIZE_POLICY

        );



        appointmentTable.setPrefHeight(300);



        refreshAppointmentTable();



        Button refreshButton = new Button("Refresh");

        refreshButton.setOnAction(e -> refreshAppointmentTable());



        card.getChildren().addAll(

                heading,

                appointmentTable,

                refreshButton

        );



        return card;

    }



    private void refreshAppointmentTable() {



        if (appointmentTable != null) {

            appointmentTable.getItems().setAll(

                    appointmentManager.getAppointments()

            );

        }

    }



    // APPOINTMENTS PAGE



    private void showAppointments() {



        content.getChildren().clear();



        content.getChildren().add(

                createHeader(

                        "Appointments",

                        "View and manage patient bookings"

                )

        );



        content.getChildren().add(createAppointmentsCard());

    }



    // DOCTORS PAGE



    private void showDoctors() {



        content.getChildren().clear();



        content.getChildren().add(

                createHeader(

                        "Our Doctors",

                        "Choose a specialist and book your consultation"

                )

        );



        TilePane doctorGrid = new TilePane();



        doctorGrid.setHgap(20);

        doctorGrid.setVgap(20);

        doctorGrid.setPrefColumns(2);



        // Correct TilePane sizing methods.

        doctorGrid.setPrefTileWidth(280);

        doctorGrid.setPrefTileHeight(260);



        for (doctor d : appointmentManager.getDoctors()) {



            VBox card = new VBox(12);

            card.getStyleClass().add("doctor-card");

            card.setPadding(new Insets(20));

            card.setAlignment(Pos.TOP_CENTER);



            card.setStyle(

                    "-fx-background-color: white;" +

                    "-fx-background-radius: 16;" +

                    "-fx-border-color: #E2E8F0;" +

                    "-fx-border-radius: 16;"

            );



            String doctorNameText = d.getName();



            String initial = doctorNameText.isEmpty()

                    ? "D"

                    : doctorNameText.substring(0, 1).toUpperCase();



            Label avatar = new Label(initial);

            avatar.getStyleClass().add("doctor-avatar");

            avatar.setStyle(

                    "-fx-background-color: #DFF3FC;" +

                    "-fx-background-radius: 50%;" +

                    "-fx-min-width: 65px;" +

                    "-fx-min-height: 65px;" +

                    "-fx-max-width: 65px;" +

                    "-fx-max-height: 65px;" +

                    "-fx-alignment: center;" +

                    "-fx-font-size: 25px;" +

                    "-fx-font-weight: bold;" +

                    "-fx-text-fill: #087BA8;"

            );



            Label name = new Label(doctorNameText);

            name.setStyle(

                    "-fx-font-size: 18px;" +

                    "-fx-font-weight: bold;" +

                    "-fx-text-fill: #123B67;"

            );



            Label specialty = new Label(d.getSpecialty());

            specialty.setStyle(

                    "-fx-background-color: #E0F2FE;" +

                    "-fx-text-fill: #0369A1;" +

                    "-fx-padding: 6 12;" +

                    "-fx-background-radius: 20;" +

                    "-fx-font-weight: bold;"

            );



            Label availability = new Label(

                    "Available: " + d.getAvailability()

            );



            availability.setStyle(

                    "-fx-font-size: 12px;" +

                    "-fx-text-fill: #64748B;"

            );



            Button bookButton = new Button("Book Appointment");

            bookButton.getStyleClass().add("primary-button");

            bookButton.setMaxWidth(Double.MAX_VALUE);



            bookButton.setOnAction(e -> {



                // Return to dashboard and select this doctor.

                showDashboard();



                doctorSelect.setValue(d);

                patientNameField.requestFocus();

            });



            card.getChildren().addAll(

                    avatar,

                    name,

                    specialty,

                    availability,

                    bookButton

            );



            doctorGrid.getChildren().add(card);

        }



        ScrollPane scrollPane = new ScrollPane(doctorGrid);



        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollPane.setStyle("-fx-background-color: transparent;");



        VBox.setVgrow(scrollPane, Priority.ALWAYS);



        content.getChildren().add(scrollPane);

    }



    // PATIENT PROFILE



    private void showProfile() {



        content.getChildren().clear();



        content.getChildren().add(

                createHeader(

                        "Patient Profile",

                        "Manage your personal information"

                )

        );



        VBox card = new VBox(15);

        card.getStyleClass().add("card");

        card.setPadding(new Insets(25));

        card.setStyle(

                "-fx-background-color: white;" +

                "-fx-background-radius: 12;" +

                "-fx-border-color: #E2E8F0;" +

                "-fx-border-radius: 12;"

        );



        TextField nameField = new TextField(

                currentPatient.getName()

        );



        TextField idField = new TextField(

                currentPatient.getId()

        );



        idField.setEditable(false);



        TextArea historyArea = new TextArea(

                currentPatient.getMedicalHistory()

        );



        historyArea.setPrefRowCount(4);



        Button saveButton = new Button("Save Profile");

        saveButton.getStyleClass().add("primary-button");





        saveButton.setOnAction(e -> {



            String name = nameField.getText().trim();



            if (name.isEmpty()) {



                showAlert(Alert.AlertType.WARNING, "Missing Name",
                        "Name cannot be empty. Please enter your name.");

                return;

            }



            currentPatient.setName(name);



            currentPatient.setMedicalHistory(

                    historyArea.getText()

            );



            showAlert(Alert.AlertType.INFORMATION, "Profile Updated",
                    "Your profile has been updated successfully.");

        });



        card.getChildren().addAll(

                new Label("Patient Name"),

                nameField,

                new Label("Patient ID"),

                idField,

                new Label("Medical History"),

                historyArea,

                saveButton

        );



        content.getChildren().add(card);

    }



    // MAIN METHOD



    public static void main(String[] args) {

        launch(args);

    }

}
