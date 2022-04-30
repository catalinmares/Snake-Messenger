package com.example.snakemessenger.chats;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.example.snakemessenger.general.Utilities;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.general.Constants;
import com.example.snakemessenger.managers.CommunicationManager;
import com.example.snakemessenger.models.ContactWrapper;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

import static com.example.snakemessenger.MainActivity.db;

public class SendMessageActivity extends AppCompatActivity {
    public static final String TAG = "[SendMessageActivity]";

    private TextView titleTextView;
    private EditText searchEditText;
    private ImageView searchButton;

    private TextView selectedContactsTextView;
    private CheckBox selectAllCheckbox;
    private TextView noContactsTextView;

    private RecyclerView recyclerView;
    private CustomContactsAdapter contactsAdapter;
    private List<ContactWrapper> contactWrappers;

    private View rootView;
    private ImageView emojiImageView;
    private EmojiconEditText messageContentEditText;

    private List<String> selectedContacts;
    private boolean isSearchButtonPressed;
    private boolean multipleSelection;
    private boolean selected;
    private boolean unselected;

    private String imagePath;

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

        EmojIconActions emojIconActions = new EmojIconActions(this, rootView, messageContentEditText, emojiImageView);
        emojIconActions.ShowEmojIcon();
        emojIconActions.setIconsIds(R.drawable.ic_baseline_keyboard_24, R.drawable.ic_baseline_emoji_emotions_24);

        db.getContactDao().getAllContacts().observe(this, contacts -> {
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
        });

        List<Contact> contacts = db.getContactDao().getContacts();
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

