package com.kpblog.tt.model;

import com.kpblog.tt.util.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerPurchase {
    private Date purchaseDate;
    private int quantity, receiptNum;
    private String customerId, notes;

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getReceiptNum() {
        return receiptNum;
    }

    public void setReceiptNum(int receiptNum) {
        this.receiptNum = receiptNum;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString(){
        String cp = "\n%s, quantity=%d, receipt=%d, date=%s, %s";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.YYYY_MM_HH_MM_SS_FORMAT);
        final String dateStr = sdf.format(this.purchaseDate);
        return String.format(cp, this.customerId, this.quantity, this.receiptNum, dateStr, this.notes);
    }
}
