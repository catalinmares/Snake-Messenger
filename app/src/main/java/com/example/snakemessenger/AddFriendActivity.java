package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
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
    private StorageReference storageReference;

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
        storageReference = FirebaseStorage.getInstance().getReference();

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
                showUserProfile(users.get(position));
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

    private void showUserProfile(final User user) {
        final String userID = user.getUserID();
        String userName = user.getName();
        String userStatus = user.getStatus();
        String userPic = user.getPicture();

        mUserProfileName.setText(userName);
        mUserProfileStatus.setText(userStatus);

        if (userPic.equals("yes")) {
            final long ONE_MEGABYTE = 1024 * 1024;

            storageReference.child(userID + "-profile_pic")
                    .getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mUserProfilePicture.setImageBitmap(bitmap);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddFriendActivity.this, "Failed to load profile picture.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        db.collection("users")
                .document(currentUserID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        final User activeUser = documentSnapshot.toObject(User.class);

                        db.collection("users")
                                .document(userID)
                                .collection("friend requests")
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        boolean friendRequestFound = false;
                                        for (DocumentSnapshot documentSnapshot1 : queryDocumentSnapshots) {
                                            if (documentSnapshot1.getString("userID").equals(currentUserID)) {
                                                friendRequestFound = true;
                                                break;
                                            }
                                        }

                                        if (friendRequestFound) {
                                            mAddFriend.setText("Cancel request");
                                            mAddFriend.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    cancelFriendRequest(activeUser, userID);
                                                }
                                            });
                                        } else if (!activeUser.getFriends().contains(userID)) {
                                            db.collection("users")
                                                    .document(currentUserID)
                                                    .collection("friend requests")
                                                    .get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            boolean friendRequestRecieved = false;
                                                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                                                if (document.getString("userID").equals(userID)) {
                                                                    friendRequestRecieved = true;
                                                                    break;
                                                                }
                                                            }

                                                            if (friendRequestRecieved) {
                                                                mAddFriend.setText("Accept request");
                                                                mAddFriend.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        acceptFriendRequest(activeUser, userID);
                                                                    }
                                                                });

                                                                mSendMessage.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        Toast.makeText(
                                                                                AddFriendActivity.this,
                                                                                "You need to accept the friend request first",
                                                                                Toast.LENGTH_SHORT
                                                                        ).show();
                                                                    }
                                                                });
                                                            } else {
                                                                mAddFriend.setText("Add friend");
                                                                mAddFriend.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        addFriend(activeUser, userID);
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                        } else {
                                            mAddFriend.setText("Remove friend");
                                            mAddFriend.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    removeFriend(activeUser, userID);
                                                }
                                            });
                                        }
                                    }
                                });
                    }
                });

        userProfile.show();
    }

    private void addFriend(final User activeUser, final String userID) {
        Map<String, Object> friendRequestData = new HashMap<>();
        friendRequestData.put("userID", currentUserID);

        db.collection("users")
                .document(userID)
                .collection("friend requests")
                .document(activeUser.getUserID())
                .set(friendRequestData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mAddFriend.setText("Cancel request");
                            Toast.makeText(
                                    AddFriendActivity.this,
                                    "Friend request sent",
                                    Toast.LENGTH_SHORT
                            ).show();

                            mAddFriend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cancelFriendRequest(activeUser, userID);
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
                        } else {
                            Toast.makeText(
                                    AddFriendActivity.this,
                                    "Couldn't send the friend request",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
    }

    private void cancelFriendRequest(final User activeUser, final String userID) {
        mAddFriend.setText("Add friend");
        Toast.makeText(
                AddFriendActivity.this,
                "Request canceled",
                Toast.LENGTH_SHORT
        ).show();

        db.collection("users")
                .document(userID)
                .collection("friend requests")
                .document(activeUser.getUserID())
                .delete();

        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(activeUser, userID);
            }
        });

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

    private void acceptFriendRequest(final User activeUser, final String userID) {
        mAddFriend.setText("Remove friend");
        Toast.makeText(
                AddFriendActivity.this,
                "Request accepted",
                Toast.LENGTH_SHORT
        ).show();

        List<String> friends = activeUser.getFriends();
        friends.add(userID);
        db.collection("users")
                .document(activeUser.getUserID())
                .update("friends", friends);

        db.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User otherUser = documentSnapshot.toObject(User.class);

                        List<String> otherFriends = otherUser.getFriends();
                        otherFriends.add(activeUser.getUserID());

                        db.collection("users")
                                .document(userID)
                                .update("friends", otherFriends);
                    }
                });

        db.collection("users")
                .document(activeUser.getUserID())
                .collection("friend requests")
                .document(userID)
                .delete();

        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFriend(activeUser, userID);
            }
        });

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPrivateChat(userID);
            }
        });

        Map<String, Object> userData = new HashMap<>();
        userData.put("userID", userID);

        db.collection("users")
                .document(activeUser.getUserID())
                .collection("friends")
                .document(userID)
                .set(userData);

        Map<String, Object> activeUserData = new HashMap<>();
        activeUserData.put("userID", activeUser.getUserID());

        db.collection("users")
                .document(userID)
                .collection("friends")
                .document(activeUser.getUserID())
                .set(activeUserData);
    }

    private void removeFriend(final User activeUser, final String userID) {
        mAddFriend.setText("Add friend");
        Toast.makeText(
                AddFriendActivity.this,
                "Friend removed",
                Toast.LENGTH_SHORT
        ).show();

        List<String> myFriends = activeUser.getFriends();
        myFriends.remove(userID);
        db.collection("users")
                .document(currentUserID)
                .update("friends", myFriends);

        db.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User otherUser = documentSnapshot.toObject(User.class);

                        List<String> friends = otherUser.getFriends();
                        friends.remove(currentUserID);

                        db.collection("users")
                                .document(userID)
                                .update("friends", friends);
                    }
                });

        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend(activeUser, userID);
            }
        });

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

        db.collection("users")
                .document(userID)
                .collection("friends")
                .document(activeUser.getUserID())
                .delete();

        db.collection("users")
                .document(activeUser.getUserID())
                .collection("friends")
                .document(userID)
                .delete();
    }

    private void sendUserToPrivateChat(String userID) {

    }
}
