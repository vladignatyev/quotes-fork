package com.brain.words.puzzle.data.storage

interface KeyValueStorage {
    fun putString(key: String, value: String?)

    fun getString(key: String, defValue: String?): String?

    fun putLong(key: String, value: Long)

    fun getLong(key: String, defValue: Long): Long

    fun putInt(key: String, value: Int)

    fun getInt(key: String, defValue: Int): Int

    fun putBoolean(key: String, value: Boolean)

    fun getBoolean(key: String, defValue: Boolean): Boolean

    fun putStringSet(key: String, value: Set<String>?)

    fun getStringSet(key: String, defValue: Set<String>?): Set<String>?

    fun putFloat(key: String, value: Float)

    fun getFloat(key: String, defValue: Float): Float

    operator fun contains(key: String): Boolean

    fun remove(key: String)

    fun clear()
}