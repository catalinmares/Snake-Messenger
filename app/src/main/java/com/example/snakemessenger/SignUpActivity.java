package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ACCESS_GALLERY = 2;
    private Uri imageUri;
    private Button takePicture, pickPicture, signUp;
    private EditText name, email, password, confirm;
    private ImageView profilePic;
    private ProgressDialog mProgressDialog;
    private boolean customProfilePic = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        takePicture = findViewById(R.id.take_pic_brn);
        pickPicture = findViewById(R.id.pick_pic_btn);
        signUp = findViewById(R.id.signup_button2);
        name = findViewById(R.id.name);
        profilePic = findViewById(R.id.profile_pic);
        email = findViewById(R.id.email2);
        password = findViewById(R.id.password2);
        confirm = findViewById(R.id.password_confirm);

        mProgressDialog = new ProgressDialog(this);

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA};

                        requestPermissions(permission, REQUEST_IMAGE_CAPTURE);
                    } else {
                        dispatchTakePictureIntent();
                    }
                } else {
                    dispatchTakePictureIntent();
                }
            }
        });

        pickPicture.setOnClickListener(new View.OnClickListener() {
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

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = name.getText().toString();
                String userEmail = email.getText().toString();
                String userPassword = password.getText().toString();
                String confirmPassword = confirm.getText().toString();
                boolean fieldsCompleted = true;

                if (TextUtils.isEmpty(userName)) {
                    name.setError("Name is required!");
                    fieldsCompleted = false;
                }

                if (TextUtils.isEmpty(userEmail)) {
                    email.setError("Email is required!");
                    fieldsCompleted = false;
                }

                if (TextUtils.isEmpty(userPassword)) {
                    password.setError("Password is required!");
                    fieldsCompleted = false;
                }

                if (TextUtils.isEmpty(confirmPassword)) {
                    confirm.setError("Password confirmation is required!");
                    fieldsCompleted = false;
                }

                if (!fieldsCompleted) {
                    Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (userPassword.length() < 6) {
                    Toast.makeText(SignUpActivity.this, "Password must contain at least 6 characters", Toast.LENGTH_SHORT).show();
                } else if (!userEmail.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    Toast.makeText(SignUpActivity.this, "Please provide a valid e-mail address.", Toast.LENGTH_SHORT).show();
                } else if (!userPassword.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "The two passwords do not match.", Toast.LENGTH_SHORT).show();
                } else {
                    createAccount(userName, userEmail, userPassword);
                }
            }
        });
    }

    private void createAccount(final String name, final String email, final String password) {
        mProgressDialog.setTitle("Creating new account");
        mProgressDialog.setMessage("Please wait while we create your new account.");
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(MainActivity.TAG, "User profile updated.");
                                            }
                                        }
                                    });

                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignUpActivity.this, "A verification e-mail has been sent " +
                                                        "to the provided address", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(SignUpActivity.this, "There was an error sending the " +
                                                        "verification e-mail", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                            final String userID = user.getUid();

                            Log.d(MainActivity.TAG, "Successfully created user with ID " + userID);

                            final DocumentReference docRef = db.collection("users").document(userID);

                            Map<String, Object> userData = new HashMap<String, Object>();
                            userData.put("userID", userID);
                            userData.put("name", name);

                            if (customProfilePic) {
                                profilePic.setDrawingCacheEnabled(true);
                                profilePic.buildDrawingCache();

                                Bitmap bitmap = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();

                                UploadTask uploadTask = storageReference.child(userID + "-profile_pic").putBytes(data);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUpActivity.this, "There was an error " +
                                                "adding the profile picture.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                userData.put("picture", "yes");
                            } else {
                                userData.put("picture", "no");
                            }

                            userData.put("email", email);
                            userData.put("status", "Available");

                            mProgressDialog.dismiss();

                            docRef.set(userData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(MainActivity.TAG, "DocumentSnapshot added with ID: " + docRef.getId());

                                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mAuth.signOut();
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(MainActivity.TAG, "Error adding document", e);
                                            Toast.makeText(SignUpActivity.this, "There was an error processing the request.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            mProgressDialog.dismiss();

                            Toast.makeText(SignUpActivity.this, "Sign Up failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profilePic.setImageBitmap(imageBitmap);
            customProfilePic = true;
        } else if (requestCode == REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);


                profilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(SignUpActivity.this, "Failed to load image from device.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            customProfilePic = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(SignUpActivity.this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchPickPictureIntent();
            } else {
                Toast.makeText(SignUpActivity.this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchPickPictureIntent() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (pickPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickPictureIntent, REQUEST_ACCESS_GALLERY);
        }
    }
}
