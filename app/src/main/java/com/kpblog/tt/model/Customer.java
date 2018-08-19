package com.kpblog.tt.model;

import java.util.Date;

public class Customer {
    private Date optInDate, lastVisitDate, optOutDate, lastContactedDate;
    private boolean optIn, testUser;
    private double purchaseCredit;
    private double referralCredit;
    private String customerId, referrerId;

    public double getTotalCredit() {
        return getPurchaseCredit() + getReferralCredit();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Date getOptInDate() {
        return optInDate;
    }

    public void setOptInDate(Date optInDate) {
        this.optInDate = optInDate;
    }

    public Date getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(Date lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    public boolean isOptIn() {
        return optIn;
    }

    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    public Date getOptOutDate() {
        return optOutDate;
    }

    public void setOptOutDate(Date optOutDate) {
        this.optOutDate = optOutDate;
    }

    public boolean isTestUser() {
        return testUser;
    }

    public void setTestUser(boolean testUser) {
        this.testUser = testUser;
    }

    public Date getLastContactedDate() {
        return lastContactedDate;
    }

    public void setLastContactedDate(Date lastContactedDate) {
        this.lastContactedDate = lastContactedDate;
    }

    public String getReferrerId() {
        return referrerId;
    }

    public void setReferrerId(String referrerId) {
        this.referrerId = referrerId;
    }

    public double getReferralCredit() {
        return referralCredit;
    }

    public void setReferralCredit(double referralCredit) {
        this.referralCredit = referralCredit;
    }

    public double getPurchaseCredit() {
        return purchaseCredit;
    }

    public void setPurchaseCredit(double purchaseCredit) {
        this.purchaseCredit = purchaseCredit;
    }

    @Override
    public String toString() {
        String customerFormat = "%s, %f, %s, %s, in=%s, test=%s, out=%s, lastContacted=%s, referrer=%s";
        return String.format(customerFormat, this.customerId, getTotalCredit(), this.lastVisitDate, this.optIn, this.optInDate, this.isTestUser(), this.optOutDate, this.lastContactedDate, this.referrerId);
    }
}
