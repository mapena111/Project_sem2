
package com.MediConnect.database;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentRecord {

    private int paymentID;
    private int billID;
    private BigDecimal amount;
    private String paymentMethod;
    private LocalDateTime paymentDate;
    private String referenceNumber;

    public PaymentRecord(int paymentID, int billID,
                         BigDecimal amount, String paymentMethod,
                         LocalDateTime paymentDate, String referenceNumber) {

        this.paymentID = paymentID;
        this.billID = billID;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.referenceNumber = referenceNumber;
    }

    public int getPaymentID() {
        return paymentID;
    }

    public int getBillID() {
        return billID;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }
}
