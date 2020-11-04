package com.example.snakemessenger;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.snakemessenger.chats.ChatActivity;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;

import java.util.Calendar;
import java.util.Objects;

import static com.example.snakemessenger.App.MESSAGE_CHANNEL_ID;

public class NotificationHandler extends BroadcastReceiver {
    public static final String TAG = BroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String phone = intent.getStringExtra("phone");

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if (remoteInput != null) {
            String message = Objects.requireNonNull(remoteInput.getCharSequence("key_text_reply")).toString();

            Contact contact = MainActivity.db.getContactDao().findByPhone(phone);

            Payload messagePayload = Payload.fromBytes(message.getBytes());
            Nearby.getConnectionsClient(context).sendPayload(contact.getEndpointID(), messagePayload);

            saveMessageInfoToDatabase(contact, messagePayload, message);

            Intent activityIntent = new Intent(context, ChatActivity.class);
            activityIntent.putExtra("phone", contact.getPhone());
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context,
                    0,
                    activityIntent,
                    0
            );

            Notification repliedNotification = new NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.snake_logo)
                    .setContentTitle(contact.getName())
                    .setContentText("You: " + message)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setColor(Color.BLUE)
                    .setSound(null)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(contact.getId(), repliedNotification);
        }
    }

    private void saveMessageInfoToDatabase(Contact contact, Payload payload, String message) {
        MainActivity.db.getMessageDao().addMessage(new Message(
                0,
                payload.getId(),
                payload.getType(),
                contact.getPhone(),
                message,
                Calendar.getInstance().getTime(),
                Message.SENT
        ));

        Log.d(TAG, "saveMessageInfoToDatabase: saved message to Room");
    }
}
