package com.example.snakemessenger.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import com.example.snakemessenger.general.Constants;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.models.Message;
import com.example.snakemessenger.managers.CommunicationManager;

import java.util.Objects;

import static com.example.snakemessenger.MainActivity.currentChat;
import static com.example.snakemessenger.MainActivity.db;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String contactDeviceId = intent.getStringExtra(Constants.EXTRA_CONTACT_DEVICE_ID);

        Bundle remoteInputBundle = RemoteInput.getResultsFromIntent(intent);

        if (remoteInputBundle != null) {
            String message = Objects.requireNonNull(remoteInputBundle.getCharSequence(Constants.REMOTE_INPUT_RESULT_KEY)).toString();
            Contact contact = db.getContactDao().findByDeviceId(contactDeviceId);

            Message sentMessage = CommunicationManager.buildAndDeliverMessage(context, message, contact);

            if (currentChat == null || !currentChat.equals(contact.getDeviceID())) {
                NotificationHandler.sendMessageNotification(context, contact, sentMessage);
            }
        } else {
            int notificationId = intent.getIntExtra(Constants.EXTRA_NOTIFICATION_ID, -1);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.cancel(notificationId);
        }
    }
}
