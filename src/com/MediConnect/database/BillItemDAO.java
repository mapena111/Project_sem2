
package com.MediConnect.database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillItemDAO {

    // ADD A NEW BILL ITEM
    public boolean addBillItem(int billID, String description,
                               BigDecimal amount) {

        String sql = "INSERT INTO BillItems (BillID, Description, Amount) "
                   + "VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, billID);
            statement.setString(2, description);
            statement.setBigDecimal(3, amount);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // GET ALL ITEMS BELONGING TO A BILL
    public List<BillItemRecord> getItemsByBillID(int billID) {

        List<BillItemRecord> items = new ArrayList<>();

        String sql = "SELECT * FROM BillItems WHERE BillID = ? "
                   + "ORDER BY BillItemID";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, billID);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    items.add(new BillItemRecord(
                            result.getInt("BillItemID"),
                            result.getInt("BillID"),
                            result.getString("Description"),
                            result.getBigDecimal("Amount")
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    // CALCULATE THE TOTAL OF ALL ITEMS IN A BILL
    public BigDecimal calculateBillTotal(int billID) {

        String sql = "SELECT COALESCE(SUM(Amount), 0) AS Total "
                   + "FROM BillItems WHERE BillID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, billID);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getBigDecimal("Total");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    // CHECK WHETHER A BILL HAS ANY RECORDED PAYMENTS
    public boolean hasPayments(int billID) {

        String sql = "SELECT COUNT(*) FROM Payments WHERE BillID = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, billID);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    // EDIT AN EXISTING BILL ITEM
    public boolean updateBillItem(int billItemID,
                                  String description,
                                  BigDecimal amount) {

        if (description == null || description.trim().isEmpty()
                || description.trim().length() > 200
                || amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        String findBillSQL =
                "SELECT BillID FROM BillItems "
                + "WHERE BillItemID = ?";

        String lockBillSQL =
                "SELECT BillID FROM Bills WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE BillID = ?";

        String paymentSQL =
                "SELECT COUNT(*) FROM Payments WHERE BillID = ?";

        String updateSQL =
                "UPDATE BillItems SET Description = ?, Amount = ? "
                + "WHERE BillItemID = ? AND BillID = ?";

        String totalSQL =
                "SELECT COALESCE(SUM(Amount), 0) AS Total "
                + "FROM BillItems WHERE BillID = ?";

        String updateTotalSQL =
                "UPDATE Bills SET TotalAmount = ? WHERE BillID = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int billID;

                // FIND THE BILL ITEM
                try (PreparedStatement statement =
                             connection.prepareStatement(findBillSQL)) {

                    statement.setInt(1, billItemID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }

                        billID = result.getInt("BillID");
                    }
                }

                // LOCK THE BILL WHILE CHECKING AND UPDATING
                try (PreparedStatement statement =
                             connection.prepareStatement(lockBillSQL)) {

                    statement.setInt(1, billID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                // PREVENT EDITING A BILL WITH PAYMENTS
                try (PreparedStatement statement =
                             connection.prepareStatement(paymentSQL)) {

                    statement.setInt(1, billID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next() && result.getInt(1) > 0) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                // UPDATE THE ITEM
                try (PreparedStatement statement =
                             connection.prepareStatement(updateSQL)) {

                    statement.setString(1, description.trim());
                    statement.setBigDecimal(2, amount);
                    statement.setInt(3, billItemID);
                    statement.setInt(4, billID);

                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }

                // RECALCULATE THE BILL TOTAL
                BigDecimal newTotal;

                try (PreparedStatement statement =
                             connection.prepareStatement(totalSQL)) {

                    statement.setInt(1, billID);

                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        newTotal = result.getBigDecimal("Total");
                    }
                }

                // SAVE THE NEW BILL TOTAL
                try (PreparedStatement statement =
                             connection.prepareStatement(updateTotalSQL)) {

                    statement.setBigDecimal(1, newTotal);
                    statement.setInt(2, billID);

                    statement.executeUpdate();
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

    // DELETE A BILL ITEM
    public boolean deleteBillItem(int billItemID) {

        String findBillSQL =
                "SELECT BillID FROM BillItems WHERE BillItemID = ?";

        String lockBillSQL =
                "SELECT BillID FROM Bills WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE BillID = ?";

        String paymentSQL =
                "SELECT COUNT(*) FROM Payments WHERE BillID = ?";

        String deleteSQL =
                "DELETE FROM BillItems WHERE BillItemID = ? "
                + "AND BillID = ?";

        String totalSQL =
                "SELECT COALESCE(SUM(Amount), 0) AS Total "
                + "FROM BillItems WHERE BillID = ?";

        String updateTotalSQL =
                "UPDATE Bills SET TotalAmount = ? WHERE BillID = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                int billID;

                // FIND THE BILL
                try (PreparedStatement statement =
                             connection.prepareStatement(findBillSQL)) {

                    statement.setInt(1, billItemID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }

                        billID = result.getInt("BillID");
                    }
                }

                // LOCK THE BILL
                try (PreparedStatement statement =
                             connection.prepareStatement(lockBillSQL)) {

                    statement.setInt(1, billID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                // CHECK FOR PAYMENTS
                try (PreparedStatement statement =
                             connection.prepareStatement(paymentSQL)) {

                    statement.setInt(1, billID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next() && result.getInt(1) > 0) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                // DELETE THE ITEM
                try (PreparedStatement statement =
                             connection.prepareStatement(deleteSQL)) {

                    statement.setInt(1, billItemID);
                    statement.setInt(2, billID);

                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }

                // RECALCULATE THE BILL TOTAL
                BigDecimal newTotal;

                try (PreparedStatement statement =
                             connection.prepareStatement(totalSQL)) {

                    statement.setInt(1, billID);

                    try (ResultSet result = statement.executeQuery()) {
                        result.next();
                        newTotal = result.getBigDecimal("Total");
                    }
                }

                // UPDATE THE BILL
                try (PreparedStatement statement =
                             connection.prepareStatement(updateTotalSQL)) {

                    statement.setBigDecimal(1, newTotal);
                    statement.setInt(2, billID);

                    statement.executeUpdate();
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
}
