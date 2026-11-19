
package com.MediConnect.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TreatmentDAO {

    // Register a new treatment
    public int registerTreatment(
            int visitID,
            String treatmentDescription,
            BigDecimal treatmentCost) {

        String sql = "INSERT INTO Treatments "
                   + "(VisitID, TreatmentDescription, TreatmentCost) "
                   + "VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, visitID);
            statement.setString(2, treatmentDescription);
            statement.setBigDecimal(3, treatmentCost);

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

    // Retrieve all treatments
    public List<TreatmentRecord> getAllTreatments() {

        List<TreatmentRecord> treatments = new ArrayList<>();

        String sql = "SELECT TreatmentID, VisitID, "
                   + "TreatmentDescription, TreatmentCost "
                   + "FROM Treatments ORDER BY TreatmentID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                treatments.add(mapTreatment(result));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return treatments;
    }

    // Search treatments
    public List<TreatmentRecord> searchTreatments(String keyword) {

        List<TreatmentRecord> treatments = new ArrayList<>();

        String sql = "SELECT TreatmentID, VisitID, "
                   + "TreatmentDescription, TreatmentCost "
                   + "FROM Treatments "
                   + "WHERE CAST(TreatmentID AS VARCHAR(20)) LIKE ? "
                   + "OR CAST(VisitID AS VARCHAR(20)) LIKE ? "
                   + "OR TreatmentDescription LIKE ? "
                   + "ORDER BY TreatmentID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String searchValue = "%" + keyword + "%";

            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    treatments.add(mapTreatment(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return treatments;
    }

    // Update treatment description and cost
    public boolean updateTreatment(
            int treatmentID,
            String treatmentDescription,
            BigDecimal treatmentCost) {

        String sql = "UPDATE Treatments "
                   + "SET TreatmentDescription = ?, TreatmentCost = ? "
                   + "WHERE TreatmentID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, treatmentDescription);
            statement.setBigDecimal(2, treatmentCost);
            statement.setInt(3, treatmentID);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Convert database results into TreatmentRecord objects
    private TreatmentRecord mapTreatment(ResultSet result) throws Exception {

        return new TreatmentRecord(
                result.getInt("TreatmentID"),
                result.getInt("VisitID"),
                result.getString("TreatmentDescription"),
                result.getBigDecimal("TreatmentCost")
        );
    }
}
