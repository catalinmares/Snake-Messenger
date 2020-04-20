package com.example.snakemessenger.friendRequests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FriendRequestsFragment extends Fragment {
    private View friendRequestsFragmentView;
    private RecyclerView mFriendRequestsRecyclerView;
    private FriendRequestsAdapter mAdapter;
    private List<FriendRequest> friendRequests;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

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

        Query query = db.collection("users")
                .document(currentUser.getUid())
                .collection("friend requests");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                friendRequests = queryDocumentSnapshots.toObjects(FriendRequest.class);
                setAdapter();
            }
        });

        return friendRequestsFragmentView;
    }

    private void setAdapter() {
        mAdapter = new FriendRequestsAdapter(getContext(), friendRequests);
        mFriendRequestsRecyclerView.setAdapter(mAdapter);
    }
}
