package com.example.go.myapplication03

import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import android.util.Base64

class AESCipher(key: String) {
    private val secretKey: SecretKeySpec

    init {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hashedKey = sha256.digest(key.toByteArray())
        secretKey = SecretKeySpec(hashedKey, "AES")
    }

    fun encrypt(message: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
        val encrypted = cipher.doFinal(message.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(enc: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
        val decrypted = cipher.doFinal(Base64.decode(enc, Base64.DEFAULT))
        return String(decrypted, StandardCharsets.UTF_8)
    }
}
