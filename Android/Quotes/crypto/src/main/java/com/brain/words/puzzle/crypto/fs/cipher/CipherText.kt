package com.brain.words.puzzle.crypto.fs.cipher

import java.util.*

data class CipherText(
    val iv: ByteArray,
    val cipherText: ByteArray,
    val mac: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CipherText

        if (!Arrays.equals(iv, other.iv)) return false
        if (!Arrays.equals(cipherText, other.cipherText)) return false
        if (!Arrays.equals(mac, other.mac)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(iv)
        result = 31 * result + Arrays.hashCode(cipherText)
        result = 31 * result + Arrays.hashCode(mac)
        return result
    }
}