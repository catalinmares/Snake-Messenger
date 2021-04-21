package com.example.snakemessenger.authentication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snakemessenger.MainActivity;
import com.example.snakemessenger.R;

public class SignInActivity extends AppCompatActivity {
    private EditText phone, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signIn = findViewById(R.id.signin_button);
        TextView signUp = findViewById(R.id.signup_now);

        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);

        TextView reset = findViewById(R.id.password_reset2);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userPhone = phone.getText().toString();
                String userPassword = password.getText().toString();

                if (TextUtils.isEmpty(userPhone) || TextUtils.isEmpty(userPassword)) {
                    Toast.makeText(SignInActivity.this, "All fields are required.", Toast.LENGTH_SHORT).show();
                } else {
                    signIn(userPhone, userPassword);
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userPhone = phone.getText().toString();

//                TODO - handle password reset logic
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void signIn(String phone, String password) {
        SharedPreferences loginPreferences =
                getApplicationContext().getSharedPreferences("LOGIN_DETAILS", MODE_PRIVATE);

        String loginPhone = loginPreferences.getString("phone", "");
        String loginPassword = loginPreferences.getString("password", "");

        if (phone.equals(loginPhone) && password.equals(loginPassword)) {
            SharedPreferences.Editor editor = loginPreferences.edit();
            editor.putBoolean("signedIn", true);
            editor.apply();
            sendUserToMainActivity();
        } else {
            Toast.makeText(SignInActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
