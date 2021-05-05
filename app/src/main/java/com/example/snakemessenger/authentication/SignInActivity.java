package com.example.snakemessenger.authentication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;
import com.example.snakemessenger.general.Constants;

public class SignInActivity extends AppCompatActivity {
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signIn = findViewById(R.id.signin_button);
        TextView signUp = findViewById(R.id.signup_now);

        password = findViewById(R.id.password);

        signIn.setOnClickListener(view -> {
            String userPassword = password.getText().toString();

            if (TextUtils.isEmpty(userPassword)) {
                Toast.makeText(SignInActivity.this, Constants.TOAST_ALL_FIELDS_REQUIRED, Toast.LENGTH_SHORT).show();
            } else {
                signIn(userPassword);
            }
        });

        signUp.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void signIn(String password) {
        SharedPreferences loginPreferences =
                getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);

        String loginPassword = loginPreferences.getString(Constants.SHARED_PREFERENCES_PASSWORD, "");

        if (password.equals(loginPassword)) {
            SharedPreferences.Editor editor = loginPreferences.edit();
            editor.putBoolean(Constants.SHARED_PREFERENCES_SIGNED_IN, true);
            editor.apply();
            sendUserToMainActivity();
        } else {
            Toast.makeText(SignInActivity.this, Constants.TOAST_INVALID_CREDENTIALS, Toast.LENGTH_SHORT).show();
        }
    }
}
