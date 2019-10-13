package com.brain.words.puzzle.crypto.fs.cipher

import android.util.Base64

internal object DefaultCipherTextSerializer : CipherTextSerializer {

    override fun toString(cipherText: CipherText): String {
        val iv = toBase64(cipherText.iv)
        val ct = toBase64(cipherText.cipherText)
        val mac = toBase64(cipherText.mac)
        return "$iv|$ct|$mac"
    }

    override fun fromString(serialized: String): CipherText {
        val parts = serialized.split("|")

        if (parts.size != 3) {
            throw CipherDeserializationException("Unsupported parts size ${parts.size}")
        }

        val iv = fromBase64(parts[0])
        val ct = fromBase64(parts[1])
        val hmac = fromBase64(parts[2])
        return CipherText(iv, ct, hmac)
    }

    private fun toBase64(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun fromBase64(string: String) = Base64.decode(string, Base64.NO_WRAP)
}