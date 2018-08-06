package com.kpblog.tt.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.TextInputLayout;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.kpblog.tt.receiver.TraTemptationReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

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


    public static String exportDatabase(String sourceDbName) {
        String exportedDbPath = "";

        final File documentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File exportedFolder = new File (documentPath, Constants.EXPORTED_FOLDER_NAME);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        String fileName = sdf.format(new Date()) + ".db";
        File dest = new File(exportedFolder, fileName);

        FileOutputStream output = null;
        FileInputStream fis = null;
        try {
            if (!exportedFolder.exists()){
                exportedFolder.mkdir();
            }

            File dbFile = new File(sourceDbName);
            fis = new FileInputStream(dbFile);

            // Open the empty db as the output stream
            output = new FileOutputStream(dest);

            copyFile(fis, output);

            exportedDbPath = String.format("%s/%s/%s", documentPath.getName(), Constants.EXPORTED_FOLDER_NAME, fileName);
        } catch (Exception e){
            Log.e("Database backup", e.getMessage());
        } finally {
            try {

                // Close the streams
                if (output != null){
                    output.flush();
                    output.close();
                }

                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e){
                Log.e("Database backup", e.getMessage());
            }
        }
        return exportedDbPath;
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

    public static void setRecurringAlarm(Context context) {
        Intent receiverIntent = new Intent(context, TraTemptationReceiver.class);
        receiverIntent.setAction(Constants.DB_BACKUP_ACTION);


        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeZone(TimeZone.getTimeZone(Constants.PST_TIMEZONE));
        alarmTime.set(Calendar.HOUR_OF_DAY, 23);
        alarmTime.set(Calendar.MINUTE, 30);

        final long now = Calendar.getInstance(TimeZone.getTimeZone(Constants.PST_TIMEZONE)).getTimeInMillis();
        if (now > alarmTime.getTimeInMillis()){
            //current time is already later than the alarm time, set for next day; because if we set now, the task will run immediately
            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        //https://stackoverflow.com/questions/10930034/is-there-any-way-to-check-if-an-alarm-is-already-set/11411073
        //use same id to overwrite the old one
        PendingIntent recurringIntent = PendingIntent.getBroadcast(context,
                Constants.ALARM_ID, receiverIntent, 0);

        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //set alarm in 10 sec to test
        /*alarms.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        10 * 1000, recurringIntent);*/

        alarms.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, recurringIntent);
    }
}