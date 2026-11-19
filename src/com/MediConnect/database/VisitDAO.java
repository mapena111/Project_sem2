
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class VisitDAO {

    // Register a new patient visit
    public int registerVisit(int patientID, int doctorID,
            Integer appointmentID, String notes) throws SQLException {

        String sql = "INSERT INTO Visits "
                + "(PatientID, DoctorID, AppointmentID, Notes) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, patientID);
            statement.setInt(2, doctorID);

            if (appointmentID == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, appointmentID);
            }

            statement.setString(4, notes);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return -1;
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    // Retrieve all patient visits
    public List<VisitRecord> getAllVisits() throws SQLException {

        List<VisitRecord> visits = new ArrayList<>();

        String sql = "SELECT * FROM Visits ORDER BY VisitDate DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                visits.add(mapVisit(result));
            }
        }

        return visits;
    }

    // Search visits using visit, patient or doctor ID
    public List<VisitRecord> searchVisits(String keyword)
            throws SQLException {

        List<VisitRecord> visits = new ArrayList<>();

        String sql = "SELECT * FROM Visits WHERE "
                + "CAST(VisitID AS VARCHAR(20)) LIKE ? OR "
                + "CAST(PatientID AS VARCHAR(20)) LIKE ? OR "
                + "CAST(DoctorID AS VARCHAR(20)) LIKE ? OR "
                + "Notes LIKE ? "
                + "ORDER BY VisitDate DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchValue = "%" + keyword.trim() + "%";

            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            statement.setString(4, searchValue);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    visits.add(mapVisit(result));
                }
            }
        }

        return visits;
    }

    // Update consultation notes
    public boolean updateVisitNotes(int visitID, String notes)
            throws SQLException {

        String sql = "UPDATE Visits SET Notes = ? WHERE VisitID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, notes);
            statement.setInt(2, visitID);

            return statement.executeUpdate() > 0;
        }
    }

    // Convert database records into VisitRecord objects
    private VisitRecord mapVisit(ResultSet result) throws SQLException {

        Timestamp timestamp = result.getTimestamp("VisitDate");

        Integer appointmentID = result.getObject(
                "AppointmentID", Integer.class);

        return new VisitRecord(
                result.getInt("VisitID"),
                result.getInt("PatientID"),
                result.getInt("DoctorID"),
                appointmentID,
                timestamp == null ? null : timestamp.toLocalDateTime(),
                result.getString("Notes")
        );
    }
}
