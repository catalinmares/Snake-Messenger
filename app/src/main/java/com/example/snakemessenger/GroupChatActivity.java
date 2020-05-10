package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {
    private static final int UPDATE_GROUP_SETTINGS = 17;
    private String groupID;
    private String groupName;
    private String adminID;
    private String currentUserID;
    private String groupDescription;
    private boolean groupPicture;

    private Toolbar mToolbar;
    private TextView mGroupName;
    private TextView mGroupDescription;
    private CircleImageView mGroupImage;

    private ImageButton mSendMessageButton;
    private EditText mUserMessageInput;
    private RecyclerView mRecyclerView;
    private ChatAdapter mChatAdapter;
    private List<Message> messages;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        currentUserID = mAuth.getCurrentUser().getUid();

        groupID = getIntent().getExtras().getString("groupID");
        groupName = getIntent().getExtras().getString("name");
        groupDescription = getIntent().getExtras().getString("description");
        adminID = getIntent().getExtras().getString("adminID");
        groupPicture = getIntent().getExtras().getBoolean("picture");

        initializeFields();

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mUserMessageInput.getText().toString();

                if (!TextUtils.isEmpty(message)) {
                    saveMessageInfoToDatabase(message);
                    mUserMessageInput.setText("");
                }
            }
        });
    }

    private void initializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);

        mGroupName = mToolbar.findViewById(R.id.chat_name);
        mGroupName.setText(groupName);

        mGroupDescription = mToolbar.findViewById(R.id.chat_description);
        mGroupDescription.setText(groupDescription);

        mGroupImage = mToolbar.findViewById(R.id.chat_image);

        if (groupPicture) {
            final long TEN_MEGABYTES = 10 * 1024 * 1024;
            storageReference.child(groupID + "-profile_pic")
                    .getBytes(TEN_MEGABYTES)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mGroupImage.setImageBitmap(bitmap);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mGroupImage.setImageResource(R.drawable.group_image);
                            Toast.makeText(GroupChatActivity.this, "Failed to load group picture.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            mGroupImage.setImageResource(R.drawable.group_image);
        }

        mSendMessageButton = findViewById(R.id.send_group_message_btn);
        mUserMessageInput = findViewById(R.id.input_group_message);

        mRecyclerView = findViewById(R.id.group_chat_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(GroupChatActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        Query query = db.collection("groups")
                .document(groupID)
                .collection("messages")
                .orderBy("timestamp");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                messages = queryDocumentSnapshots.toObjects(Message.class);
                setAdapter();
            }
        });
    }

    private void setAdapter() {
        mChatAdapter = new ChatAdapter(messages);
        mRecyclerView.setAdapter(mChatAdapter);
        mRecyclerView.scrollToPosition(messages.size() - 1);
    }

    private void saveMessageInfoToDatabase(String message) {
        Map<String, Object> messageData = new HashMap<String, Object>();
        messageData.put("sender", currentUserID);
        messageData.put("content", message);
        messageData.put("timestamp", Timestamp.now());

        db.collection("groups")
                .document(groupID)
                .collection("messages")
                .document()
                .set(messageData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.group_options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.edit_group_option) {
            if (currentUserID.equals(adminID)) {
                sendUserToEditGroupActivity();
            } else {
                Toast.makeText(
                        GroupChatActivity.this,
                        "You are not allowed to edit this group",
                        Toast.LENGTH_SHORT
                ).show();
            }
        } else if (item.getItemId() == R.id.leave_group_option) {
            leaveGroup();
        }

        return true;
    }

    private void leaveGroup() {

    }

    private void sendUserToEditGroupActivity() {
        Intent editGroupIntent = new Intent(GroupChatActivity.this, CreateGroupActivity.class);
        editGroupIntent.putExtra("new", false);
        editGroupIntent.putExtra("groupID", groupID);
        editGroupIntent.putExtra("name", groupName);
        editGroupIntent.putExtra("description", groupDescription);
        editGroupIntent.putExtra("picture", groupPicture);
        startActivityForResult(editGroupIntent, UPDATE_GROUP_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPDATE_GROUP_SETTINGS && resultCode == RESULT_OK) {
            mGroupName.setText(data.getExtras().getString("name"));
            mGroupDescription.setText(data.getExtras().getString("description"));

            boolean updatedPic = data.getExtras().getBoolean("picture");

            if (updatedPic) {
                final long TEN_MEGABYTES = 10 * 1024 * 1024;
                storageReference.child(groupID + "-profile_pic")
                        .getBytes(TEN_MEGABYTES)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                mGroupImage.setImageBitmap(bitmap);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mGroupImage.setImageResource(R.drawable.group_image);
                                Toast.makeText(GroupChatActivity.this, "Failed to load group picture.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }
}
