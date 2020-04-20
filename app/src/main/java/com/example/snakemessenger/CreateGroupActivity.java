package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {
    private static final int REQUEST_ACCESS_GALLERY = 100;
    private CircleImageView mGroupPicture;
    private EditText mGroupName, mGroupDescription;
    private Button mAddMember, mSubmit;

    private List<User> members;
    private UsersAdapter mAdapter;
    private RecyclerView mMembersRecyclerView;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private String currentUserID;
    boolean customProfilePic = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar mToolbar = findViewById(R.id.create_group_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create group");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        currentUserID = currentUser.getUid();

        mGroupPicture = findViewById(R.id.create_group_image);
        mGroupName = findViewById(R.id.create_group_name_edit);
        mGroupDescription = findViewById(R.id.create_group_description_edit);

        mAddMember = findViewById(R.id.create_group_add_friend_btn);
        mSubmit = findViewById(R.id.create_group_submit_btn);

        mMembersRecyclerView = findViewById(R.id.create_group_members_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                CreateGroupActivity.this,
                LinearLayoutManager.VERTICAL,
                false
        );

        mMembersRecyclerView.setLayoutManager(layoutManager);

        members = new ArrayList<User>();

        db.collection("users")
                .document(currentUserID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            members.add(document.toObject(User.class));
                            mAdapter = new UsersAdapter(CreateGroupActivity.this, members);
                            mMembersRecyclerView.setAdapter(mAdapter);
                        }
                    }
                });

        mGroupPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};

                        requestPermissions(permission, REQUEST_ACCESS_GALLERY);
                    } else {
                        dispatchPickPictureIntent();
                    }
                } else {
                    dispatchPickPictureIntent();
                }
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName = mGroupName.getText().toString();
                String groupDescription = mGroupDescription.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    mGroupName.setError("This field is required");
                } else {
                    createNewGroup(groupName, groupDescription);
                }
            }
        });
    }

    private void dispatchPickPictureIntent() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (pickPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPictureIntent, REQUEST_ACCESS_GALLERY);
        }
    }

    private void createNewGroup(final String name, final String description) {
        List<String> userIDs = new ArrayList<String>();

        final String groupPic;

        for (User member : members) {
            userIDs.add(member.getUserID());
        }

        Map<String, Object> groupData = new HashMap<String, Object>();
        groupData.put("name", name);
        groupData.put("description", description);
        groupData.put("adminID", currentUserID);
        groupData.put("users", userIDs);

        if (customProfilePic) {
            groupPic = "yes";

            mGroupPicture.setDrawingCacheEnabled(true);
            mGroupPicture.buildDrawingCache();

            Bitmap bitmap = ((BitmapDrawable) mGroupPicture.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.child(name + "-" + currentUserID + "-profile_pic").putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(
                            CreateGroupActivity.this,
                            "There was an error adding the profile picture.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        } else {
            groupPic = "no";
        }

        groupData.put("picture", groupPic);

        db.collection("groups")
                .document(name + "-" + currentUserID)
                .set(groupData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                CreateGroupActivity.this,
                                "Group created successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        sendUserToGroupConversation(name, description, groupPic);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                CreateGroupActivity.this,
                                "There was an error processing the request.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void sendUserToGroupConversation(String groupName, String groupDescription, String groupPic) {
        Intent groupChatIntent = new Intent(CreateGroupActivity.this, GroupChatActivity.class);
        groupChatIntent.putExtra("name", groupName);
        groupChatIntent.putExtra("adminID", currentUserID);
        groupChatIntent.putExtra("description", groupDescription);
        groupChatIntent.putExtra("picture", groupPic);
        startActivity(groupChatIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                mGroupPicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(CreateGroupActivity.this, "Failed to load image from device.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            customProfilePic = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchPickPictureIntent();
            } else {
                Toast.makeText(CreateGroupActivity.this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
