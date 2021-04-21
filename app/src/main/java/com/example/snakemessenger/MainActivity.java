package com.example.snakemessenger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import com.example.snakemessenger.authentication.SignInActivity;
import com.example.snakemessenger.notifications.NotificationHandler;
import com.example.snakemessenger.database.AppDatabase;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;
import com.example.snakemessenger.services.AppStoppedService;
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
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;
import androidx.viewpager2.widget.ViewPager2;
import android.view.Menu;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MyMainActivity";
    public static final String SERVICE_ID = "com.example.snakemessenger";

    public static String currentChat = null;

    public static Map<Integer, List<Message>> notificationMessages = new HashMap<>();

    public static AppDatabase db;

    private SharedPreferences loginPreferences;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;

    public static String codeName;

    public static boolean advertising = false;
    public static boolean discovering = false;

    public static Map<String, String> pendingConnectionsData;

    private EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull final String endpointId, @NonNull final DiscoveredEndpointInfo info) {
                    String endpointPhone = info.getEndpointName();

                    Log.d(TAG, "onEndpointFound: found device with endpointId " + endpointId + " and phone " + endpointPhone);

                    Contact contact = MainActivity.db.getContactDao().findByPhone(endpointPhone);

                    if (contact != null && !contact.isConnected()) {
                        Log.d(TAG, "onEndpointFound: device is in local DB. Sending connection request...");

                        contact.setNearby(true);
                        contact.setEndpointID(endpointId);
                        db.getContactDao().updateContact(contact);

                        Nearby.getConnectionsClient(getApplicationContext()).requestConnection(codeName, endpointId, connectionLifecycleCallback)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onEndpointFound: connection request successfully sent");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onEndpointFound: failed to send connection request. Error: " + e.getMessage());

                                        if (discovering) {
                                            stopDiscovering();
                                        }

                                        Runnable startDiscoveryAgain = new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!discovering) {
                                                    startDiscovering();
                                                }
                                            }
                                        };

                                        Handler stopDiscoveringHandler = new Handler();
                                        stopDiscoveringHandler.postDelayed(startDiscoveryAgain, 3000);
                                    }
                                });
                        /* First contact with this contact */
                    } else if (contact == null) {
                        Log.d(TAG, "onEndpointFound: new device found. Adding it to the database");

                        db.getContactDao().addContact(new Contact(
                                0,
                                endpointPhone,
                                endpointPhone,
                                endpointId,
                                false,
                                "",
                                "",
                                null,
                                false,
                                false,
                                true
                        ));

                        Nearby.getConnectionsClient(getApplicationContext()).requestConnection(codeName, endpointId, connectionLifecycleCallback)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onEndpointFound: connection request successfully sent");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onEndpointFound: failed to send connection request. Error: " + e.getMessage());

                                        if (discovering) {
                                            stopDiscovering();
                                        }

                                        Runnable startDiscoveryAgain = new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!discovering) {
                                                    startDiscovering();
                                                }
                                            }
                                        };

                                        Handler stopDiscoveringHandler = new Handler();
                                        stopDiscoveringHandler.postDelayed(startDiscoveryAgain, 3000);
                                    }
                                });
                    }
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    Log.d(TAG, "onEndpointLost: device with endpointId " + endpointId + " went outside communication range.");

                    Contact contact = db.getContactDao().findById(endpointId);
                    contact.setNearby(false);
                    db.getContactDao().updateContact(contact);
                }
            };

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull final String endpointId, @NonNull final ConnectionInfo connectionInfo) {
                    final String endpointPhone = connectionInfo.getEndpointName();

                    Log.d(TAG, "onConnectionInitiated: initiated connection with device having endpointId " + endpointId + " and phone " + endpointPhone);

                    pendingConnectionsData.put(endpointId, endpointPhone);

                    Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(endpointId, payloadCallback)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onConnectionInitiated: Successfully accepted connection request for device");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pendingConnectionsData.remove(endpointId);

                                    Toast.makeText(
                                            MainActivity.this,
                                            "onConnectionInitiated: Failed to reconnect to " + endpointPhone + ".",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    Log.d(TAG, "onConnectionInitiated: Couldn't accept reconnection request for device");
                                }
                            });

                    Contact contact = db.getContactDao().findByPhone(endpointPhone);

                    if (contact != null) {
                        Log.d(TAG, "onConnectionInitiated: Device is in local DB. Updating endpointId...");
                        contact.setEndpointID(endpointId);
                        contact.setNearby(true);
                        db.getContactDao().updateContact(contact);
                    } else {
                        Log.d(TAG, "onEndpointFound: new device found. Adding it to the database");

                        db.getContactDao().addContact(new Contact(
                                0,
                                endpointPhone,
                                endpointPhone,
                                endpointId,
                                false,
                                "",
                                "",
                                null,
                                false,
                                false,
                                true
                        ));
                    }
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
                    String endpointPhone = pendingConnectionsData.get(endpointId);

                    Log.d(TAG, "onConnectionResult: result connection with device having endpointId " + endpointId + " and phone " + endpointPhone);

                    Contact contact = db.getContactDao().findByPhone(endpointPhone);

                    if (result.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_OK) {
                        Log.d(TAG, "onConnectionResult: the connection is established");

                        contact.setLastActive("");
                        contact.setConnected(true);
                        db.getContactDao().updateContact(contact);

                        sendUndeliveredMessages(contact);

                        Log.d(TAG, "onConnectionResult: reconnected to an old device");
                    } else if (result.getStatus().getStatusCode() == ConnectionsStatusCodes.STATUS_ERROR) {
                        Toast.makeText(
                                MainActivity.this,
                                "There was an error connecting with" + pendingConnectionsData.get(endpointId) + ".",
                                Toast.LENGTH_SHORT
                        ).show();

                        Log.d(TAG, "onConnectionResult: couldn't establish a connection between devices");
                    }

                    pendingConnectionsData.remove(endpointId);
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    Contact contact = db.getContactDao().findById(endpointId);

                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                    
                    contact.setLastActive(df.format(currentTime));
                    contact.setConnected(false);
                    contact.setNearby(false);
                    db.getContactDao().updateContact(contact);

                    Log.d(TAG, "onDisconnected: device with endpointId " + endpointId + " and phone " + contact.getPhone() + " disconnected.");
                }
            };

    private PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                    Contact contact = db.getContactDao().findById(endpointId);
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

                    db.getMessageDao().addMessage(receivedMessage);

                    if (currentChat == null || !currentChat.equals(contact.getPhone())) {
                        NotificationHandler.sendMessageNotification(MainActivity.this, contact, receivedMessage);
                    }

                    if (!contact.isChat()) {
                        contact.setChat(true);

                        db.getContactDao().updateContact(contact);
                    }

                    Log.d(TAG, "onPayloadReceived: received a message from " + contact.getName());
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                    long payloadId = payloadTransferUpdate.getPayloadId();

                    Message message = db.getMessageDao().getMessageByPayloadId(payloadId);

                    Log.d(TAG, "onPayloadTransferUpdate: update about transfer with status " + payloadTransferUpdate.getStatus());

                    if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS &&
                    message != null && message.getStatus() == Message.SENT) {
                        message.setStatus(Message.DELIVERED);
                        db.getMessageDao().updateMessage(message);

                        Log.d(TAG, "onPayloadTransferUpdate: payload was delivered to its receiver");
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mToolbar = findViewById(R.id.main_page_toolbar);
        mToolbar.setTitle("Snake Messenger");
        setSupportActionBar(mToolbar);

        startService(new Intent(getApplicationContext(), AppStoppedService.class));

        pendingConnectionsData = new HashMap<>();

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "SnakeMessengerDB")
                .allowMainThreadQueries()
                .build();

        Log.d(TAG, "onCreate: initialized Room DB");

        loginPreferences = getApplicationContext().getSharedPreferences("LOGIN_DETAILS", MODE_PRIVATE);

        codeName = loginPreferences.getString("phone", "");

        Log.d(TAG, "onCreate: user is signed in and will advertise using codeName " + codeName);

        ViewPager2 mViewPager2 = findViewById(R.id.main_tabs_pager);
        mViewPager2.setAdapter(new TabsAccessorAdapter(this));

        TabLayout mTabLayout = findViewById(R.id.main_tabs);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(mTabLayout, mViewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Chats");
                        break;
                    case 1:
                        tab.setText("Contacts");
                        break;
                    default:
                        break;
                }
            }
        });

        tabLayoutMediator.attach();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            Log.d(TAG, "onCreate: app does not have all the required permissions. Requesting permissions...");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                        this,
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_REQUIRED_PERMISSIONS
                );
            }
        }

        if (!advertising) {
            startAdvertising();
        }

        if (!discovering) {
            startDiscovering();
        }
    }

    private void sendUndeliveredMessages(Contact contact) {
        List<Message> undeliveredMessages = db.getMessageDao().getUndeliveredMessages(contact.getPhone());

        for (Message message : undeliveredMessages) {
            Payload messagePayload = Payload.fromBytes(message.getContent().getBytes());
            message.setPayloadId(messagePayload.getId());
            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(contact.getEndpointID(), messagePayload);

            db.getMessageDao().updateMessage(message);
        }
    }

    public void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startAdvertising(
                        codeName, SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        advertising = true;
                        Log.d(TAG, "startAdvertising: successfully started advertising");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "There was a problem starting advertising.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "startAdvertising: couldn't start advertising.Error: " + e.getMessage());
                    }
                });
    }

    private void stopAdvertising() {
        Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();

        advertising = false;

        Log.d(TAG, "stopAdvertising: stopped advertising");
    }

    public void startDiscovering() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(MainActivity.STRATEGY).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        discovering = true;
                        Log.d(TAG, "startDiscovering: started discovering");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (!Objects.equals(e.getMessage(), "8002: STATUS_ALREADY_DISCOVERING")) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "There was a problem starting discovering",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        Log.d(TAG, "startDiscovering: couldn't start discovering. Error: " + e.getMessage());
                    }
                });
    }

    public void stopDiscovering() {
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
        discovering = false;

        Log.d(TAG, "stopDiscovering: stopped discovering");
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Missing permissions", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_settings_option) {
            Log.d(TAG, "onOptionsItemSelected: settings option selected");
            sendUserToEditProfileActivity();
        } else if (item.getItemId() == R.id.main_sign_out_option) {
            Log.d(TAG, "onOptionsItemSelected: sign out option selected");
            SharedPreferences.Editor editor = loginPreferences.edit();
            editor.putBoolean("signedIn", false);
            editor.apply();

            if (advertising) {
                stopAdvertising();
            }

            if (discovering) {
                stopDiscovering();
            }

            sendUserToLoginActivity();
            Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private void sendUserToEditProfileActivity() {
        Log.d(TAG, "sendUserToSettingActivity: starting settings activity...");
        Intent settingsIntent = new Intent(MainActivity.this, EditProfileActivity.class);
        settingsIntent.putExtra("phone", "");
        startActivity(settingsIntent);
    }

    private void sendUserToLoginActivity() {
        Log.d(TAG, "sendUserToLoginActivity: starting login activity...");
        Intent loginIntent = new Intent(MainActivity.this, SignInActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
        finish();
    }
}
