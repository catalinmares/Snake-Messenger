package com.example.snakemessenger;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.snakemessenger.database.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {
    public static final String TAG = "ContactsFragment";
    private View contactsFragmentView;
    private SwipeRefreshLayout mRefreshLayout;
    private TextView mNoContacts;
    private RecyclerView mContactsRecyclerView;
    private ContactsAdapter mAdapter;
    private List<Contact> contacts;
    private FloatingActionButton mAddContact;

    private TextView mClosePopup;
    private CircleImageView mUserProfilePicture;
    private ImageView mUserStatus;
    private TextView mUserProfileName;
    private TextView mUserDescription;
    private Button mSendMessage;
    private Button mEditProfile;

    private Dialog userProfileDialog;

    private Contact displayedContact;

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contactsFragmentView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mNoContacts = contactsFragmentView.findViewById(R.id.no_contacts);
        mAddContact = contactsFragmentView.findViewById(R.id.add_friend_btn);
        mAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewFriend();
            }
        });
        mRefreshLayout = contactsFragmentView.findViewById(R.id.refresh);
        mContactsRecyclerView = contactsFragmentView.findViewById(R.id.friends_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mContactsRecyclerView.setLayoutManager(layoutManager);

        mContactsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mContactsRecyclerView, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                CircleImageView profilePic = view.findViewById(R.id.contact_image_item);
                BitmapDrawable drawable = (BitmapDrawable) profilePic.getDrawable();
                Bitmap image = drawable.getBitmap();

                displayedContact = contacts.get(position);
                showUserProfile(displayedContact, image);
            }

            @Override
            public void onLongClick(View child, int position) {

            }
        }));

        Log.d(TAG, "onCreateView: initialized RecyclerView");

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                contacts = MainActivity.db.getContactDao().getContacts();
                initializeContactsList(contacts);

                mRefreshLayout.setRefreshing(false);
            }
        });

        initializeUserProfileDialog();

        MainActivity.db.getContactDao().getAllContacts().observe(getViewLifecycleOwner(), new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> changedContacts) {
                contacts = changedContacts;
                initializeContactsList(contacts);

                if (displayedContact != null) {
                    displayedContact = MainActivity.db.getContactDao().findByPhone(displayedContact.getPhone());

                    if (displayedContact.isConnected()) {
                        mUserStatus.setVisibility(View.VISIBLE);
                    } else {
                        mUserStatus.setVisibility(View.GONE);
                    }
                }
            }
        });

        return contactsFragmentView;
    }

    @Override
    public void onResume() {
        contacts = MainActivity.db.getContactDao().getContacts();
        initializeContactsList(contacts);

        if (!MainActivity.discovering) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.startDiscovering();

            Log.d(TAG, "onResume: started discovering in MainActivity");
        }

        super.onResume();
    }

    public void initializeContactsList(List<Contact> contacts) {
        mAdapter = new ContactsAdapter(contacts, getContext());
        mContactsRecyclerView.setAdapter(mAdapter);

        if (contacts.isEmpty()) {
            mNoContacts.setVisibility(View.VISIBLE);
        } else {
            mNoContacts.setVisibility(View.INVISIBLE);
        }

        Log.d(TAG, "initializeContactsList: initialized contacts list with " + contacts.size() + " items");
    }

    private void initializeUserProfileDialog() {
        userProfileDialog = new Dialog(requireContext());
        userProfileDialog.setContentView(R.layout.user_profile_layout);
        userProfileDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                displayedContact = null;
                Log.d(TAG, "onDismiss: user profile dialog dismissed");
            }
        });

        mClosePopup = userProfileDialog.findViewById(R.id.user_profile_close);
        mUserProfilePicture = userProfileDialog.findViewById(R.id.user_profile_pic);
        mUserStatus = userProfileDialog.findViewById(R.id.status);
        mUserProfileName = userProfileDialog.findViewById(R.id.user_profile_name);
        mUserDescription = userProfileDialog.findViewById(R.id.user_description);
        mSendMessage = userProfileDialog.findViewById(R.id.accept_btn);
        mEditProfile = userProfileDialog.findViewById(R.id.decline_btn);

        mClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfileDialog.dismiss();
            }
        });

        Log.d(TAG, "initializeUserProfileDialog: initialized dialog");
    }

    private void showUserProfile(Contact contact, Bitmap image) {
        mUserProfileName.setText(contact.getName());
        mUserDescription.setText(contact.getDescription());
        mUserProfilePicture.setImageBitmap(image);

        final Contact selectedContact = MainActivity.db.getContactDao().findByPhone(contact.getPhone());

        if (selectedContact.isConnected()) {
            mUserStatus.setVisibility(View.VISIBLE);
        } else {
            mUserStatus.setVisibility(View.GONE);
        }

        mSendMessage.setText("Send message");
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPrivateChat(selectedContact);
                userProfileDialog.dismiss();
            }
        });

        mEditProfile.setText("Edit contact");
        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToEditProfile(selectedContact);
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

    private void sendUserToPrivateChat(Contact contact) {
        Log.d(TAG, "sendUserToPrivateChat: sending user to private chat with device named " + contact.getName());

        Intent privateChatIntent = new Intent(getActivity(), PrivateChatActivity.class);
        privateChatIntent.putExtra("phone", contact.getPhone());
        startActivity(privateChatIntent);
    }

    private void addNewFriend() {
        if (MainActivity.discovering) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.stopDiscovering();

            Log.d(TAG, "addNewFriend: stopped discovering in MainActivity");
        }

        Log.d(TAG, "addNewFriend: sending user to AddContactActivity...");

        Intent addFriendIntent = new Intent(getContext(), AddContactActivity.class);
        startActivity(addFriendIntent);
    }
}
