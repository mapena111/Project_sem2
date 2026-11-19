
package com.MediConnect.database;

public class Staff {

    private int staffID;
    private String fullName;
    private String username;
    private String role;

    public Staff(int staffID, String fullName, String username, String role) {
        this.staffID = staffID;
        this.fullName = fullName;
        this.username = username;
        this.role = role;
    }

    public int getStaffID() {
        return staffID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
