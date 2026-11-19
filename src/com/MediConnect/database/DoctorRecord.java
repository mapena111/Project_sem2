
package com.MediConnect.database;

public class DoctorRecord {

    private int doctorID;
    private String fullName;
    private String specialization;
    private String phone;
    private String email;
    private double consultationFee;

    public DoctorRecord(
            int doctorID,
            String fullName,
            String specialization,
            String phone,
            String email,
            double consultationFee) {

        this.doctorID = doctorID;
        this.fullName = fullName;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
        this.consultationFee = consultationFee;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public double getConsultationFee() {
        return consultationFee;
    }
}
