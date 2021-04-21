package com.example.snakemessenger.authentication;

import androidx.annotation.NonNull;
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

import com.example.snakemessenger.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_ACCESS_GALLERY = 2;

    private CircleImageView profilePic;
    private EditText name, phone, password, confirm;

    private Uri imageUri;
    private boolean customPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        customPicture = false;
        profilePic = findViewById(R.id.profile_pic);
        Button signUp = findViewById(R.id.signup_button);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);
        confirm = findViewById(R.id.password_confirm);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = name.getText().toString();
                String userPhone = phone.getText().toString();
                String userPassword = password.getText().toString();
                String confirmPassword = confirm.getText().toString();
                boolean fieldsCompleted = true;

                if (TextUtils.isEmpty(userName)) {
                    name.setError("Name is required!");
                    fieldsCompleted = false;
                }

                if (TextUtils.isEmpty(userPhone)) {
                    phone.setError("Phone number is required!");
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
                } else if (!userPassword.equals(confirmPassword)) {
                    Toast.makeText(SignUpActivity.this, "The two passwords do not match.", Toast.LENGTH_SHORT).show();
                } else {
                    createAccount(userName, userPhone, userPassword);
                }
            }
        });
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

    private void createAccount(final String name, final String phone, final String password) {
        SharedPreferences loginPreferences =
                getApplicationContext().getSharedPreferences("LOGIN_DETAILS", MODE_PRIVATE);

        SharedPreferences.Editor editor = loginPreferences.edit();
        editor.putString("name", name);
        editor.putString("phone", phone);
        editor.putString("password", password);
        editor.putString("status", "Available");
        editor.putBoolean("signedIn", false);

        if (customPicture) {
            editor.putString("photoUri", imageUri.toString());
        } else {
            editor.putString("photoUri", null);
        }

        editor.apply();

        Toast.makeText(
                SignUpActivity.this,
                "Account successfully created",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            customPicture = true;

            Bundle extras = data.getExtras();
            assert extras != null;

            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profilePic.setImageBitmap(imageBitmap);

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
                profilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(SignUpActivity.this, "Failed to load image from device.", Toast.LENGTH_SHORT).show();
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
