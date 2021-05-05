package com.example.snakemessenger.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import com.example.snakemessenger.R;
import com.example.snakemessenger.chats.ChatActivity;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.models.Message;
import com.example.snakemessenger.general.Constants;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.snakemessenger.MainActivity.db;
import static com.example.snakemessenger.MainActivity.notificationMessages;

public class NotificationHandler {
    public static final String MESSAGE_CHANNEL_ID = "Messages channel";

    public static void createNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel messageChannel = new NotificationChannel(
                    MESSAGE_CHANNEL_ID,
                    Constants.MESSAGES_NOTIFICATION_CHANNEL,
                    NotificationManager.IMPORTANCE_HIGH
            );

            messageChannel.setDescription(Constants.MESSAGES_NOTIFICATION_CHANNEL_DESCRIPTION);

            manager.createNotificationChannel(messageChannel);
        }
    }

    public static void sendMessageNotification(Context context, Contact contact, Message message) {
        Intent activityIntent = new Intent(context, ChatActivity.class);
        activityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
        );

        activityIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                contact.getId(),
                activityIntent,
                0
        );

        Intent replyIntent;
        PendingIntent replyPendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            replyIntent = new Intent(context, NotificationReceiver.class);
            replyIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
            replyIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, contact.getId());
            replyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    contact.getId(),
                    replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        } else {
            replyIntent = new Intent(context, ChatActivity.class);
            replyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            replyIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
            replyPendingIntent = PendingIntent.getActivity(
                    context,
                    contact.getId(),
                    replyIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );
        }

        RemoteInput remoteInput = new RemoteInput.Builder(Constants.REMOTE_INPUT_RESULT_KEY)
                .setLabel(Constants.REMOTE_INPUT_LABEL_TEXT)
                .build();

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_send_white_24dp,
                Constants.NOTIFICATION_MESSAGE_REPLY_TEXT,
                replyPendingIntent
        ).addRemoteInput(remoteInput).build();

        NotificationCompat.MessagingStyle messagingStyle =
                new NotificationCompat.MessagingStyle(Constants.NOTIFICATION_MESSAGE_USER_NAME);

        List<Message> messages = notificationMessages.get(contact.getId());

        if (messages == null) {
            messages = new ArrayList<>();
        }

        messages.add(message);
        notificationMessages.put(contact.getId(), messages);

        for (Message msg : messages) {
            NotificationCompat.MessagingStyle.Message notificationMessage;
            if (msg.getStatus() == Constants.MESSAGE_STATUS_RECEIVED) {
                Contact sender = db.getContactDao().findByDeviceId(msg.getSource());

                notificationMessage = new NotificationCompat.MessagingStyle.Message(
                        msg.getContent(),
                        msg.getTimestamp(),
                        sender.getName()
                );
            } else {
                SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
                notificationMessage = new NotificationCompat.MessagingStyle.Message(
                        msg.getContent(),
                        msg.getTimestamp(),
                        sharedPreferences.getString(Constants.SHARED_PREFERENCES_NAME, Constants.NOTIFICATION_MESSAGE_USER_NAME)
                );
            }

            messagingStyle.addMessage(notificationMessage);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.snake_logo)
                .setStyle(messagingStyle)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLUE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .addAction(replyAction);

        if (message.getStatus() == Constants.MESSAGE_STATUS_RECEIVED) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(contact.getId(), builder.build());
    }
}
