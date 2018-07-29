package com.kpblog.tt.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerPurchase {
    private Date purchaseDate;
    private int quantity, receiptNum;
    private String customerId;

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

    @Override
    public String toString(){
        String cp = "\n%s, quantity=%d, receipt=%d, date=%s";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String dateStr = sdf.format(this.purchaseDate);
        return String.format(cp, this.customerId, this.quantity, this.receiptNum, dateStr);
    }
}
