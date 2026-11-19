
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffDAO {

    // REGISTER A NEW STAFF MEMBER
    public boolean registerStaff(
            String fullName,
            String username,
            String password,
            String role) throws SQLException {

        String checkSQL = "SELECT StaffID FROM Staff WHERE Username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement checkStatement =
                     connection.prepareStatement(checkSQL)) {

            checkStatement.setString(1, username);

            try (ResultSet result = checkStatement.executeQuery()) {
                if (result.next()) {
                    return false;
                }
            }

            String hashedPassword = PasswordUtil.hashPassword(password);

            String insertSQL =
                    "INSERT INTO Staff (FullName, Username, PasswordHash, Role, Status) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement insertStatement =
                    connection.prepareStatement(insertSQL)) {

                insertStatement.setString(1, fullName);
                insertStatement.setString(2, username);
                insertStatement.setString(3, hashedPassword);
                insertStatement.setString(4, role);
                insertStatement.setString(5, "Active");

                return insertStatement.executeUpdate() > 0;
            }
        }
    }


    // RETRIEVE STAFF DETAILS AFTER SUCCESSFUL LOGIN
    public Staff getAuthenticatedStaff(String username, String password)
            throws SQLException {

        String sql =
                "SELECT StaffID, FullName, Username, PasswordHash, Role, Status "
                + "FROM Staff WHERE Username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String hashedPassword =
                            result.getString("PasswordHash");

                    String status = result.getString("Status");

                    if (status == null ||
                            !status.equalsIgnoreCase("Active")) {
                        return null;
                    }

                    if (PasswordUtil.checkPassword(password, hashedPassword)) {

                        return new Staff(
                                result.getInt("StaffID"),
                                result.getString("FullName"),
                                result.getString("Username"),
                                result.getString("Role")
                        );
                    }
                }
            }
        }

        return null;
    }


    // EXISTING LOGIN METHOD
    public boolean authenticateStaff(String username, String password)
            throws SQLException {

        return getAuthenticatedStaff(username, password) != null;
    }
}
