package com.kpblog.tt.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.kpblog.tt.dao.DatabaseHandler;
import com.kpblog.tt.util.Constants;
import com.kpblog.tt.util.Util;

import java.io.File;

public class BackgroundIntentService extends IntentService {
    private String TAG = "BackgroundIntentService";
    public BackgroundIntentService() {
        super("BackgroundIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String intentAction = intent.getAction();

        if (Constants.SCHEDULED_DB_BACKUP_ACTION.equals(intentAction)) {
            //schedule the next backup time
            Util.setNextDbBackupAlarm(this, Util.getNightlyDbBackupTime());

            File exportedDbFile = Util.exportDatabaseAsFile(this);

            Util.uploadToServer(this, exportedDbFile, Constants.DROPBOX_EXPORTED_FOLDER);

            //use this to also schedule the daily broadcast
            Util.scheduleDailyBroadcast(this);

            Util.textDailyCode(this);

        }
        else if (Constants.SCHEDULED_TEXT_ACTION.equals(intentAction)){
            int broadcastRecipientListId = intent.getIntExtra(Constants.BROADCAST_RECIPIENT_LIST_ID, -1);
            DatabaseHandler handler = new DatabaseHandler(this);
            Util.sendScheduledBroadcastByBroadcastId(this, handler, broadcastRecipientListId);
        }
        else if (Constants.DB_EXPORT.equals(intentAction)){
            Util.exportDatabase(this);
        }
    }
}
