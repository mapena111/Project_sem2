
package com.MediConnect.database;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Hash a password before storing it in the database
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Verify a password entered during login
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
