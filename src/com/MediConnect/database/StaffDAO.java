
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateStaffDetails(
            String fullName,
            String username,
            String password) {

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (fullName.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Full name cannot exceed 100 characters.");
        }

        if (username.trim().length() > 50) {
            throw new IllegalArgumentException(
                    "Username cannot exceed 50 characters.");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }
    }


    // =====================================================
    // ADMINISTRATOR AUTHORIZATION
    // =====================================================

    private void requireAdministrator(
            Connection connection,
            int staffID) throws SQLException {

        String sql =
                "SELECT StaffID FROM Staff "
                + "WHERE StaffID = ? "
                + "AND Role = 'Administrator' "
                + "AND Status = 'Active'";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, staffID);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    throw new SecurityException(
                            "Administrator permission is required.");
                }
            }
        }
    }


    // =====================================================
    // INITIAL ADMINISTRATOR SETUP
    // Preserves compatibility with AdminSetup.java
    // =====================================================

    public boolean registerStaff(
            String fullName,
            String username,
            String password,
            String role) throws SQLException {

        validateStaffDetails(fullName, username, password);

        // This method is only for the initial administrator.
        if (!"admin".equalsIgnoreCase(username.trim())
                || !"Administrator".equalsIgnoreCase(role)) {

            throw new SecurityException(
                    "This registration method is reserved for "
                    + "initial administrator setup.");
        }

        String sql =
                "INSERT INTO Staff "
                + "(FullName, Username, PasswordHash, Role, Status) "
                + "VALUES (?, ?, ?, 'Administrator', 'Active')";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);

            connection.setAutoCommit(false);

            try {

                // Only permit setup when the Staff table is empty.
                String checkSQL =
                        "SELECT COUNT(*) FROM Staff "
                        + "WITH (UPDLOCK, HOLDLOCK)";

                try (PreparedStatement checkStatement =
                             connection.prepareStatement(checkSQL);
                     ResultSet result = checkStatement.executeQuery()) {

                    result.next();

                    if (result.getInt(1) > 0) {
                        connection.rollback();
                        return false;
                    }
                }

                String hashedPassword =
                        PasswordUtil.hashPassword(password);

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, fullName.trim());
                    statement.setString(2, username.trim());
                    statement.setString(3, hashedPassword);

                    boolean registered =
                            statement.executeUpdate() > 0;

                    connection.commit();

                    return registered;
                }

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }


    // =====================================================
    // REGISTER ADDITIONAL STAFF
    // Administrator only
    // =====================================================

    public boolean registerStaffByAdmin(
            int administratorID,
            String fullName,
            String username,
            String password) throws SQLException {

        validateStaffDetails(fullName, username, password);

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);

            connection.setAutoCommit(false);

            try {

                requireAdministrator(connection, administratorID);

                String checkSQL =
                        "SELECT StaffID FROM Staff "
                        + "WITH (UPDLOCK, HOLDLOCK) "
                        + "WHERE Username = ?";

                try (PreparedStatement checkStatement =
                             connection.prepareStatement(checkSQL)) {

                    checkStatement.setString(1, username.trim());

                    try (ResultSet result =
                                 checkStatement.executeQuery()) {

                        if (result.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                String hashedPassword =
                        PasswordUtil.hashPassword(password);

                String insertSQL =
                        "INSERT INTO Staff "
                        + "(FullName, Username, PasswordHash, Role, Status) "
                        + "VALUES (?, ?, ?, 'Staff', 'Active')";

                try (PreparedStatement statement =
                             connection.prepareStatement(insertSQL)) {

                    statement.setString(1, fullName.trim());
                    statement.setString(2, username.trim());
                    statement.setString(3, hashedPassword);

                    boolean registered =
                            statement.executeUpdate() > 0;

                    connection.commit();

                    return registered;
                }

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }


    // =====================================================
    // VIEW ALL STAFF
    // Administrator only
    // =====================================================


public List<Staff> getAllStaff(int administratorID) throws SQLException {

    String sql = "SELECT StaffID, FullName, Username, Role, Status "
               + "FROM Staff ORDER BY StaffID";

    try (Connection connection = DatabaseConnection.getConnection()) {

        requireAdministrator(connection, administratorID);

        List<Staff> staffList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                staffList.add(new Staff(
                    result.getInt("StaffID"),
                    result.getString("FullName"),
                    result.getString("Username"),
                    result.getString("Role"),
                    result.getString("Status")
                ));
            }
        }

        return staffList;
    }
}



    // =====================================================
    // UPDATE STAFF DETAILS
    // Administrator only
    // =====================================================

    public boolean updateStaffDetails(
            int administratorID,
            int targetStaffID,
            String fullName,
            String username) throws SQLException {

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Full name is required.");
        }

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Username is required.");
        }

        if (fullName.trim().length() > 100
                || username.trim().length() > 50) {

            throw new IllegalArgumentException(
                    "Full name or username exceeds the allowed length.");
        }

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);

            connection.setAutoCommit(false);

            try {

                requireAdministrator(connection, administratorID);

                String checkSQL =
                        "SELECT StaffID FROM Staff "
                        + "WITH (UPDLOCK, HOLDLOCK) "
                        + "WHERE Username = ? AND StaffID <> ?";

                try (PreparedStatement checkStatement =
                             connection.prepareStatement(checkSQL)) {

                    checkStatement.setString(1, username.trim());
                    checkStatement.setInt(2, targetStaffID);

                    try (ResultSet result =
                                 checkStatement.executeQuery()) {

                        if (result.next()) {
                            connection.rollback();
                            return false;
                        }
                    }
                }

                String sql =
                        "UPDATE Staff "
                        + "SET FullName = ?, Username = ? "
                        + "WHERE StaffID = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, fullName.trim());
                    statement.setString(2, username.trim());
                    statement.setInt(3, targetStaffID);

                    boolean updated =
                            statement.executeUpdate() > 0;

                    connection.commit();

                    return updated;
                }

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }


    // =====================================================
    // ACTIVATE OR DEACTIVATE STAFF
    // Administrator only
    // =====================================================

    public boolean setStaffStatus(
            int administratorID,
            int targetStaffID,
            boolean active) throws SQLException {

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE);

            connection.setAutoCommit(false);

            try {

                requireAdministrator(connection, administratorID);

                String roleSQL =
                        "SELECT Role FROM Staff "
                        + "WITH (UPDLOCK, HOLDLOCK) "
                        + "WHERE StaffID = ?";

                String targetRole = null;

                try (PreparedStatement statement =
                             connection.prepareStatement(roleSQL)) {

                    statement.setInt(1, targetStaffID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next()) {
                            targetRole = result.getString("Role");
                        }
                    }
                }

                if (targetRole == null) {
                    connection.rollback();
                    return false;
                }

                // Prevent deactivating the last active administrator.
                if (!active
                        && "Administrator".equalsIgnoreCase(targetRole)) {

                    String countSQL =
                            "SELECT COUNT(*) FROM Staff "
                            + "WITH (UPDLOCK, HOLDLOCK) "
                            + "WHERE Role = 'Administrator' "
                            + "AND Status = 'Active'";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(countSQL);
                         ResultSet result = statement.executeQuery()) {

                        result.next();

                        if (result.getInt(1) <= 1) {

                            connection.rollback();

                            throw new IllegalStateException(
                                    "The last active Administrator "
                                    + "cannot be deactivated.");
                        }
                    }
                }

                String sql =
                        "UPDATE Staff SET Status = ? "
                        + "WHERE StaffID = ?";

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(
                            1, active ? "Active" : "Inactive");

                    statement.setInt(2, targetStaffID);

                    boolean updated =
                            statement.executeUpdate() > 0;

                    connection.commit();

                    return updated;
                }

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }


    // =====================================================
    // CHANGE OWN PASSWORD
    // Staff members can only change their own passwords
    // =====================================================

    public boolean changeOwnPassword(
            int staffID,
            String currentPassword,
            String newPassword) throws SQLException {

        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException(
                    "Current password is required.");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "New password must contain at least 8 characters.");
        }

        String selectSQL =
                "SELECT PasswordHash, Status "
                + "FROM Staff WHERE StaffID = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                String currentHash;
                String status;

                try (PreparedStatement statement =
                             connection.prepareStatement(selectSQL)) {

                    statement.setInt(1, staffID);

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }

                        currentHash =
                                result.getString("PasswordHash");

                        status = result.getString("Status");
                    }
                }

                if (status == null
                        || !status.equalsIgnoreCase("Active")) {

                    connection.rollback();
                    return false;
                }

                if (!PasswordUtil.checkPassword(
                        currentPassword, currentHash)) {

                    connection.rollback();
                    return false;
                }

                String newHash =
                        PasswordUtil.hashPassword(newPassword);

                String updateSQL =
                        "UPDATE Staff SET PasswordHash = ? "
                        + "WHERE StaffID = ? AND Status = 'Active'";

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSQL)) {

                    statement.setString(1, newHash);
                    statement.setInt(2, staffID);

                    boolean updated =
                            statement.executeUpdate() > 0;

                    connection.commit();

                    return updated;
                }

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }


    // =====================================================
    // RETRIEVE AUTHENTICATED STAFF
    // Existing login functionality preserved
    // =====================================================

    public Staff getAuthenticatedStaff(
            String username,
            String password) throws SQLException {

        String sql =
                "SELECT StaffID, FullName, Username, "
                + "PasswordHash, Role, Status "
                + "FROM Staff WHERE Username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    String hashedPassword =
                            result.getString("PasswordHash");

                    String status =
                            result.getString("Status");

                    if (status == null
                            || !status.equalsIgnoreCase("Active")) {

                        return null;
                    }

                    if (PasswordUtil.checkPassword(
                            password, hashedPassword)) {

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


    // =====================================================
    // EXISTING LOGIN METHOD
    // =====================================================

    public boolean authenticateStaff(
            String username,
            String password) throws SQLException {

        return getAuthenticatedStaff(username, password) != null;
    }
}
