package com.example.snakemessenger.chats;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.snakemessenger.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PreviewPictureActivity extends AppCompatActivity {
    public static Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_picture);

        ImageView pictureImageView = findViewById(R.id.picture_image_view);

        FloatingActionButton sendPictureButton = findViewById(R.id.send_picture_btn);
        sendPictureButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            finish();
        });

        FloatingActionButton cancelButton = findViewById(R.id.cancel_btn);
        cancelButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        if (imageBitmap != null) {
            Glide.with(this).load(imageBitmap).into(pictureImageView);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}