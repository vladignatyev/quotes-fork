package com.quote.mosaic.crypto.fs.sp

import android.content.Context
import com.pddstudio.preferences.encrypted.EncryptedPreferences

class CompatSecurePreferences(context: Context, passwordProvider: PasswordProvider) :
    SecurePreferences {

    private val encryptedPreferences: EncryptedPreferences

    init {
        encryptedPreferences = EncryptedPreferences.Builder(context)
            .withEncryptionPassword(passwordProvider.password())
            .withPreferenceName(ENCRYPTED_PREFERENCES_KEY_STORE_FILE_NAME)
            .build()
    }

    override fun putString(key: String, value: String) =
        encryptedPreferences.edit().putString(key, value).apply()

    override fun getString(key: String, defValue: String?): String? =
        encryptedPreferences.getString(key, defValue)

    override fun putBoolean(key: String, value: Boolean) =
        encryptedPreferences.edit().putBoolean(key, value).apply()

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        encryptedPreferences.getBoolean(key, defValue)

    override fun remove(key: String) =
        encryptedPreferences.edit().remove(key).apply()

    companion object {
        private const val ENCRYPTED_PREFERENCES_KEY_STORE_FILE_NAME = "com.quote.mosaic.EncryptedPreferences"
    }
}