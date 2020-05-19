package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {
    private static final int REQUEST_ACCESS_GALLERY = 100;
    private CircleImageView mGroupPicture;
    private EditText mGroupName, mGroupDescription;
    private Button mAddMembers, mSubmit;
    private Dialog addMembers;
    private ProgressDialog mProgressDialog;

    private EditText mMemberName;
    private Button mAddMember;
    private Button mDeleteGroup;

    private List<User> members;
    private UsersAdapter mAdapter;
    private RecyclerView mMembersRecyclerView;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private String currentUserID;
    private boolean customProfilePic = false;
    private boolean newGroup;
    private String name;
    private String description;
    private String groupID;
    private boolean picModified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Toolbar mToolbar = findViewById(R.id.create_group_toolbar);
        setSupportActionBar(mToolbar);

        newGroup = getIntent().getExtras().getBoolean("new");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        currentUserID = currentUser.getUid();

        mGroupPicture = findViewById(R.id.create_group_image);
        mGroupName = findViewById(R.id.create_group_name_edit);
        mGroupDescription = findViewById(R.id.create_group_description_edit);

        mDeleteGroup = findViewById(R.id.delete_group_btn);
        mAddMembers = findViewById(R.id.create_group_add_friend_btn);
        mSubmit = findViewById(R.id.create_group_submit_btn);

        if (!newGroup) {
            getSupportActionBar().setTitle("Edit group");
            mDeleteGroup.setVisibility(View.VISIBLE);

            groupID = getIntent().getExtras().getString("groupID");
            name = getIntent().getExtras().getString("name");
            description = getIntent().getExtras().getString("description");
            customProfilePic = getIntent().getExtras().getBoolean("picture");

            mGroupName.setText(name);
            mGroupDescription.setText(description);

            if (customProfilePic) {
                final long TEN_MEGABYTES = 10 * 1024 * 1024;
                storageReference.child(groupID + "-profile_pic")
                        .getBytes(TEN_MEGABYTES)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                mGroupPicture.setImageBitmap(bitmap);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mGroupPicture.setImageResource(R.drawable.group_image);
                                Toast.makeText(CreateGroupActivity.this, "Failed to load group picture.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {
            getSupportActionBar().setTitle("Create group");
            mDeleteGroup.setVisibility(View.INVISIBLE);
        }

        initializeAddMembersDialog();

        mMembersRecyclerView = findViewById(R.id.create_group_members_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                CreateGroupActivity.this,
                LinearLayoutManager.VERTICAL,
                false
        );

        mMembersRecyclerView.setLayoutManager(layoutManager);

        if (!newGroup) {
            db.collection("groups")
                    .document(groupID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            List<String> users = (List<String>) documentSnapshot.get("users");

                            db.collection("users")
                                    .whereIn("userID", users)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            members = queryDocumentSnapshots.toObjects(User.class);
                                            mAdapter = new UsersAdapter(CreateGroupActivity.this, members);
                                            mMembersRecyclerView.setAdapter(mAdapter);
                                        }
                                    });
                        }
                    });
        } else {
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
        }

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

        mDeleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteGroup();
            }
        });

        mAddMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMembers.show();
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName = mGroupName.getText().toString();
                String groupDescription = mGroupDescription.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    mGroupName.setError("This field is required");
                } else if (newGroup) {
                    createNewGroup(groupName, groupDescription);
                } else {
                    updateGroup(groupName, groupDescription);
                }
            }
        });

        mProgressDialog = new ProgressDialog(this);
    }

    private void deleteGroup() {
        final AlertDialog deleteGroupDialog = new AlertDialog.Builder(CreateGroupActivity.this)
                .setTitle("Delete group")
                .setMessage("Are you sure you want to delete this group? After deletion, all messages will be lost without any chance of recovery.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.collection("groups")
                                .document(groupID)
                                .delete();
                        dialogInterface.dismiss();

                        storageReference.child(groupID + "-profile_pic")
                            .delete();

                        Toast.makeText(
                                CreateGroupActivity.this,
                                "Group deleted",
                                Toast.LENGTH_SHORT
                        ).show();

                        Intent goBackToChat = new Intent();
                        setResult(Activity.RESULT_CANCELED, goBackToChat);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void initializeAddMembersDialog() {
        addMembers = new Dialog(CreateGroupActivity.this);
        addMembers.setContentView(R.layout.add_members_layout);

        mMemberName = addMembers.findViewById(R.id.add_member_field);
        mAddMember = addMembers.findViewById(R.id.add_member_btn);

        mAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = mMemberName.getText().toString();

                if (!TextUtils.isEmpty(name)) {
                    db.collection("users")
                            .document(currentUserID)
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    final List<String> friends = (List<String>) documentSnapshot.get("friends");

                                    db.collection("users")
                                            .whereEqualTo("name", name)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    boolean found = false;
                                                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                                        if (friends.contains(doc.getString("userID"))) {
                                                            found = true;

                                                            boolean ok = true;

                                                            for (User member : members) {
                                                                if (member.getUserID().equals(doc.getString("userID"))) {
                                                                    ok = false;
                                                                    break;
                                                                }
                                                            }

                                                            if (ok) {
                                                                members.add(doc.toObject(User.class));
                                                                mAdapter = new UsersAdapter(CreateGroupActivity.this, members);
                                                                mMembersRecyclerView.setAdapter(mAdapter);

                                                                Toast.makeText(
                                                                        CreateGroupActivity.this,
                                                                        doc.getString("name") + " was added to the group",
                                                                        Toast.LENGTH_SHORT
                                                                ).show();
                                                            } else {
                                                                Toast.makeText(
                                                                        CreateGroupActivity.this,
                                                                        doc.getString("name") + " is already a member of the group",
                                                                        Toast.LENGTH_SHORT
                                                                ).show();
                                                            }

                                                            mMemberName.setText("");

                                                            break;
                                                        }
                                                    }

                                                    if (!found) {
                                                        Toast.makeText(
                                                                CreateGroupActivity.this,
                                                                "No friend named " + name + " found in your friends list",
                                                                Toast.LENGTH_SHORT
                                                        ).show();
                                                    }
                                                }
                                            });
                                }
                            });
                } else {
                    Toast.makeText(
                            CreateGroupActivity.this,
                            "Please insert your friend's name first",
                            Toast.LENGTH_SHORT
                    ).show();
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
        mProgressDialog.setTitle("Creating new group");
        mProgressDialog.setMessage("Please wait while we create your new group.");
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.show();

        List<String> userIDs = new ArrayList<String>();
        groupID = UUID.randomUUID().toString();

        for (User member : members) {
            userIDs.add(member.getUserID());
        }

        Map<String, Object> groupData = new HashMap<String, Object>();
        groupData.put("name", name);
        groupData.put("groupID", groupID);
        groupData.put("description", description);
        groupData.put("adminID", currentUserID);
        groupData.put("users", userIDs);
        groupData.put("picture", customProfilePic);

        db.collection("groups")
                .document(groupID)
                .set(groupData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (customProfilePic) {
                            mGroupPicture.setDrawingCacheEnabled(true);
                            mGroupPicture.buildDrawingCache();

                            Bitmap bitmap = ((BitmapDrawable) mGroupPicture.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();

                            storageReference.child(groupID + "-profile_pic")
                                    .putBytes(data)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(
                                                    CreateGroupActivity.this,
                                                    "Group created successfully",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            sendUserToGroupConversation(name, description, customProfilePic);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(
                                            CreateGroupActivity.this,
                                            "There was an error adding the profile picture.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });
                        } else {
                            mProgressDialog.dismiss();
                            sendUserToGroupConversation(name, description, customProfilePic);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(
                                CreateGroupActivity.this,
                                "There was an error processing the request.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void updateGroup(final String groupName, final String groupDescription) {
        mProgressDialog.setTitle("Updating group");
        mProgressDialog.setMessage("Please wait while we update your group.");
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.show();

        List<String> userIDs = new ArrayList<String>();

        for (User member : members) {
            userIDs.add(member.getUserID());
        }

        Map<String, Object> groupData = new HashMap<String, Object>();
        groupData.put("name", groupName);
        groupData.put("groupID", groupID);
        groupData.put("description", groupDescription);
        groupData.put("adminID", currentUserID);
        groupData.put("users", userIDs);
        groupData.put("picture", customProfilePic || picModified);

        db.collection("groups")
                .document(groupID)
                .update(groupData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (picModified) {
                            mGroupPicture.setDrawingCacheEnabled(true);
                            mGroupPicture.buildDrawingCache();

                            Bitmap bitmap = ((BitmapDrawable) mGroupPicture.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();

                            storageReference.child(groupID + "-profile_pic")
                                    .putBytes(data)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(
                                                    CreateGroupActivity.this,
                                                    "Group updated successfully",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            sendUserToUpdatedGroupConversation(groupName, groupDescription, customProfilePic);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mProgressDialog.dismiss();
                                            Toast.makeText(
                                                    CreateGroupActivity.this,
                                                    "There was an error adding the profile picture.",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                        } else {
                            sendUserToUpdatedGroupConversation(groupName, groupDescription, customProfilePic);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(
                                CreateGroupActivity.this,
                                "There was an error processing the request.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void sendUserToUpdatedGroupConversation(String groupName, String groupDescription, boolean customProfilePic) {
        Intent goBackToChat = new Intent();
        goBackToChat.putExtra("name", groupName);
        goBackToChat.putExtra("description", groupDescription);
        goBackToChat.putExtra("picture", customProfilePic);
        setResult(Activity.RESULT_OK, goBackToChat);
        finish();
    }

    private void sendUserToGroupConversation(String groupName, String groupDescription, boolean groupPic) {
        Intent groupChatIntent = new Intent(CreateGroupActivity.this, GroupChatActivity.class);
        groupChatIntent.putExtra("groupID", groupID);
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

            if (!newGroup) {
                picModified = true;
            }
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