            searchEditText.setText("");
            searchEditText.setVisibility(View.GONE);
            titleTextView.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);

            isSearchButtonPressed = false;
        } else if (multipleSelection) {
            Log.d(TAG, "onBackPressed: user had selected some contacts. Reset contacts selected to none");

            multipleSelection = false;

            selectedContacts.clear();
            selectedContactsTextView.setText(R.string.no_contacts_selected);
            selectAllCheckbox.setChecked(false);
        } else {
            Log.d(TAG, "onBackPressed: leaving activity...");
            super.onBackPressed();
        }
    }

    private void initializeToolbar() {
        Toolbar mToolbar = findViewById(R.id.send_message_bar_layout);
        setSupportActionBar(mToolbar);

        titleTextView = mToolbar.findViewById(R.id.title);

        searchEditText = mToolbar.findViewById(R.id.search_edit_text);
        searchEditText.setVisibility(View.GONE);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String requestedName = charSequence.toString();

                Log.d(TAG, "onTextChanged: edit text changed content to " + requestedName);

                List<Contact> contacts;
                List<ContactWrapper> newContactWrappers = new ArrayList<>();

                if (!requestedName.equals("")) {
                    String formattedName = formatNameRequest(requestedName);

                    Log.d(TAG, "onTextChanged: user requested custom name " + formattedName);

                    contacts = db.getContactDao().getMatchingContacts("%" + formattedName + "%");

                } else {
                    Log.d(TAG, "onTextChanged: user removed edit text content");

                    contacts = db.getContactDao().getContacts();
                }

                Log.d(TAG, "onTextChanged: request matched " + contacts.size() + " contacts");

                for (Contact contact : contacts) {
                    Log.d(TAG, "onTextChanged: parsing contact " + contact.getName());

                    boolean selected = isContactSelected(contact);

                    if (!selected) {
                        Log.d(TAG, "onTextChanged: no previous selection found for " + contact.getName());

                        newContactWrappers.add(new ContactWrapper(contact, false));
                    } else {
                        Log.d(TAG, "onTextChanged: " + contact.getName() + " was previously selected");

                        newContactWrappers.add(new ContactWrapper(contact, true));
                    }
                }

                contactWrappers = newContactWrappers;
                initializeContactsList(contactWrappers);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        searchButton = mToolbar.findViewById(R.id.search_btn);
        searchButton.setOnClickListener(view -> {
            Log.d(TAG, "onClick: search button clicked");

            isSearchButtonPressed = true;

            titleTextView.setVisibility(View.GONE);
            searchEditText.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.GONE);
        });

        ImageView mBackButton = mToolbar.findViewById(R.id.back_btn);
        mBackButton.setOnClickListener(view -> onBackPressed());

        Log.d(TAG, "initializeToolbar: initialized toolbar");
    }

    private void initializeViews() {
        rootView = findViewById(R.id.root_view);

        ImageView cameraImageView = findViewById(R.id.pick_picture_btn);
        cameraImageView.setOnClickListener(v -> Utilities.showImagePickDialog(SendMessageActivity.this));
        emojiImageView = findViewById(R.id.pick_emoji_btn);

        selectedContactsTextView = findViewById(R.id.selected_contacts);

        selectAllCheckbox = findViewById(R.id.select_all);
        selectAllCheckbox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            List<Contact> contacts;
            List<ContactWrapper> newContactWrappers = new ArrayList<>();

            if (isChecked) {
                Log.d(TAG, "onCheckedChanged: checkbox was checked");

                contacts = db.getContactDao().getContacts();

                if (!selected) {
                    selectedContacts.clear();

                    for (Contact contact : contacts) {
                        selectedContacts.add(contact.getDeviceID());
                    }

                    searchEditText.setText("");
                    searchEditText.setVisibility(View.GONE);
                    titleTextView.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.VISIBLE);

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

                    String requestedName = searchEditText.getText().toString();

                    if (!requestedName.equals("")) {
                        Log.d(TAG, "onCheckedChanged: user requested custom name");

                        String formattedName = formatNameRequest(requestedName);

                        contacts = db.getContactDao().getMatchingContacts("%" + formattedName + "%");
                    } else {
                        Log.d(TAG, "onCheckedChanged: user did not search for anything");
                        contacts = db.getContactDao().getContacts();
                    }
                } else {
                    Log.d(TAG, "onCheckedChanged: edit text is not active");

                    contacts = db.getContactDao().getContacts();
                }
            }

            unselected = false;

            if (selectedContacts.isEmpty()) {
                selectedContactsTextView.setText(R.string.no_contacts_selected);
            } else if (selectedContacts.size() == 1) {
                selectedContactsTextView.setText(MessageFormat.format("{0} contact selected.", selectedContacts.size()));
            } else {
                selectedContactsTextView.setText(MessageFormat.format("{0} contacts selected.", selectedContacts.size()));
            }

            for (Contact contact : contacts) {
                newContactWrappers.add(new ContactWrapper(contact, isChecked));
            }

            contactWrappers = newContactWrappers;
            initializeContactsList(contactWrappers);
        });

        noContactsTextView = findViewById(R.id.no_contacts);
        messageContentEditText = findViewById(R.id.input_message);

        ImageView sendMessageButton = findViewById(R.id.send_message_btn);
        sendMessageButton.setOnClickListener(view -> {
            if (selectedContacts.isEmpty()) {
                Toast.makeText(
                        SendMessageActivity.this,
                        Constants.TOAST_SELECT_AT_LEAST_ONE_CONTACT,
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                String message = messageContentEditText.getText().toString();
                new Thread(() -> broadcastMessage(message)).start();

                Toast.makeText(
                        SendMessageActivity.this,
                        Constants.TOAST_MESSAGE_SENT,
                        Toast.LENGTH_SHORT
                ).show();

                messageContentEditText.setText("");
            }
        });

        Log.d(TAG, "initializeViews: views were initialized");
    }

    private void initializeRecyclerView() {
        recyclerView = findViewById(R.id.send_message_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerViewClickListener() {
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

                    if (selectAllCheckbox.isChecked()) {
                        unselected = true;
                        selectAllCheckbox.setChecked(false);
                    }
                } else {
                    Log.d(TAG, "onLongClick: contact was not selected previously. Selecting...");

                    contactWrapper.setSelected(true);
                    selectedContacts.add(contactWrapper.getContact().getDeviceID());

                    if (selectedContacts.size() == contactWrappers.size()) {
                        selected = true;
                        selectAllCheckbox.setChecked(true);
                    }
                }

                if (selectedContacts.isEmpty()) {
                    Log.d(TAG, "onLongClick: now there is no selected contact");

                    multipleSelection = false;

                    selectedContactsTextView.setText(R.string.no_contacts_selected);
                } else {
                    multipleSelection = true;

                    if (selectedContacts.size() == 1) {
                        Log.d(TAG, "onLongClick: there is 1 contact selected");

                        selectedContactsTextView.setText(MessageFormat.format("{0} contact selected.", selectedContacts.size()));
                    } else {
                        Log.d(TAG, "onLongClick: there are " + selectedContacts.size() + " contacts selected");

                        selectedContactsTextView.setText(MessageFormat.format("{0} contacts selected.", selectedContacts.size()));
                    }
                }

                contactsAdapter.notifyItemChanged(position);
            }
        }));

        Log.d(TAG, "initializeRecyclerView: initialized RecyclerView");
    }

    private void initializeContactsList(List<ContactWrapper> contactWrappers) {
        contactsAdapter = new CustomContactsAdapter(this, contactWrappers);
        recyclerView.setAdapter(contactsAdapter);

        if (contactWrappers.size() == 0) {
            noContactsTextView.setVisibility(View.VISIBLE);
        } else {
            noContactsTextView.setVisibility(View.GONE);
        }
    }

    private void removeSelectedContact(Contact contact) {
        Iterator<String> it = selectedContacts.iterator();

        while (it.hasNext()) {
            String currentContactDeviceId = it.next();

            if (currentContactDeviceId.equals(contact.getDeviceID())) {
                it.remove();
            }
        }
    }

    private boolean isContactSelected(Contact contact) {
        for (String selectedContact : selectedContacts) {
            if (selectedContact.equals(contact.getDeviceID())) {
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
        chatIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
        startActivity(chatIntent);
    }

    private void broadcastMessage(String message) {
        for (String contactDeviceId : selectedContacts) {
            Contact contact = db.getContactDao().findByDeviceId(contactDeviceId);
            CommunicationManager.buildAndDeliverMessage(getApplicationContext(), message, contact);
        }
    }

    private void broadcastImageMessage(Bitmap imageBitmap) {
        for (String contactDeviceId : selectedContacts) {
            Contact contact = db.getContactDao().findByDeviceId(contactDeviceId);
            CommunicationManager.buildAndDeliverImageMessage(getApplicationContext(), imageBitmap, imagePath, contact);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_PREVIEW_PICTURE && resultCode == Activity.RESULT_OK) {
            new Thread(() -> {
                broadcastImageMessage(PreviewPictureActivity.imageBitmap);
                PreviewPictureActivity.imageBitmap = null;
                imagePath = null;
            }).start();
            Toast.makeText(SendMessageActivity.this, "Picture sent!", Toast.LENGTH_SHORT).show();
        } else if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            if (extras != null) {
                PreviewPictureActivity.imageBitmap = (Bitmap) extras.get(Constants.EXTRA_IMAGE_CAPTURE_DATA);

                Intent previewPictureIntent = new Intent(SendMessageActivity.this, PreviewPictureActivity.class);
                startActivityForResult(previewPictureIntent, Constants.REQUEST_PREVIEW_PICTURE);
            }
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            if (imageUri != null) {
                imagePath = imageUri.toString();
            }

            try {
                PreviewPictureActivity.imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            Intent previewPictureIntent = new Intent(SendMessageActivity.this, PreviewPictureActivity.class);
            startActivityForResult(previewPictureIntent, Constants.REQUEST_PREVIEW_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchTakePictureIntent(SendMessageActivity.this);
            } else {
                Toast.makeText(SendMessageActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchPickPictureIntent(SendMessageActivity.this);
            } else {
                Toast.makeText(SendMessageActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        }
    }
}