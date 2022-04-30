package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.snakemessenger.databinding.ActivityEditProfileBinding;
import com.example.snakemessenger.general.Utilities;
import com.example.snakemessenger.models.Contact;
import com.example.snakemessenger.general.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.snakemessenger.MainActivity.db;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;

    private SharedPreferences loginPreferences;
    private Uri imageUri = null;
    private boolean customPicture = false;

    private Contact contact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);

        String contactDeviceId = Objects.requireNonNull(getIntent().getExtras()).getString(Constants.EXTRA_CONTACT_DEVICE_ID, "");
        contact = db.getContactDao().findByDeviceId(contactDeviceId);

        updateUI();

        binding.setProfileImage.setOnClickListener(view -> Utilities.showImagePickDialog(EditProfileActivity.this));

        binding.updateSettingsButton.setOnClickListener(view -> {
            boolean ok = true;
            String userName = binding.setUserName.getText().toString();
            String userDescription = binding.setProfileStatus.getText().toString();

            if (contact == null) {
                SharedPreferences.Editor editor = loginPreferences.edit();

                if (!TextUtils.isEmpty(userName)) {
                    editor.putString(Constants.SHARED_PREFERENCES_NAME, userName);
                } else {
                    binding.setUserName.setError(Constants.ERROR_NAME_REQUIRED_TEXT);
                    ok = false;
                }

                if (!TextUtils.isEmpty(userDescription)) {
                    editor.putString(Constants.SHARED_PREFERENCES_STATUS, userDescription);
                }

                if (customPicture) {
                    editor.putString(Constants.SHARED_PREFERENCES_PHOTO_URI, imageUri.toString());
                }

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
                    binding.setUserName.setError(Constants.ERROR_NAME_REQUIRED_TEXT);
                    ok = false;
                }

                if (!TextUtils.isEmpty(userDescription)) {
                    contact.setDescription(userDescription);
                }

                if (customPicture) {
                    contact.setPhotoUri(imageUri.toString());
                }

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

        binding.setUserName.setText(name);
        binding.setDeviceId.setText(deviceId);
        binding.setProfileStatus.setText(description);

        if (photoUri != null && !photoUri.isEmpty()) {
            imageUri = Uri.parse(photoUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                binding.setProfileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(EditProfileActivity.this, Constants.TOAST_FAILED_TO_LOAD_IMAGE, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            customPicture = true;

            Bundle extras = data.getExtras();
            assert extras != null;

            Bitmap imageBitmap = (Bitmap) extras.get(Constants.EXTRA_IMAGE_CAPTURE_DATA);
            binding.setProfileImage.setImageBitmap(imageBitmap);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            assert imageBitmap != null;

            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), imageBitmap, "Title", null);

            imageUri = Uri.parse(path);
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
            customPicture = true;

            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                binding.setProfileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(EditProfileActivity.this, Constants.TOAST_FAILED_TO_LOAD_IMAGE, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchTakePictureIntent(EditProfileActivity.this);
            } else {
                Toast.makeText(EditProfileActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchPickPictureIntent(EditProfileActivity.this);
            } else {
                Toast.makeText(EditProfileActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
