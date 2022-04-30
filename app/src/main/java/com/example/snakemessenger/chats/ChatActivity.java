package com.example.snakemessenger.chats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.example.snakemessenger.databinding.ActivityChatBinding;
import com.example.snakemessenger.general.Constants;
import com.example.snakemessenger.general.Utilities;
import com.example.snakemessenger.managers.CommunicationManager;
import com.example.snakemessenger.managers.DateManager;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.models.Contact;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static com.example.snakemessenger.MainActivity.db;
import static com.example.snakemessenger.MainActivity.myDeviceId;
import static com.example.snakemessenger.MainActivity.currentChat;

public class ChatActivity extends AppCompatActivity {
    public static final String TAG = "[ChatActivity]";

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

    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeToolbar();

        String contactDeviceId = Objects.requireNonNull(getIntent().getStringExtra(Constants.EXTRA_CONTACT_DEVICE_ID));
        contact = db.getContactDao().findByDeviceId(contactDeviceId);

        if (MainActivity.notificationMessages.get(contact.getId()) != null) {
            MainActivity.notificationMessages.put(contact.getId(), new ArrayList<>());
        }

        updateUI(contact);
        initializeViews();
        initializeRecyclerView();

        EmojIconActions emojIconActions = new EmojIconActions(this, rootView, userMessageInput, emojiImageView);
        emojIconActions.ShowEmojIcon();
        emojIconActions.setIconsIds(R.drawable.ic_baseline_keyboard_24, R.drawable.ic_baseline_emoji_emotions_24);

        db.getContactDao().findChangedByDeviceId(contactDeviceId).observe(this, changedContact -> {
            if (changedContact.isConnected() && !isContactConnected) {
                isContactConnected = true;
                contactStatus.setText(R.string.active_now);
                contactConnected.setVisibility(View.VISIBLE);
            } else if (!changedContact.isConnected() && isContactConnected) {
                Date currentTime = new Date(System.currentTimeMillis());
                SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);

                isContactConnected = false;

                contactStatus.setText(
                        String.format(
                                "Last seen %s",
                                DateManager.getLastActiveText(
                                        df.format(currentTime),
                                        df.format(new Date(contact.getLastActive()))
                                )
                        )
                );

                contactConnected.setVisibility(View.GONE);
            }

            contact = changedContact;
        });

        chatAdapter = new ChatAdapter(this, new ArrayList<>(), contact);
        messagesRecyclerView.setAdapter(chatAdapter);

        db.getMessageDao().getLiveMessages(myDeviceId, contactDeviceId).observe(this, newMessages -> {
            chatAdapter.setMessages(newMessages);
            messagesRecyclerView.scrollToPosition(newMessages.size() - 1);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentChat = contact.getDeviceID();
    }

    @Override
    protected void onStop() {
        currentChat = null;
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
        }

        return true;
    }

    private void initializeToolbar() {
        Toolbar mToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);

        contactName = mToolbar.findViewById(R.id.chat_name);
        contactStatus = mToolbar.findViewById(R.id.chat_description);
        contactPicture = mToolbar.findViewById(R.id.chat_image);
        contactConnected = mToolbar.findViewById(R.id.status);

        ImageView mBackButton = mToolbar.findViewById(R.id.back_btn);
        mBackButton.setOnClickListener(view -> onBackPressed());

        Log.d(TAG, "initializeToolbar: initialized toolbar");
    }

    private void initializeViews() {
        rootView = findViewById(R.id.root_view);

        ImageView cameraImageView = findViewById(R.id.pick_picture_btn);
        cameraImageView.setOnClickListener(v -> Utilities.showImagePickDialog(ChatActivity.this));
        emojiImageView = findViewById(R.id.pick_emoji_btn);

        userMessageInput = findViewById(R.id.input_message);

        ImageView sendMessageButton = findViewById(R.id.send_message_btn);
        sendMessageButton.setOnClickListener(view -> {
            String message = userMessageInput.getText().toString();

            if (!TextUtils.isEmpty(message)) {
                if (message.getBytes().length > ConnectionsClient.MAX_BYTES_DATA_SIZE) {
                    Toast.makeText(ChatActivity.this, Constants.TOAST_MESSAGE_TOO_LONG, Toast.LENGTH_SHORT).show();
                } else {
                    new Thread(() -> CommunicationManager.buildAndDeliverMessage(getApplicationContext(), message, contact)).start();
                    userMessageInput.setText("");
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

    private void updateUI(Contact contact) {
        contactName.setText(contact.getName());

        if (!contact.isConnected()) {
            Date currentTime = new Date(System.currentTimeMillis());
            SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.US);

            isContactConnected = false;

            contactStatus.setText(
                    String.format(
                            "Last seen %s",
                            DateManager.getLastActiveText(
                                    df.format(currentTime),
                                    df.format(new Date(contact.getLastActive()))
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
                Toast.makeText(ChatActivity.this, Constants.TOAST_FAILED_TO_LOAD_IMAGE, Toast.LENGTH_SHORT).show();

                e.printStackTrace();
            }
        }
    }

    private void sendUserToEditProfileActivity(Contact contact) {
        Log.d(TAG, "sendUserToSettingActivity: starting edit contact activity...");

        Intent editContactIntent = new Intent(ChatActivity.this, EditProfileActivity.class);
        editContactIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
        startActivityForResult(editContactIntent, Constants.REQUEST_EDIT_PROFILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_EDIT_PROFILE && resultCode == Activity.RESULT_OK) {
            contact = db.getContactDao().findByDeviceId(contact.getDeviceID());
            chatAdapter.setContact(contact);
            updateUI(contact);
        } else if (requestCode == Constants.REQUEST_PREVIEW_PICTURE && resultCode == Activity.RESULT_OK) {
            new Thread(() -> {
                CommunicationManager.buildAndDeliverImageMessage(getApplicationContext(), PreviewPictureActivity.imageBitmap, imagePath, contact);
                PreviewPictureActivity.imageBitmap = null;
                imagePath = null;
            }).start();
            Toast.makeText(ChatActivity.this, "Picture sent!", Toast.LENGTH_SHORT).show();
        } else if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            if (extras != null) {
                PreviewPictureActivity.imageBitmap = (Bitmap) extras.get(Constants.EXTRA_IMAGE_CAPTURE_DATA);

                Intent previewPictureIntent = new Intent(ChatActivity.this, PreviewPictureActivity.class);
                startActivityForResult(previewPictureIntent, Constants.REQUEST_PREVIEW_PICTURE);
            }
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            if (imageUri != null) {
                imagePath = imageUri.toString();
            }

            try {
                PreviewPictureActivity.imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            Intent previewPictureIntent = new Intent(ChatActivity.this, PreviewPictureActivity.class);
            startActivityForResult(previewPictureIntent, Constants.REQUEST_PREVIEW_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchTakePictureIntent(ChatActivity.this);
            } else {
                Toast.makeText(ChatActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchPickPictureIntent(ChatActivity.this);
            } else {
                Toast.makeText(ChatActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
