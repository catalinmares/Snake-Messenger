package com.example.snakemessenger.chats;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.example.snakemessenger.database.Contact;
import com.example.snakemessenger.database.Message;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.Payload;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class SendMessageActivity extends AppCompatActivity {
    public static final String TAG = SendMessageActivity.class.getSimpleName();

    private TextView mTitle;
    private EditText mSearchEdit;
    private ImageView mSearchButton;

    private TextView mSelectedContacts;
    private CheckBox mSelectAll;
    private TextView mNoContacts;

    private RecyclerView mRecyclerView;
    private CustomContactsAdapter mContactsAdapter;
    private List<ContactWrapper> contactWrappers;

    private View rootView;
    private ImageView emojiImageView;
    private EmojiconEditText mMessageContent;

    private List<String> selectedContacts;
    private boolean isSearchButtonPressed;
    private boolean multipleSelection;
    private boolean selected;
    private boolean unselected;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        selectedContacts = new ArrayList<>();
        isSearchButtonPressed = false;
        multipleSelection = false;
        selected = false;
        unselected = false;

        initializeToolbar();
        initializeViews();
        initializeRecyclerView();

        EmojIconActions emojIconActions = new EmojIconActions(this, rootView, mMessageContent, emojiImageView);
        emojIconActions.ShowEmojIcon();
        emojIconActions.setIconsIds(R.drawable.ic_baseline_keyboard_24, R.drawable.ic_baseline_emoji_emotions_24);

        MainActivity.db.getContactDao().getAllContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                List<ContactWrapper> changedContactWrappers = new ArrayList<>();

                for (Contact contact : contacts) {
                    boolean selected = isContactSelected(contact);

                    if (!selected) {
                        changedContactWrappers.add(new ContactWrapper(contact, false));
                    } else {
                        changedContactWrappers.add(new ContactWrapper(contact, true));
                    }
                }

                contactWrappers = changedContactWrappers;
                initializeContactsList(contactWrappers);
            }
        });

        List<Contact> contacts = MainActivity.db.getContactDao().getContacts();
        contactWrappers = new ArrayList<>();

        for (Contact contact : contacts) {
            contactWrappers.add(new ContactWrapper(contact, false));
        }

        initializeContactsList(contactWrappers);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: back button was pressed");

        if (isSearchButtonPressed) {
            Log.d(TAG, "onBackPressed: user was searching a custom contact. Reset toolbar to default state");

            mSearchEdit.setText("");
            mSearchEdit.setVisibility(View.GONE);
            mTitle.setVisibility(View.VISIBLE);
            mSearchButton.setVisibility(View.VISIBLE);

            isSearchButtonPressed = false;
        } else if (multipleSelection) {
            Log.d(TAG, "onBackPressed: user had selected some contacts. Reset contacts selected to none");

            multipleSelection = false;

            selectedContacts.clear();
            mSelectedContacts.setText("No contacts selected.");
            mSelectAll.setChecked(false);
        } else {
            Log.d(TAG, "onBackPressed: leaving activity...");
            super.onBackPressed();
        }
    }

    private void initializeToolbar() {
        Toolbar mToolbar = findViewById(R.id.send_message_bar_layout);
        setSupportActionBar(mToolbar);

        mTitle = mToolbar.findViewById(R.id.title);

        mSearchEdit = mToolbar.findViewById(R.id.search_edit_text);
        mSearchEdit.setVisibility(View.GONE);
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String requestedName = editable.toString();

                Log.d(TAG, "afterTextChanged: edit text changed content to " + requestedName);

                List<Contact> contacts;
                List<ContactWrapper> newContactWrappers = new ArrayList<>();

                if (!requestedName.equals("")) {
                    String formattedName = formatNameRequest(requestedName);

                    Log.d(TAG, "afterTextChanged: user requested custom name " + formattedName);

                    contacts = MainActivity.db.getContactDao().getMatchingContacts("%" + formattedName + "%");

                } else {
                    Log.d(TAG, "afterTextChanged: user removed edit text content");

                    contacts = MainActivity.db.getContactDao().getContacts();
                }

                Log.d(TAG, "afterTextChanged: request matched " + contacts.size() + " contacts");

                for (Contact contact : contacts) {
                    Log.d(TAG, "afterTextChanged: parsing contact " + contact.getName());

                    boolean selected = isContactSelected(contact);

                    if (!selected) {
                        Log.d(TAG, "afterTextChanged: no previous selection found for " + contact.getName());

                        newContactWrappers.add(new ContactWrapper(contact, false));
                    } else {
                        Log.d(TAG, "afterTextChanged: " + contact.getName() + " was previously selected");

                        newContactWrappers.add(new ContactWrapper(contact, true));
                    }
                }

                contactWrappers = newContactWrappers;
                initializeContactsList(contactWrappers);
            }
        });

        mSearchButton = mToolbar.findViewById(R.id.search_btn);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: search button clicked");

                isSearchButtonPressed = true;

                mTitle.setVisibility(View.GONE);
                mSearchEdit.setVisibility(View.VISIBLE);
                mSearchButton.setVisibility(View.GONE);
            }
        });

        ImageView mBackButton = mToolbar.findViewById(R.id.back_btn);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Log.d(TAG, "initializeToolbar: initialized toolbar");
    }

    private void initializeViews() {
        rootView = findViewById(R.id.root_view);

        emojiImageView = findViewById(R.id.pick_emoji_btn);

        mSelectedContacts = findViewById(R.id.selected_contacts);

        mSelectAll = findViewById(R.id.select_all);
        mSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                List<Contact> contacts;
                List<ContactWrapper> newContactWrappers = new ArrayList<>();

                if (isChecked) {
                    Log.d(TAG, "onCheckedChanged: checkbox was checked");

                    contacts = MainActivity.db.getContactDao().getContacts();

                    if (!selected) {
                        selectedContacts.clear();

                        for (Contact contact : contacts) {
                            selectedContacts.add(contact.getPhone());
                        }

                        mSearchEdit.setText("");
                        mSearchEdit.setVisibility(View.GONE);
                        mTitle.setVisibility(View.VISIBLE);
                        mSearchButton.setVisibility(View.VISIBLE);

                        isSearchButtonPressed = false;
                    }

                    selected = false;
                    multipleSelection = true;
                } else {
                    Log.d(TAG, "onCheckedChanged: checkbox was unchecked");

                    if (!unselected) {
                        selectedContacts.clear();
                        multipleSelection = false;
                    }

                    if (isSearchButtonPressed) {
                        Log.d(TAG, "onCheckedChanged: edit text is active");

                        String requestedName = mSearchEdit.getText().toString();

                        if (!requestedName.equals("")) {
                            Log.d(TAG, "onCheckedChanged: user requested custom name");

                            String formattedName = formatNameRequest(requestedName);

                            contacts = MainActivity.db.getContactDao().getMatchingContacts("%" + formattedName + "%");
                        } else {
                            Log.d(TAG, "onCheckedChanged: user did not search for anything");
                            contacts = MainActivity.db.getContactDao().getContacts();
                        }
                    } else {
                        Log.d(TAG, "onCheckedChanged: edit text is not active");

                        contacts = MainActivity.db.getContactDao().getContacts();
                    }
                }

                unselected = false;

                if (selectedContacts.isEmpty()) {
                    mSelectedContacts.setText("No contacts selected.");
                } else if (selectedContacts.size() == 1) {
                    mSelectedContacts.setText(selectedContacts.size() + " contact selected.");
                } else {
                    mSelectedContacts.setText(selectedContacts.size() + " contacts selected.");
                }

                for (Contact contact : contacts) {
                    newContactWrappers.add(new ContactWrapper(contact, isChecked));
                }

                contactWrappers = newContactWrappers;
                initializeContactsList(contactWrappers);
            }
        });

        mNoContacts = findViewById(R.id.no_contacts);
        mMessageContent = findViewById(R.id.input_message);

        ImageView mSendMessage = findViewById(R.id.send_message_btn);
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedContacts.isEmpty()) {
                    Toast.makeText(
                            SendMessageActivity.this,
                            "Please select at least one contact!",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    String message = mMessageContent.getText().toString();
                    broadcastMessage(message);

                    Toast.makeText(
                            SendMessageActivity.this,
                            "Message sent.",
                            Toast.LENGTH_SHORT
                    ).show();

                    mMessageContent.setText("");
                }
            }
        });

        Log.d(TAG, "initializeViews: views were initialized");
    }

    private void initializeRecyclerView() {
        mRecyclerView = findViewById(R.id.send_message_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                mRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                ContactWrapper contactWrapper = contactWrappers.get(position);

                Log.d(TAG, "onClick: clicked on contact " + contactWrapper.getContact().getName());

                if (multipleSelection) {
                    onLongClick(view, position);
                } else {
                    sendUserToChatActivity(contactWrapper.getContact());
                }
            }

            @Override
            public void onLongClick(View child, int position) {
                ContactWrapper contactWrapper = contactWrappers.get(position);

                Log.d(TAG, "onLongClick: long clicked on contact " + contactWrapper.getContact().getName());
                Log.d(TAG, "onLongClick: before action, there are " + selectedContacts.size() + " contacts selected");

                if (contactWrapper.isSelected()) {
                    Log.d(TAG, "onLongClick: contact was selected previously. Unselecting...");

                    contactWrapper.setSelected(false);
                    removeSelectedContact(contactWrapper.getContact());

                    if (mSelectAll.isChecked()) {
                        unselected = true;
                        mSelectAll.setChecked(false);
                    }
                } else {
                    Log.d(TAG, "onLongClick: contact was not selected previously. Selecting...");

                    contactWrapper.setSelected(true);
                    selectedContacts.add(contactWrapper.getContact().getPhone());

                    if (selectedContacts.size() == contactWrappers.size()) {
                        selected = true;
                        mSelectAll.setChecked(true);
                    }
                }

                if (selectedContacts.isEmpty()) {
                    Log.d(TAG, "onLongClick: now there is no selected contact");

                    multipleSelection = false;

                    mSelectedContacts.setText("No contacts selected.");
                } else {
                    multipleSelection = true;

                    if (selectedContacts.size() == 1) {
                        Log.d(TAG, "onLongClick: there is 1 contact selected");

                        mSelectedContacts.setText(selectedContacts.size() + " contact selected.");
                    } else {
                        Log.d(TAG, "onLongClick: there are " + selectedContacts.size() + " contacts selected");

                        mSelectedContacts.setText(selectedContacts.size() + " contacts selected.");
                    }
                }

                mContactsAdapter.notifyItemChanged(position);
            }
        }));

        Log.d(TAG, "initializeRecyclerView: initialized RecyclerView");
    }

    private void initializeContactsList(List<ContactWrapper> contactWrappers) {
        mContactsAdapter = new CustomContactsAdapter(this, contactWrappers);
        mRecyclerView.setAdapter(mContactsAdapter);

        if (contactWrappers.size() == 0) {
            mNoContacts.setVisibility(View.VISIBLE);
        } else {
            mNoContacts.setVisibility(View.GONE);
        }
    }

    private void removeSelectedContact(Contact contact) {
        Iterator<String> it = selectedContacts.iterator();

        while (it.hasNext()) {
            String currentContactPhone = it.next();

            if (currentContactPhone.equals(contact.getPhone())) {
                it.remove();
            }
        }
    }

    private boolean isContactSelected(Contact contact) {
        for (String selectedContact : selectedContacts) {
            if (selectedContact.equals(contact.getPhone())) {
                return true;
            }
        }

        return false;
    }

    private String formatNameRequest(String requestedName) {
        String[] nameParts = requestedName.split(" ");
        StringBuilder returnStringBuilder = new StringBuilder();

        for (String namePart : nameParts) {
            returnStringBuilder.append(namePart.substring(0, 1).toUpperCase()).append(namePart.substring(1).toLowerCase()).append(" ");
        }

        String returnString = returnStringBuilder.toString();

        return returnString.substring(0, returnString.length() - 1);
    }

    private void sendUserToChatActivity(Contact contact) {
        Log.d(TAG, "sendUserToPrivateChat: sending user to private chat with device named " + contact.getName());

        Intent chatIntent = new Intent(SendMessageActivity.this, ChatActivity.class);
        chatIntent.putExtra("phone", contact.getPhone());
        startActivity(chatIntent);
    }

    private void broadcastMessage(String message) {
        for (String contactPhone : selectedContacts) {
            Contact contact = MainActivity.db.getContactDao().findByPhone(contactPhone);
            Payload messagePayload = Payload.fromBytes(message.getBytes());
            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(contact.getEndpointID(), messagePayload);

            saveMessageInfoToDatabase(contact, messagePayload, message);
        }
    }

    private void saveMessageInfoToDatabase(Contact contact, Payload payload, String message) {
        MainActivity.db.getMessageDao().addMessage(new Message(
                0,
                payload.getId(),
                payload.getType(),
                contact.getPhone(),
                message,
                Calendar.getInstance().getTime(),
                Message.SENT
        ));

        Log.d(TAG, "saveMessageInfoToDatabase: saved message for " + contact.getName() + " to Room");

        if (!contact.isChat()) {
            contact.setChat(true);

            MainActivity.db.getContactDao().updateContact(contact);

            Log.d(TAG, "saveMessageInfoToDatabase: user is a new chat contact");
        }
    }
}