package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button signIn, signUp;
    EditText email, password;
    TextView reset;
    TextView resetText, resetTimer;
    private int retryTime;
    private boolean retryVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        signIn = findViewById(R.id.signin_button);
        signUp = findViewById(R.id.signup_button);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);

        reset = findViewById(R.id.password_reset2);
        resetText = findViewById(R.id.reset_text);
        resetTimer = findViewById(R.id.reset_timer);

        resetText.setVisibility(View.GONE);
        resetTimer.setVisibility(View.GONE);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = email.getText().toString();
                String userPassword = password.getText().toString();

                if (TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword)) {
                    Toast.makeText(SignInActivity.this, "All fields are required.", Toast.LENGTH_SHORT).show();
                } else {
                    signIn(userEmail, userPassword);
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
                String userEmail = email.getText().toString();

                if (resetTimer.getVisibility() == View.VISIBLE && !resetTimer.getText().toString().equals("00:00")) {
                    Toast.makeText(SignInActivity.this, "Please wait until timer reaches 0 " +
                            "before resending the request.", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(userEmail)) {
                    Toast.makeText(SignInActivity.this, "In order to reset your password, " +
                            "you need to complete the e-mail field above", Toast.LENGTH_LONG).show();
                } else if (!userEmail.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    Toast.makeText(SignInActivity.this, "In order to reset you password, " +
                            "you need to provide a valid e-mail address in the field above.", Toast.LENGTH_LONG).show();
                } else {
                    mAuth.sendPasswordResetEmail(userEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignInActivity.this, "A reset password e-mail " +
                                                "has been sent to the provided address", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(SignInActivity.this, "There was an error sending the reset request. " +
                                                "Please check your Internet connection or the correctness of the provided e-mail address.", Toast.LENGTH_LONG).show();
                                    }

                                    retryTime = 120;
                                    resetText.setVisibility(View.VISIBLE);
                                    resetTimer.setVisibility(View.VISIBLE);

                                    new CountDownTimer(120000, 1000) {

                                        public void onTick(long millisUntilFinished) {
                                            if (retryTime == 120) {
                                                resetTimer.setText("02:" + checkDigit(retryTime - 120));
                                            } else if (retryTime >= 60) {
                                                resetTimer.setText("01:" + checkDigit(retryTime - 60));
                                            } else {
                                                resetTimer.setText("00:" + checkDigit(retryTime));
                                            }

                                            retryTime--;
                                        }

                                        public void onFinish() {
                                            resetText.setVisibility(View.GONE);
                                            resetTimer.setVisibility(View.GONE);
                                        }

                                    }.start();
                                }
                            });
                }
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;

                            if (user.isEmailVerified()) {
                                sendUserToMainActivity();
                            } else {
                                Toast.makeText(SignInActivity.this, "You need to confirm your e-mail address first.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }
}
