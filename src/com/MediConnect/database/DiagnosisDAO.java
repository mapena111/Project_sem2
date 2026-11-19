
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DiagnosisDAO {

    // Register a new diagnosis
    public int registerDiagnosis(int visitID, String diagnosisDescription) {

        String sql = "INSERT INTO Diagnosis (VisitID, DiagnosisDescription) "
                   + "VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, visitID);
            statement.setString(2, diagnosisDescription);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    // Retrieve all diagnoses
    public List<DiagnosisRecord> getAllDiagnoses() {

        List<DiagnosisRecord> diagnoses = new ArrayList<>();

        String sql = "SELECT DiagnosisID, VisitID, DiagnosisDescription "
                   + "FROM Diagnosis ORDER BY DiagnosisID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                diagnoses.add(mapDiagnosis(result));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return diagnoses;
    }

    // Search diagnoses
    public List<DiagnosisRecord> searchDiagnoses(String keyword) {

        List<DiagnosisRecord> diagnoses = new ArrayList<>();

        String sql = "SELECT DiagnosisID, VisitID, DiagnosisDescription "
                   + "FROM Diagnosis "
                   + "WHERE CAST(DiagnosisID AS VARCHAR(20)) LIKE ? "
                   + "OR CAST(VisitID AS VARCHAR(20)) LIKE ? "
                   + "OR DiagnosisDescription LIKE ? "
                   + "ORDER BY DiagnosisID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchValue = "%" + keyword + "%";

            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    diagnoses.add(mapDiagnosis(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return diagnoses;
    }

    // Update a diagnosis description
    public boolean updateDiagnosis(int diagnosisID, String diagnosisDescription) {

        String sql = "UPDATE Diagnosis SET DiagnosisDescription = ? "
                   + "WHERE DiagnosisID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, diagnosisDescription);
            statement.setInt(2, diagnosisID);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Convert database results into DiagnosisRecord objects
    private DiagnosisRecord mapDiagnosis(ResultSet result) throws Exception {

        return new DiagnosisRecord(
                result.getInt("DiagnosisID"),
                result.getInt("VisitID"),
                result.getString("DiagnosisDescription")
        );
    }
}