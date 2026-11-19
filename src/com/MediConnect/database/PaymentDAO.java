
package com.MediConnect.database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public boolean recordPayment(int billID, BigDecimal amount,
                                 String paymentMethod,
                                 String referenceNumber) {

        String insertSQL = "INSERT INTO Payments "
                + "(BillID, Amount, PaymentMethod, ReferenceNumber) "
                + "VALUES (?, ?, ?, ?)";

        String updateSQL = "UPDATE Bills "
                + "SET AmountPaid = ?, "
                + "PaymentStatus = CASE "
                + "WHEN ? >= TotalAmount THEN 'Paid' "
                + "WHEN ? > 0 THEN 'Partially Paid' "
                + "ELSE 'Unpaid' END "
                + "WHERE BillID = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                BigDecimal currentPaid;

                String checkSQL = "SELECT TotalAmount, AmountPaid "
                        + "FROM Bills WHERE BillID = ?";

                BigDecimal totalAmount;

                try (PreparedStatement check =
                             connection.prepareStatement(checkSQL)) {

                    check.setInt(1, billID);

                    try (ResultSet result = check.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }

                        totalAmount = result.getBigDecimal("TotalAmount");
                        currentPaid = result.getBigDecimal("AmountPaid");
                    }
                }

                BigDecimal newPaid = currentPaid.add(amount);

                if (amount.compareTo(BigDecimal.ZERO) <= 0
                        || newPaid.compareTo(totalAmount) > 0) {

                    connection.rollback();
                    return false;
                }

                try (PreparedStatement insert =
                             connection.prepareStatement(insertSQL)) {

                    insert.setInt(1, billID);
                    insert.setBigDecimal(2, amount);
                    insert.setString(3, paymentMethod);
                    insert.setString(4, referenceNumber);

                    insert.executeUpdate();
                }

                try (PreparedStatement update =
                             connection.prepareStatement(updateSQL)) {

                    update.setBigDecimal(1, newPaid);
                    update.setBigDecimal(2, newPaid);
                    update.setBigDecimal(3, newPaid);
                    update.setInt(4, billID);

                    update.executeUpdate();
                }

                connection.commit();
                return true;

            } catch (SQLException e) {
                connection.rollback();
                throw e;

            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<PaymentRecord> getPaymentsByBillID(int billID) {

        List<PaymentRecord> payments = new ArrayList<>();

        String sql = "SELECT * FROM Payments "
                + "WHERE BillID = ? ORDER BY PaymentDate DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, billID);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Timestamp timestamp = result.getTimestamp("PaymentDate");

                    payments.add(new PaymentRecord(
                            result.getInt("PaymentID"),
                            result.getInt("BillID"),
                            result.getBigDecimal("Amount"),
                            result.getString("PaymentMethod"),
                            timestamp != null ? timestamp.toLocalDateTime() : null,
                            result.getString("ReferenceNumber")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }
}
