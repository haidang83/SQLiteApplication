package com.kpblog.tt.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kpblog.tt.dao.DatabaseHandler;
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
            Util.setRecurringAlarm(context);
        }
        else if (Constants.DB_BACKUP_ACTION.equals(intentAction)) {
            final String sourceDbName = context.getDatabasePath(DatabaseHandler.DATABASE_NAME).getPath();
            String exportedDbPath = Util.exportDatabase(sourceDbName);
        }
    }
}