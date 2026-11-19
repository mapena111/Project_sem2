package com.MediConnect.database;

public class PatientHistoryRecord {

    private int patientID;
    private String patientName;
    private int visitID;
    private String visitDate;
    private String doctorName;
    private String diagnosis;
    private String treatment;
    private double treatmentCost;
    private double billAmount;
    private String paymentStatus;

    public PatientHistoryRecord(
            int patientID,
            String patientName,
            int visitID,
            String visitDate,
            String doctorName,
            String diagnosis,
            String treatment,
            double treatmentCost,
            double billAmount,
            String paymentStatus) {

        this.patientID = patientID;
        this.patientName = patientName;
        this.visitID = visitID;
        this.visitDate = visitDate;
        this.doctorName = doctorName;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.treatmentCost = treatmentCost;
        this.billAmount = billAmount;
        this.paymentStatus = paymentStatus;
    }

    public int getPatientID() { return patientID; }
    public String getPatientName() { return patientName; }
    public int getVisitID() { return visitID; }
    public String getVisitDate() { return visitDate; }
    public String getDoctorName() { return doctorName; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatment() { return treatment; }
    public double getTreatmentCost() { return treatmentCost; }
    public double getBillAmount() { return billAmount; }
    public String getPaymentStatus() { return paymentStatus; }
}