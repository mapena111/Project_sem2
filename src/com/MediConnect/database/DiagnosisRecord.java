
package com.MediConnect.database;

public class DiagnosisRecord {

    private int diagnosisID;
    private int visitID;
    private String diagnosisDescription;

    public DiagnosisRecord(int diagnosisID, int visitID, String diagnosisDescription) {
        this.diagnosisID = diagnosisID;
        this.visitID = visitID;
        this.diagnosisDescription = diagnosisDescription;
    }

    public int getDiagnosisID() {
        return diagnosisID;
    }

    public int getVisitID() {
        return visitID;
    }

    public String getDiagnosisDescription() {
        return diagnosisDescription;
    }
}
