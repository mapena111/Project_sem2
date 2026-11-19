
package com.MediConnect.database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {

    // CREATE A BILL USING AN EXISTING VISIT
    public int createBill(int patientID, Integer visitID) {

        if (visitID == null || visitID <= 0) {
            System.out.println("A valid visit is required to create a bill.");
            return -1;
        }

        String visitSQL =
                "SELECT v.PatientID, d.ConsultationFee "
              + "FROM Visits v "
              + "INNER JOIN Doctors d ON v.DoctorID = d.DoctorID "
              + "WHERE v.VisitID = ?";

        String billSQL =
                "INSERT INTO Bills "
              + "(PatientID, VisitID, TotalAmount, AmountPaid, PaymentStatus) "
              + "VALUES (?, ?, 0, 0, 'Unpaid')";

        String treatmentSQL =
                "SELECT TreatmentDescription, TreatmentCost "
              + "FROM Treatments WHERE VisitID = ? "
              + "ORDER BY TreatmentID";

        String itemSQL =
                "INSERT INTO BillItems (BillID, Description, Amount) "
              + "VALUES (?, ?, ?)";

        String totalSQL =
                "SELECT COALESCE(SUM(Amount), 0) AS Total "
              + "FROM BillItems WHERE BillID = ?";

        String updateTotalSQL =
                "UPDATE Bills SET TotalAmount = ? WHERE BillID = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int actualPatientID;
                BigDecimal consultationFee;

                // 1. GET PATIENT AND CONSULTATION FEE FROM THE VISIT
                try (PreparedStatement statement =
                             connection.prepareStatement(visitSQL)) {

                    statement.setInt(1, visitID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            System.out.println("Selected visit does not exist.");
                            return -1;
                        }

                        actualPatientID = result.getInt("PatientID");
                        consultationFee =
                                result.getBigDecimal("ConsultationFee");
                    }
                }

                // ENSURE THE SELECTED PATIENT MATCHES THE VISIT
                if (patientID != actualPatientID) {
                    connection.rollback();

                    System.out.println(
                            "The selected patient does not match the visit.");

                    return -1;
                }

                int billID;

                // 2. CREATE THE BILL
                try (PreparedStatement statement =
                             connection.prepareStatement(
                                     billSQL,
                                     Statement.RETURN_GENERATED_KEYS)) {

                    statement.setInt(1, actualPatientID);
                    statement.setInt(2, visitID);

                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            connection.rollback();
                            return -1;
                        }

                        billID = keys.getInt(1);
                    }
                }

                // 3. ADD THE DOCTOR'S CONSULTATION FEE
                if (consultationFee != null
                        && consultationFee.compareTo(BigDecimal.ZERO) > 0) {

                    try (PreparedStatement statement =
                                 connection.prepareStatement(itemSQL)) {

                        statement.setInt(1, billID);
                        statement.setString(2, "Consultation Fee");
                        statement.setBigDecimal(3, consultationFee);

                        statement.executeUpdate();
                    }
                }

                // 4. ADD ALL TREATMENTS RECORDED FOR THE VISIT
                try (PreparedStatement treatmentStatement =
                             connection.prepareStatement(treatmentSQL);
                     PreparedStatement itemStatement =
                             connection.prepareStatement(itemSQL)) {

                    treatmentStatement.setInt(1, visitID);

                    try (ResultSet treatments =
                                 treatmentStatement.executeQuery()) {

                        while (treatments.next()) {

                            String description =
                                    treatments.getString("TreatmentDescription");

                            BigDecimal cost =
                                    treatments.getBigDecimal("TreatmentCost");

                            if (cost == null
                                    || cost.compareTo(BigDecimal.ZERO) <= 0) {
                                continue;
                            }

                            String itemDescription =
                                    "Treatment: " + description;

                            if (itemDescription.length() > 200) {
                                itemDescription =
                                        itemDescription.substring(0, 200);
                            }

                            itemStatement.setInt(1, billID);
                            itemStatement.setString(2, itemDescription);
                            itemStatement.setBigDecimal(3, cost);

                            itemStatement.executeUpdate();
                        }
                    }
                }

                // 5. CALCULATE THE TOTAL FROM THE BILL ITEMS
                BigDecimal totalAmount;

                try (PreparedStatement statement =
                             connection.prepareStatement(totalSQL)) {

                    statement.setInt(1, billID);

                    try (ResultSet result = statement.executeQuery()) {

                        result.next();
                        totalAmount = result.getBigDecimal("Total");
                    }
                }

                // 6. UPDATE THE BILL TOTAL
                try (PreparedStatement statement =
                             connection.prepareStatement(updateTotalSQL)) {

                    statement.setBigDecimal(1, totalAmount);
                    statement.setInt(2, billID);

                    statement.executeUpdate();
                }

                // 7. SAVE EVERYTHING
                connection.commit();

                return billID;

            } catch (SQLException e) {

                connection.rollback();
                throw e;

            } finally {

                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }

        return -1;
    }

    // GET ALL BILLS
    public List<BillRecord> getAllBills() {

        List<BillRecord> bills = new ArrayList<>();

        String sql = "SELECT * FROM Bills ORDER BY BillID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Integer visitID =
                        (Integer) result.getObject("VisitID");

                bills.add(new BillRecord(
                        result.getInt("BillID"),
                        result.getInt("PatientID"),
                        visitID,
                        result.getDate("BillDate").toLocalDate(),
                        result.getBigDecimal("TotalAmount"),
                        result.getBigDecimal("AmountPaid"),
                        result.getString("PaymentStatus")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bills;
    }

    // SEARCH BILLS
    public List<BillRecord> searchBills(String keyword) {

        List<BillRecord> bills = new ArrayList<>();

        String sql = "SELECT * FROM Bills "
                   + "WHERE CAST(BillID AS VARCHAR) LIKE ? "
                   + "OR CAST(PatientID AS VARCHAR) LIKE ? "
                   + "OR CAST(VisitID AS VARCHAR) LIKE ? "
                   + "OR PaymentStatus LIKE ? "
                   + "ORDER BY BillID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            String search = "%" + keyword + "%";

            statement.setString(1, search);
            statement.setString(2, search);
            statement.setString(3, search);
            statement.setString(4, search);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Integer visitID =
                            (Integer) result.getObject("VisitID");

                    bills.add(new BillRecord(
                            result.getInt("BillID"),
                            result.getInt("PatientID"),
                            visitID,
                            result.getDate("BillDate").toLocalDate(),
                            result.getBigDecimal("TotalAmount"),
                            result.getBigDecimal("AmountPaid"),
                            result.getString("PaymentStatus")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bills;
    }

    // UPDATE BILL TOTAL
    public boolean updateBillTotal(int billID, BigDecimal totalAmount) {

        String sql = "UPDATE Bills SET TotalAmount = ? WHERE BillID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, totalAmount);
            statement.setInt(2, billID);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
