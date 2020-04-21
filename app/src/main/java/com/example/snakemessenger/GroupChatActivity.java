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

public class GroupChatActivity extends AppCompatActivity {
    private String groupName;
    private String adminID;
    private String currentUserID;
    private String groupDescription;
    private String groupPicture;

    private String currentDate, currentTime;

    private Toolbar mToolbar;
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

        groupName = getIntent().getExtras().get("name").toString();
        groupDescription = getIntent().getExtras().get("description").toString();
        adminID = getIntent().getExtras().get("adminID").toString();
        groupPicture = getIntent().getExtras().get("picture").toString();

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

        TextView mGroupName = mToolbar.findViewById(R.id.chat_name);
        mGroupName.setText(groupName);

        TextView mGroupDescription = mToolbar.findViewById(R.id.chat_description);
        mGroupDescription.setText(groupDescription);

        final CircleImageView mGroupImage = mToolbar.findViewById(R.id.chat_image);

        if (groupPicture.equals("yes")) {
            final long ONE_MEGABYTE = 1024 * 1024;

            storageReference.child(groupName + "-" + adminID + "-profile_pic")
                    .getBytes(ONE_MEGABYTE)
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
                .document(groupName + "-" + adminID)
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.group_options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
