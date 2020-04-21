package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton mSendMessageButton;
    private EditText mUserMessageInput;
    private RecyclerView mRecyclerView;
    private ChatAdapter mChatAdapter;
    private List<Message> messages;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private String currentUserID;
    private String friendID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        currentUserID = mAuth.getCurrentUser().getUid();

        friendID = getIntent().getExtras().getString("userID");

        updateUI();
        initializeViews();

        mSendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String message = mUserMessageInput.getText().toString();

                if (!TextUtils.isEmpty(message)) {
                    db.collection("users")
                            .document(currentUserID)
                            .collection("friends")
                            .document(friendID)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        saveMessageInfoToDatabase(message);
                                        mUserMessageInput.setText("");
                                    } else {
                                        Toast.makeText(
                                                PrivateChatActivity.this,
                                                "You are no longer friends. Please make friends again before chatting",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    private void updateUI() {
        mToolbar = findViewById(R.id.private_chat_bar_layout);
        setSupportActionBar(mToolbar);

        final TextView mFriendName = mToolbar.findViewById(R.id.chat_name);
        final TextView mFriendStatus = mToolbar.findViewById(R.id.chat_description);
        final CircleImageView mFriendProfilePic = mToolbar.findViewById(R.id.chat_image);


        db.collection("users")
                .document(friendID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDoc = task.getResult();

                            if (userDoc.exists()) {
                                User user = userDoc.toObject(User.class);

                                mFriendName.setText(user.getName());
                                mFriendStatus.setText(user.getStatus());

                                if (user.getPicture().equals("yes")) {
                                    final long ONE_MEGABYTE = 1024 * 1024;

                                    storageReference.child(user.getUserID() + "-profile_pic")
                                            .getBytes(ONE_MEGABYTE)
                                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                @Override
                                                public void onSuccess(byte[] bytes) {
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                    mFriendProfilePic.setImageBitmap(bitmap);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(
                                                            PrivateChatActivity.this,
                                                            "Failed to load friend's profile picture.",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(
                                        PrivateChatActivity.this,
                                        "There was an error retrieving user data",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        } else {
                            Toast.makeText(
                                    PrivateChatActivity.this,
                                    "There was an error retrieving user data",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }

    private void initializeViews() {
        mSendMessageButton = findViewById(R.id.send_private_message_btn);
        mUserMessageInput = findViewById(R.id.input_private_message);

        mRecyclerView = findViewById(R.id.private_chat_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(PrivateChatActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        Query query = db.collection("users")
                .document(currentUserID)
                .collection("friends")
                .document(friendID)
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
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MM/yy");
        String currentDate = currentDateFormat.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
        String currentTime = currentTimeFormat.format(calForTime.getTime());

        Map<String, Object> messageData = new HashMap<String, Object>();
        messageData.put("senderID", currentUserID);
        messageData.put("date", currentDate);
        messageData.put("time", currentTime);
        messageData.put("content", message);
        messageData.put("timestamp", Timestamp.now());

        db.collection("users")
                .document(currentUserID)
                .collection("friends")
                .document(friendID)
                .collection("messages")
                .document()
                .set(messageData);

        db.collection("users")
                .document(friendID)
                .collection("friends")
                .document(currentUserID)
                .collection("messages")
                .document()
                .set(messageData);

        db.collection("users")
                .document(currentUserID)
                .collection("chats")
                .document(friendID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Map<String, Object> userChat = new HashMap<>();
                            userChat.put("userID", friendID);

                            db.collection("users")
                                    .document(currentUserID)
                                    .collection("chats")
                                    .document(friendID)
                                    .set(userChat);
                        }
                    }
                });

        db.collection("users")
                .document(friendID)
                .collection("chats")
                .document(currentUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            Map<String, Object> friendChat = new HashMap<>();
                            friendChat.put("userID", currentUserID);

                            db.collection("users")
                                    .document(friendID)
                                    .collection("chats")
                                    .document(currentUserID)
                                    .set(friendChat);
                        }
                    }
                });

    }
}
