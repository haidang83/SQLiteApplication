package com.kpblog.tt.util;

import android.Manifest;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static final int FREE_DRINK_THRESHOLD = 10;
    public static final int TODAY_CREDIT_LIMIT = 10; //number of drinks that can be purchased at 1 time (to avoid typo)
    public static final String AT_LEAST_ONE_DIGIT_REGEXP = "[0-9]+";
    public static final String FOUR_DIGIT_REGEXP = "[0-9]{4}";
    public static final String TEN_DIGIT_REGEXP = "[0-9]{10}";

    public static final long BUTTON_CLICK_ELAPSE_THRESHOLD = 3000;

    public static final int SINGLE_PURCHASE_QUANTITY_LIMIT = 5;

    public static final String SHARED_PREF_ADMIN_CODE_KEY = "adminCode";
    public static final String DAILY_CODE_SHARED_PREF_KEY = "dailyCode";

    public static final String EXPORTED_FOLDER_NAME = "exportedDb";
    public static final String YYYY_MM_HH_MM_SS_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String PST_TIMEZONE = "America/Los_Angeles";
    public static final int NIGHT_ALARM_ID = 1234;
    public static final int MORNING_ALARM_ID = 2345;
    public static final String DB_BACKUP_ACTION = "DB_BACKUP";

    public static long DAYS_TO_MILLIS = 24 * 60 * 60 * 1000;

    public static String[] PERMISSIONS_STORAGE_SMS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS
    };
}
