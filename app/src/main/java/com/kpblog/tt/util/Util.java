package com.kpblog.tt.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;
import com.kpblog.tt.R;
import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.model.Customer;
import com.kpblog.tt.model.CustomerBroadcast;
import com.kpblog.tt.model.CustomerClaimCode;
import com.kpblog.tt.receiver.TraTemptationReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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


    public static String exportDatabase(Context ctx) {
        File exportedFile = exportDatabaseAsFile(ctx);

        //test code
        //uploadToServer(ctx, exportedFile);

        String[] fileNameParts = exportedFile.getAbsolutePath().split(File.separator);
        String fileName = exportedFile.getName();
        final int length = fileNameParts.length;
        if (length > 3){
            fileName = fileNameParts[length - 3] + File.separator + fileNameParts[length - 2] + File.separator + fileNameParts[length - 1];
        }
        return fileName;
    }

    public static File exportDatabaseAsFile(Context ctx) {
        final String sourceDbName = ctx.getDatabasePath(DatabaseHandler.DATABASE_NAME).getPath();
        final File documentPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File exportedFolder = new File (documentPath, Constants.EXPORTED_FOLDER_NAME);

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.BACKUP_DATE_FORMAT_YYYY_MM_DD_HHMMSS);
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
        return dest;
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

    public static Calendar getScheduledTimeForHourMin(int scheduleHour, int scheduledMin) {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, scheduleHour);
        alarmTime.set(Calendar.MINUTE, scheduledMin);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        final long now = System.currentTimeMillis();
        if (now >= alarmTime.getTimeInMillis()){
            //current time is already later than the alarm time, set for next day
            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        return alarmTime;
    }

    public static Calendar getNightlyDbBackupTime() {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, 9);
        alarmTime.set(Calendar.MINUTE, 30);
        alarmTime.set(Calendar.SECOND, 0);

        final long now = System.currentTimeMillis();
        if (now >= alarmTime.getTimeInMillis()){
            //current time is already later than the alarm time, set for next day; because if we set now, the task will run immediately
            alarmTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        return alarmTime;
    }

    public static void setNextDbBackupAlarm(Context context, Calendar alarmTime) {
        Intent receiverIntent = new Intent(context, TraTemptationReceiver.class);
        receiverIntent.setAction(Constants.SCHEDULED_DB_BACKUP_ACTION);

        //https://stackoverflow.com/questions/10930034/is-there-any-way-to-check-if-an-alarm-is-already-set/11411073
        //use same id to overwrite the old one
        PendingIntent dbBackupPendingIntent = PendingIntent.getBroadcast(context,
                Constants.NIGHT_ALARM_ID, receiverIntent, 0);

        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //set alarm in 10 sec to test
        /*alarms.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        10 * 1000, recurringIntent);*/

        alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), dbBackupPendingIntent);
        Log.d("Util", "db backup scheduled for " + new SimpleDateFormat(Constants.YYYY_MM_HH_MM_SS_FORMAT).format(alarmTime.getTime()));
    }

    public static void setAlarmForScheduledJob(Context ctx, Calendar alarmTime, int id){

        PendingIntent scheduleJobIntent = getPendingIntentForScheduledJob(ctx, id);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), scheduleJobIntent);
    }

    public static void cancelAlarmForScheduledJob(Context ctx, int id){
        PendingIntent scheduleJobIntent = getPendingIntentForScheduledJob(ctx, id);
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(scheduleJobIntent);
    }

    private static PendingIntent getPendingIntentForScheduledJob(Context ctx, int id) {
        Intent intent = new Intent(ctx, TraTemptationReceiver.class);
        intent.putExtra(Constants.BROADCAST_RECIPIENT_LIST_ID, id);
        intent.setAction(Constants.SCHEDULED_TEXT_ACTION);
        return PendingIntent.getBroadcast(ctx, id, intent, 0);
    }


    /**
     * create this method because the PhoneUtils returns a different format (xxx) xxx-xxxx
     * xxx-xxx-xxxx
     * @param customerId
     * @return formatted customerId
     */
    public static String formatPhoneNumber(String customerId) {
        String areaCode = customerId.substring(0, 3);
        String line = customerId.substring(3, 6);
        String number = customerId.substring(6,10);

        return areaCode + "-" + line + "-" + number;
    }

    public static void textDailyCode(Context ctx) {
        DatabaseHandler handler = new DatabaseHandler(ctx);
        List<String> phonesToText = handler.getAllAdmins();
        final String dailyCode = Util.generateRandom4DigitCode();
        String message = String.format("Daily cashier code: %s", dailyCode);

        textMultipleRecipients(phonesToText, message);

        //save into shared pref
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.SHARED_PREF_DAILY_CODE_KEY, dailyCode);
        editor.commit();
    }

    public static List<String> textPromoToMultipleRecipientsAndUpdateLastTexted(List<String> recipients, String message,
                                                                        DatabaseHandler handler, boolean updateLastTexted,
                                                                        String promoName) {

        try {

            Date today = new Date();

            if (promoName != null && !promoName.isEmpty()){
                message = message.replace(Constants.PROMO_NAME_PLACE_HOLDER, promoName);
            }

            for (String phone : recipients){
                //text and update database 1 by 1 so that there's some time gap between text
                //dont want carrier to block as spam

                String messageWithCode = message;
                if(message.contains(Constants.CLAIM_CODE_PLACE_HOLDER)){
                    //need to fill in the claim code and save it to customerClaim table with the promo name
                    final String claimCode = Util.generateRandom4DigitCode();
                    messageWithCode = MessageFormat.format(message, claimCode);
                    CustomerClaimCode cc = new CustomerClaimCode(phone, claimCode, today, promoName);
                    handler.insertOrUpdateCustomerClaimCode(cc);
                }
                textSingleRecipient(phone, messageWithCode);
                if (updateLastTexted){
                    handler.updateLastTexted(phone, today.getTime());
                }

            }
        } catch (Exception e){
            //catch so it continues with other steps
            Log.e("Util", "error:", e.getCause());
        }

        return recipients;
    }

    public static String replaceClaimCodePlaceHolderType(String msg) {
        return msg.replace("%s", Constants.CLAIM_CODE_PLACE_HOLDER);
    }

    public static void textMultipleRecipients(List<String> recipients, String message){
        textPromoToMultipleRecipientsAndUpdateLastTexted(recipients, message, null, false, "");
    }

    public static void textSingleRecipient(String phone, String msg) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phone, null, msg, null, null);
    }

    public static int getMissingCreditRoundedUp(double totalCredit) {
        return (int) Math.ceil(Constants.FREE_DRINK_THRESHOLD - totalCredit);
    }

    public static void sendAdminCodeAndSaveToSharedPref(String targetPhoneNum, Context ctx, String messageFormat) {
        String code = Util.generateRandom4DigitCode();
        String msg = String.format(messageFormat, code);
        Util.textSingleRecipient(targetPhoneNum, msg);

        //save into shared pref
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.SHARED_PREF_ADMIN_CODE_KEY, code);
        editor.apply();
    }

    /**
     * after successful unlock, clear out the admin code and the user can have 5-min of not having to enter it again
     * @param sp
     */
    public static void clearAdminCodeAndSetExpirationTime(SharedPreferences sp) {
        //clear sharedpref code
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Constants.SHARED_PREF_ADMIN_CODE_KEY);
        editor.putLong(Constants.SHARED_PREF_ADMIN_CODE_EXPIRATION_KEY, System.currentTimeMillis() + Constants.FIVE_MIN_TO_MILLIS);
        editor.apply();
    }

    public static void expireAdminCode(Context ctx){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Constants.SHARED_PREF_ADMIN_CODE_EXPIRATION_KEY);
        editor.apply();
    }

    public static boolean isAdminCodeRequired(Context ctx){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        long expirationTime = sp.getLong(Constants.SHARED_PREF_ADMIN_CODE_EXPIRATION_KEY, 0);

        return (System.currentTimeMillis() > expirationTime);
    }

    public static void displayToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    public static boolean isCashierCodeValid(String inputCode, Context ctx, TextInputLayout cashierLayout, String errMsg) {
        boolean isValid = false;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        String expectedCode = sp.getString(Constants.SHARED_PREF_DAILY_CODE_KEY, null);

        if (!inputCode.isEmpty() && inputCode.equals(expectedCode)){
            isValid = true;
            cashierLayout.setErrorEnabled(false);
        }
        else {
            cashierLayout.setError(errMsg);
        }

        return isValid;
    }

    //synchronized because if multiple jobs run at the same time, there's a chance that a customer might be in more than 1 job
    public static synchronized void sendScheduledBroadcastByBroadcastId(Context ctx, DatabaseHandler handler, int broadcastId) {
        StringBuffer result = new StringBuffer("Running scheduled broadcast: ");
        CustomerBroadcast cb = handler.getCustomerBroadcastById(broadcastId);
        if (cb == null){
            result.append(MessageFormat.format("CustomerBroadcast = null for id={0}", broadcastId));
        }
        else if (cb.getStatus().equals(Constants.STATUS_READY)){


            List<String> recipientSent = new ArrayList<String>();

            if (Constants.BROADCAST_TYPE_SCHEDULED_FREE_FORM.equals(cb.getType())){
                //for free-form, can use the customer list from the db to text
                recipientSent = Util.textPromoToMultipleRecipientsAndUpdateLastTexted(cb.getRecipientPhoneNumbers(),
                        cb.getMessage(), handler, true, cb.getPromoName());
            }
            else {
                //for other type, need to look up the customer list.
                //need to do this so that we text based on the latest customer's data

                if (Constants.BROADCAST_TYPE_SCHEDULED_CREDIT_REMINDER.equals(cb.getType())){
                    recipientSent = broadcastCreditReminder(handler, cb);
                }
                else if (Constants.BROADCAST_TYPE_SCHEDULED_NEW_PROMO.equals(cb.getType())){
                    recipientSent = broadcastInactiveWithNewPromo(handler, cb);
                }
                else if (Constants.BROADCAST_TYPE_SCHEDULED_PROMO_REM.equals(cb.getType())){
                    recipientSent = broadcastInactiveOldPromoReminder(handler, cb);
                }

                //save the phone numbers that we texted into the recipientList
                if (recipientSent.size() > 0){
                    handler.massInsertPhoneNumbersIntoRecipientListId(cb.getRecipientListId(), recipientSent);
                }
            }

            handler.updateBroadcastStatusById(cb.getRecipientListId(), Constants.STATUS_SENT);

            result.append(getBroadcastTypeShortName(cb) + "->" + recipientSent.size());
        }
        else {
            result.append(MessageFormat.format("CustomerBroadcastId={0}, type={1} has status={2}. Should not be processed by scheduled job.", broadcastId, cb.getType(), cb.getStatus()));
        }

        List<String> admins = handler.getAllAdmins();
        String textMsg = result.toString();
        Util.textMultipleRecipients(admins, textMsg);
    }

    private static List<String> broadcastInactiveOldPromoReminder(DatabaseHandler handler, CustomerBroadcast cb) {
        List<String> recipientSent = new ArrayList<String>();

        try {

            //2. get the inactive ones already having promo code & send reminder
            List<CustomerClaimCode> claimCodeList = handler.getCustomerClaimCodeWithPromoForInactiveUsers();
            recipientSent = Util.textPromoReminder(claimCodeList, cb.getMessage(), true, handler);
        } catch (Exception e){

        }
        return recipientSent;
    }

    private static List<String> broadcastInactiveWithNewPromo(DatabaseHandler handler, CustomerBroadcast cb) {
        List<String> recipientSent = new ArrayList<String>();

        try {
            //1. get the inactive customer without promo & send the promo code
            List<String> phoneList = handler.getInactiveCustomerPhoneNumbersWithoutPromo();

            recipientSent = Util.textPromoToMultipleRecipientsAndUpdateLastTexted(phoneList,
                    cb.getMessage(), handler, true, cb.getPromoName());

        } catch (Exception e){

        }

        return recipientSent;
    }

    private static List<String> broadcastCreditReminder(DatabaseHandler handler, CustomerBroadcast cb) {
        List<String> recipientSent = new ArrayList<String>();

        try {
            List<Customer> customers = handler.getCustomersForCreditReminder();

            recipientSent = Util.textCreditReminder(customers, cb.getMessage(), handler, true);
        } catch (Exception e){

        }
        return recipientSent;
    }

    private static List<String> textCreditReminder(List<Customer> customers, String message,
                                              DatabaseHandler handler, boolean updateLastTexted) {

        List<String> recipientSent = new ArrayList<String>();
        long timestamp = System.currentTimeMillis();

        try {

            for (Customer c : customers){
                String textMsg = String.format(message, c.getTotalCredit(), Constants.FREE_DRINK_THRESHOLD - c.getTotalCredit());
                Util.textSingleRecipient(c.getCustomerId(), textMsg);

                if (updateLastTexted){
                    handler.updateLastTexted(c.getCustomerId(), timestamp);
                }

                recipientSent.add(c.getCustomerId());
            }
        } catch (Exception e){
            //catch so it doesnt block other steps
        }

        return recipientSent;
    }

    private static List<String> textPromoReminder(List<CustomerClaimCode> claimCodeList, String message,
                                             boolean updateLastTexted, DatabaseHandler handler) {

        long timestamp = System.currentTimeMillis();
        List<String> recipientSent = new ArrayList<String>();

        try {

            for (CustomerClaimCode ccc : claimCodeList){

                String messageWithPromoName = message.replace(Constants.PROMO_NAME_PLACE_HOLDER, ccc.getPromoName());
                String textMsg = MessageFormat.format(messageWithPromoName, ccc.getClaimCode());
                Util.textSingleRecipient(ccc.getCustomerId(), textMsg);
                recipientSent.add(ccc.getCustomerId());

                if (updateLastTexted){
                    handler.updateLastTexted(ccc.getCustomerId(), timestamp);
                }
            }
        } catch (Exception e){
            //catch so it doesn't affect other step
        }

        return recipientSent;
    }

    public static String getBroadcastTypeShortName(CustomerBroadcast customerBroadcast) {
        return customerBroadcast.getType().replace("SCHEDULED_", "");
    }

    public static void scheduleDailyBroadcast(Context context) {
        DatabaseHandler handler = new DatabaseHandler(context);

        Calendar scheduledTime = getScheduledTimeForHourMin(Constants.SCHEDULED_BROADCAST_HOUR, Constants.SCHEDULED_BROADCAST_MIN);

        final Resources resources = context.getResources();
        String creditReminderMsg = resources.getString(R.string.drinkCreditReminderMessage);
        int creditReminderBroadcastId = handler.insertIntoCustomerBroadcastTable(scheduledTime.getTimeInMillis(), creditReminderMsg, Constants.BROADCAST_TYPE_SCHEDULED_CREDIT_REMINDER, "", null);
        //need to set the alarm to send the message
        Util.setAlarmForScheduledJob(context, scheduledTime, creditReminderBroadcastId);

        String newPromoMsg = Util.replaceClaimCodePlaceHolderType(resources.getString(R.string.inactiveUser_sendPromo));
        String promoName = Constants.INACTIVE_USER_PROMO_NAME;
        int newPromoBroadcastId = handler.insertIntoCustomerBroadcastTable(scheduledTime.getTimeInMillis(), newPromoMsg, Constants.BROADCAST_TYPE_SCHEDULED_NEW_PROMO, promoName, null);
        Util.setAlarmForScheduledJob(context, scheduledTime, newPromoBroadcastId);

        String promoReminderMsg = Util.replaceClaimCodePlaceHolderType(resources.getString(R.string.inactiveUser_promoReminder));
        int promoReminderBroadcastId = handler.insertIntoCustomerBroadcastTable(scheduledTime.getTimeInMillis(), promoReminderMsg, Constants.BROADCAST_TYPE_SCHEDULED_PROMO_REM, "", null);
        Util.setAlarmForScheduledJob(context, scheduledTime, promoReminderBroadcastId);
    }

    public static void uploadToServer(Context ctx, File file) {
        try {
            final Resources resources = ctx.getResources();
            String accessToken = resources.getString(R.string.dbAccessToken);

            DbxRequestConfig config = DbxRequestConfig.newBuilder("traTemptation").build();
            DbxClientV2 client = new DbxClientV2(config, accessToken);

            uploadFile(client, file);

            synchLocalAndRemoteFolder(file.getParentFile(), client);
        } catch (DbxException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void synchLocalAndRemoteFolder(File localFolder, DbxClientV2 dropboxClient) throws DbxException, IOException {
        long deleteTimeCutOff = System.currentTimeMillis() - (Constants.DAYS_TO_KEEP_DB_BACKUP * Constants.DAYS_TO_MILLIS);

        List<String> remoteFilesToKeep = new ArrayList<String>();
        List<String> remoteFilesToDelete = new ArrayList<String>();
        ListFolderResult listFolderResult = dropboxClient.files().listFolder("");
        for (Metadata metadata : listFolderResult.getEntries()) {
            String name = metadata.getName();
            if (name.endsWith(".db")) {
                if (isFileUpForDeletion(name, deleteTimeCutOff)){
                    remoteFilesToDelete.add(name);
                }
                else {
                    remoteFilesToKeep.add(name);
                }
            }
        }

        File[] localFiles = localFolder.listFiles();
        List<File> localFilesToUpload = new ArrayList<File>();
        List<File> localFilesToDelete = new ArrayList<File>();
        for (File localFile : localFiles){
            if (Util.isFileUpForDeletion(localFile.getName(), deleteTimeCutOff)){
                localFilesToDelete.add(localFile);
            }
            else if (!remoteFilesToKeep.contains(localFile.getName())){
                //local file not uploaded, add to list to upload
                localFilesToUpload.add(localFile);
            }
        }


        Util.deleteLocalFiles(localFilesToDelete);
        uploadFile(localFilesToUpload, dropboxClient);
        deleteRemoteFiles(remoteFilesToDelete, dropboxClient);
    }

    private static void deleteRemoteFiles(List<String> remoteFilesToDelete, DbxClientV2 dropboxClient) {
        try {
            for (String remoteFile : remoteFilesToDelete){
                dropboxClient.files().deleteV2(Constants.DROPBOX_ROOT + remoteFile);
            }
        } catch (DbxException e) {
            e.printStackTrace();
            Log.e("Util", e.toString());
        }
    }

    private static void deleteLocalFiles(List<File> localFilesToDelete) {
        for (File f : localFilesToDelete){
            f.delete();
        }
    }

    private static boolean isFileUpForDeletion(String fileName, long deleteTimeCutOff) {
        boolean delete = false;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.BACKUP_DATE_FORMAT_YYYY_MM_DD_HHMMSS);
            long localFileDate = sdf.parse(fileName.replace(".db", "")).getTime();
            delete = localFileDate < deleteTimeCutOff;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return delete;
    }

    private static void uploadFile(List<File> files, DbxClientV2 client) throws DbxException, IOException {

        for (File file : files){
            uploadFile(client, file);
        }
    }

    private static void uploadFile(DbxClientV2 client, File file) throws DbxException, IOException {
        InputStream inputStream = new FileInputStream(file);
        final String remoteFileName = file.getName();

        client.files().uploadBuilder(Constants.DROPBOX_ROOT + remoteFileName)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
    }
}