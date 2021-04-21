package com.example.snakemessenger.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;
import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = BroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        String phone = intent.getStringExtra("phone");

        Bundle remoteInputBundle = RemoteInput.getResultsFromIntent(intent);

        if (remoteInputBundle != null) {
            String message = Objects.requireNonNull(remoteInputBundle.getCharSequence("key_text_reply")).toString();

            Contact contact = MainActivity.db.getContactDao().findByPhone(phone);

            Payload messagePayload = Payload.fromBytes(message.getBytes());
            Nearby.getConnectionsClient(context).sendPayload(contact.getEndpointID(), messagePayload);

            Message sentMessage = new Message(
                    0,
                    messagePayload.getId(),
                    messagePayload.getType(),
                    contact.getPhone(),
                    message,
                    Calendar.getInstance().getTime(),
                    Message.SENT
            );

            MainActivity.db.getMessageDao().addMessage(sentMessage);

            if (MainActivity.currentChat == null || !MainActivity.currentChat.equals(contact.getPhone())) {
                NotificationHandler.sendMessageNotification(context, contact, sentMessage);
            }
        } else {
            String endpointId = intent.getStringExtra("endpointId");
            assert endpointId != null;

            final String endpointName = intent.getStringExtra("endpointName");
            assert endpointName != null;

            String action = intent.getStringExtra("action");
            assert action != null;

            int notificationId = intent.getIntExtra("notificationId", -1);

            final PayloadCallback payloadCallback =
                    new PayloadCallback() {
                        @Override
                        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                            Contact contact = MainActivity.db.getContactDao().findById(endpointId);
                            byte[] messageBytes = payload.asBytes();
                            String message = new String(messageBytes);

                            Message receivedMessage = new Message(
                                    0,
                                    payload.getId(),
                                    payload.getType(),
                                    contact.getPhone(),
                                    message,
                                    Calendar.getInstance().getTime(),
                                    Message.RECEIVED
                            );

                            MainActivity.db.getMessageDao().addMessage(receivedMessage);

                            if (MainActivity.currentChat == null || !MainActivity.currentChat.equals(contact.getPhone())) {
                                NotificationHandler.sendMessageNotification(context, contact, receivedMessage);
                            }

                            if (!contact.isChat()) {
                                contact.setChat(true);

                                MainActivity.db.getContactDao().updateContact(contact);
                            }

                            Log.d(TAG, "onPayloadReceived: received a message from " + contact.getName());
                        }

                        @Override
                        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {
                            long payloadId = update.getPayloadId();

                            Message message = MainActivity.db.getMessageDao().getMessageByPayloadId(payloadId);

                            Log.d(TAG, "onPayloadTransferUpdate: update about transfer with status " + update.getStatus());

                            if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS &&
                                    message != null && message.getStatus() == Message.SENT) {
                                message.setStatus(Message.DELIVERED);
                                MainActivity.db.getMessageDao().updateMessage(message);

                                Log.d(TAG, "onPayloadTransferUpdate: payload was delivered to its receiver");
                            }
                        }
                    };

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.cancel(notificationId);
        }
    }
}
