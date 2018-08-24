package com.kpblog.tt.model;

import java.util.List;

public class CustomerBroadcast {
    private long timestamp;
    private int recipientListId;
    private String message, type, promoName;
    private List<String> recipientPhoneNumbers;

    public CustomerBroadcast(long timestamp, int recipientListId, String message, String type, String promoName){
        this.timestamp = timestamp;
        this.recipientListId = recipientListId;
        this.message = message;
        this.type = type;
        this.promoName = promoName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getRecipientListId() {
        return recipientListId;
    }

    public void setRecipientListId(int recipientListId) {
        this.recipientListId = recipientListId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPromoName() {
        return promoName;
    }

    public void setPromoName(String promoName) {
        this.promoName = promoName;
    }

    public List<String> getRecipientPhoneNumbers() {
        return recipientPhoneNumbers;
    }

    public void setRecipientPhoneNumbers(List<String> recipientPhoneNumbers) {
        this.recipientPhoneNumbers = recipientPhoneNumbers;
    }
}
