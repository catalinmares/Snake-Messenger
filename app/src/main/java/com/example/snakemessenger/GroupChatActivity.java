package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupChatActivity extends AppCompatActivity {
    private String groupName;
    private String adminID;
    private String currentUserID, currentUserName, currentUserPic;
    private Bitmap currentUserProfilePicture;

    private String currentDate, currentTime;

    private Toolbar mToolbar;
    private ImageButton mSendMessageButton;
    private EditText mUserMessageInput;
    private RecyclerView mRecyclerView;
    private GroupChatAdapter mGroupChatAdapter;
    private List<GroupMessage> messages;

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

        groupName = getIntent().getExtras().get("name").toString();
        adminID = getIntent().getExtras().get("adminID").toString();

        initializeFields();
        getUserInfo();

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

        TextView mGroupName = mToolbar.findViewById(R.id.group_chat_name);
        mGroupName.setText(groupName);

        mSendMessageButton = findViewById(R.id.send_group_message_btn);
        mUserMessageInput = findViewById(R.id.input_group_message);

        mRecyclerView = findViewById(R.id.group_chat_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(GroupChatActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        getMessages();
    }

    private void getMessages() {
        messages = new ArrayList<>();
        db.collection("groups")
                .document(groupName + "-" + adminID)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                GroupMessage message = document.toObject(GroupMessage.class);
                                messages.add(message);
                            }

                            setAdapter();
                        }
                    }
                });
    }

    private void setAdapter() {
        mGroupChatAdapter = new GroupChatAdapter(messages);
        mRecyclerView.setAdapter(mGroupChatAdapter);
    }

    private void getUserInfo() {
        db.collection("users")
                .document(currentUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            currentUserName = documentSnapshot.getString("name");
                            currentUserPic = documentSnapshot.getString("picture");

                            assert currentUserPic != null;
                            if (currentUserPic.equals("yes")) {
                                final long ONE_MEGABYTE = 1024 * 1024;

                                storageReference.child(currentUserID + "-profile_pic")
                                        .getBytes(ONE_MEGABYTE)
                                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(GroupChatActivity.this, "Failed to load profile picture.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(GroupChatActivity.this, "Failed to retrieve user information", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveMessageInfoToDatabase(String message) {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MM/yy");
        currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");

        currentTime = currentTimeFormat.format(calForTime.getTime());

        Map<String, Object> messageData = new HashMap<String, Object>();
        messageData.put("senderID", currentUserID);
        messageData.put("date", currentDate);
        messageData.put("time", currentTime);
        messageData.put("content", message);
        messageData.put("timestamp", Timestamp.now());

        db.collection("groups")
                .document(groupName + "-" + adminID)
                .collection("messages")
                .document()
                .set(messageData);

        getMessages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
