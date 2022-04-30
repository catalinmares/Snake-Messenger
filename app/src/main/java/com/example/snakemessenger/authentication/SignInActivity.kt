package com.example.snakemessenger.authentication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.snakemessenger.MainActivity
import com.example.snakemessenger.databinding.ActivityLoginBinding
import com.example.snakemessenger.general.Constants

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.signinButton.setOnClickListener {
            val userPassword = binding.password.text.toString()
            if (TextUtils.isEmpty(userPassword)) {
                Toast.makeText(this@SignInActivity, Constants.TOAST_ALL_FIELDS_REQUIRED, Toast.LENGTH_SHORT).show()
            } else {
                signIn(userPassword)
            }
        }
        binding.signupNow.setOnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun sendUserToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun signIn(password: String) {
        val loginPreferences: SharedPreferences = applicationContext.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val loginPassword = loginPreferences.getString(Constants.SHARED_PREFERENCES_PASSWORD, "")
        if (password == loginPassword) {
            val editor = loginPreferences.edit()
            editor.putBoolean(Constants.SHARED_PREFERENCES_SIGNED_IN, true)
            editor.apply()
            sendUserToMainActivity()
        } else {
            Toast.makeText(this@SignInActivity, Constants.TOAST_INVALID_CREDENTIALS, Toast.LENGTH_SHORT).show()
        }
    }
}