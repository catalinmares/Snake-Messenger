package com.example.snakemessenger.contacts;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.snakemessenger.EditProfileActivity;
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.chats.ChatActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.general.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.snakemessenger.MainActivity.db;

public class ContactsFragment extends Fragment {
    public static final String TAG = "[ContactsFragment]";

    private MainActivity mainActivity;

    private View contactsFragmentView;

    private TextView noContacts;

    private RecyclerView contactsRecyclerView;
    private ContactsAdapter contactsAdapter;

    private Contact displayedContact;

    private Dialog userProfileDialog;
    private CircleImageView userProfilePicture;
    private ImageView userStatus;
    private TextView userProfileName;
    private TextView userDescription;
    private Button sendMessageBtn, editProfileBtn, deleteContactBtn;

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contactsFragmentView = inflater.inflate(R.layout.fragment_contacts, container, false);

        mainActivity = (MainActivity) getActivity();

        initializeViews();
        initializeRecyclerView();
        initializeUserProfileDialog();

        contactsAdapter = new ContactsAdapter(getContext(), new ArrayList<>());
        contactsRecyclerView.setAdapter(contactsAdapter);

        db.getContactDao().getAllContacts().observe(getViewLifecycleOwner(), changedContacts -> {
            contactsAdapter.setContacts(changedContacts);

            if (changedContacts.isEmpty()) {
                noContacts.setVisibility(View.VISIBLE);
                contactsRecyclerView.setVisibility(View.GONE);
            } else {
                noContacts.setVisibility(View.GONE);
                contactsRecyclerView.setVisibility(View.VISIBLE);
            }

            if (displayedContact != null) {
                displayedContact = db.getContactDao().findByDeviceId(displayedContact.getDeviceID());

                if (displayedContact.isConnected()) {
                    userStatus.setVisibility(View.VISIBLE);
                } else {
                    userStatus.setVisibility(View.GONE);
                }
            }
        });

        return contactsFragmentView;
    }

    private void initializeViews() {
        noContacts = contactsFragmentView.findViewById(R.id.no_contacts);

        FloatingActionButton mAddContact = contactsFragmentView.findViewById(R.id.add_friend_btn);
        mAddContact.setOnClickListener(view -> addNewContact());
    }

    private void initializeRecyclerView() {
        contactsRecyclerView = contactsFragmentView.findViewById(R.id.friends_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        contactsRecyclerView.setLayoutManager(layoutManager);

        contactsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                contactsRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                CircleImageView profilePic = view.findViewById(R.id.contact_image_item);
                BitmapDrawable drawable = (BitmapDrawable) profilePic.getDrawable();
                Bitmap image = drawable.getBitmap();

                displayedContact = contactsAdapter.getContacts().get(position);
                showUserProfile(displayedContact, image);
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));

        Log.d(TAG, "initializeRecyclerView: initialized RecyclerView");
    }

    private void initializeUserProfileDialog() {
        userProfileDialog = new Dialog(requireContext());
        userProfileDialog.setContentView(R.layout.contact_profile_layout);
        userProfileDialog.setOnDismissListener(dialogInterface -> {
            displayedContact = null;
            Log.d(TAG, "onDismiss: user profile dialog dismissed");
        });

        TextView mClosePopup = userProfileDialog.findViewById(R.id.contact_profile_close);
        userProfilePicture = userProfileDialog.findViewById(R.id.contact_profile_pic);
        userStatus = userProfileDialog.findViewById(R.id.contact_profile_status);
        userProfileName = userProfileDialog.findViewById(R.id.contact_profile_name);
        userDescription = userProfileDialog.findViewById(R.id.contact_description);
        sendMessageBtn = userProfileDialog.findViewById(R.id.left_btn);
        editProfileBtn = userProfileDialog.findViewById(R.id.right_btn);
        deleteContactBtn = userProfileDialog.findViewById(R.id.delete_btn);

        mClosePopup.setOnClickListener(view -> userProfileDialog.dismiss());

        Log.d(TAG, "initializeUserProfileDialog: initialized dialog");
    }

    private void showUserProfile(final Contact contact, Bitmap image) {
        userProfileName.setText(contact.getName());
        userDescription.setText(contact.getDescription());
        Glide.with(mainActivity).load(image).into(userProfilePicture);

        if (contact.isConnected()) {
            userStatus.setVisibility(View.VISIBLE);
        } else {
            userStatus.setVisibility(View.GONE);
        }

        sendMessageBtn.setText(R.string.send_message);
        sendMessageBtn.setOnClickListener(view -> {
            sendUserToChatActivity(contact);
            userProfileDialog.dismiss();
        });

        editProfileBtn.setText(R.string.edit_contact);
        editProfileBtn.setOnClickListener(v -> {
            sendUserToEditProfile(contact);
            userProfileDialog.dismiss();
        });

        deleteContactBtn.setText(R.string.delete_contact);
        deleteContactBtn.setOnClickListener(view -> {
            contact.setSaved(false);
            contact.setPhotoUri(null);
            contact.setName(contact.getDeviceID());
            contact.setDescription("");

            db.getContactDao().updateContact(contact);
            userProfileDialog.dismiss();
        });

        userProfileDialog.show();

        Log.d(TAG, "showUserProfile: showed profile for device named " + contact.getName());
    }

    private void sendUserToEditProfile(Contact contact) {
        Log.d(TAG, "sendUserToEditProfile: sending user to editing profile of device named " + contact.getName());

        Intent editProfileIntent = new Intent(getActivity(), EditProfileActivity.class);
        editProfileIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
        startActivity(editProfileIntent);
    }

    private void sendUserToChatActivity(Contact contact) {
        Log.d(TAG, "sendUserToPrivateChat: sending user to private chat with device named " + contact.getName());

        Intent privateChatIntent = new Intent(getActivity(), ChatActivity.class);
        privateChatIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
        startActivity(privateChatIntent);
    }

    private void addNewContact() {
        Log.d(TAG, "addNewFriend: sending user to AddContactActivity...");

        Intent addContactIntent = new Intent(getContext(), AddContactActivity.class);
        startActivity(addContactIntent);
    }
}
