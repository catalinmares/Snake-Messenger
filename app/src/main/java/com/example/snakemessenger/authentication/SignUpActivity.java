package com.example.snakemessenger.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.snakemessenger.general.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    private CircleImageView profilePictureImageView;
    private EditText nameEditText, passwordEditText, confirmPasswordEditText;

    private Uri imageUri;
    private boolean customPicture;

    private String model;
    private String androidId;

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

        profilePictureImageView.setOnClickListener(view -> Utilities.showImagePickDialog(SignUpActivity.this));

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

        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
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
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchTakePictureIntent(SignUpActivity.this);
            } else {
                Toast.makeText(SignUpActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchPickPictureIntent(SignUpActivity.this);
            } else {
                Toast.makeText(SignUpActivity.this, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
