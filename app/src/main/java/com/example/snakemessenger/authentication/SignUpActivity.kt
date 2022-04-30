package com.example.snakemessenger.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.snakemessenger.MainActivity
import com.example.snakemessenger.databinding.ActivitySignUpBinding
import com.example.snakemessenger.general.Constants
import com.example.snakemessenger.general.Utilities
import java.io.ByteArrayOutputStream
import java.io.IOException

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private var imageUri: Uri? = null
    private var customPicture = false

    private var model: String? = null
    private var androidId: String? = null

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        model = Build.MODEL
        androidId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        customPicture = false

        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.profilePic.setOnClickListener {
            Utilities.showImagePickDialog(this@SignUpActivity)
        }
        binding.signupButton.setOnClickListener {
            val userName = binding.name.text.toString()
            val userPassword: String = binding.password.text.toString()
            val confirmPassword: String = binding.passwordConfirm.text.toString()
            var fieldsCompleted = true
            if (TextUtils.isEmpty(userName)) {
                binding.name.error = Constants.ERROR_NAME_REQUIRED_TEXT
                fieldsCompleted = false
            }
            if (TextUtils.isEmpty(userPassword)) {
                binding.password.error = Constants.ERROR_PASSWORD_REQUIRED_TEXT
                fieldsCompleted = false
            }
            if (TextUtils.isEmpty(confirmPassword)) {
                binding.passwordConfirm.error = Constants.ERROR_PASSWORD_CONFIRMATION_REQUIRED_TEXT
                fieldsCompleted = false
            }
            if (!fieldsCompleted) {
                Toast.makeText(this@SignUpActivity, Constants.TOAST_ALL_FIELDS_REQUIRED, Toast.LENGTH_SHORT).show()
            } else if (userPassword.length < 6) {
                Toast.makeText(this@SignUpActivity, Constants.TOAST_PASSWORD_TOO_SHORT, Toast.LENGTH_SHORT).show()
            } else if (userPassword != confirmPassword) {
                Toast.makeText(this@SignUpActivity, Constants.TOAST_PASSWORDS_DONT_MATCH, Toast.LENGTH_SHORT).show()
            } else {
                createAccount(userName, userPassword)
            }
        }
    }

    private fun createAccount(name: String, password: String) {
        val loginPreferences = applicationContext.getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE)
        val deviceId = "$model-$androidId"
        val editor = loginPreferences.edit()
        editor.putString(Constants.SHARED_PREFERENCES_NAME, name)
        editor.putString(Constants.SHARED_PREFERENCES_DEVICE_ID, deviceId)
        editor.putString(Constants.SHARED_PREFERENCES_PASSWORD, password)
        editor.putString(Constants.SHARED_PREFERENCES_STATUS, Constants.SHARED_PREFERENCES_STATUS_AVAILABLE)
        editor.putBoolean(Constants.SHARED_PREFERENCES_SIGNED_IN, false)
        if (customPicture) {
            editor.putString(Constants.SHARED_PREFERENCES_PHOTO_URI, imageUri.toString())
        } else {
            editor.putString(Constants.SHARED_PREFERENCES_PHOTO_URI, null)
        }
        editor.apply()
        Toast.makeText(this@SignUpActivity, Constants.TOAST_ACCOUNT_CREATED, Toast.LENGTH_SHORT).show()
        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            customPicture = true
            val extras = data?.extras
            val imageBitmap = extras?.get(Constants.EXTRA_IMAGE_CAPTURE_DATA) as Bitmap?
            binding.profilePic.setImageBitmap(imageBitmap)
            val bytes = ByteArrayOutputStream()
            imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(this.contentResolver, imageBitmap, "Title", null)
            imageUri = Uri.parse(path)
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY && resultCode == RESULT_OK) {
            customPicture = true
            imageUri = data?.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                binding.profilePic.setImageBitmap(bitmap)
            } catch (e: IOException) {
                Toast.makeText(this@SignUpActivity, Constants.TOAST_FAILED_TO_LOAD_IMAGE, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchTakePictureIntent(this@SignUpActivity)
            } else {
                Toast.makeText(this@SignUpActivity, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == Constants.REQUEST_ACCESS_GALLERY) {
            if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utilities.dispatchPickPictureIntent(this@SignUpActivity)
            } else {
                Toast.makeText(this@SignUpActivity, Constants.TOAST_PERMISSION_DENIED, Toast.LENGTH_SHORT).show()
            }
        }
    }
}