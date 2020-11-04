package com.example.snakemessenger.chats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snakemessenger.DateManager;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateChatActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String userPhone;

    private Toolbar mToolbar;
    private ImageView mBackButton;
    private CircleImageView mContactPicture;
    private ImageView mContactConnected;
    private TextView mContactName;
    private TextView mContactStatus;

    private String contactPhone;
    private boolean isContactConnected;
    private Contact contact;

    private EditText mUserMessageInput;
    private ImageButton mSendMessageButton;

    private RecyclerView mRecyclerView;
    private ChatAdapter mChatAdapter;
    private List<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeToolbar();

        sharedPreferences = getApplicationContext().getSharedPreferences("LOGIN_DETAILS", MODE_PRIVATE);

        userPhone = sharedPreferences.getString("phone", null);
        contactPhone = getIntent().getExtras().getString("phone", "");

        contact = MainActivity.db.getContactDao().findByPhone(contactPhone);

        updateUI(contact);
        initializeViews();

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mUserMessageInput.getText().toString();

                if (!TextUtils.isEmpty(message)) {
                    if (message.getBytes().length > ConnectionsClient.MAX_BYTES_DATA_SIZE) {
                        Toast.makeText(
                                PrivateChatActivity.this,
                                "The message is too long!",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Payload messagePayload = Payload.fromBytes(message.getBytes());
                        Nearby.getConnectionsClient(getApplicationContext()).sendPayload(contact.getEndpointID(), messagePayload);
                        mUserMessageInput.setText("");

                        MainActivity.db.getMessageDao().addMessage(new Message(
                                0,
                                messagePayload.getId(),
                                messagePayload.getType(),
                                contactPhone,
                                message,
                                Calendar.getInstance().getTime(),
                                Message.SENT
                        ));

                        if (!contact.isChat()) {
                            contact.setChat(true);

                            MainActivity.db.getContactDao().updateContact(contact);
                        }
                    }
                }
            }
        });

        MainActivity.db.getContactDao().findChangedByPhone(contactPhone).observe(this, new Observer<Contact>() {
            @Override
            public void onChanged(Contact changedContact) {
                if (changedContact.isConnected() && !isContactConnected) {
                    isContactConnected = true;
                    mContactStatus.setText("Active now");
                    mContactConnected.setVisibility(View.VISIBLE);
                } else if (!changedContact.isConnected() && isContactConnected) {
                    Date currentTime = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    isContactConnected = false;
                    mContactStatus.setText(
                            String.format(
                                    "Last seen %s",
                                    DateManager.getLastActiveText(
                                            df.format(currentTime),
                                            changedContact.getLastActive()
                                    )
                            )
                    );

                    mContactConnected.setVisibility(View.GONE);
                }

                contact = changedContact;
            }
        });

        messages = MainActivity.db.getMessageDao().getMessages(contactPhone);
        initializeMessagesList(messages);

        MainActivity.db.getMessageDao().getLiveMessages(contactPhone).observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                initializeMessagesList(messages);
            }
        });
    }

    private void initializeMessagesList(List<Message> messages) {
        mChatAdapter = new ChatAdapter(getApplicationContext(), messages);
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.scrollToPosition(messages.size() - 1);
    }

    private void initializeToolbar() {
        mToolbar = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mToolbar);

        mContactName = mToolbar.findViewById(R.id.chat_name);
        mContactStatus = mToolbar.findViewById(R.id.chat_description);
        mContactPicture = mToolbar.findViewById(R.id.chat_image);
        mContactConnected = mToolbar.findViewById(R.id.status);

        mBackButton = mToolbar.findViewById(R.id.back_btn);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void updateUI(Contact contact) {
        mContactName.setText(contact.getName());

        if (!contact.isConnected()) {
            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            isContactConnected = false;
            mContactStatus.setText(
                    String.format(
                            "Last seen %s",
                            DateManager.getLastActiveText(
                                    df.format(currentTime),
                                    contact.getLastActive()
                            )
                    )
            );

            mContactConnected.setVisibility(View.GONE);
        } else {
            mContactStatus.setText("Active now");
            mContactConnected.setVisibility(View.VISIBLE);
            isContactConnected = true;
        }

        if (contact.getPhotoUri() != null) {
            Uri imageUri = Uri.parse(contact.getPhotoUri());

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                mContactPicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(
                        PrivateChatActivity.this,
                        "Failed to load image from device.",
                        Toast.LENGTH_SHORT
                ).show();

                e.printStackTrace();
            }
        }
    }

    private void initializeViews() {
        mSendMessageButton = findViewById(R.id.send_message_btn);
        mUserMessageInput = findViewById(R.id.input_message);

        mRecyclerView = findViewById(R.id.chat_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(PrivateChatActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void saveMessageInfoToDatabase(String message) {

    }
}
