package com.example.snakemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    Button signUp;
    EditText name, email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signUp = findViewById(R.id.signup_button2);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email2);
        password = findViewById(R.id.password2);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = name.getText().toString();
                String userEmail = email.getText().toString();
                String userPassword = password.getText().toString();

                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPassword)) {
                    Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                } else if (userPassword.length() < 6) {
                    Toast.makeText(SignUpActivity.this, "Password must contain at least 6 characters", Toast.LENGTH_SHORT).show();
                } else if (!userEmail.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                    Toast.makeText(SignUpActivity.this, "Please provide a valid e-mail address.", Toast.LENGTH_SHORT).show();
                } else {
                    createAccount(userName, userEmail, userPassword);
                }
            }
        });
    }

    private void createAccount(final String name, final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;

                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(SignUpActivity.this, "A verification e-mail has been sent " +
                                                        "to the provided address", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(SignUpActivity.this, "There was an error sending the " +
                                                        "verification e-mail", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                            final String userID = user.getUid();

                            Log.d(MainActivity.TAG, "Successfully created user with ID " + userID);

                            final DocumentReference docRef = db.collection("users").document(userID);

                            Map<String, Object> userData = new HashMap<String, Object>();
                            userData.put("name", name);
                            userData.put("picture", "default");
                            userData.put("email", email);
                            userData.put("password", password);
                            userData.put("status", "default");

                            docRef.set(userData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(MainActivity.TAG, "DocumentSnapshot added with ID: " + docRef.getId());

                                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mAuth.signOut();
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(MainActivity.TAG, "Error adding document", e);
                                            Toast.makeText(SignUpActivity.this, "There was an error processing the request.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up failed.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d(MainActivity.TAG, "ERROR during SIGNUP: " + task.getException().toString());
                        }
                    }
                });
    }
}
