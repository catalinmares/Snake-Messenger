package com.example.snakemessenger;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private View friendsFragmentView;
    private RecyclerView mFriendsRecyclerView;
    private FriendsAdapter mAdapter;
    private List<Friend> friends;
    private FloatingActionButton mAddFriend;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private TextView mClosePopup;
    private CircleImageView mUserProfilePicture;
    private TextView mUserProfileName;
    private TextView mUserProfileStatus;
    private Button mRemoveFriend;
    private Button mSendMessage;

    private Dialog userProfileDialog;

    public FriendsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        friendsFragmentView = inflater.inflate(R.layout.fragment_friends, container, false);
        mAddFriend = friendsFragmentView.findViewById(R.id.add_friend_btn);
        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewFriend();
            }
        });

        mFriendsRecyclerView = friendsFragmentView.findViewById(R.id.friends_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mFriendsRecyclerView.setLayoutManager(layoutManager);

        Query query = db.collection("users")
                .document(currentUser.getUid())
                .collection("friends");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                friends = queryDocumentSnapshots.toObjects(Friend.class);
                mAdapter = new FriendsAdapter(getActivity(), friends);
                mFriendsRecyclerView.setAdapter(mAdapter);
            }
        });

        initializeUserProfileDialog();

        mFriendsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mFriendsRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                CircleImageView profilePic = view.findViewById(R.id.friend_image_item);
                BitmapDrawable drawable = (BitmapDrawable) profilePic.getDrawable();
                final Bitmap image = drawable.getBitmap();

                db.collection("users")
                        .document(friends.get(position).getUserID())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                showUserProfile(documentSnapshot.toObject(User.class), image);
                            }
                        });
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));

        return friendsFragmentView;
    }

    private void initializeUserProfileDialog() {
        userProfileDialog = new Dialog(getContext());
        userProfileDialog.setContentView(R.layout.user_profile_layout);

        mClosePopup = userProfileDialog.findViewById(R.id.user_profile_close);
        mUserProfilePicture = userProfileDialog.findViewById(R.id.user_profile_pic);
        mUserProfileName = userProfileDialog.findViewById(R.id.user_profile_name);
        mUserProfileStatus = userProfileDialog.findViewById(R.id.user_profile_status);
        mRemoveFriend = userProfileDialog.findViewById(R.id.user_profile_add_remove_btn);
        mSendMessage = userProfileDialog.findViewById(R.id.user_profile_send_message);

        mClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfileDialog.dismiss();
            }
        });
    }

    private void showUserProfile(User user, Bitmap image) {
        final String userID = user.getUserID();
        String userName = user.getName();
        String userStatus = user.getStatus();
        String userPic = user.getPicture();

        mUserProfileName.setText(userName);
        mUserProfileStatus.setText(userStatus);
        mUserProfilePicture.setImageBitmap(image);

        mRemoveFriend.setText("Remove friend");
        mRemoveFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendListManager.precessRemoveFriend(currentUser.getUid(), userID);

                Toast.makeText(
                        getActivity(),
                        "Friend removed",
                        Toast.LENGTH_SHORT
                ).show();

                userProfileDialog.dismiss();
            }
        });

        mSendMessage.setText("Send message");
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfileDialog.dismiss();
                sendUserToPrivateChat(userID);
            }
        });

        userProfileDialog.show();
    }

    private void sendUserToPrivateChat(String userID) {
        Intent privateChatIntent = new Intent(getActivity(), PrivateChatActivity.class);
        privateChatIntent.putExtra("userID", userID);
        startActivity(privateChatIntent);
    }

    private void addNewFriend() {
        Intent addFriendIntent = new Intent(getContext(), AddFriendActivity.class);
        startActivity(addFriendIntent);
    }
}
