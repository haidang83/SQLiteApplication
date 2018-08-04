package com.kpblog.tt.util;

import android.support.design.widget.TextInputLayout;
import android.telephony.PhoneNumberUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Random;

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


    public static String generateRandom4DigitCode() {
        return String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * Creates the specified <code>toFile</code> as a byte for byte copy of the
     * <code>fromFile</code>. If <code>toFile</code> already exists, then it
     * will be replaced with a copy of <code>fromFile</code>. The name and path
     * of <code>toFile</code> will be that of <code>toFile</code>.<br/>
     * <br/>
     * <i> Note: <code>fromFile</code> and <code>toFile</code> will be closed by
     * this function.</i>
     *
     * @param fromFile
     *            - FileInputStream for the file to copy from.
     * @param toFile
     *            - FileInputStream for the file to copy to.
     */
    public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
        FileChannel fromChannel = null;
        FileChannel toChannel = null;
        try {
            fromChannel = fromFile.getChannel();
            toChannel = toFile.getChannel();
            fromChannel.transferTo(0, fromChannel.size(), toChannel);
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }
}