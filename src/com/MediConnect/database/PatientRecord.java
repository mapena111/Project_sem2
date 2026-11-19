
package com.MediConnect.database;

import java.sql.Date;

public class PatientRecord {

    private int patientID;
    private String fullName;
    private String gender;
    private Date dateOfBirth;
    private String phone;
    private String email;
    private String address;
    private Date registrationDate;

    public PatientRecord(int patientID, String fullName,
            String gender, Date dateOfBirth, String phone,
            String email, String address, Date registrationDate) {

        this.patientID = patientID;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.registrationDate = registrationDate;
    }

    public int getPatientID() {
        return patientID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGender() {
        return gender;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }
}
