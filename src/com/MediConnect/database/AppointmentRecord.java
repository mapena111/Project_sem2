
package com.MediConnect.database;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentRecord {

    private int appointmentID;
    private int patientID;
    private int doctorID;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;

    public AppointmentRecord(int appointmentID, int patientID,
            int doctorID, LocalDate appointmentDate,
            LocalTime appointmentTime, String status) {

        this.appointmentID = appointmentID;
        this.patientID = patientID;
        this.doctorID = doctorID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public int getAppointmentID() {
        return appointmentID;
    }

    public int getPatientID() {
        return patientID;
    }

    public int getDoctorID() {
        return doctorID;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public String getStatus() {
        return status;
    }
}
