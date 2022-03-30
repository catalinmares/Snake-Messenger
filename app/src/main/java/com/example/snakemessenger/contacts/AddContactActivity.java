package com.example.snakemessenger.contacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.snakemessenger.EditProfileActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.example.snakemessenger.chats.ChatActivity;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.general.Constants;

import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.snakemessenger.MainActivity.db;

public class AddContactActivity extends AppCompatActivity {
    public static final String TAG = "[AddContactActivity]";

    private Dialog userProfileDialog;
    private CircleImageView userProfilePictureImageView;
    private TextView userProfileNameTextView;
    private Button leftButton, rightButton;
    private TextView noDevicesTextView;

    private RecyclerView usersRecyclerView;
    private NearbyContactsAdapter nearbyContactsAdapter;
    private List<Contact> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        initializeToolbar();

        noDevicesTextView = findViewById(R.id.no_devices);

        initializeRecyclerView();
        initializeUserProfileDialog();

        users = new ArrayList<>();
        nearbyContactsAdapter = new NearbyContactsAdapter(this, users);
        usersRecyclerView.setAdapter(nearbyContactsAdapter);

        db.getContactDao().getLiveNearbyContacts().observe(this, contacts -> {
            if (contacts.size() == 0) {
                noDevicesTextView.setVisibility(View.VISIBLE);
                usersRecyclerView.setVisibility(View.GONE);
            } else {
                noDevicesTextView.setVisibility(View.GONE);
                usersRecyclerView.setVisibility(View.VISIBLE);
            }

            nearbyContactsAdapter.setUsers(contacts);
            users = contacts;
        });
    }

    private void initializeToolbar() {
        Toolbar mToolbar = findViewById(R.id.basic_bar_layout);
        setSupportActionBar(mToolbar);

        TextView mTitle = mToolbar.findViewById(R.id.title);
        mTitle.setText(R.string.nearby_users);

        ImageView mBackButton = mToolbar.findViewById(R.id.back_btn);
        mBackButton.setOnClickListener(view -> onBackPressed());
    }

    private void initializeRecyclerView() {
        usersRecyclerView = findViewById(R.id.add_friend_recycler_view);

        RecyclerView.LayoutManager layoutManager =
                new LinearLayoutManager(AddContactActivity.this, LinearLayoutManager.VERTICAL, false);
        usersRecyclerView.setLayoutManager(layoutManager);

        usersRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(AddContactActivity.this, usersRecyclerView, new RecyclerViewClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        CircleImageView userPic = view.findViewById(R.id.contact_image_item);
                        BitmapDrawable drawable = (BitmapDrawable) userPic.getDrawable();
                        Bitmap image = drawable.getBitmap();
                        showUserProfile(users.get(position), image);
                    }

                    @Override
                    public void onLongClick(View child, int position) {

                    }
                }));

        Log.d(TAG, "initializeRecyclerView: initialized RecyclerView");
    }

    private void initializeUserProfileDialog() {
        userProfileDialog = new Dialog(AddContactActivity.this);
        userProfileDialog.setContentView(R.layout.user_profile_layout);

        TextView mClosePopup = userProfileDialog.findViewById(R.id.user_profile_close);
        userProfilePictureImageView = userProfileDialog.findViewById(R.id.user_profile_pic);
        userProfileNameTextView = userProfileDialog.findViewById(R.id.user_profile_name);
        leftButton = userProfileDialog.findViewById(R.id.left_btn);
        rightButton = userProfileDialog.findViewById(R.id.right_btn);

        mClosePopup.setOnClickListener(view -> userProfileDialog.dismiss());

        Log.d(TAG, "initializeUserProfileDialog: initialized dialog");
    }

    private void showUserProfile(final Contact contact, Bitmap image) {
        leftButton.setText(R.string.send_message);
        leftButton.setOnClickListener(v -> {
            sendUserToChatActivity(contact);
            userProfileDialog.dismiss();
        });

        if (contact.isSaved()) {
            rightButton.setText(R.string.delete_contact);
            rightButton.setOnClickListener(v -> {
                contact.setSaved(false);
                contact.setDescription("");
                contact.setName(contact.getDeviceID());
                contact.setPhotoUri(null);
                db.getContactDao().updateContact(contact);

                Toast.makeText(AddContactActivity.this, Constants.TOAST_CONTACT_DELETED, Toast.LENGTH_SHORT).show();
                userProfileDialog.dismiss();
            });
        } else {
            rightButton.setText(R.string.save_contact);
            rightButton.setOnClickListener(v -> {
                sendUserToEditProfile(contact);

                userProfileDialog.dismiss();
            });
        }

        Glide.with(this).load(image).into(userProfilePictureImageView);
        userProfileNameTextView.setText(contact.getName());

        userProfileDialog.show();
    }

    private void sendUserToChatActivity(Contact contact) {
        Log.d(TAG, "sendUserToPrivateChat: sending user to private chat with device named " + contact.getName());

        Intent privateChatIntent = new Intent(AddContactActivity.this, ChatActivity.class);
        privateChatIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
        startActivity(privateChatIntent);
    }

    private void sendUserToEditProfile(Contact contact) {
        Log.d(TAG, "sendUserToEditProfile: sending user to editing profile of device named " + contact.getName());

        Intent editProfileIntent = new Intent(AddContactActivity.this, EditProfileActivity.class);
        editProfileIntent.putExtra(Constants.EXTRA_CONTACT_DEVICE_ID, contact.getDeviceID());
        startActivity(editProfileIntent);
    }
}
