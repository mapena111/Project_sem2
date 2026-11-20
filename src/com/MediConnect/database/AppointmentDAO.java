
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

    // Check whether a doctor already has a scheduled appointment
    private boolean hasScheduledAppointment(
            Connection connection,
            int doctorID,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            int excludedAppointmentID) throws SQLException {

        String sql = "SELECT AppointmentID FROM Appointments "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE DoctorID = ? "
                + "AND AppointmentDate = CAST(? AS date) "
                + "AND AppointmentTime = CAST(? AS time) "
                + "AND Status = 'Scheduled' "
                + "AND AppointmentID <> ?";

        try (PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setInt(1, doctorID);
            statement.setString(2, appointmentDate.toString());
            statement.setString(3, appointmentTime.toString());
            statement.setInt(4, excludedAppointmentID);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // Book a new appointment
    public boolean bookAppointment(
            int patientID,
            int doctorID,
            LocalDate appointmentDate,
            LocalTime appointmentTime) throws SQLException {

        String sql = "INSERT INTO Appointments "
                + "(PatientID, DoctorID, AppointmentDate, "
                + "AppointmentTime, Status) "
                + "VALUES (?, ?, ?, ?, 'Scheduled')";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);

            try {
                if (hasScheduledAppointment(
                        connection, doctorID, appointmentDate,
                        appointmentTime, -1)) {

                    throw new SQLException(
                            "This doctor already has a scheduled "
                            + "appointment at the selected date and time.");
                }

                try (PreparedStatement statement =
                        connection.prepareStatement(sql)) {

                    statement.setInt(1, patientID);
                    statement.setInt(2, doctorID);
                    statement.setDate(3, Date.valueOf(appointmentDate));
                    statement.setTime(4, Time.valueOf(appointmentTime));

                    boolean success = statement.executeUpdate() > 0;

                    connection.commit();
                    return success;
                }

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    // Retrieve all appointments
    public List<AppointmentRecord> getAllAppointments()
            throws SQLException {

        List<AppointmentRecord> appointments = new ArrayList<>();

        String sql = "SELECT * FROM Appointments "
                + "ORDER BY AppointmentDate DESC, AppointmentTime DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                appointments.add(mapAppointment(result));
            }
        }

        return appointments;
    }


    // Search appointments using appointment ID, patient name or ID,
    // doctor name or ID, and appointment status
    public List<AppointmentRecord> searchAppointments(String keyword)
            throws SQLException {

        List<AppointmentRecord> appointments = new ArrayList<>();

        String sql =
                "SELECT a.* FROM Appointments a "
                + "LEFT JOIN Patients p ON a.PatientID = p.PatientID "
                + "LEFT JOIN Doctors d ON a.DoctorID = d.DoctorID "
                + "WHERE CAST(a.AppointmentID AS VARCHAR(20)) LIKE ? "
                + "OR CAST(a.PatientID AS VARCHAR(20)) LIKE ? "
                + "OR p.FullName LIKE ? "
                + "OR CAST(a.DoctorID AS VARCHAR(20)) LIKE ? "
                + "OR d.FullName LIKE ? "
                + "OR a.Status LIKE ? "
                + "ORDER BY a.AppointmentDate DESC, "
                + "a.AppointmentTime DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            String searchValue = "%" + keyword.trim() + "%";

            for (int i = 1; i <= 6; i++) {
                statement.setString(i, searchValue);
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    appointments.add(mapAppointment(result));
                }
            }
        }

        return appointments;
    }

    // Update appointment details
    public boolean updateAppointment(
            int appointmentID,
            int patientID,
            int doctorID,
            LocalDate appointmentDate,
            LocalTime appointmentTime) throws SQLException {

        String getStatusSQL =
                "SELECT Status FROM Appointments "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE AppointmentID = ?";

        String updateSQL =
                "UPDATE Appointments SET PatientID = ?, DoctorID = ?, "
                + "AppointmentDate = ?, AppointmentTime = ? "
                + "WHERE AppointmentID = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);

            try {
                String currentStatus;

                try (PreparedStatement statement =
                        connection.prepareStatement(getStatusSQL)) {

                    statement.setInt(1, appointmentID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }

                        currentStatus = result.getString("Status");
                    }
                }

                if ("Scheduled".equalsIgnoreCase(currentStatus)) {

                    if (hasScheduledAppointment(
                            connection, doctorID, appointmentDate,
                            appointmentTime, appointmentID)) {

                        throw new SQLException(
                                "This doctor already has a scheduled "
                                + "appointment at the selected date and time.");
                    }
                }

                try (PreparedStatement statement =
                        connection.prepareStatement(updateSQL)) {

                    statement.setInt(1, patientID);
                    statement.setInt(2, doctorID);
                    statement.setDate(3, Date.valueOf(appointmentDate));
                    statement.setTime(4, Time.valueOf(appointmentTime));
                    statement.setInt(5, appointmentID);

                    boolean success = statement.executeUpdate() > 0;

                    connection.commit();
                    return success;
                }

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    // Change appointment status
    public boolean updateAppointmentStatus(
            int appointmentID, String status) throws SQLException {

        String getAppointmentSQL =
                "SELECT DoctorID, AppointmentDate, AppointmentTime "
                + "FROM Appointments "
                + "WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE AppointmentID = ?";

        String updateSQL =
                "UPDATE Appointments SET Status = ? "
                + "WHERE AppointmentID = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);

            try {
                if ("Scheduled".equalsIgnoreCase(status)) {

                    try (PreparedStatement statement =
                            connection.prepareStatement(getAppointmentSQL)) {

                        statement.setInt(1, appointmentID);

                        try (ResultSet result = statement.executeQuery()) {

                            if (!result.next()) {
                                connection.rollback();
                                return false;
                            }

                            int doctorID = result.getInt("DoctorID");

                            LocalDate date = result.getDate(
                                    "AppointmentDate").toLocalDate();

                            LocalTime time = result.getTime(
                                    "AppointmentTime").toLocalTime();

                            if (hasScheduledAppointment(
                                    connection, doctorID, date, time,
                                    appointmentID)) {

                                throw new SQLException(
                                        "This doctor already has a scheduled "
                                        + "appointment at this date and time.");
                            }
                        }
                    }
                }

                try (PreparedStatement statement =
                        connection.prepareStatement(updateSQL)) {

                    statement.setString(1, status);
                    statement.setInt(2, appointmentID);

                    boolean success = statement.executeUpdate() > 0;

                    connection.commit();
                    return success;
                }

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
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
