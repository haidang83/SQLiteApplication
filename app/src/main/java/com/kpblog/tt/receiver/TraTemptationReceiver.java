package com.kpblog.tt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.service.BackgroundIntentService;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

/**
 * [good example] https://code.tutsplus.com/tutorials/android-fundamentals-scheduling-recurring-tasks--mobile-5788
 * [persist when device turns off] http://blog.teamtreehouse.com/scheduling-time-sensitive-tasks-in-android-with-alarmmanager
 * [check if alarm is already set] https://www.developer-tech.com/news/2013/jun/17/scheduling-a-task-to-run-repeatedly-in-android/
 */
public class TraTemptationReceiver extends BroadcastReceiver {
    private static final String DEBUG_TAG = "TraTemptationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DEBUG_TAG, "onReceive()");

        final String intentAction = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(intentAction)) {
            //called when boot is completed
            Util.setNextDbBackupAlarm(context, Util.getNightlyDbBackupTime());
        }
        else {
            //just redirect to BackgroundIntentService
            intent.setClass(context, BackgroundIntentService.class);
            context.startService(intent);
        }
        /*else if (Constants.DB_BACKUP_ACTION.equals(intentAction)) {
            //schedule the next backup time
            Util.setNextDbBackupAlarm(context, Util.getNightlyDbBackupTime());

            final String sourceDbName = context.getDatabasePath(DatabaseHandler.DATABASE_NAME).getPath();
            String exportedDbPath = Util.exportDatabase(sourceDbName);

            //use this to also schedule the daily broadcast
            Util.scheduleDailyBroadcast(context);

            Util.textDailyCode(context);

        }
        else if (Constants.SCHEDULED_TEXT_ACTION.equals(intentAction)){
            int broadcastRecipientListId = intent.getIntExtra(Constants.BROADCAST_RECIPIENT_LIST_ID, -1);
            DatabaseHandler handler = new DatabaseHandler(context);
            Util.sendScheduledBroadcastByBroadcastId(context, handler, broadcastRecipientListId);
        }*/
    }

}
