package com.brain.words.puzzle.crypto.fs.cipher

import com.brain.words.puzzle.crypto.fs.cipher.CipherDeserializationException
import com.brain.words.puzzle.crypto.fs.cipher.CipherText

interface CipherTextSerializer {

    fun toString(cipherText: CipherText): String

    @Throws(CipherDeserializationException::class)
    fun fromString(serialized: String): CipherText
}