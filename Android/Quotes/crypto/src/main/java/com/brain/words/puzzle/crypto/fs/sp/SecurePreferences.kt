package com.brain.words.puzzle.crypto.fs.sp

import android.content.Context
import android.os.Build
import com.brain.words.puzzle.crypto.aes.AesEncryptionManager
import com.brain.words.puzzle.crypto.fs.cipher.DefaultCipherTextSerializer
import timber.log.Timber
import java.security.KeyStore

interface SecurePreferences {

    fun putString(key: String, value: String)
    fun getString(key: String, defValue: String?): String?

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defValue: Boolean): Boolean

    fun remove(key: String)

    companion object {
        fun default(context: Context, uniqueIdProvider: PasswordProvider) = getSecurePreferences(context, uniqueIdProvider)

        private const val SHARED_PREFERENCES_COMPAT_SECURE_FILE_NAME = "com.brain.words.puzzle.crypto.CompatSecurePreferences"

        private const val PREFERENCE_PRE_MARSHMALLOW_KEY = "PREFERENCE_PRE_MARSHMALLOW"

        private const val STATE_UNKNOWN = -1
        private const val STATE_PRE_MARSHMALLOW = 0
        private const val STATE_MARSHMALLOW_PLUS = 1

        /**
         * We need compat preferences if:
         * 1. We are on pre-M because Android keystore is a mess before M
         * 2. We are on M+ and the Keystore does not work because it is sometimes a mess on M+ also
         * (for example, it did not work on Huawei Mate 9 (Android 7.0) according to the
         * beta pre-launch report - from what it looks like from the logs, the keystore was simply
         * crashing:
         *
         * W/KeyStore(10241): KeyStore binder is null, key = USRPKEY_AesKeyAlias, uid = -1
         * W/KeyStore(10241): KeyStore binder is null, key = USRSKEY_AesKeyAlias, uid = -1
         * W/KeyStore(10241): KeyStore binder is null, key = USRCERT_AesKeyAlias, uid = -1
         * W/KeyStore(10241): KeyStore binder is null, key = CACERT_AesKeyAlias, uid = -1
         * D/AndroidRuntime(10241): Shutting down VM
         */
        private fun getSecurePreferences(context: Context, passwordProvider: PasswordProvider): SecurePreferences {
            val sharedPreferences = context.getSharedPreferences(
                SHARED_PREFERENCES_COMPAT_SECURE_FILE_NAME, Context.MODE_PRIVATE)

            val persistedState = sharedPreferences.getInt(PREFERENCE_PRE_MARSHMALLOW_KEY, STATE_UNKNOWN)
            val needCompatPreferences = if (persistedState == STATE_UNKNOWN) {
                val needsCompatManager = Build.VERSION.SDK_INT < Build.VERSION_CODES.M

                val state = if (needsCompatManager) {
                    STATE_PRE_MARSHMALLOW
                } else {
                    if (keystoreIsOk()) {
                        STATE_MARSHMALLOW_PLUS
                    } else {
                        STATE_PRE_MARSHMALLOW
                    }
                }

                @Suppress("ApplySharedPref")
                sharedPreferences
                    .edit()
                    .putInt(PREFERENCE_PRE_MARSHMALLOW_KEY, state)
                    .commit()
                state == STATE_PRE_MARSHMALLOW
            } else {
                persistedState == STATE_PRE_MARSHMALLOW
            }

            return if (needCompatPreferences) {
                CompatSecurePreferences(context, passwordProvider)
            } else {
                AesSecurePreferences(context, DefaultCipherTextSerializer, getManager())
            }
        }

        // Tests that AesManager is instantiated without errors & is capable of properly encrypting
        // and decrypting.
        private fun keystoreIsOk(): Boolean =
            try {
                val manager = getManager()
                val ct = manager.encrypt("test".toByteArray())
                val pt = manager.decrypt(ct)

                if (String(pt) != "test") {
                    Timber.e("d(e(pt)) != pt")
                    false
                } else {
                    true
                }
            } catch (e: Exception) {
                Timber.e(e, "Keystore is broken, falling back to compat")
                false
            }

        private fun getManager(): AesEncryptionManager {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            return AesEncryptionManager(keyStore)
        }
    }
}