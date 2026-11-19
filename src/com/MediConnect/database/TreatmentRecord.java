
package com.MediConnect.database;

import java.math.BigDecimal;

public class TreatmentRecord {

    private int treatmentID;
    private int visitID;
    private String treatmentDescription;
    private BigDecimal treatmentCost;

    public TreatmentRecord(
            int treatmentID,
            int visitID,
            String treatmentDescription,
            BigDecimal treatmentCost) {

        this.treatmentID = treatmentID;
        this.visitID = visitID;
        this.treatmentDescription = treatmentDescription;
        this.treatmentCost = treatmentCost;
    }

    public int getTreatmentID() {
        return treatmentID;
    }

    public int getVisitID() {
        return visitID;
    }

    public String getTreatmentDescription() {
        return treatmentDescription;
    }

    public BigDecimal getTreatmentCost() {
        return treatmentCost;
    }
}
