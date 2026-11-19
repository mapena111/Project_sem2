
package com.MediConnect.database;

import java.math.BigDecimal;

public class BillItemRecord {

    private int billItemID;
    private int billID;
    private String description;
    private BigDecimal amount;

    public BillItemRecord(int billItemID, int billID,
                          String description, BigDecimal amount) {

        this.billItemID = billItemID;
        this.billID = billID;
        this.description = description;
        this.amount = amount;
    }

    public int getBillItemID() {
        return billItemID;
    }

    public int getBillID() {
        return billID;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
