package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendActivity extends AppCompatActivity {
    private EditText mSearchName;
    private ImageButton mSearch;

    private TextView mClosePopup;
    private CircleImageView mUserProfilePicture;
    private TextView mUserProfileName;
    private TextView mUserProfileStatus;
    private Button mAddFriend;
    private Button mSendMessage;

    private RecyclerView mUsersRecyclerView;
    private UsersAdapter mAdapter;
    private List<User> users;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private Dialog userProfile;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        Toolbar mToolbar = findViewById(R.id.add_friend_toolbar);
        setSupportActionBar(mToolbar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        db = FirebaseFirestore.getInstance();

        mSearchName = mToolbar.findViewById(R.id.friend_search_name);
        mSearch = mToolbar.findViewById(R.id.friend_search_btn);

        mUsersRecyclerView = findViewById(R.id.add_friend_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(AddFriendActivity.this, LinearLayoutManager.VERTICAL, false);
        mUsersRecyclerView.setLayoutManager(layoutManager);

        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mSearchName.getText().toString();

                if (!TextUtils.isEmpty(name)) {
                    displaySearchResults(name);
                } else {
                    Toast.makeText(AddFriendActivity.this, "Please type a name first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        initializeUserProfileDialog();

        mUsersRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(AddFriendActivity.this, mUsersRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                CircleImageView userPic = view.findViewById(R.id.friend_image_item);
                BitmapDrawable drawable = (BitmapDrawable) userPic.getDrawable();
                Bitmap image = drawable.getBitmap();
                showUserProfile(users.get(position), image);
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));
    }

    private void initializeUserProfileDialog() {
        userProfile = new Dialog(AddFriendActivity.this);
        userProfile.setContentView(R.layout.user_profile_layout);

        mClosePopup = userProfile.findViewById(R.id.user_profile_close);
        mUserProfilePicture = userProfile.findViewById(R.id.user_profile_pic);
        mUserProfileName = userProfile.findViewById(R.id.user_profile_name);
        mUserProfileStatus = userProfile.findViewById(R.id.user_profile_status);
        mAddFriend = userProfile.findViewById(R.id.user_profile_add_remove_btn);
        mSendMessage = userProfile.findViewById(R.id.user_profile_send_message);

        mClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfile.dismiss();
            }
        });
    }

    private void displaySearchResults(final String name) {
        users = new ArrayList<User>();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);

                                if (user.getName().contains(name) && !user.getUserID().equals(currentUserID)) {
                                    users.add(user);
                                }
                            }

                            mAdapter = new UsersAdapter(AddFriendActivity.this, users);
                            mUsersRecyclerView.setAdapter(mAdapter);
                        }
                    }
                });
    }

    private void showUserProfile(final User user, Bitmap image) {
        final String userID = user.getUserID();
        String userName = user.getName();
        String userStatus = user.getStatus();

        mUserProfileName.setText(userName);
        mUserProfileStatus.setText(userStatus);
        mUserProfilePicture.setImageBitmap(image);

        db.collection("users")
                .document(currentUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            List<String> friends = (List<String>) userData.get("friends");

                            if (friends.contains(userID)) {
                                initializeUserFriendsFields(userID);
                            } else {
                                db.collection("requests")
                                        .whereEqualTo("sender", currentUserID)
                                        .whereEqualTo("receiver", userID)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    List<DocumentSnapshot> docs = task.getResult().getDocuments();

                                                    if (!docs.isEmpty()) {
                                                        initializeUserFriendRequestSent(userID);
                                                    } else {
                                                        db.collection("requests")
                                                                .whereEqualTo("sender", userID)
                                                                .whereEqualTo("receiver", currentUserID)
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            List<DocumentSnapshot> docs = task.getResult().getDocuments();

                                                                            if (!docs.isEmpty()) {
                                                                                initializeUserFriendRequestReceived(userID);
                                                                            } else {
                                                                                initializeUserNotFriendsFields(userID);
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });

        userProfile.show();
    }

    private void initializeUserFriendsFields(final String userID) {
        mAddFriend.setText("Remove friend");
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFriend(userID);
            }
        });

        mSendMessage.setText("Send message");
        mSendMessage.setActivated(true);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPrivateChat(userID);
            }
        });
    }

    private void initializeUserNotFriendsFields(final String userID) {
        mAddFriend.setText("Add friend");
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(userID);
            }
        });

        mSendMessage.setText("Send message");
        mSendMessage.setActivated(false);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        AddFriendActivity.this,
                        "You need to send the user a friend request first",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void initializeUserFriendRequestSent(final String userID) {
        mAddFriend.setText("Cancel request");
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelFriendRequest(userID);
            }
        });

        mSendMessage.setText("Send message");
        mSendMessage.setActivated(false);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        AddFriendActivity.this,
                        "The user didn't accept your request yet",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void initializeUserFriendRequestReceived(final String userID) {
        mAddFriend.setText("Accept request");
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptFriendRequest(userID);
            }
        });

        mSendMessage.setText("Delete request");
        mSendMessage.setActivated(true);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFriendRequest(userID);
            }
        });
    }

    private void addFriend(final String userID) {
        FriendListManager.processAddFriend(currentUserID, userID);

        mAddFriend.setText("Cancel request");
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelFriendRequest(userID);
            }
        });

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        AddFriendActivity.this,
                        "The user didn't accept your request yet",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Toast.makeText(
                AddFriendActivity.this,
                "Friend request sent",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void removeFriend(final String userID) {
        FriendListManager.precessRemoveFriend(currentUserID, userID);

        mAddFriend.setText("Add friend");
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(userID);
            }
        });

        mSendMessage.setText("Send message");
        mSendMessage.setActivated(false);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        AddFriendActivity.this,
                        "You need to send the user a friend request first",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Toast.makeText(
                AddFriendActivity.this,
                "Friend removed",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void acceptFriendRequest(final String userID) {
        FriendListManager.processAcceptFriendRequest(currentUserID, userID);

        mAddFriend.setText("Remove friend");
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFriend(userID);
            }
        });

        mSendMessage.setText("Send message");
        mSendMessage.setActivated(true);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPrivateChat(userID);
            }
        });

        Toast.makeText(
                AddFriendActivity.this,
                "Request accepted",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void deleteFriendRequest(final String userID) {
        FriendListManager.processDeleteFriendRequest(currentUserID, userID);

        mAddFriend.setText("Add friend");
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(userID);
            }
        });

        mSendMessage.setText("Send message");
        mSendMessage.setActivated(false);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        AddFriendActivity.this,
                        "You need to send the user a friend request first",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Toast.makeText(
                AddFriendActivity.this,
                "Request deleted",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void cancelFriendRequest(final String userID) {
        FriendListManager.processDeleteFriendRequest(userID, currentUserID);

        mAddFriend.setText("Add friend");
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(userID);
            }
        });

        mSendMessage.setText("Send message");
        mSendMessage.setActivated(false);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        AddFriendActivity.this,
                        "You need to send the user a friend request first",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        Toast.makeText(
                AddFriendActivity.this,
                "Request canceled",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void sendUserToPrivateChat(String userID) {
        Intent privateChatIntent = new Intent(AddFriendActivity.this, PrivateChatActivity.class);
        privateChatIntent.putExtra("userID", userID);
        startActivity(privateChatIntent);
    }
}
