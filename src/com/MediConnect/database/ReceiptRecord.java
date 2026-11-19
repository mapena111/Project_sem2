
package com.MediConnect.database;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReceiptRecord {

    private int paymentID;
    private int billID;
    private int patientID;
    private String patientName;
    private int visitID;

    private BigDecimal totalBill;
    private BigDecimal amountPaid;
    private BigDecimal balance;

    private String paymentMethod;
    private String referenceNumber;

    private LocalDate billDate;
    private LocalDateTime paymentDate;

    public ReceiptRecord(int paymentID, int billID,
                         int patientID, String patientName,
                         int visitID, BigDecimal totalBill,
                         BigDecimal amountPaid, BigDecimal balance,
                         String paymentMethod, String referenceNumber,
                         LocalDate billDate, LocalDateTime paymentDate) {

        this.paymentID = paymentID;
        this.billID = billID;
        this.patientID = patientID;
        this.patientName = patientName;
        this.visitID = visitID;
        this.totalBill = totalBill;
        this.amountPaid = amountPaid;
        this.balance = balance;
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
        this.billDate = billDate;
        this.paymentDate = paymentDate;
    }

    public int getPaymentID() {
        return paymentID;
    }

    public int getBillID() {
        return billID;
    }

    public int getPatientID() {
        return patientID;
    }

    public String getPatientName() {
        return patientName;
    }

    public int getVisitID() {
        return visitID;
    }

    public BigDecimal getTotalBill() {
        return totalBill;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
}
