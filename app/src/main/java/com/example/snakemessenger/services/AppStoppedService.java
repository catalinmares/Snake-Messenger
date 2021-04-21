package com.example.snakemessenger.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.MainActivity;
import com.google.android.gms.nearby.Nearby;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppStoppedService extends Service {
    public static final String TAG = "AppStoppedService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: started service");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopAdvertising();
        stopDiscovering();
        Nearby.getConnectionsClient(getApplicationContext()).stopAllEndpoints();

        Log.d(TAG, "onTaskRemoved: stopped all endpoints");

        List<Contact> contacts = MainActivity.db.getContactDao().getNearbyContacts();

        Log.d(TAG, "onTaskRemoved: iterating through " + contacts.size() + " contacts");

        for (Contact contact : contacts) {
            Log.d(TAG, "onTaskRemoved: parsing contact with name " + contact.getName() + " connected " + contact.isConnected());

            if (contact.isConnected()) {
                contact.setConnected(false);

                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                contact.setLastActive(df.format(currentTime));
            }

            contact.setNearby(false);

            MainActivity.db.getContactDao().updateContact(contact);

            Log.d(TAG, "onTaskRemoved: disconnected from device " + contact.getName());
        }

        super.onTaskRemoved(rootIntent);
    }

    public void stopAdvertising() {
        Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();

        Log.d(TAG, "stopAdvertising: stopped advertising");
    }

    public void stopDiscovering() {
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
        MainActivity.discovering = false;

        Log.d(TAG, "stopDiscovering: stopped discovering");
    }
}
