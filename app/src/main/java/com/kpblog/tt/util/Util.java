package com.kpblog.tt.util;

import android.support.design.widget.TextInputLayout;
import android.telephony.PhoneNumberUtils;

public class Util {

    public static String getUnformattedPhoneNumber(String phoneStr) {
        return PhoneNumberUtils.normalizeNumber(phoneStr);
    }

    public static boolean isPhoneNumberValid(TextInputLayout phoneLayout, String errMsg, String unformattedPhoneNum) {
        boolean isValid = false;
        try {
            if (unformattedPhoneNum != null && unformattedPhoneNum.matches(Constants.TEN_DIGIT_REGEXP)) {
                phoneLayout.setErrorEnabled(false);
                isValid = true;
            } else {
                phoneLayout.setError(errMsg);
            }
        } catch (Exception e) {
            phoneLayout.setError(errMsg);
        }


        return isValid;
    }


}