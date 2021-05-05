package com.example.snakemessenger.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.database.AppDatabase;
import com.example.snakemessenger.general.Constants;
import com.example.snakemessenger.managers.CommunicationManager;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.models.Message;
import com.example.snakemessenger.notifications.NotificationHandler;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.snakemessenger.MainActivity.db;
import static com.example.snakemessenger.MainActivity.currentChat;
import static com.example.snakemessenger.MainActivity.myDeviceId;

public class BackgroundCommunicationService extends Service {
    public static final String TAG = "BGCommunicationService";

    private static final int NOTIFICATION_ID = 1717;
    private static final String NOTIFICATION_CHANNEL_ID = "Channel_Id";

    public Map<String, String> pendingConnectionsData;
    public float batteryLevel;

    public static boolean running = false;

    private final BatteryStatusReceiver batteryStatusReceiver = new BatteryStatusReceiver();
    private class BatteryStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            batteryLevel = level * 100 / (float) scale;

            Log.d(TAG, "onReceive: received battery level " + batteryLevel);
        }
    }

    private final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull final String endpointId, @NonNull final DiscoveredEndpointInfo info) {
                    String endpointDeviceId = info.getEndpointName();

                    Log.d(TAG, "onEndpointFound: found device with endpointId " + endpointId + " and phone " + endpointDeviceId);

                    Contact contact = db.getContactDao().findByDeviceId(endpointDeviceId);

                    if (contact != null && !contact.isConnected()) {
                        Log.d(TAG, "onEndpointFound: device is in local DB. Sending connection request...");

                        contact.setNearby(true);
                        contact.setEndpointID(endpointId);
                        db.getContactDao().updateContact(contact);

                        /* First contact with this contact */
                    } else if (contact == null) {
                        Log.d(TAG, "onEndpointFound: new device found. Adding it to the database");

                        db.getContactDao().addContact(new Contact(
                                0,
                                endpointDeviceId,
                                endpointDeviceId,
                                endpointId,
                                false,
                                System.currentTimeMillis(),
                                "",
                                null,
                                false,
                                0,
                                false,
                                true
                        ));
                    }

                    if (contact == null || !contact.isConnected()) {
                        Nearby.getConnectionsClient(getApplicationContext()).requestConnection(myDeviceId, endpointId, connectionLifecycleCallback)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "onEndpointFound: connection request successfully sent"))
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "onEndpointFound: failed to send connection request. Error: " + e.getMessage());

                                    stopDiscovering();

                                    Runnable startDiscoveryAgain = () -> startDiscovering();

                                    Handler stopDiscoveringHandler = new Handler();
                                    stopDiscoveringHandler.postDelayed(startDiscoveryAgain, 3000);
                                });
                    }
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    Log.d(TAG, "onEndpointLost: device with endpointId " + endpointId + " went outside communication range.");

                    Contact contact = db.getContactDao().findById(endpointId);

                    if (contact != null) {
                        contact.setNearby(false);
                        db.getContactDao().updateContact(contact);
                    }
                }
            };

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull final String endpointId, @NonNull final ConnectionInfo connectionInfo) {
                    final String endpointDeviceId = connectionInfo.getEndpointName();

                    Log.d(TAG, "onConnectionInitiated: initiated connection with device having endpointId " + endpointId + " and phone " + endpointDeviceId);

                    pendingConnectionsData.put(endpointId, endpointDeviceId);

                    Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endpointId, payloadCallback)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "onConnectionInitiated: Successfully accepted connection request for device"))
                            .addOnFailureListener(e -> {
                                pendingConnectionsData.remove(endpointId);

                                Log.d(TAG, "onConnectionInitiated: Couldn't accept reconnection request for device");
                            });

                    Contact contact = db.getContactDao().findByDeviceId(endpointDeviceId);

                    if (contact != null) {
                        Log.d(TAG, "onConnectionInitiated: Device is in local DB. Updating endpointId...");
                        contact.setEndpointID(endpointId);
                        contact.setNearby(true);
                        db.getContactDao().updateContact(contact);
                    } else {
                        Log.d(TAG, "onEndpointFound: new device found. Adding it to the database");

                        db.getContactDao().addContact(new Contact(
                                0,
                                endpointDeviceId,
                                endpointDeviceId,
                                endpointId,
                                false,
                                System.currentTimeMillis(),
                                "",
                                null,
                                false,
                                0,
                                false,
                                true
                        ));
                    }
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
                    String endpointDeviceId = pendingConnectionsData.get(endpointId);

                    Log.d(TAG, "onConnectionResult: result connection with device having endpointId " + endpointId + " and deviceId " + endpointDeviceId);

                    Contact contact = db.getContactDao().findByDeviceId(endpointDeviceId);

                    if (result.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
                        Log.d(TAG, "onConnectionResult: the connection is established");

                        contact.setLastActive(System.currentTimeMillis());
                        contact.setConnected(true);
                        db.getContactDao().updateContact(contact);

                        CommunicationManager.deliverDirectMessages(getApplicationContext(), contact);
                        CommunicationManager.sendDeviceInformation(getApplicationContext(), contact, batteryLevel);
                    } else if (result.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_ERROR) {
                        Log.d(TAG, "onConnectionResult: couldn't establish a connection between devices");
                    }

                    pendingConnectionsData.remove(endpointId);
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    Contact contact = db.getContactDao().findById(endpointId);

                    contact.setLastActive(System.currentTimeMillis());
                    contact.setConnected(false);
                    contact.setNearby(false);
                    db.getContactDao().updateContact(contact);

                    Log.d(TAG, "onDisconnected: device with endpointId " + endpointId + " and phone " + contact.getDeviceID() + " disconnected.");
                }
            };

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                    Contact contact = db.getContactDao().findById(endpointId);
                    byte[] messageBytes = payload.asBytes();

                    Log.d(TAG, "onPayloadReceived: received a message from " + contact.getName());

                    try {
                        JSONObject messageJSON = new JSONObject(new String(messageBytes));

                        int messageType = messageJSON.getInt(Constants.JSON_MESSAGE_TYPE_KEY);

                        switch (messageType) {
                            case Constants.MESSAGE_TYPE_HELLO:
                                Log.d(TAG, "onPayloadReceived: the message's type is HELLO");

                                CommunicationManager.sendMessagesForRouting(getApplicationContext(), messageJSON, contact);
                                break;
                            case Constants.MESSAGE_TYPE_MESSAGE:
                                Log.d(TAG, "onPayloadReceived: the message's type is MESSAGE");

                                Message dbMessage = db.getMessageDao().findByMessageId(messageJSON.getString(Constants.JSON_MESSAGE_ID_KEY));

                                if (dbMessage != null) {
                                    Log.d(TAG, "onPayloadReceived: the received message is already in the local database");
                                    break;
                                }

                                String destinationId = messageJSON.getString(Constants.JSON_DESTINATION_DEVICE_ID_KEY);

                                if (destinationId.equals(myDeviceId)) {
                                    Log.d(TAG, "onPayloadReceived: the message is for the current device");

                                    Message receivedMessage = CommunicationManager.saveOwnMessageToDatabase(messageJSON, contact, payload, Constants.MESSAGE_STATUS_RECEIVED);

                                    if (currentChat == null || !currentChat.equals(contact.getDeviceID())) {
                                        NotificationHandler.sendMessageNotification(getApplicationContext(), contact, receivedMessage);
                                    }
                                } else {
                                    Log.d(TAG, "onPayloadReceived: the message is routing to another device");

                                    CommunicationManager.saveDataMemoryMessageToDatabase(messageJSON, payload, Constants.MESSAGE_STATUS_ROUTING);
                                }

                                break;
                            case Constants.MESSAGE_TYPE_ACK:
                                Log.d(TAG, "onPayloadReceived: the message's type is ACK");

                                CommunicationManager.markMessagesAsDelivered(messageJSON);
                                break;
                            default:
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                    long payloadId = payloadTransferUpdate.getPayloadId();

                    Contact contact = db.getContactDao().findById(endpointId);
                    Message message = db.getMessageDao().getMessageByPayloadId(payloadId);

                    Log.d(TAG, "onPayloadTransferUpdate: update about transfer with status " + payloadTransferUpdate.getStatus());

                    if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS &&
                            message != null && message.getStatus() != Constants.MESSAGE_STATUS_RECEIVED) {
                        message.setTimesSent(message.getTimesSent() + 1);

                        Log.d(TAG, "onPayloadTransferUpdate: increased times sent of message to " + message.getTimesSent());

                        if(message.getDestination().equals(contact.getDeviceID())) {
                            Log.d(TAG, "onPayloadTransferUpdate: the message reached its destination");

                            if (message.getSource().equals(myDeviceId)) {
                                message.setStatus(Constants.MESSAGE_STATUS_DELIVERED);
                                db.getMessageDao().updateMessage(message);
                            } else {
                                db.getMessageDao().deleteMessage(message);
                            }
                        } else if (message.getTimesSent() >= Constants.MAX_SEND_TIMES) {
                            Log.d(TAG, "onPayloadTransferUpdate: message reached max number of sends. No need to send anymore.");

                            if (message.getStatus() == Constants.MESSAGE_STATUS_SENT) {
                                Log.d(TAG, "onPayloadTransferUpdate: the message was generated by the current device. Marking it as delivered...");

                                message.setStatus(Constants.MESSAGE_STATUS_DELIVERED);
                                db.getMessageDao().updateMessage(message);
                            } else if (message.getStatus() == Constants.MESSAGE_STATUS_ROUTING) {
                                Log.d(TAG, "onPayloadTransferUpdate: the message was generated by another device. Deleting it...");

                                db.getMessageDao().deleteMessage(message);
                            }
                        }

                        Log.d(TAG, "onPayloadTransferUpdate: payload was delivered to its receiver");
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        pendingConnectionsData = new HashMap<>();
        registerReceiver(batteryStatusReceiver, intentFilter);

        Log.d(TAG, "onCreate: service has been created");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: service is starting");

        startAdvertising();
        startDiscovering();

        startForeground();

        Log.d(TAG, "onStartCommand: service has started");

        running = true;

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: service is being destroyed");

        unregisterReceiver(batteryStatusReceiver);
        stopAdvertising();
        stopDiscovering();

        Nearby.getConnectionsClient(getApplicationContext()).stopAllEndpoints();

        Log.d(TAG, "onDestroy: stopped all endpoints");

        List<Contact> contacts = db.getContactDao().getNearbyContacts();

        Log.d(TAG, "onDestroy: iterating through " + contacts.size() + " contacts");

        for (Contact contact : contacts) {
            Log.d(TAG, "onDestroy: parsing contact with name " + contact.getName() + " connected " + contact.isConnected());

            if (contact.isConnected()) {
                contact.setConnected(false);
                contact.setLastActive(System.currentTimeMillis());
            }

            contact.setNearby(false);

            db.getContactDao().updateContact(contact);

            Log.d(TAG, "onDestroy: disconnected from device " + contact.getName());
        }

        Log.d(TAG, "onDestroy: service has been destroyed");

        running = false;

        super.onDestroy();
    }

    public void startForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    Constants.SERVICE_NOTIFICATION_CHANNEL,
                    NotificationManager.IMPORTANCE_NONE
            );

            serviceChannel.setDescription(Constants.SERVICE_NOTIFICATION_CHANNEL_DESCRIPTION);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this,
                NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.snake_logo)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Communication service is running")
                .setContentIntent(pendingIntent)
                .build());
    }

    public void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Constants.STRATEGY).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startAdvertising(
                        myDeviceId, Constants.SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "startAdvertising: successfully started advertising"))
                .addOnFailureListener(e -> Log.d(TAG, "startAdvertising: couldn't start advertising. Error: " + e.getMessage()));
    }

    private void stopAdvertising() {
        Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();

        Log.d(TAG, "stopAdvertising: stopped advertising");
    }

    public void startDiscovering() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Constants.STRATEGY).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startDiscovery(Constants.SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "startDiscovering: successfully started discovering"))
                .addOnFailureListener(e -> Log.d(TAG, "startDiscovering: couldn't start discovering. Error: " + e.getMessage()));
    }

    public void stopDiscovering() {
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();

        Log.d(TAG, "stopDiscovering: stopped discovering");
    }
}
