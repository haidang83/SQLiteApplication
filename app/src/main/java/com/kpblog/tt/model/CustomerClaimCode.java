package com.kpblog.tt.model;

import com.kpblog.tt.util.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerClaimCode {
    private String customerId, claimCode, promoName;
    private Date issuedDate;
    private int claimCodeType;

    public CustomerClaimCode(String customerId, String claimCode, Date issuedDate, String promoName){
        this.customerId = customerId;
        this.claimCode = claimCode;
        this.issuedDate = issuedDate;
        this.promoName = promoName;
        if (promoName != null && !promoName.isEmpty()){
            this.claimCodeType = Constants.CLAIM_CODE_TYPE_PROMOTION;
        }
        else {
            this.claimCodeType = Constants.CLAIM_CODE_TYPE_FREE_DRINK;
        }
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

    public int getClaimCodeType() {
        return claimCodeType;
    }

    @Override
    public String toString(){
        String cp = "\n%s, code=%s, issuedDate=%s";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String dateStr = sdf.format(this.issuedDate);
        return String.format(cp, this.customerId, this.claimCode, dateStr);
    }
}
