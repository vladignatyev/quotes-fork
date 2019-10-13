package com.brain.words.puzzle.quotes.core.manager

import com.brain.words.puzzle.crypto.fs.sp.SecurePreferences

class UserManager(
    private val securePreferences: SecurePreferences
) {

    fun saveUserName(name: String) {
        securePreferences.putString(KEY_USERNAME, name)
    }

    fun getUserName(): String = securePreferences.getString(KEY_USERNAME, "").orEmpty()

    fun saveSession(token: String) {
        securePreferences.putString(KEY_ACCESS_TOKEN, token)
    }

    fun getSession(): String = securePreferences.getString(KEY_ACCESS_TOKEN, "").orEmpty()

    private companion object {
        private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
        private const val KEY_USERNAME = "KEY_USERNAME"
    }

}