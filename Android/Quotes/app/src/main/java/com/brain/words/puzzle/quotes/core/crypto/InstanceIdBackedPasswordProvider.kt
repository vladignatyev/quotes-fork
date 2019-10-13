package com.brain.words.puzzle.quotes.core.crypto

import android.provider.Settings.Secure.ANDROID_ID
import com.brain.words.puzzle.crypto.fs.sp.PasswordProvider
import com.brain.words.puzzle.quotes.core.ext.Digest

class InstanceIdBackedPasswordProvider : PasswordProvider {

    override fun password(): String = Digest.sha256(SALT + ANDROID_ID)

    companion object {
        private const val SALT = "116dcd96f930c90064252689eab46c7bff8Fc5ac3a62e657743414b71d9d9133"
    }
}