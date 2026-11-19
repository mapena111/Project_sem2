
package com.MediConnect.database;

import java.time.LocalDateTime;

public class VisitRecord {

    private int visitID;
    private int patientID;
    private int doctorID;
    private Integer appointmentID;
    private LocalDateTime visitDate;
    private String notes;

    public VisitRecord(int visitID, int patientID, int doctorID,
            Integer appointmentID, LocalDateTime visitDate,
            String notes) {

        this.visitID = visitID;
        this.patientID = patientID;
        this.doctorID = doctorID;
        this.appointmentID = appointmentID;
        this.visitDate = visitDate;
        this.notes = notes;
    }

    public int getVisitID() {
        return visitID;
    }

    public int getPatientID() {
        return patientID;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public Integer getAppointmentID() {
        return appointmentID;
    }

    public LocalDateTime getVisitDate() {
        return visitDate;
    }

    public String getNotes() {
        return notes;
    }
}
