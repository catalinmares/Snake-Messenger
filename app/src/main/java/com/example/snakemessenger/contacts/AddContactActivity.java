package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;
import com.google.android.gms.nearby.Nearby;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddContactActivity extends AppCompatActivity {
    public static final String TAG = AddContactActivity.class.getSimpleName();
    private SharedPreferences loginPreferences;
    private String codeName;

    private CircleImageView mUserProfilePicture;
    private TextView mUserProfileName;
    private Button mConnectWithUser;
    private TextView mNoDevices;

    private RecyclerView mUsersRecyclerView;
    private UsersAdapter mAdapter;
    private List<User> users = new ArrayList<>();
    private Map<String, String> pendingConnectionsData;

    private Dialog userProfile;

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
                    String endpointPhone = info.getEndpointName();

                    Log.d(TAG, "onEndpointFound: found device with endpointId " + endpointId + " and phone " + endpointPhone);

                    users.add(new User(endpointId, info.getEndpointName()));
                    mAdapter = new UsersAdapter(AddContactActivity.this, users);
                    mUsersRecyclerView.setAdapter(mAdapter);

                    mNoDevices.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    Log.d(TAG, "onEndpointLost: device with endpointId " + endpointId + " went outside communication range.");

                    Iterator<User> it = users.iterator();
                    boolean listModified = false;

                    while (it.hasNext()) {
                        User user = (User) it.next();

                        if (user.getEndpointID().equals(endpointId)) {
                            it.remove();
                            listModified = true;

                            Log.d(TAG, "onEndpointLost: removed device from nearby users list");
                        }
                    }

                    if (users.isEmpty()) {
                        mNoDevices.setVisibility(View.VISIBLE);
                    }

                    if (listModified) {
                        mAdapter = new UsersAdapter(AddContactActivity.this, users);
                        mUsersRecyclerView.setAdapter(mAdapter);

                        Log.d(TAG, "onEndpointLost: reinitialized RecyclerView adapter");
                    }
                }
            };

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull final String endpointId, @NonNull final ConnectionInfo connectionInfo) {
                    String endpointPhone = connectionInfo.getEndpointName();

                    Log.d(TAG, "onConnectionInitiated: initiated connection with device having endpointId " + endpointId + " and phone " + endpointPhone);

                    pendingConnectionsData.put(endpointId, connectionInfo.getEndpointName());
                    Nearby.getConnectionsClient(getApplicationContext())
                            .acceptConnection(endpointId, payloadCallback)
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
                                            AddContactActivity.this,
                                            "There was an error connecting to " + connectionInfo.getEndpointName(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    Log.d(TAG, "onConnectionInitiated: Couldn't accept connection request for device");
                                }
                            });
                }

                public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
                    String endpointPhone = pendingConnectionsData.get(endpointId);

                    Log.d(TAG, "onConnectionResult: result connection with device having endpointId " + endpointId + " and phone " + endpointPhone);

                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            MainActivity.db.getContactDao().addContact(new Contact(
                                    0,
                                    endpointPhone,
                                    endpointPhone,
                                    endpointId,
                                    true,
                                    "",
                                    "",
                                    null,
                                    false
                            ));

                            Toast.makeText(
                                    AddContactActivity.this,
                                    endpointPhone + " accepted your connection request.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            Log.d(TAG, "onConnectionResult: the connection is established");
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            Toast.makeText(
                                    AddContactActivity.this,
                                    endpointPhone + " rejected your connection request.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            Log.d(TAG, "onConnectionResult: the connection request was rejected by device");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            Toast.makeText(
                                    AddContactActivity.this,
                                    "There was an error connecting with" + endpointPhone + ".",
                                    Toast.LENGTH_SHORT
                            ).show();
                            Log.d(TAG, "onConnectionResult: couldn't establish a connection between devices");
                            break;
                        default:
                            // Unknown status code
                    }

                    pendingConnectionsData.remove(endpointId);
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    Contact contact = MainActivity.db.getContactDao().findById(endpointId);
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    contact.setLastActive(df.format(currentTime));
                    contact.setConnected(false);
                    MainActivity.db.getContactDao().updateContact(contact);

                    Log.d(TAG, "onDisconnected: device with endpointId " + endpointId + " and phone " + endpointId + " disconnected.");
                }
            };

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
                    Contact contact = MainActivity.db.getContactDao().findById(endpointId);
                    byte[] messageBytes = payload.asBytes();
                    String message = new String(messageBytes);

                    MainActivity.db.getMessageDao().addMessage(new Message(
                            0,
                            payload.getId(),
                            payload.getType(),
                            contact.getPhone(),
                            message,
                            Calendar.getInstance().getTime(),
                            Message.RECEIVED
                    ));

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Nearby users");
        setSupportActionBar(toolbar);

        loginPreferences = getApplicationContext().getSharedPreferences("LOGIN_DETAILS", MODE_PRIVATE);
        codeName = loginPreferences.getString("phone", "");

        Log.d(TAG, "onCreate: user will advertise using codeName " + codeName);

        pendingConnectionsData = new HashMap<>();

        mUsersRecyclerView = findViewById(R.id.add_friend_recycler_view);

        mNoDevices = findViewById(R.id.no_devices);
        mNoDevices.setVisibility(View.VISIBLE);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(AddContactActivity.this, LinearLayoutManager.VERTICAL, false);
        mUsersRecyclerView.setLayoutManager(layoutManager);

        mUsersRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(AddContactActivity.this, mUsersRecyclerView, new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        CircleImageView userPic = view.findViewById(R.id.contact_image_item);
                        BitmapDrawable drawable = (BitmapDrawable) userPic.getDrawable();
                        Bitmap image = drawable.getBitmap();
                        showUserProfile(users.get(position), image);
                    }

                    @Override
                    public void onLongClick(View child, int position) {

                    }
                }));

        Log.d(TAG, "onCreateView: initialized RecyclerView");

        initializeUserProfileDialog();
        startDiscovering();
    }

    private void startDiscovering() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(MainActivity.STRATEGY).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startDiscovery(MainActivity.SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "startDiscovering: started discovering");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddContactActivity.this, "There was a problem starting discovering. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "startDiscovering: couldn't start discovering");
                    }
                });
    }

    private void stopDiscovering() {
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();

        Log.d(TAG, "stopDiscovering: stopped discovering");
    }

    private void initializeUserProfileDialog() {
        userProfile = new Dialog(AddContactActivity.this);
        userProfile.setContentView(R.layout.request_connection_layout);

        TextView mClosePopup = userProfile.findViewById(R.id.request_connection_close);
        mUserProfilePicture = userProfile.findViewById(R.id.request_connection_pic);
        mUserProfileName = userProfile.findViewById(R.id.request_connection_name);
        mConnectWithUser = userProfile.findViewById(R.id.request_connection_btn);

        mClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfile.dismiss();
            }
        });

        Log.d(TAG, "initializeUserProfileDialog: initialized dialog");
    }

    private void showUserProfile(final User user, Bitmap image) {
        final Contact contact = MainActivity.db.getContactDao().findByPhone(user.getPhone());

        if (contact != null) {
            mConnectWithUser.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorText));
        } else {
            mConnectWithUser.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorAccent));
        }

        mUserProfileName.setText(user.getPhone());
        mUserProfilePicture.setImageBitmap(image);
        mConnectWithUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (contact == null) {
                    Nearby.getConnectionsClient(getApplicationContext())
                            .requestConnection(codeName, user.getEndpointID(), connectionLifecycleCallback)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(
                                            AddContactActivity.this,
                                            "Connection request sent.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(
                                            AddContactActivity.this,
                                            "Failed sending a connection request. Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });

                    userProfile.dismiss();
                } else {
                    Toast.makeText(
                            AddContactActivity.this,
                            "You are already connected to " + contact.getName(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });

        userProfile.show();
    }

    @Override
    public void onBackPressed() {
        stopDiscovering();
        super.onBackPressed();
    }
}
