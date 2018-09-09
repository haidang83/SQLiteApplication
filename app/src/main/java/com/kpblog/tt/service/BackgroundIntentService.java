package com.kpblog.tt.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

public class BackgroundIntentService extends IntentService {
    public BackgroundIntentService() {
        super("BackgroundIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String intentAction = intent.getAction();

        if (Constants.DB_BACKUP_ACTION.equals(intentAction)) {
            //schedule the next backup time
            Util.setNextDbBackupAlarm(this, Util.getNightlyDbBackupTime());

            final String sourceDbName = this.getDatabasePath(DatabaseHandler.DATABASE_NAME).getPath();
            String exportedDbPath = Util.exportDatabase(sourceDbName);

            //use this to also schedule the daily broadcast
            Util.scheduleDailyBroadcast(this);

            Util.textDailyCode(this);

        }
        else if (Constants.SCHEDULED_TEXT_ACTION.equals(intentAction)){
            int broadcastRecipientListId = intent.getIntExtra(Constants.BROADCAST_RECIPIENT_LIST_ID, -1);
            DatabaseHandler handler = new DatabaseHandler(this);
            Util.sendScheduledBroadcastByBroadcastId(this, handler, broadcastRecipientListId);
        }
    }
}
