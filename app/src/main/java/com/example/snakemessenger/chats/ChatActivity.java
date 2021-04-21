package com.example.snakemessenger.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snakemessenger.EditProfileActivity;
import com.example.snakemessenger.managers.DateManager;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity {
    public static final String TAG = ChatActivity.class.getSimpleName();
    public static final int EDIT_PROFILE_REQUEST = 1;

    private Contact contact;
    private boolean isContactConnected;

    private CircleImageView contactPicture;
    private ImageView contactConnected;
    private TextView contactName;
    private TextView contactStatus;

    private ChatAdapter chatAdapter;
    private RecyclerView messagesRecyclerView;

    private EmojiconEditText userMessageInput;

    private View rootView;
    private ImageView emojiImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeToolbar();

        String contactPhone = Objects.requireNonNull(getIntent().getStringExtra("phone"));
        contact = MainActivity.db.getContactDao().findByPhone(contactPhone);

        if (MainActivity.notificationMessages.get(contact.getId()) != null) {
            MainActivity.notificationMessages.put(contact.getId(), new ArrayList<Message>());
        }

        updateUI(contact);
        initializeViews();
        initializeRecyclerView();

        EmojIconActions emojIconActions = new EmojIconActions(this, rootView, userMessageInput, emojiImageView);
        emojIconActions.ShowEmojIcon();
        emojIconActions.setIconsIds(R.drawable.ic_baseline_keyboard_24, R.drawable.ic_baseline_emoji_emotions_24);

        MainActivity.db.getContactDao().findChangedByPhone(contactPhone).observe(this, new Observer<Contact>() {
            @Override
            public void onChanged(Contact changedContact) {
                if (changedContact.isConnected() && !isContactConnected) {
                    isContactConnected = true;
                    contactStatus.setText("Active now");
                    contactConnected.setVisibility(View.VISIBLE);
                } else if (!changedContact.isConnected() && isContactConnected) {
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    isContactConnected = false;
                    contactStatus.setText(
                            String.format(
                                    "Last seen %s",
                                    DateManager.getLastActiveText(
                                            df.format(currentTime),
                                            changedContact.getLastActive()
                                    )
                            )
                    );

                    contactConnected.setVisibility(View.GONE);
                }

                contact = changedContact;
            }
        });

        chatAdapter = new ChatAdapter(this, new ArrayList<Message>(), contact);
        messagesRecyclerView.setAdapter(chatAdapter);

        MainActivity.db.getMessageDao().getLiveMessages(contactPhone).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> newMessages) {
                chatAdapter.setMessages(newMessages);
                messagesRecyclerView.scrollToPosition(newMessages.size() - 1);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        MainActivity.currentChat = contact.getPhone();
    }

    @Override
    protected void onStop() {
        MainActivity.currentChat = null;
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.chat_options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.chat_edit_option) {
            Log.d(TAG, "onOptionsItemSelected: settings option selected");
            sendUserToEditProfileActivity(contact);
        } else if (item.getItemId() == R.id.chat_block_option) {
            /* TODO: Add blocking option */
        } else if (item.getItemId() == R.id.chat_delete_option) {
            /* TODO: Add deleting option */
        }

        return true;
    }

    private void sendUserToEditProfileActivity(Contact contact) {
        Log.d(TAG, "sendUserToSettingActivity: starting edit contact activity...");
        Intent editContactIntent = new Intent(ChatActivity.this, EditProfileActivity.class);
        editContactIntent.putExtra("phone", contact.getPhone());
        startActivityForResult(editContactIntent, EDIT_PROFILE_REQUEST);
    }

    private void initializeToolbar() {
        Toolbar mToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);

        contactName = mToolbar.findViewById(R.id.chat_name);
        contactStatus = mToolbar.findViewById(R.id.chat_description);
        contactPicture = mToolbar.findViewById(R.id.chat_image);
        contactConnected = mToolbar.findViewById(R.id.status);

        ImageView mBackButton = mToolbar.findViewById(R.id.back_btn);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Log.d(TAG, "initializeToolbar: initialized toolbar");
    }

    private void updateUI(Contact contact) {
        contactName.setText(contact.getName());

        if (!contact.isConnected()) {
            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

            isContactConnected = false;
            contactStatus.setText(
                    String.format(
                            "Last seen %s",
                            DateManager.getLastActiveText(
                                    df.format(currentTime),
                                    contact.getLastActive()
                            )
                    )
            );

            contactConnected.setVisibility(View.GONE);

            Log.d(TAG, "updateUI: initialized contact UI as disconnected");
        } else {
            contactStatus.setText(R.string.active_now);
            contactConnected.setVisibility(View.VISIBLE);
            isContactConnected = true;

            Log.d(TAG, "updateUI: initialized contact UI as connected");
        }

        if (contact.getPhotoUri() != null) {
            Uri imageUri = Uri.parse(contact.getPhotoUri());

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                contactPicture.setImageBitmap(bitmap);

                Log.d(TAG, "updateUI: loaded contact photo from device");
            } catch (IOException e) {
                Toast.makeText(
                        ChatActivity.this,
                        "Failed to load image from device.",
                        Toast.LENGTH_SHORT
                ).show();

                e.printStackTrace();
            }
        }
    }

    private void initializeViews() {
        rootView = findViewById(R.id.root_view);

        emojiImageView = findViewById(R.id.pick_emoji_btn);

        userMessageInput = findViewById(R.id.input_message);

        ImageView mSendMessageButton = findViewById(R.id.send_message_btn);
        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = userMessageInput.getText().toString();

                if (!TextUtils.isEmpty(message)) {
                    if (message.getBytes().length > ConnectionsClient.MAX_BYTES_DATA_SIZE) {
                        Toast.makeText(
                                ChatActivity.this,
                                "The message is too long!",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Payload messagePayload = Payload.fromBytes(message.getBytes());
                        Nearby.getConnectionsClient(getApplicationContext()).sendPayload(contact.getEndpointID(), messagePayload);
                        userMessageInput.setText("");

                        saveMessageInfoToDatabase(messagePayload, message);
                    }
                }
            }
        });

        Log.d(TAG, "initializeViews: initialized views");
    }

    private void initializeRecyclerView() {
        messagesRecyclerView = findViewById(R.id.chat_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, false);
        messagesRecyclerView.setLayoutManager(layoutManager);

        Log.d(TAG, "initializeRecyclerView: initialized RecyclerView");
    }

    private void saveMessageInfoToDatabase(Payload payload, String message) {
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

        if (!contact.isChat()) {
            contact.setChat(true);

            MainActivity.db.getContactDao().updateContact(contact);

            Log.d(TAG, "saveMessageInfoToDatabase: user is a new chat contact");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == Activity.RESULT_OK) {
            contact = MainActivity.db.getContactDao().findByPhone(contact.getPhone());
            chatAdapter.setContact(contact);
            updateUI(contact);
        }
    }
}
