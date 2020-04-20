package com.example.snakemessenger;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;


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

        return friendsFragmentView;
    }

    private void addNewFriend() {
        Intent addFriendIntent = new Intent(getContext(), AddFriendActivity.class);
        startActivity(addFriendIntent);
    }
}
