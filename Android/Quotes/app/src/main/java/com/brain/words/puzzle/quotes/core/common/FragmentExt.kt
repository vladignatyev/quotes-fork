package com.brain.words.puzzle.quotes.core.common

import androidx.fragment.app.Fragment

inline fun <reified T> Fragment.parentAs(): T {
    val parent = parentFragment ?: activity
    return parent as? T ?: throw ClassCastException("$parent must implement ${T::class}")
}

fun Fragment.args() = arguments ?: throw IllegalArgumentException("Arguments shouldn't be null")

/**
 * Returns fragment's parent as an instance of the required type.
 * Throws an [IllegalArgumentException] if required type can't be assigned from fragment's parent.
 */
inline fun <reified T : Any> Fragment.requireParent(): T {
    val parent = requireNotNull(parentFragment ?: activity) { "Fragment is not attached to a parent" }
    return requireNotNull(parent as? T) { "${parent::class.java.canonicalName} does not implement ${T::class.java.canonicalName}" }
}
