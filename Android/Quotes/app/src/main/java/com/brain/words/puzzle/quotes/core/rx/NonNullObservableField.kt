package com.brain.words.puzzle.quotes.core.rx

import androidx.databinding.ObservableField

open class NonNullObservableField<T>(value: T) : ObservableField<T>(value) {
    override fun get(): T {
        return super.get() ?: throw IllegalStateException("Never null")
    }

    override fun set(value: T) {
        super.set(value)
    }
}