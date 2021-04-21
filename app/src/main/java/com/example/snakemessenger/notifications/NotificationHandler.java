package com.example.snakemessenger.notifications;

import android.app.Notification;
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

import com.example.snakemessenger.EditProfileActivity;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.chats.ChatActivity;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class NotificationHandler {
    public static final String MESSAGE_CHANNEL_ID = "Messages channel";
    public static final String REQUEST_CHANNEL_ID = "Requests channel";

    public static void createNotificationChannels(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel messageChannel = new NotificationChannel(
                    MESSAGE_CHANNEL_ID,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );

            messageChannel.setDescription("This channel is dedicated to notifications about new incoming messages inside the application");

            NotificationChannel requestChannel = new NotificationChannel(
                    REQUEST_CHANNEL_ID,
                    "Connection requests",
                    NotificationManager.IMPORTANCE_HIGH
            );

            requestChannel.setDescription("This channel is dedicated to notifications about new incoming connection requests inside the application");

            manager.createNotificationChannel(messageChannel);
            manager.createNotificationChannel(requestChannel);
        }
    }

    public static void sendMessageNotification(Context context, Contact contact, Message message) {
        Intent activityIntent = new Intent(context, ChatActivity.class);
        activityIntent.addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
        );

        activityIntent.putExtra("phone", contact.getPhone());
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                contact.getId(),
                activityIntent,
                0
        );

        RemoteInput remoteInput = new RemoteInput.Builder("key_text_reply")
                .setLabel("Your answer...")
                .build();

        Intent replyIntent;
        PendingIntent replyPendingIntent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            replyIntent = new Intent(context, NotificationReceiver.class);
            replyIntent.putExtra("phone", contact.getPhone());
            replyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    contact.getId(),
                    replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
        } else {
            replyIntent = new Intent(context, ChatActivity.class);
            replyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            replyIntent.putExtra("phone", contact.getPhone());
            replyPendingIntent = PendingIntent.getActivity(
                    context,
                    contact.getId(),
                    activityIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );
        }

        NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_send_white_24dp,
                "Reply",
                replyPendingIntent
        ).addRemoteInput(remoteInput).build();

        NotificationCompat.MessagingStyle messagingStyle =
                new NotificationCompat.MessagingStyle("Me");

        List<Message> messages = MainActivity.notificationMessages.get(contact.getId());

        if (messages == null) {
            messages = new ArrayList<>();
        }

        messages.add(message);
        MainActivity.notificationMessages.put(contact.getId(), messages);

        for (Message msg : messages) {
            NotificationCompat.MessagingStyle.Message notificationMessage;
            if (msg.getStatus() == Message.RECEIVED) {
                Contact sender = MainActivity.db.getContactDao().findByPhone(msg.getToFrom());

                notificationMessage = new NotificationCompat.MessagingStyle.Message(
                        msg.getContent(),
                        msg.getTimestamp().getTime(),
                        sender.getName()
                );
            } else {
                SharedPreferences sharedPreferences = context.getSharedPreferences("LOGIN_DETAILS", MODE_PRIVATE);
                notificationMessage = new NotificationCompat.MessagingStyle.Message(
                        msg.getContent(),
                        msg.getTimestamp().getTime(),
                        sharedPreferences.getString("name", "You")
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

        if (message.getStatus() == Message.RECEIVED) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(contact.getId(), builder.build());
    }
}
