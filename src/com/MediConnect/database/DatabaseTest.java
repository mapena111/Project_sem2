
package com.MediConnect.database;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTest {

    public static void main(String[] args) {

        try (Connection connection = DatabaseConnection.getConnection()) {

            if (connection != null) {
                System.out.println("SUCCESS!");
                System.out.println("Connected to MediConnectDB!");
            }

        } catch (SQLException e) {

            System.out.println("DATABASE CONNECTION FAILED!");
            e.printStackTrace();

        }
    }
}
