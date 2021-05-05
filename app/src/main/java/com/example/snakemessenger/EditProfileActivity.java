package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.general.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.snakemessenger.MainActivity.db;

public class EditProfileActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ACCESS_GALLERY = 2;

    private CircleImageView profilePictureImageView;
    private EditText usernameEditText, deviceIdEditText, descriptionEditText;

    private SharedPreferences loginPreferences;
    private Uri imageUri = null;

    private Contact contact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        loginPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);

        String contactDeviceId = Objects.requireNonNull(getIntent().getExtras()).getString(Constants.EXTRA_CONTACT_DEVICE_ID, "");
        contact = db.getContactDao().findByDeviceId(contactDeviceId);

        profilePictureImageView = findViewById(R.id.set_profile_image);
        usernameEditText = findViewById(R.id.set_user_name);
        deviceIdEditText = findViewById(R.id.set_device_id);
        descriptionEditText = findViewById(R.id.set_profile_status);
        Button updateButton = findViewById(R.id.update_settings_button);

        updateUI();

        profilePictureImageView.setOnClickListener(view -> showImagePickDialog());

        updateButton.setOnClickListener(view -> {
            boolean ok = true;
            String userName = usernameEditText.getText().toString();
            String userDescription = descriptionEditText.getText().toString();

            if (contact == null) {
                SharedPreferences.Editor editor = loginPreferences.edit();

                if (!TextUtils.isEmpty(userName)) {
                    editor.putString(Constants.SHARED_PREFERENCES_NAME, userName);
                } else {
                    usernameEditText.setError(Constants.ERROR_NAME_REQUIRED_TEXT);
                    ok = false;
                }

                if (!TextUtils.isEmpty(userDescription)) {
                    editor.putString(Constants.SHARED_PREFERENCES_STATUS, userDescription);
                }

                editor.putString(Constants.SHARED_PREFERENCES_PHOTO_URI, imageUri.toString());

                if (ok) {
                    Toast.makeText(EditProfileActivity.this, Constants.TOAST_PROFILE_UPDATED, Toast.LENGTH_SHORT).show();

                    editor.apply();
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            } else {
                contact.setSaved(true);

                if (!TextUtils.isEmpty(userName)) {
                    contact.setName(userName);
                } else {
                    usernameEditText.setError(Constants.ERROR_NAME_REQUIRED_TEXT);
                    ok = false;
                }

                if (!TextUtils.isEmpty(userDescription)) {
                    contact.setDescription(userDescription);
                }

                contact.setPhotoUri(imageUri.toString());

                if (ok) {
                    Toast.makeText(
                            EditProfileActivity.this,
                            Constants.TOAST_PROFILE_UPDATED,
                            Toast.LENGTH_SHORT
                    ).show();

                    db.getContactDao().updateContact(contact);
                    setResult(Activity.RESULT_OK);
                    finish();
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

            Bitmap imageBitmap = (Bitmap) extras.get(Constants.EXTRA_IMAGE_CAPTURE_DATA);
            profilePictureImageView.setImageBitmap(imageBitmap);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            assert imageBitmap != null;

            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), imageBitmap, "Title", null);

            imageUri = Uri.parse(path);
        } else if (requestCode == REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profilePictureImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(EditProfileActivity.this, Constants.TOAST_FAILED_TO_LOAD_IMAGE, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(EditProfileActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchPickPictureIntent();
            } else {
                Toast.makeText(EditProfileActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI() {
        String name;
        String deviceId;
        String description;
        String photoUri;

        if (contact == null) {
            name = loginPreferences.getString(Constants.SHARED_PREFERENCES_NAME, "");
            deviceId = loginPreferences.getString(Constants.SHARED_PREFERENCES_DEVICE_ID, "");
            description = loginPreferences.getString(Constants.SHARED_PREFERENCES_STATUS, "");
            photoUri = loginPreferences.getString(Constants.SHARED_PREFERENCES_PHOTO_URI, "");
        } else {
            name = contact.getName();
            deviceId = contact.getDeviceID();
            description = contact.getDescription();
            photoUri = contact.getPhotoUri();
        }

        usernameEditText.setText(name);
        deviceIdEditText.setText(deviceId);
        descriptionEditText.setText(description);

        if (photoUri != null) {
            imageUri = Uri.parse(photoUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profilePictureImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(EditProfileActivity.this, Constants.TOAST_FAILED_TO_LOAD_IMAGE, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void showImagePickDialog() {
        String[] options = {Constants.OPTION_CAMERA, Constants.OPTION_GALLERY};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Constants.PICK_PROFILE_PICTURE_TEXT)
                .setItems(options, (dialogInterface, i) -> {
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
                })
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
    }

    private void dispatchPickPictureIntent() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        try {
            startActivityForResult(pickPictureIntent, REQUEST_ACCESS_GALLERY);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        if (pickPictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(pickPictureIntent, REQUEST_ACCESS_GALLERY);
//        }
    }
}
