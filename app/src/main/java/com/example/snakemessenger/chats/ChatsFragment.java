package com.example.snakemessenger.chats;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.example.snakemessenger.general.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import static com.example.snakemessenger.MainActivity.db;

public class ChatsFragment extends Fragment {
    public static final String TAG = "[ChatsFragment]";

    private View chatsFragmentView;

    private TextView noChats;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView chatsRecyclerView;
    private ChatsAdapter chatsAdapter;

    public ChatsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        chatsFragmentView = inflater.inflate(R.layout.fragment_chats, container, false);

        initializeViews();
        initializeRecyclerView();

        db.getContactDao().getLiveChatContacts().observe(getViewLifecycleOwner(), contacts -> {
            if (contacts.size() > 0) {
                noChats.setVisibility(View.GONE);
                chatsRecyclerView.setVisibility(View.VISIBLE);
            } else {
                noChats.setVisibility(View.VISIBLE);
                chatsRecyclerView.setVisibility(View.GONE);
            }

            chatsAdapter.setChats(contacts);
        });

        db.getMessageDao().getLiveMessage().observe(getViewLifecycleOwner(), message -> {
            List<Contact> contacts = db.getContactDao().getChatContacts();

            if (contacts.size() > 0) {
                noChats.setVisibility(View.GONE);
                chatsRecyclerView.setVisibility(View.VISIBLE);
            } else {
                noChats.setVisibility(View.VISIBLE);
                chatsRecyclerView.setVisibility(View.GONE);
            }

            chatsAdapter.setChats(contacts);
        });

        return chatsFragmentView;
    }

    private void initializeViews() {
        swipeRefreshLayout = chatsFragmentView.findViewById(R.id.refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            List<Contact> contacts = db.getContactDao().getChatContacts();

            if (contacts.size() > 0) {
                noChats.setVisibility(View.GONE);
                chatsRecyclerView.setVisibility(View.VISIBLE);
            } else {
                noChats.setVisibility(View.VISIBLE);
                chatsRecyclerView.setVisibility(View.GONE);
            }

            chatsAdapter.setChats(contacts);

            swipeRefreshLayout.setRefreshing(false);
        });

        noChats = chatsFragmentView.findViewById(R.id.no_chats);
        chatsAdapter = new ChatsAdapter(getContext(), new ArrayList<>());

        FloatingActionButton mNewChat = chatsFragmentView.findViewById(R.id.new_chat_btn);
        mNewChat.setOnClickListener(view -> sendUserToSendMessageActivity());
    }

    private void initializeRecyclerView() {
        chatsRecyclerView = chatsFragmentView.findViewById(R.id.chats_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        chatsRecyclerView.setLayoutManager(layoutManager);

        chatsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                chatsRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                Contact currentChatContact = chatsAdapter.getChats().get(position);
                sendUserToPrivateChat(currentChatContact);
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));

        chatsRecyclerView.setAdapter(chatsAdapter);

        Log.d(TAG, "initializeRecyclerView: initialized RecyclerView");
    }

    private void sendUserToSendMessageActivity() {
        Intent sendMessageIntent = new Intent(getActivity(), SendMessageActivity.class);
        startActivity(sendMessageIntent);
    }

    private void sendUserToPrivateChat(Contact contact) {
        Intent privateChatIntent = new Intent(getActivity(), ChatActivity.class);
        privateChatIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
        startActivity(privateChatIntent);
    }
}
