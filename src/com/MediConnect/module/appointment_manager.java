package com.MediConnect.module;

import java.util.ArrayList;
import java.util.List;

public class appointment_manager {
    private List<doctor> doctors;
    private List<patient> patients;
    private List<appointment> appointments;

    public appointment_manager() {
        this.doctors = new ArrayList<>();
        this.patients = new ArrayList<>();
        this.appointments = new ArrayList<>();
    }

    public void addDoctor(doctor doctor) {
        doctors.add(doctor);
    }

    public void addPatient(patient patient) {
        patients.add(patient);
    }

    public void scheduleAppointment(appointment appointment) {
        appointments.add(appointment);
    }

    public List<doctor> getDoctors() {
        return doctors;
    }

    public List<patient> getPatients() {
        return patients;
    }

    public List<appointment> getAppointments() {
        return appointments;
    }
}