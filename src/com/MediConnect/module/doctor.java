package com.MediConnect.module;

public class doctor {
    private String name;
    private String specialty;
    private String availability;

    public doctor(String name, String specialty, String availability) {
        this.name = name;
        this.specialty = specialty;
        this.availability = availability;
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getAvailability() {
        return availability;
    }

    @Override
    public String toString() {
        return name + " - " + specialty + " (" + availability + ")";
    }
}