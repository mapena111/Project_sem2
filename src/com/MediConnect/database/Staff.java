
package com.MediConnect.database;

public class Staff {

    private int staffID;
    private String fullName;
    private String username;
    private String role;
    private String status;

    // Existing constructor — preserved for compatibility
    public Staff(int staffID, String fullName, String username, String role) {
        this(staffID, fullName, username, role, "Active");
    }

    // New constructor including account status
    public Staff(int staffID, String fullName, String username,
                 String role, String status) {

        this.staffID = staffID;
        this.fullName = fullName;
        this.username = username;
        this.role = role;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
