package com.kpblog.tt.util;

import android.Manifest;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    public static final int FREE_DRINK_THRESHOLD = 10;
    public static final int TODAY_CREDIT_LIMIT = 10; //number of drinks that can be purchased at 1 time (to avoid typo)
    public static final String AT_LEAST_ONE_DIGIT_REGEXP = "[0-9]+";
    public static final String NUMBER_OR_DECIMAL_REGEXP = "[0-9]+([.][0-9]{1})?";
    public static final String FOUR_DIGIT_REGEXP = "[0-9]{4}";
    public static final String TEN_DIGIT_REGEXP = "[0-9]{10}";

    public static final long BUTTON_CLICK_ELAPSE_THRESHOLD = 3000;

    public static final int SINGLE_PURCHASE_QUANTITY_LIMIT = 5;

    public static final String SHARED_PREF_ADMIN_CODE_KEY = "adminCode";
    public static final String SHARED_PREF_DAILY_CODE_KEY = "dailyCode";
    public static final String SHARED_PREF_ADMIN_CODE_EXPIRATION_KEY = "adminCodeExpiration";

    public static final String EXPORTED_FOLDER_NAME = "exportedDb";
    public static final String YYYY_MM_HH_MM_SS_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String PST_TIMEZONE = "America/Los_Angeles";
    public static final int NIGHT_ALARM_ID = 1234;
    public static final int MORNING_ALARM_ID = 2345;
    public static final String SCHEDULED_DB_BACKUP_ACTION = "SCHEDULED_DB_BACKUP";
    public static final String DB_EXPORT = "DB_EXPORT";
    public static final String SCHEDULED_TEXT_ACTION = "SCHEDULED_TEXT";

    public static long DAYS_TO_MILLIS = 24 * 60 * 60 * 1000;
    public static long FIVE_MIN_TO_MILLIS = 5 * 60 * 1000;

    public static int FIRST_PURCHASE_IMMEDIATE_REFERRAL_CREDIT = 1;
    public static double IMMEDIATE_REFERRAL_CREDIT_RATE = 0.5;
    public static double SECOND_LEVEL_REFERRAL_CREDIT_RATE = 0.25;

    public static final String CLAIM_CODE_PLACE_HOLDER = "{0}";
    public static final String PROMO_NAME_PLACE_HOLDER = "{promo}";
    public static final String INACTIVE_USER_PROMO_NAME = "20% off";
    public static final String BROADCAST_TYPE_SCHEDULED_FREE_FORM = "SCHEDULED_FREE_FORM";
    public static String BROADCAST_TYPE_SCHEDULED_CREDIT_REMINDER = "SCHEDULED_CREDIT_REMINDER";
    public static String BROADCAST_TYPE_SCHEDULED_NEW_PROMO = "SCHEDULED_PROMOTION_NEW";
    public static String BROADCAST_TYPE_SCHEDULED_PROMO_REM = "SCHEDULED_PROMOTION_REMINDER";

    public static final String STATUS_READY = "ready";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_CANCELLED = "cancelled";

    public static final String TBD = "TBD";
    public static final String NA = "N/A";

    public static final int SCHEDULED_BROADCAST_HOUR = 15;
    public static final int SCHEDULED_BROADCAST_MIN = 30;

    public static String[] PERMISSIONS_STORAGE_SMS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SEND_SMS
    };

    //for templated query
    public static final int DRINK_REMINDER_LAST_VISIT_MIN = 1;
    public static final int DRINK_REMINDER_LAST_VISIT_MAX = 60;
    public static final int DRINK_REMINDER_LAST_TEXTED_MIN = 1;
    public static final int DRINK_REMINDER_LAST_TEXTED_MAX = 60;
    public static final int DRINK_REMINDER_CREDIT_MIN = 7;
    public static final int DRINK_REMINDER_CREDIT_MAX = 10;

    public static final int INACTIVE_LAST_VISIT_MIN = DRINK_REMINDER_LAST_VISIT_MIN;
    public static final int INACTIVE_LAST_VISIT_MAX = DRINK_REMINDER_LAST_VISIT_MAX;
    public static final int INACTIVE_LAST_TEXTED_MIN = DRINK_REMINDER_LAST_TEXTED_MIN;
    public static final int INACTIVE_LAST_TEXTED_MAX = DRINK_REMINDER_LAST_TEXTED_MAX;
    public static final int INACTIVE_CREDIT_MIN = 1;
    public static final int INACTIVE_CREDIT_MAX = 6;

    public static final int CLAIM_CODE_TYPE_FREE_DRINK = 1;
    public static final int CLAIM_CODE_TYPE_PROMOTION = 2;

    public static final String DATE_FORMAT_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT_HH_MM = "HH:mm";

    public enum EXISTING_PROMO_REQUIREMENT {
        IGNORE, NEITHER_EXISTING_PROMO_NOR_FREE_DRINK, HAS_EXISTING_PROMO_ONLY, HAS_PROMO_OR_FREE_DRINK, HAS_FREE_DRINK_ONLY
    }

    public static final String BROADCAST_RECIPIENT_LIST_ID = "recipientListId";
}
