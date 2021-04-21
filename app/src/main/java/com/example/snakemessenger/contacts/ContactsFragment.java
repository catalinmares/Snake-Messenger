package com.example.snakemessenger.contacts;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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
import com.example.snakemessenger.database.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends Fragment {
    public static final String TAG = "ContactsFragment";

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

        contactsAdapter = new ContactsAdapter(getContext(), new ArrayList<Contact>());
        contactsRecyclerView.setAdapter(contactsAdapter);

        MainActivity.db.getContactDao().getAllContacts().observe(getViewLifecycleOwner(), new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> changedContacts) {
                contactsAdapter.setContacts(changedContacts);

                if (changedContacts.isEmpty()) {
                    noContacts.setVisibility(View.VISIBLE);
                    contactsRecyclerView.setVisibility(View.GONE);
                } else {
                    noContacts.setVisibility(View.GONE);
                    contactsRecyclerView.setVisibility(View.VISIBLE);
                }

                if (displayedContact != null) {
                    displayedContact = MainActivity.db.getContactDao().findByPhone(displayedContact.getPhone());

                    if (displayedContact.isConnected()) {
                        userStatus.setVisibility(View.VISIBLE);
                    } else {
                        userStatus.setVisibility(View.GONE);
                    }
                }
            }
        });

        return contactsFragmentView;
    }

    @Override
    public void onResume() {
        if (!MainActivity.discovering) {
            mainActivity.startDiscovering();

            Log.d(TAG, "onResume: started discovering in MainActivity");
        }

        super.onResume();
    }

    private void initializeViews() {
        noContacts = contactsFragmentView.findViewById(R.id.no_contacts);

        FloatingActionButton mAddContact = contactsFragmentView.findViewById(R.id.add_friend_btn);
        mAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewFriend();
            }
        });
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
        userProfileDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                displayedContact = null;
                Log.d(TAG, "onDismiss: user profile dialog dismissed");
            }
        });

        TextView mClosePopup = userProfileDialog.findViewById(R.id.contact_profile_close);
        userProfilePicture = userProfileDialog.findViewById(R.id.contact_profile_pic);
        userStatus = userProfileDialog.findViewById(R.id.contact_profile_status);
        userProfileName = userProfileDialog.findViewById(R.id.contact_profile_name);
        userDescription = userProfileDialog.findViewById(R.id.contact_description);
        sendMessageBtn = userProfileDialog.findViewById(R.id.left_btn);
        editProfileBtn = userProfileDialog.findViewById(R.id.right_btn);
        deleteContactBtn = userProfileDialog.findViewById(R.id.delete_btn);

        mClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfileDialog.dismiss();
            }
        });

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
        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToChatActivity(contact);
                userProfileDialog.dismiss();
            }
        });

        editProfileBtn.setText(R.string.edit_contact);
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToEditProfile(contact);
                userProfileDialog.dismiss();
            }
        });

        deleteContactBtn.setText(R.string.delete_contact);
        deleteContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contact.setSaved(false);
                contact.setPhotoUri(null);
                contact.setName(contact.getPhone());
                contact.setDescription("");
                MainActivity.db.getContactDao().updateContact(contact);
                userProfileDialog.dismiss();
            }
        });

        userProfileDialog.show();

        Log.d(TAG, "showUserProfile: showed profile for device named " + contact.getName());
    }

    private void sendUserToEditProfile(Contact contact) {
        Log.d(TAG, "sendUserToEditProfile: sending user to editing profile of device named " + contact.getName());

        Intent editProfileIntent = new Intent(getActivity(), EditProfileActivity.class);
        editProfileIntent.putExtra("name", contact.getName());
        editProfileIntent.putExtra("phone", contact.getPhone());
        startActivity(editProfileIntent);
    }

    private void sendUserToChatActivity(Contact contact) {
        Log.d(TAG, "sendUserToPrivateChat: sending user to private chat with device named " + contact.getName());

        Intent privateChatIntent = new Intent(getActivity(), ChatActivity.class);
        privateChatIntent.putExtra("phone", contact.getPhone());
        startActivity(privateChatIntent);
    }

    private void addNewFriend() {
        Log.d(TAG, "addNewFriend: sending user to AddContactActivity...");

        Intent addFriendIntent = new Intent(getContext(), AddContactActivity.class);
        startActivity(addFriendIntent);
    }
}
