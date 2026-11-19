
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    // REGISTER DOCTOR
    public boolean registerDoctor(
            String fullName,
            String specialization,
            String phone,
            String email,
            double consultationFee) throws SQLException {

        String sql = "INSERT INTO Doctors "
                + "(FullName, Specialization, Phone, Email, ConsultationFee) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName);
            statement.setString(2, specialization);
            statement.setString(3, phone);
            statement.setString(4, email);
            statement.setDouble(5, consultationFee);

            return statement.executeUpdate() > 0;
        }
    }

    // RETRIEVE ACTIVE DOCTORS
    public List<DoctorRecord> getAllDoctors() throws SQLException {

        List<DoctorRecord> doctors = new ArrayList<>();

        String sql = "SELECT * FROM Doctors "
                + "WHERE IsArchived = 0 "
                + "ORDER BY DoctorID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                doctors.add(mapDoctor(result));
            }
        }

        return doctors;
    }

    // SEARCH ACTIVE DOCTORS
    public List<DoctorRecord> searchDoctors(String keyword)
            throws SQLException {

        List<DoctorRecord> doctors = new ArrayList<>();

        String sql = "SELECT * FROM Doctors WHERE IsArchived = 0 AND ("
                + "FullName LIKE ? "
                + "OR Specialization LIKE ? "
                + "OR CAST(DoctorID AS VARCHAR(20)) LIKE ?) "
                + "ORDER BY DoctorID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchValue = "%" + keyword.trim() + "%";

            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    doctors.add(mapDoctor(result));
                }
            }
        }

        return doctors;
    }

    // UPDATE DOCTOR DETAILS
    public boolean updateDoctor(
            int doctorID,
            String fullName,
            String specialization,
            String phone,
            String email,
            double consultationFee) throws SQLException {

        String sql = "UPDATE Doctors SET "
                + "FullName = ?, Specialization = ?, "
                + "Phone = ?, Email = ?, ConsultationFee = ? "
                + "WHERE DoctorID = ? AND IsArchived = 0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName);
            statement.setString(2, specialization);
            statement.setString(3, phone);
            statement.setString(4, email);
            statement.setDouble(5, consultationFee);
            statement.setInt(6, doctorID);

            return statement.executeUpdate() > 0;
        }
    }

    // ARCHIVE DOCTOR
    public boolean archiveDoctor(int doctorID) throws SQLException {

        String sql = "UPDATE Doctors SET IsArchived = 1 "
                + "WHERE DoctorID = ? AND IsArchived = 0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, doctorID);

            return statement.executeUpdate() > 0;
        }
    }

    // CONVERT DATABASE ROW INTO DOCTOR RECORD
    private DoctorRecord mapDoctor(ResultSet result)
            throws SQLException {

        return new DoctorRecord(
                result.getInt("DoctorID"),
                result.getString("FullName"),
                result.getString("Specialization"),
                result.getString("Phone"),
                result.getString("Email"),
                result.getDouble("ConsultationFee")
        );
    }
}
