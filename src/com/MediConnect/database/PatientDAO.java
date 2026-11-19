
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // REGISTER A NEW PATIENT
    public int registerPatient(
            String fullName,
            String gender,
            java.sql.Date dateOfBirth,
            String phone,
            String email,
            String address) throws SQLException {

        String sql = "INSERT INTO Patients "
                + "(FullName, Gender, DateOfBirth, Phone, Email, Address) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, fullName);
            statement.setString(2, gender);
            statement.setDate(3, dateOfBirth);
            statement.setString(4, phone);
            statement.setString(5, email);
            statement.setString(6, address);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Patient registration failed.");
    }


    // RETRIEVE ACTIVE PATIENTS ONLY
    public List<PatientRecord> getAllPatients() throws SQLException {

        List<PatientRecord> patients = new ArrayList<>();

        String sql = "SELECT * FROM Patients "
                + "WHERE IsArchived = 0 "
                + "ORDER BY PatientID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                patients.add(new PatientRecord(
                        result.getInt("PatientID"),
                        result.getString("FullName"),
                        result.getString("Gender"),
                        result.getDate("DateOfBirth"),
                        result.getString("Phone"),
                        result.getString("Email"),
                        result.getString("Address"),
                        result.getDate("RegistrationDate")
                ));
            }
        }

        return patients;
    }


    // SEARCH ACTIVE PATIENTS ONLY
    public List<PatientRecord> searchPatients(String keyword)
            throws SQLException {

        List<PatientRecord> patients = new ArrayList<>();

        String sql = "SELECT * FROM Patients "
                + "WHERE IsArchived = 0 AND ("
                + "FullName LIKE ? "
                + "OR CAST(PatientID AS VARCHAR(20)) LIKE ? "
                + "OR Phone LIKE ?) "
                + "ORDER BY PatientID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchValue = "%" + keyword.trim() + "%";

            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    patients.add(new PatientRecord(
                            result.getInt("PatientID"),
                            result.getString("FullName"),
                            result.getString("Gender"),
                            result.getDate("DateOfBirth"),
                            result.getString("Phone"),
                            result.getString("Email"),
                            result.getString("Address"),
                            result.getDate("RegistrationDate")
                    ));
                }
            }
        }

        return patients;
    }


    // UPDATE ACTIVE PATIENT DETAILS
    public boolean updatePatient(
            int patientID,
            String fullName,
            String gender,
            java.sql.Date dateOfBirth,
            String phone,
            String email,
            String address) throws SQLException {

        String sql = "UPDATE Patients SET "
                + "FullName = ?, Gender = ?, DateOfBirth = ?, "
                + "Phone = ?, Email = ?, Address = ? "
                + "WHERE PatientID = ? AND IsArchived = 0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName);
            statement.setString(2, gender);
            statement.setDate(3, dateOfBirth);
            statement.setString(4, phone);
            statement.setString(5, email);
            statement.setString(6, address);
            statement.setInt(7, patientID);

            return statement.executeUpdate() > 0;
        }
    }


    // SOFT DELETE (ARCHIVE) A PATIENT
    public boolean archivePatient(int patientID) throws SQLException {

        String sql = "UPDATE Patients "
                + "SET IsArchived = 1 "
                + "WHERE PatientID = ? AND IsArchived = 0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientID);

            return statement.executeUpdate() > 0;
        }
    }
}
