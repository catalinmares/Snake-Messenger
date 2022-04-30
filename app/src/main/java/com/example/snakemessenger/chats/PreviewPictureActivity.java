package com.example.snakemessenger.chats;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.snakemessenger.R;
import com.example.snakemessenger.databinding.ActivityPreviewPictureBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PreviewPictureActivity extends AppCompatActivity {
    public static Bitmap imageBitmap;
    private ActivityPreviewPictureBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreviewPictureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUIAction();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    private void setUIAction() {
        binding.sendPictureBtn.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            finish();
        });

        binding.cancelBtn.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        if (imageBitmap != null) {
            Glide.with(this).load(imageBitmap).into(binding.pictureImageView);
        }
    }
}