package com.example.snakemessenger.crypto

import android.util.Base64
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {
    private val byteVector: ByteArray = ByteArray(16)

    fun generateKey() : String {
        val leftLimit = 97 // 'a'
        val rightLimit = 122 // 'z'

        val targetStringLength = 16
        val random = Random()
        val buffer = StringBuilder(targetStringLength)
        for (i in 0 until targetStringLength) {
            val randomLimitedInt = leftLimit + (random.nextFloat() * (rightLimit - leftLimit + 1)).toInt()
            buffer.append(randomLimitedInt.toChar())
        }
        return buffer.toString()
    }

    fun encryptMessage(key: String, text: String) : String {
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keyBytes: ByteArray = key.toByteArray()

        val keySpec = SecretKeySpec(keyBytes, "AES")
        val ivSpec = IvParameterSpec(byteVector)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val results: ByteArray = cipher.doFinal(text.toByteArray())

        return Base64.encodeToString(results, Base64.NO_WRAP or Base64.DEFAULT)
    }

    fun decryptMessage(key: String, text: String) : String {
        val encryptedBytes = Base64.decode(text, Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        val keyBytes: ByteArray = key.toByteArray()

        val keySpec = SecretKeySpec(keyBytes, "AES")
        val ivSpec = IvParameterSpec(byteVector)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val decrypted = cipher.doFinal(encryptedBytes)

        return String(decrypted)
    }
}