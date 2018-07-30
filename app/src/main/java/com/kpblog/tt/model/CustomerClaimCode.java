package com.kpblog.tt.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerClaimCode {
    private String customerId, claimCode;
    private Date issuedDate;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getClaimCode() {
        return claimCode;
    }

    public void setClaimCode(String claimCode) {
        this.claimCode = claimCode;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    @Override
    public String toString(){
        String cp = "\n%s, code=%s, issuedDate=%s";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String dateStr = sdf.format(this.issuedDate);
        return String.format(cp, this.customerId, this.claimCode, dateStr);
    }
}
