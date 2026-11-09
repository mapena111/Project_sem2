
package com.MediConnect.module;

public class appointment {
    private doctor doctor;
    private patient patient;
    private String date;
    private String time;
    private String status;

    public appointment(doctor doctor, patient patient,
                       String date, String time, String status) {
        this.doctor = doctor;
        this.patient = patient;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public doctor getDoctor() {
        return doctor;
    }

    public patient getPatient() {
        return patient;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Patient: " + patient.getName()
                + " | Doctor: " + doctor.getName()
                + " | Date: " + date
                + " | Time: " + time
                + " | Status: " + status;
    }
}
