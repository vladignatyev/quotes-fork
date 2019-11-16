package com.quote.mosaic.core.ext

import java.security.MessageDigest

object Digest {
    fun sha256(base: String) = hash(base, "SHA-256")

    fun sha1(base: String) = hash(base, "SHA-1")

    private fun hash(base: String, alg: String)
            = MessageDigest.getInstance(alg)
        .digest(base.toByteArray(charset("UTF-8")))
        .fold(StringBuilder(), { hexString, byte ->
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
            hexString
        }).toString()
}