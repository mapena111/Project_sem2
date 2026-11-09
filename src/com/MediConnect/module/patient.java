
package com.MediConnect.module;

public class patient {
    private String name;
    private String id;
    private String medicalHistory;

    public patient(String name, String id, String medicalHistory) {
        this.name = name;
        this.id = id;
        this.medicalHistory = medicalHistory;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    @Override
    public String toString() {
        return "Patient ID: " + id + " | Name: " + name
                + " (History: " + medicalHistory + ")";
    }
}
