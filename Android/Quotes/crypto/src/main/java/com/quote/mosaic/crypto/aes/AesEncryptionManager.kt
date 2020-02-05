package com.quote.mosaic.crypto.aes

import android.annotation.TargetApi
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.quote.mosaic.crypto.fs.cipher.CipherText
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

@TargetApi(Build.VERSION_CODES.M)
class AesEncryptionManager(private val keyStore: KeyStore) {

    init {
        if (!keyStore.containsAlias(AES_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    AES_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build())
            keyGenerator.generateKey()
        }
    }

    fun encrypt(plainText: ByteArray): CipherText {
        val c = Cipher.getInstance(AES_TRANSFORMATION)
        c.init(Cipher.ENCRYPT_MODE, getSecretKey())
        return CipherText(c.iv, c.doFinal(plainText), EMPTY_BYTE_ARRAY)
    }

    fun decrypt(cipherText: CipherText): ByteArray {
        val c = Cipher.getInstance(AES_TRANSFORMATION)
        c.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(GCM_TAG_LEN, cipherText.iv))
        return c.doFinal(cipherText.cipherText)
    }

    private fun getSecretKey() = keyStore.getKey(AES_KEY_ALIAS, null)

    private companion object {
        private const val AES_KEY_ALIAS = "AesKeyAlias"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"

        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LEN = 128

        // GCM Ensures integrity - HMAC is not necessary.
        private val EMPTY_BYTE_ARRAY = ByteArray(0)
    }
}