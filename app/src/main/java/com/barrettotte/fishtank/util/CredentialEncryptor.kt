package com.barrettotte.fishtank.util

import java.security.KeyStore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts and decrypts credentials using AES-256-GCM keys stored in the Android Keystore. */
object CredentialEncryptor {

    private const val KEYSTORE_ALIAS = "fishtank_credentials_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    /** Encrypt a plaintext string. Returns a Base64-encoded string containing the IV and ciphertext. */
    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Prepend IV to ciphertext so we can extract it during decryption
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /** Decrypt a Base64-encoded string produced by encrypt(). Returns the original plaintext. */
    fun decrypt(encoded: String): String {
        val combined = Base64.decode(encoded, Base64.NO_WRAP)

        // Extract IV from the first 12 bytes
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH, iv))

        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    /** Get the existing key from the Keystore, or generate a new one if it doesn't exist. */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val existingKey = keyStore.getEntry(KEYSTORE_ALIAS, null)
        if (existingKey is KeyStore.SecretKeyEntry) {
            return existingKey.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return keyGenerator.generateKey()
    }
}
