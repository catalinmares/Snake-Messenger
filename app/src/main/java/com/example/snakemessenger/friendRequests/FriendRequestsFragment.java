package com.example.snakemessenger.friendRequests;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.FriendListManager;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.example.snakemessenger.User;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class FriendRequestsFragment extends Fragment {
    private View friendRequestsFragmentView;
    private RecyclerView mFriendRequestsRecyclerView;
    private FriendRequestsAdapter mAdapter;
    private List<FriendRequest> friendRequests;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private TextView mClosePopup;
    private CircleImageView mUserProfilePicture;
    private TextView mUserProfileName;
    private TextView mUserProfileStatus;
    private Button mAcceptRequest;
    private Button mDeleteRequest;

    private Dialog userProfileDialog;

    public FriendRequestsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        friendRequestsFragmentView = inflater.inflate(R.layout.fragment_friend_requests, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        mFriendRequestsRecyclerView = friendRequestsFragmentView.findViewById(R.id.friend_requests_recycler_view);
        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mFriendRequestsRecyclerView.setLayoutManager(layoutManager);

        Query query = db.collection("requests")
                .whereEqualTo("receiver", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                friendRequests = queryDocumentSnapshots.toObjects(FriendRequest.class);
                mAdapter = new FriendRequestsAdapter(getContext(), friendRequests);
                mFriendRequestsRecyclerView.setAdapter(mAdapter);
            }
        });

        initializeUserProfileDialog();

        mFriendRequestsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                mFriendRequestsRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                CircleImageView profilePic = view.findViewById(R.id.friend_request_image_item);
                BitmapDrawable drawable = (BitmapDrawable) profilePic.getDrawable();
                final Bitmap image = drawable.getBitmap();

                db.collection("users")
                        .document(friendRequests.get(position).getSender())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    showUserProfile(documentSnapshot.toObject(User.class), image);
                                } else {
                                    Toast.makeText(
                                            getActivity(),
                                            "There was an error processing the request",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                        });
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));

        return friendRequestsFragmentView;
    }

    private void initializeUserProfileDialog() {
        userProfileDialog = new Dialog(getContext());
        userProfileDialog.setContentView(R.layout.user_profile_layout);

        mClosePopup = userProfileDialog.findViewById(R.id.user_profile_close);
        mUserProfilePicture = userProfileDialog.findViewById(R.id.user_profile_pic);
        mUserProfileName = userProfileDialog.findViewById(R.id.user_profile_name);
        mUserProfileStatus = userProfileDialog.findViewById(R.id.user_profile_status);
        mAcceptRequest = userProfileDialog.findViewById(R.id.user_profile_add_remove_btn);
        mDeleteRequest = userProfileDialog.findViewById(R.id.user_profile_send_message);

        mClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfileDialog.dismiss();
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

        mAcceptRequest.setText("Accept request");
        mAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("requests")
                        .whereEqualTo("sender", userID)
                        .whereEqualTo("receiver", currentUser.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                                    Toast.makeText(
                                            getActivity(),
                                            "Friend request not available anymore",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                } else {
                                    FriendListManager.processAcceptFriendRequest(currentUser.getUid(), userID);

                                    Toast.makeText(
                                            getActivity(),
                                            "Request accepted",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });

                userProfileDialog.dismiss();
            }
        });

        mDeleteRequest.setText("Delete request");
        mDeleteRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("requests")
                        .whereEqualTo("sender", userID)
                        .whereEqualTo("receiver", currentUser.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                                    Toast.makeText(
                                            getActivity(),
                                            "Friend request not available anymore",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                } else {
                                    FriendListManager.processDeleteFriendRequest(currentUser.getUid(), userID);

                                    Toast.makeText(
                                            getActivity(),
                                            "Request deleted",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });

                userProfileDialog.dismiss();
            }
        });

        userProfileDialog.show();
    }
}
