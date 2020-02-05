package com.quote.mosaic.data.storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils

class PreferencesStorage(
    context: Context, name: String
) : KeyValueStorage {

    private val preferences: SharedPreferences

    init {
        require(!TextUtils.isEmpty(name)) { "Can't create preference storage without a name" }
        preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    @SuppressLint("ApplySharedPref")
    override fun putString(key: String, value: String?) {
        preferences.edit().putString(key, value).commit()
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferences.getString(key, defValue)
    }

    @SuppressLint("ApplySharedPref")
    override fun putLong(key: String, value: Long) {
        preferences.edit().putLong(key, value).commit()
    }

    override fun getLong(key: String, defValue: Long): Long {
        return preferences.getLong(key, defValue)
    }

    @SuppressLint("ApplySharedPref")
    override fun putInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).commit()
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferences.getInt(key, defValue)
    }

    @SuppressLint("ApplySharedPref")
    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).commit()
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    override fun getStringSet(key: String, defValue: Set<String>?): Set<String>? {
        return preferences.getStringSet(key, defValue)
    }

    @SuppressLint("ApplySharedPref")
    override fun putStringSet(key: String, value: Set<String>?) {
        preferences.edit().putStringSet(key, value).commit()
    }

    @SuppressLint("ApplySharedPref")
    override fun putFloat(key: String, value: Float) {
        preferences.edit().putFloat(key, value).commit()
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return preferences.getFloat(key, defValue)
    }

    override fun contains(key: String): Boolean {
        return preferences.contains(key)
    }

    @SuppressLint("ApplySharedPref")
    override fun remove(key: String) {
        preferences.edit().remove(key).commit()
    }

    @SuppressLint("ApplySharedPref")
    override fun clear() {
        preferences.edit().clear().commit()
    }

    companion object {
        val QUOTES_PREFERENCES_STORAGE = "QUOTES_PREFERENCES_STORAGE"
    }
}
