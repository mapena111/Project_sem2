
package com.MediConnect.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReceiptDAO {

    private final String receiptSQL =
            "WITH PaymentBalances AS ( " +
            "    SELECT p.PaymentID, p.BillID, p.Amount, " +
            "           p.PaymentMethod, p.ReferenceNumber, " +
            "           p.PaymentDate, " +
            "           SUM(p.Amount) OVER ( " +
            "               PARTITION BY p.BillID " +
            "               ORDER BY p.PaymentDate, p.PaymentID " +
            "               ROWS UNBOUNDED PRECEDING " +
            "           ) AS CumulativePaid " +
            "    FROM Payments p " +
            ") " +
            "SELECT pb.PaymentID, pb.BillID, " +
            "       b.PatientID, pt.FullName, " +
            "       v.VisitID, b.TotalAmount, pb.Amount, " +
            "       (b.TotalAmount - pb.CumulativePaid) AS Balance, " +
            "       pb.PaymentMethod, pb.ReferenceNumber, " +
            "       b.BillDate, pb.PaymentDate " +
            "FROM PaymentBalances pb " +
            "INNER JOIN Bills b ON pb.BillID = b.BillID " +
            "INNER JOIN Patients pt ON b.PatientID = pt.PatientID " +
            "INNER JOIN Visits v ON b.VisitID = v.VisitID " +
            "                      AND v.PatientID = b.PatientID ";

    public List<ReceiptRecord> getAllReceipts() {

        List<ReceiptRecord> receipts = new ArrayList<>();

        String sql = receiptSQL +
                "ORDER BY pb.PaymentDate DESC, pb.PaymentID DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                receipts.add(createReceiptRecord(result));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return receipts;
    }

    public ReceiptRecord getReceiptByPaymentID(int paymentID) {

        String sql = receiptSQL + "WHERE pb.PaymentID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, paymentID);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return createReceiptRecord(result);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ReceiptRecord createReceiptRecord(ResultSet result)
            throws SQLException {

        BigDecimal totalBill = result.getBigDecimal("TotalAmount");
        BigDecimal amountPaid = result.getBigDecimal("Amount");
        BigDecimal balance = result.getBigDecimal("Balance");

        Date billDate = result.getDate("BillDate");
        Timestamp paymentDate = result.getTimestamp("PaymentDate");

        return new ReceiptRecord(
                result.getInt("PaymentID"),
                result.getInt("BillID"),
                result.getInt("PatientID"),
                result.getString("FullName"),
                result.getInt("VisitID"),
                totalBill,
                amountPaid,
                balance,
                result.getString("PaymentMethod"),
                result.getString("ReferenceNumber"),
                billDate != null ? billDate.toLocalDate() : null,
                paymentDate != null
                        ? paymentDate.toLocalDateTime() : null
        );
    }

    // TEMPORARY TEST METHOD
    public static void main(String[] args) {

        ReceiptDAO dao = new ReceiptDAO();

        List<ReceiptRecord> receipts = dao.getAllReceipts();

        System.out.println("Total receipts found: " + receipts.size());

        for (ReceiptRecord receipt : receipts) {

            System.out.println("-----------------------------");
            System.out.println("Payment ID: " + receipt.getPaymentID());
            System.out.println("Bill ID: " + receipt.getBillID());
            System.out.println("Patient: " + receipt.getPatientName());
            System.out.println("Patient ID: " + receipt.getPatientID());
            System.out.println("Visit ID: " + receipt.getVisitID());
            System.out.println("Amount Received: " + receipt.getAmountPaid());
            System.out.println("Payment Method: " + receipt.getPaymentMethod());
            System.out.println("Reference: " + receipt.getReferenceNumber());
            System.out.println("Remaining Balance: " + receipt.getBalance());
            System.out.println("Payment Date: " + receipt.getPaymentDate());
        }
    }
}
