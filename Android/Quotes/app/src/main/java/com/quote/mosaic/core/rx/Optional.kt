package com.quote.mosaic.core.rx

sealed class Optional<T> {
    data class Some<T> internal constructor(val value: T) : Optional<T>()
    open class None<T> internal constructor() : Optional<T>()
    private object NoneInstance : None<Any>()

    fun valueOrNull(): T? = when (this) {
        is Some -> this.value
        is None -> null
    }

    companion object {
        fun <T> of(value: T?): Optional<T> = value?.let { Some(value) } ?: none()
        @Suppress("UNCHECKED_CAST")
        fun <T> none(): None<T> = NoneInstance as None<T>
    }
}
