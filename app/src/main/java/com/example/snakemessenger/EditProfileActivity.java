package com.example.snakemessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ACCESS_GALLERY = 2;

    private CircleImageView mProfilePicture;
    private EditText mUsername, mStatus;
    private Button mUpdateButton;

    private SharedPreferences loginPreferences;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loginPreferences = getApplicationContext().getSharedPreferences("LOGIN_DETAILS", MODE_PRIVATE);

        mProfilePicture = findViewById(R.id.set_profile_image);
        mUsername = findViewById(R.id.set_user_name);
        mStatus = findViewById(R.id.set_profile_status);
        mUpdateButton = findViewById(R.id.update_settings_button);

        updateUI();

        mProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ok = true;
                String userName = mUsername.getText().toString();
                String userStatus = mStatus.getText().toString();

                SharedPreferences.Editor editor = loginPreferences.edit();

                if (!TextUtils.isEmpty(userName)) {
                    editor.putString("name", userName);
                } else {
                    mUsername.setError("Name cannot be empty");
                    ok = false;
                }

                if (!TextUtils.isEmpty(userStatus)) {
                    editor.putString("status", userStatus);
                }

                editor.putString("photoUri", imageUri.toString());

                if (ok) {
                    Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    editor.apply();
                    sendUserToMainActivity();
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
            mProfilePicture.setImageBitmap(imageBitmap);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), imageBitmap, "Title", null);

            imageUri = Uri.parse(path);
        } else if (requestCode == REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                mProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(SettingsActivity.this, "Failed to load image from device.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
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
                Toast.makeText(SettingsActivity.this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchPickPictureIntent();
            } else {
                Toast.makeText(SettingsActivity.this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI() {
        String name = loginPreferences.getString("name", "");
        mUsername.setText(name);

        String status = loginPreferences.getString("status", "");
        mStatus.setText(status);

        String photoUri = loginPreferences.getString("photoUri", null);

        if (photoUri != null) {
            imageUri = Uri.parse(photoUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                mProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(SettingsActivity.this, "Failed to load image from device.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a profile picture")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
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

                                break;

                            case 1:
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
                    }
                })
                .show();
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

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }
}
