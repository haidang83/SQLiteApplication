package com.kpblog.tt.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class FileLoggingTree extends Timber.DebugTree {

    private static final String TAG = FileLoggingTree.class.getSimpleName();

    private Context context;

    public FileLoggingTree(Context context) {
        this.context = context;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {

        try {

            File logFolder = Util.getLocalLogFolder();

            if (!logFolder.exists()) {
                logFolder.mkdirs();
            }

            String fileNameTimeStamp = new SimpleDateFormat(Constants.DATE_FORMAT_YYYY_MM_DD, Locale.getDefault()).format(new Date());
            String logTimeStamp = new SimpleDateFormat("E yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault()).format(new Date
                    ());

            String fileName = fileNameTimeStamp + ".txt";

            File file = new File(logFolder, fileName);

            file.createNewFile();

            if (file.exists()) {

                OutputStream fileOutputStream = new FileOutputStream(file, true);

                fileOutputStream.write((logTimeStamp + ": " + message + "\n").getBytes());
                fileOutputStream.close();

            }

        } catch (Exception e) {
            Log.e(TAG, "Error while logging into file : " + e);
        }

    }
}
