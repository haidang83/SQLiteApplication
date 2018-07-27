package com.kpblog.sqliteapplication.model;

import java.sql.Date;

public class Customer {
    private Date optInDate, lastVisitDate, optOutDate;
    private boolean optIn, testUser;
    private int totalCredit;
    private String customerId;

    public int getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(int totalCredit) {
        this.totalCredit = totalCredit;
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

    @Override
    public String toString() {
        String customerFormat = "%s, %d, %s, %s, in=%s, test=%s, out=%s";
        return String.format(customerFormat, this.customerId, this.totalCredit, this.lastVisitDate, this.optIn, this.optInDate, this.isTestUser(), this.optOutDate);
    }
}
