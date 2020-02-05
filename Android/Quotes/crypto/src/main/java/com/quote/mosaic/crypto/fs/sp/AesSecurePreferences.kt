package com.quote.mosaic.crypto.fs.sp

import android.content.Context
import com.quote.mosaic.crypto.aes.AesEncryptionManager
import com.quote.mosaic.crypto.fs.cipher.CipherDeserializationException
import com.quote.mosaic.crypto.fs.cipher.CipherTextSerializer
import java.io.UnsupportedEncodingException
import java.security.GeneralSecurityException

class AesSecurePreferences(
    context: Context,
    private val cipherTextSerializer: CipherTextSerializer,
    private val aesEncryptionManager: AesEncryptionManager
) : SecurePreferences {

    private val prefs = context.getSharedPreferences(AES_SECURE_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)

    override fun putString(key: String, value: String) =
        prefs.edit().putString(key, encrypt(value)).apply()

    override fun getString(key: String, defValue: String?): String? =
        getString(key) ?: defValue

    override fun putBoolean(key: String, value: Boolean) =
        prefs.edit().putString(key, encrypt(value.toString())).apply()

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        getString(key)?.toBoolean() ?: defValue

    override fun remove(key: String) =
        prefs.edit().remove(key).apply()

    private fun getString(key: String): String? {
        val encryptedString = prefs.getString(key, null) ?: return null

        // Try to decrypt, if decryption fails, remove the value as it becomes effectively useless.
        return try {
            decrypt(encryptedString)
        } catch (e: GeneralSecurityException) {
            remove(key)
            null
        } catch (e: UnsupportedEncodingException) {
            remove(key)
            null
        } catch (e: CipherDeserializationException) {
            remove(key)
            null
        }
    }

    private fun decrypt(string: String): String {
        val ct = cipherTextSerializer.fromString(string)
        val pt = aesEncryptionManager.decrypt(ct)
        return String(pt, Charsets.UTF_8)
    }

    private fun encrypt(string: String): String {
        val ct = aesEncryptionManager.encrypt(string.toByteArray(Charsets.UTF_8))
        return cipherTextSerializer.toString(ct)
    }

    companion object {
        private const val AES_SECURE_SHARED_PREFERENCES_FILE_NAME = "com.quote.mosaic.AesSecurePreferences"
    }
}
