package com.quote.mosaic.crypto.fs.cipher

interface CipherTextSerializer {

    fun toString(cipherText: CipherText): String

    @Throws(CipherDeserializationException::class)
    fun fromString(serialized: String): CipherText
}