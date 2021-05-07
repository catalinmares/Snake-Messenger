package com.example.snakemessenger.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.snakemessenger.R;
import com.example.snakemessenger.general.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ACCESS_GALLERY = 2;

    private CircleImageView profilePictureImageView;
    private EditText nameEditText, passwordEditText, confirmPasswordEditText;

    private Uri imageUri;
    private boolean customPicture;

    String model;
    String androidId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        model = Build.MODEL;
        androidId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        customPicture = false;
        profilePictureImageView = findViewById(R.id.profile_pic);
        Button signUp = findViewById(R.id.signup_button);
        nameEditText = findViewById(R.id.name);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.password_confirm);

        profilePictureImageView.setOnClickListener(view -> showImagePickDialog());

        signUp.setOnClickListener(view -> {
            String userName = nameEditText.getText().toString();
            String userPassword = passwordEditText.getText().toString();
            String confirmPassword = confirmPasswordEditText.getText().toString();
            boolean fieldsCompleted = true;

            if (TextUtils.isEmpty(userName)) {
                nameEditText.setError(Constants.ERROR_NAME_REQUIRED_TEXT);
                fieldsCompleted = false;
            }

            if (TextUtils.isEmpty(userPassword)) {
                passwordEditText.setError(Constants.ERROR_PASSWORD_REQUIRED_TEXT);
                fieldsCompleted = false;
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                confirmPasswordEditText.setError(Constants.ERROR_PASSWORD_CONFIRMATION_REQUIRED_TEXT);
                fieldsCompleted = false;
            }

            if (!fieldsCompleted) {
                Toast.makeText(SignUpActivity.this, Constants.TOAST_ALL_FIELDS_REQUIRED, Toast.LENGTH_SHORT).show();
            } else if (userPassword.length() < 6) {
                Toast.makeText(SignUpActivity.this, Constants.TOAST_PASSWORD_TOO_SHORT, Toast.LENGTH_SHORT).show();
            } else if (!userPassword.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, Constants.TOAST_PASSWORDS_DONT_MATCH, Toast.LENGTH_SHORT).show();
            } else {
                createAccount(userName, userPassword);
            }
        });
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

    private void createAccount(final String name, final String password) {
        SharedPreferences loginPreferences =
                getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);

        String deviceId = model + "-" + androidId;

        SharedPreferences.Editor editor = loginPreferences.edit();
        editor.putString(Constants.SHARED_PREFERENCES_NAME, name);
        editor.putString(Constants.SHARED_PREFERENCES_DEVICE_ID, deviceId);
        editor.putString(Constants.SHARED_PREFERENCES_PASSWORD, password);
        editor.putString(Constants.SHARED_PREFERENCES_STATUS, Constants.SHARED_PREFERENCES_STATUS_AVAILABLE);
        editor.putBoolean(Constants.SHARED_PREFERENCES_SIGNED_IN, false);

        if (customPicture) {
            editor.putString(Constants.SHARED_PREFERENCES_PHOTO_URI, imageUri.toString());
        } else {
            editor.putString(Constants.SHARED_PREFERENCES_PHOTO_URI, null);
        }

        editor.apply();

        Toast.makeText(SignUpActivity.this, Constants.TOAST_ACCOUNT_CREATED, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            customPicture = true;

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
            customPicture = true;

            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                profilePictureImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(SignUpActivity.this, Constants.TOAST_FAILED_TO_LOAD_IMAGE, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SignUpActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchPickPictureIntent();
            } else {
                Toast.makeText(SignUpActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatchPickPictureIntent() {
        Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        try {
            startActivityForResult(pickPictureIntent, REQUEST_ACCESS_GALLERY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
