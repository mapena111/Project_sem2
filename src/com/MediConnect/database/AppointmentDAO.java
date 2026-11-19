
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    // Book a new appointment
    public boolean bookAppointment(int patientID, int doctorID,
            LocalDate appointmentDate, LocalTime appointmentTime)
            throws SQLException {

        String sql = "INSERT INTO Appointments "
                + "(PatientID, DoctorID, AppointmentDate, AppointmentTime, Status) "
                + "VALUES (?, ?, ?, ?, 'Scheduled')";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientID);
            statement.setInt(2, doctorID);
            statement.setDate(3, Date.valueOf(appointmentDate));
            statement.setTime(4, Time.valueOf(appointmentTime));

            return statement.executeUpdate() > 0;
        }
    }

    // Retrieve all appointments
    public List<AppointmentRecord> getAllAppointments()
            throws SQLException {

        List<AppointmentRecord> appointments = new ArrayList<>();

        String sql = "SELECT * FROM Appointments "
                + "ORDER BY AppointmentDate DESC, AppointmentTime DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                appointments.add(mapAppointment(result));
            }
        }

        return appointments;
    }

    // Search appointments using appointment, patient or doctor ID
    public List<AppointmentRecord> searchAppointments(String keyword)
            throws SQLException {

        List<AppointmentRecord> appointments = new ArrayList<>();

        String sql = "SELECT * FROM Appointments WHERE "
                + "CAST(AppointmentID AS VARCHAR(20)) LIKE ? OR "
                + "CAST(PatientID AS VARCHAR(20)) LIKE ? OR "
                + "CAST(DoctorID AS VARCHAR(20)) LIKE ? OR "
                + "Status LIKE ? "
                + "ORDER BY AppointmentDate DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchValue = "%" + keyword.trim() + "%";

            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            statement.setString(4, searchValue);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    appointments.add(mapAppointment(result));
                }
            }
        }

        return appointments;
    }

    // Update appointment details
    public boolean updateAppointment(int appointmentID, int patientID,
            int doctorID, LocalDate appointmentDate,
            LocalTime appointmentTime) throws SQLException {

        String sql = "UPDATE Appointments SET PatientID = ?, DoctorID = ?, "
                + "AppointmentDate = ?, AppointmentTime = ? "
                + "WHERE AppointmentID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientID);
            statement.setInt(2, doctorID);
            statement.setDate(3, Date.valueOf(appointmentDate));
            statement.setTime(4, Time.valueOf(appointmentTime));
            statement.setInt(5, appointmentID);

            return statement.executeUpdate() > 0;
        }
    }

    // Change appointment status
    public boolean updateAppointmentStatus(int appointmentID, String status)
            throws SQLException {

        String sql = "UPDATE Appointments SET Status = ? "
                + "WHERE AppointmentID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status);
            statement.setInt(2, appointmentID);

            return statement.executeUpdate() > 0;
        }
    }

    // Convert database results into AppointmentRecord objects
    private AppointmentRecord mapAppointment(ResultSet result)
            throws SQLException {

        return new AppointmentRecord(
                result.getInt("AppointmentID"),
                result.getInt("PatientID"),
                result.getInt("DoctorID"),
                result.getDate("AppointmentDate").toLocalDate(),
                result.getTime("AppointmentTime").toLocalTime(),
                result.getString("Status")
        );
    }
}
