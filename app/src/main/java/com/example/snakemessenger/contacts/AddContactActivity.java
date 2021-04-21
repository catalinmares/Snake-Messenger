package com.example.snakemessenger.contacts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
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
import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.RecyclerTouchListener;
import com.example.snakemessenger.RecyclerViewClickListener;
import com.example.snakemessenger.chats.ChatActivity;
import com.example.snakemessenger.database.Contact;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddContactActivity extends AppCompatActivity {
    public static final String TAG = AddContactActivity.class.getSimpleName();

    private Dialog userProfile;
    private CircleImageView userProfilePicture;
    private TextView userProfileName;
    private Button leftButton, rightButton;
    private TextView noDevices;

    private RecyclerView usersRecyclerView;
    private NearbyContactsAdapter nearbyContactsAdapter;
    private List<Contact> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        initializeToolbar();

        noDevices = findViewById(R.id.no_devices);

        initializeRecyclerView();
        initializeUserProfileDialog();

        users = new ArrayList<>();
        nearbyContactsAdapter = new NearbyContactsAdapter(this, users);
        usersRecyclerView.setAdapter(nearbyContactsAdapter);

        MainActivity.db.getContactDao().getLiveNearbyContacts().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                if (contacts.size() == 0) {
                    noDevices.setVisibility(View.VISIBLE);
                    usersRecyclerView.setVisibility(View.GONE);
                } else {
                    noDevices.setVisibility(View.GONE);
                    usersRecyclerView.setVisibility(View.VISIBLE);
                }

                nearbyContactsAdapter.setUsers(contacts);
                users = contacts;
            }
        });
    }

    private void initializeToolbar() {
        Toolbar mToolbar = findViewById(R.id.basic_bar_layout);
        setSupportActionBar(mToolbar);

        TextView mTitle = mToolbar.findViewById(R.id.title);
        mTitle.setText("Nearby users");

        ImageView mBackButton = mToolbar.findViewById(R.id.back_btn);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
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
        userProfile = new Dialog(AddContactActivity.this);
        userProfile.setContentView(R.layout.user_profile_layout);

        TextView mClosePopup = userProfile.findViewById(R.id.user_profile_close);
        userProfilePicture = userProfile.findViewById(R.id.user_profile_pic);
        userProfileName = userProfile.findViewById(R.id.user_profile_name);
        leftButton = userProfile.findViewById(R.id.left_btn);
        rightButton = userProfile.findViewById(R.id.right_btn);

        mClosePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfile.dismiss();
            }
        });

        Log.d(TAG, "initializeUserProfileDialog: initialized dialog");
    }

    private void showUserProfile(final Contact contact, Bitmap image) {
        leftButton.setText("Send message");
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToChatActivity(contact);
                userProfile.dismiss();
            }
        });

        if (contact.isSaved()) {
            rightButton.setText("Delete contact");
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contact.setSaved(false);
                    contact.setDescription("");
                    contact.setName(contact.getPhone());
                    contact.setPhotoUri(null);
                    MainActivity.db.getContactDao().updateContact(contact);

                    Toast.makeText(AddContactActivity.this, "Contact deleted", Toast.LENGTH_SHORT).show();
                    userProfile.dismiss();
                }
            });
        } else {
            rightButton.setText("Save contact");
            rightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contact.setSaved(true);
                    MainActivity.db.getContactDao().updateContact(contact);

                    Toast.makeText(AddContactActivity.this, "Contact saved", Toast.LENGTH_SHORT).show();
                    userProfile.dismiss();
                }
            });
        }

        Glide.with(this).load(image).into(userProfilePicture);
        userProfileName.setText(contact.getName());

        userProfile.show();
    }

    private void sendUserToChatActivity(Contact contact) {
        Log.d(TAG, "sendUserToPrivateChat: sending user to private chat with device named " + contact.getName());

        Intent privateChatIntent = new Intent(AddContactActivity.this, ChatActivity.class);
        privateChatIntent.putExtra("phone", contact.getPhone());
        startActivity(privateChatIntent);
    }
}
