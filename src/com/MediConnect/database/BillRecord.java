
package com.MediConnect.database;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BillRecord {

    private int billID;
    private int patientID;
    private Integer visitID;
    private LocalDate billDate;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private String paymentStatus;

    public BillRecord(int billID, int patientID, Integer visitID,
                      LocalDate billDate, BigDecimal totalAmount,
                      BigDecimal amountPaid, String paymentStatus) {

        this.billID = billID;
        this.patientID = patientID;
        this.visitID = visitID;
        this.billDate = billDate;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.paymentStatus = paymentStatus;
    }

    public int getBillID() {
        return billID;
    }

    public int getPatientID() {
        return patientID;
    }

    public Integer getVisitID() {
        return visitID;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public BigDecimal getBalance() {
        return totalAmount.subtract(amountPaid);
    }
}
