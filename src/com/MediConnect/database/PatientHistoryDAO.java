
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PatientHistoryDAO {

    public ArrayList<PatientHistoryRecord> getPatientHistory(int patientID) {

        ArrayList<PatientHistoryRecord> history = new ArrayList<>();

        String sql =
            "SELECT p.PatientID, p.FullName, " +
            "v.VisitID, " +
            "CONVERT(VARCHAR(10), v.VisitDate, 23) AS VisitDate, " +
            "d.FullName AS DoctorName, " +

            "COALESCE(diag.Diagnoses, 'No diagnosis recorded') AS Diagnosis, " +
            "COALESCE(treat.Treatments, 'No treatment recorded') AS Treatment, " +
            "COALESCE(treat.TotalTreatmentCost, 0) AS TreatmentCost, " +

            "COALESCE(b.BillAmount, 0) AS BillAmount, " +
            "COALESCE(b.PaymentStatus, 'No bill') AS PaymentStatus " +

            "FROM Patients p " +

            "INNER JOIN Visits v ON p.PatientID = v.PatientID " +

            "LEFT JOIN Doctors d ON v.DoctorID = d.DoctorID " +

            "OUTER APPLY (" +
            "SELECT STRING_AGG(DiagnosisDescription, '; ') AS Diagnoses " +
            "FROM Diagnosis WHERE VisitID = v.VisitID" +
            ") diag " +

            "OUTER APPLY (" +
            "SELECT STRING_AGG(TreatmentDescription, '; ') AS Treatments, " +
            "SUM(TreatmentCost) AS TotalTreatmentCost " +
            "FROM Treatments WHERE VisitID = v.VisitID" +
            ") treat " +

            "OUTER APPLY (" +
            "SELECT SUM(TotalAmount) AS BillAmount, " +
            "CASE " +
            "WHEN COUNT(*) = 0 THEN 'No bill' " +
            "WHEN SUM(COALESCE(AmountPaid, 0)) >= " +
            "SUM(COALESCE(TotalAmount, 0)) THEN 'Paid' " +
            "WHEN SUM(COALESCE(AmountPaid, 0)) > 0 THEN 'Partially Paid' " +
            "ELSE 'Unpaid' END AS PaymentStatus " +
            "FROM Bills WHERE VisitID = v.VisitID" +
            ") b " +

            "WHERE p.PatientID = ? " +

            "ORDER BY v.VisitDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientID);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {

                    PatientHistoryRecord record =
                        new PatientHistoryRecord(
                            rs.getInt("PatientID"),
                            rs.getString("FullName"),
                            rs.getInt("VisitID"),
                            rs.getString("VisitDate"),
                            rs.getString("DoctorName"),
                            rs.getString("Diagnosis"),
                            rs.getString("Treatment"),
                            rs.getDouble("TreatmentCost"),
                            rs.getDouble("BillAmount"),
                            rs.getString("PaymentStatus")
                        );

                    history.add(record);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }
}
