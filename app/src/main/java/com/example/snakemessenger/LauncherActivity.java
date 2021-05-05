package com.example.snakemessenger;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.snakemessenger.authentication.SignInActivity;
import com.example.snakemessenger.general.Constants;

import java.util.Timer;
import java.util.TimerTask;

public class LauncherActivity extends AppCompatActivity {
    public static final String TAG = LauncherActivity.class.getSimpleName();

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            Log.d(TAG, "onCreate: app does not have all the required permissions. Requesting permissions...");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                        this,
                        REQUIRED_PERMISSIONS,
                        REQUEST_CODE_REQUIRED_PERMISSIONS
                );
            }
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

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
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