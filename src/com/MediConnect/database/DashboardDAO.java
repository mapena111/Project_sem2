
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DashboardDAO {

    // Get the total number of registered patients
    public int getTotalPatients() throws SQLException {
        return getCount("SELECT COUNT(*) FROM Patients");
    }

    // Get the total number of doctors
    public int getTotalDoctors() throws SQLException {
        return getCount("SELECT COUNT(*) FROM Doctors");
    }

    // Get the total number of appointments
    public int getTotalAppointments() throws SQLException {
        return getCount("SELECT COUNT(*) FROM Appointments");
    }

    // Get the total number of staff members
    public int getTotalStaff() throws SQLException {
        return getCount("SELECT COUNT(*) FROM Staff");
    }

    // Get the number of different patients who visited on a selected date
    public int getPatientsVisitedOnDate(LocalDate date) throws SQLException {

        String sql = """
                SELECT COUNT(DISTINCT PatientID)
                FROM Visits
                WHERE VisitDate >= ?
                AND VisitDate < DATEADD(day, 1, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(date));
            statement.setDate(2, java.sql.Date.valueOf(date));

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        }

        return 0;
    }

    // Reusable method for retrieving database counts
    private int getCount(String sql) throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return result.getInt(1);
            }
        }

        return 0;
    }
}
