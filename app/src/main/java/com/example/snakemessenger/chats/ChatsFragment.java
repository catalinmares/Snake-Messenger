package com.example.snakemessenger.chats;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.snakemessenger.PrivateChatActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    private View mChatsFragmentView;
    private RecyclerView mChatsRecyclerView;
    private ChatsAdapter mAdapter;
    private List<Chat> chats;
    private FloatingActionButton mNewChat;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private HashMap<String, Bitmap> profilePictures = new HashMap<>();

    public ChatsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        mChatsFragmentView = inflater.inflate(R.layout.fragment_chats, container, false);
        mNewChat = mChatsFragmentView.findViewById(R.id.new_chat_btn);
        mNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Start a new conversation", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mChatsRecyclerView = mChatsFragmentView.findViewById(R.id.chats_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mChatsRecyclerView.setLayoutManager(layoutManager);

        Query query = db.collection("users")
                .document(currentUser.getUid())
                .collection("chats");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                chats = queryDocumentSnapshots.toObjects(Chat.class);
                mAdapter = new ChatsAdapter(getActivity(), chats);
                mChatsRecyclerView.setAdapter(mAdapter);
            }
        });

        mChatsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mChatsRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Chat currentChat = chats.get(position);

                sendUserToPrivateChat(currentChat.getUserID());
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));

        return mChatsFragmentView;
    }

    private void sendUserToPrivateChat(String userID) {
        Intent privateChatIntent = new Intent(getActivity(), PrivateChatActivity.class);
        privateChatIntent.putExtra("userID", userID);
        startActivity(privateChatIntent);
    }
}
