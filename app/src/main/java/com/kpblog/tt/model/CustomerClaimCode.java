package com.kpblog.tt.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerClaimCode {
    private String customerId, claimCode, promoName;
    private Date issuedDate;

    public CustomerClaimCode(String customerId, String claimCode, Date issuedDate, String promoName){
        this.customerId = customerId;
        this.claimCode = claimCode;
        this.issuedDate = issuedDate;
        this.promoName = promoName;
    }

    public CustomerClaimCode(String customerId, String claimCode, Date issuedDate){
       this(customerId, claimCode, issuedDate, "");
    }

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

    public String getPromoName() {
        return promoName;
    }

    @Override
    public String toString(){
        String cp = "\n%s, code=%s, issuedDate=%s";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String dateStr = sdf.format(this.issuedDate);
        return String.format(cp, this.customerId, this.claimCode, dateStr);
    }
}
