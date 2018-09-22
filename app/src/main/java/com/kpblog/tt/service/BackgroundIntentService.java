package com.kpblog.tt.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.dropbox.core.v2.DbxClientV2;
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

            DbxClientV2 client = Util.getDbxClientV2(this);

            //synch exported db folder
            Util.synchLocalAndRemoteFolder(Util.getDbExportedFolder(), client, Constants.DROPBOX_EXPORTED_FOLDER);

            //synch log files
            Util.synchLocalAndRemoteFolder(Util.getLocalLogFolder(), client, Constants.DROPBOX_LOG_FOLDER);

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
        else if (Constants.SYNCH_FOLDERS.equals(intentAction)){
            DbxClientV2 client = Util.getDbxClientV2(this);

            //synch exported db folder
            Util.synchLocalAndRemoteFolder(Util.getDbExportedFolder(), client, Constants.DROPBOX_EXPORTED_FOLDER);

            //synch log files
            Util.synchLocalAndRemoteFolder(Util.getLocalLogFolder(), client, Constants.DROPBOX_LOG_FOLDER);
        }
    }
}
