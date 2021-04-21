package com.example.snakemessenger;

import android.app.Application;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.snakemessenger.notifications.NotificationHandler;

public class App extends Application {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHandler.createNotificationChannels(getSystemService(NotificationManager.class));
    }
}
