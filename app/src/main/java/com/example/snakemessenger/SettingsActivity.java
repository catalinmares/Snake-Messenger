package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private CircleImageView mCircleImageView;
    private EditText mUsername, mStatus;
    private Button mUpdateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        mCircleImageView = findViewById(R.id.set_profile_image);
        mUsername = findViewById(R.id.set_user_name);
        mStatus = findViewById(R.id.set_profile_status);
        mUpdateButton = findViewById(R.id.update_settings_button);

        updateUI(currentUser);

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ok = true;
                final DocumentReference docRef = db.collection("users").document(currentUser.getUid());
                String userName = mUsername.getText().toString();
                String userStatus = mStatus.getText().toString();

                if (!TextUtils.isEmpty(userName)) {
                    docRef.update("name", userName);

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(userName)
                            .build();

                    currentUser.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(MainActivity.TAG, "User profile updated.");
                                    }
                                }
                            });
                } else {
                    mUsername.setError("Name cannot be empty");
                    ok = false;
                }

                if (!TextUtils.isEmpty(userStatus)) {
                    docRef.update("status", userStatus);
                } else {
                    docRef.update("status", "Available");
                }

                if (ok) {
                    Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                }
            }
        });
    }

    private void updateUI(final FirebaseUser currentUser) {
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d(MainActivity.TAG, "DocumentSnapshot data: " + documentSnapshot.getData());

                            String userName = documentSnapshot.getString("name");
                            String profilePic = documentSnapshot.getString("picture");
                            String userStatus = documentSnapshot.getString("status");

                            assert profilePic != null;
                            if (profilePic.equals("yes")) {
                                final long ONE_MEGABYTE = 1024 * 1024;

                                storageReference.child(currentUser.getUid() + "-profile_pic")
                                        .getBytes(ONE_MEGABYTE)
                                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                mCircleImageView.setImageBitmap(bitmap);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SettingsActivity.this, "Failed to load profile picture.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            mUsername.setText(userName);
                            mStatus.setText(userStatus);
                        } else {
                            Toast.makeText(SettingsActivity.this, "Failed to retrieve user information", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }
}
