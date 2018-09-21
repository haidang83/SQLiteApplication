package com.kpblog.tt;

import android.app.Application;

import com.kpblog.tt.util.FileLoggingTree;

import timber.log.Timber;

public class TraTemptationApplication extends Application{


    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new FileLoggingTree(getApplicationContext()));
    }

}
