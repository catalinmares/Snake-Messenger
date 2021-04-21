package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.Activity;
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
import com.example.snakemessenger.database.Contact;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ACCESS_GALLERY = 2;

    private CircleImageView mProfilePicture;
    private EditText mUsername, mDescription;

    private SharedPreferences loginPreferences;
    private Uri imageUri = null;

    private Contact contact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        loginPreferences = getApplicationContext().getSharedPreferences("LOGIN_DETAILS", MODE_PRIVATE);

        String contactPhone = Objects.requireNonNull(getIntent().getExtras()).getString("phone", "");
        contact = MainActivity.db.getContactDao().findByPhone(contactPhone);

        mProfilePicture = findViewById(R.id.set_profile_image);
        mUsername = findViewById(R.id.set_user_name);
        mDescription = findViewById(R.id.set_profile_status);
        Button mUpdateButton = findViewById(R.id.update_settings_button);

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
                String userDescription = mDescription.getText().toString();

                if (contact == null) {
                    SharedPreferences.Editor editor = loginPreferences.edit();

                    if (!TextUtils.isEmpty(userName)) {
                        editor.putString("name", userName);
                    } else {
                        mUsername.setError("Name cannot be empty");
                        ok = false;
                    }

                    if (!TextUtils.isEmpty(userDescription)) {
                        editor.putString("status", userDescription);
                    }

                    editor.putString("photoUri", imageUri.toString());

                    if (ok) {
                        Toast.makeText(
                                EditProfileActivity.this,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        editor.apply();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                } else {
                    contact.setSaved(true);

                    if (!TextUtils.isEmpty(userName)) {
                        contact.setName(userName);
                    } else {
                        mUsername.setError("Name cannot be empty");
                        ok = false;
                    }

                    if (!TextUtils.isEmpty(userDescription)) {
                        contact.setDescription(userDescription);
                    }

                    contact.setPhotoUri(imageUri.toString());

                    if (ok) {
                        Toast.makeText(
                                EditProfileActivity.this,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                        MainActivity.db.getContactDao().updateContact(contact);
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            assert extras != null;

            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mProfilePicture.setImageBitmap(imageBitmap);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            assert imageBitmap != null;

            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), imageBitmap, "Title", null);

            imageUri = Uri.parse(path);
        } else if (requestCode == REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                mProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(EditProfileActivity.this, "Failed to load image from device.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(EditProfileActivity.this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchPickPictureIntent();
            } else {
                Toast.makeText(EditProfileActivity.this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI() {
        String name;
        String description;
        String photoUri;

        if (contact == null) {
            name = loginPreferences.getString("name", "");
            description = loginPreferences.getString("status", "");
            photoUri = loginPreferences.getString("photoUri", "");
        } else {
            name = contact.getName();
            description = contact.getDescription();
            photoUri = contact.getPhotoUri();
        }

        mUsername.setText(name);
        mDescription.setText(description);

        if (photoUri != null) {
            imageUri = Uri.parse(photoUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                mProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(EditProfileActivity.this, "Failed to load image from device.", Toast.LENGTH_SHORT).show();
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
}
