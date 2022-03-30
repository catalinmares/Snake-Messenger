package com.example.snakemessenger;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.snakemessenger.authentication.SignInActivity;
import com.example.snakemessenger.general.Constants;

import java.util.Timer;
import java.util.TimerTask;

public class LauncherActivity extends AppCompatActivity {
    public static final String TAG = "[LauncherActivity]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (!hasPermissions(this, Constants.REQUIRED_PERMISSIONS)) {
            Log.d(TAG, "onCreate: app does not have all the required permissions. Requesting permissions...");

            ActivityCompat.requestPermissions(
                    this,
                    Constants.REQUIRED_PERMISSIONS,
                    Constants.REQUEST_PERMISSIONS
            );
        } else {
            enterApplication();
        }
    }

    private void enterApplication() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SharedPreferences loginPreferences = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);

                boolean signedIn = loginPreferences.getBoolean(Constants.SHARED_PREFERENCES_SIGNED_IN, false);

                if (!signedIn) {
                    Log.d(TAG, "onCreate: user is not signed in. Sending him to login activity...");
                    sendUserToLoginActivity();
                } else {
                    sendUserToMainActivity();
                }
            }
        }, 500);
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != Constants.REQUEST_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, Constants.TOAST_MISSING_PERMISSIONS, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        enterApplication();
    }

    private void sendUserToLoginActivity() {
        Log.d(TAG, "sendUserToLoginActivity: starting login activity...");
        Intent loginIntent = new Intent(LauncherActivity.this, SignInActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
    }

    private void sendUserToMainActivity() {
        Log.d(TAG, "sendUserToMainActivity: starting main activity...");
        Intent mainIntent = new Intent(LauncherActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
    }
}