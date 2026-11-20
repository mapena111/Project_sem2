
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


	// Register a new patient visit with database validation
	public int registerVisit(int patientID, int doctorID,
	        Integer appointmentID, String notes) throws SQLException {

	    String patientDoctorSQL =
	            "SELECT COUNT(*) FROM Patients p "
	            + "CROSS JOIN Doctors d "
	            + "WHERE p.PatientID = ? AND p.IsArchived = 0 "
	            + "AND d.DoctorID = ? AND d.IsArchived = 0";

	    String appointmentSQL =
	            "SELECT PatientID, DoctorID, Status "
	            + "FROM Appointments WITH (UPDLOCK, HOLDLOCK) "
	            + "WHERE AppointmentID = ?";

	    String insertSQL =
	            "INSERT INTO Visits "
	            + "(PatientID, DoctorID, AppointmentID, VisitDate, Notes) "
	            + "VALUES (?, ?, ?, GETDATE(), ?)";

	    try (Connection connection = DatabaseConnection.getConnection()) {

	        connection.setAutoCommit(false);

	        try {
	            // 1. Verify that the patient and doctor exist and are active
	            try (PreparedStatement statement =
	                    connection.prepareStatement(patientDoctorSQL)) {

	                statement.setInt(1, patientID);
	                statement.setInt(2, doctorID);

	                try (ResultSet result = statement.executeQuery()) {
	                    if (!result.next() || result.getInt(1) != 1) {
	                        throw new SQLException(
	                                "The selected patient or doctor does not exist "
	                                + "or is archived.");
	                    }
	                }
	            }

	            // 2. If an appointment was selected, validate its details
	            if (appointmentID != null) {

	                try (PreparedStatement statement =
	                        connection.prepareStatement(appointmentSQL)) {

	                    statement.setInt(1, appointmentID);

	                    try (ResultSet result = statement.executeQuery()) {

	                        if (!result.next()) {
	                            throw new SQLException(
	                                    "The selected appointment does not exist.");
	                        }

	                        int appointmentPatientID =
	                                result.getInt("PatientID");

	                        int appointmentDoctorID =
	                                result.getInt("DoctorID");

	                        String status = result.getString("Status");

	                        if (appointmentPatientID != patientID
	                                || appointmentDoctorID != doctorID) {

	                            throw new SQLException(
	                                    "The selected appointment does not belong "
	                                    + "to the selected patient and doctor.");
	                        }

	                        if (!"Completed".equalsIgnoreCase(status)) {
	                            throw new SQLException(
	                                    "Only completed appointments can be "
	                                    + "registered as patient visits.");
	                        }
	                    }
	                }
	            }

	            // 3. Insert the visit and automatically record its date
	            try (PreparedStatement statement =
	                    connection.prepareStatement(
	                            insertSQL, Statement.RETURN_GENERATED_KEYS)) {

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
	                    throw new SQLException(
	                            "The visit could not be registered.");
	                }

	                try (ResultSet keys = statement.getGeneratedKeys()) {
	                    if (keys.next()) {
	                        int visitID = keys.getInt(1);
	                        connection.commit();
	                        return visitID;
	                    }
	                }
	            }

	            throw new SQLException(
	                    "Visit registration failed. No generated ID was returned.");

	        } catch (SQLException e) {
	            connection.rollback();
	            throw e;
	        }
	    }
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

    // Search visits using visit ID, patient name/ID, doctor name/ID or notes
    public List<VisitRecord> searchVisits(String keyword)
            throws SQLException {

        List<VisitRecord> visits = new ArrayList<>();

        String sql = "SELECT v.* FROM Visits v "
                + "LEFT JOIN Patients p ON v.PatientID = p.PatientID "
                + "LEFT JOIN Doctors d ON v.DoctorID = d.DoctorID "
                + "WHERE CAST(v.VisitID AS VARCHAR(20)) LIKE ? "
                + "OR CAST(v.PatientID AS VARCHAR(20)) LIKE ? "
                + "OR p.FullName LIKE ? "
                + "OR CAST(v.DoctorID AS VARCHAR(20)) LIKE ? "
                + "OR d.FullName LIKE ? "
                + "OR v.Notes LIKE ? "
                + "ORDER BY v.VisitDate DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchValue = "%" + keyword.trim() + "%";

            for (int i = 1; i <= 6; i++) {
                statement.setString(i, searchValue);
            }

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
