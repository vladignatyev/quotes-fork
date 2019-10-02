package com.brain.words.puzzle.quotes.core.rx

import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class ClearableBehaviorProcessor<T> private constructor() {

    private val behaviorProcessor = BehaviorProcessor.create<Optional<T>>()

    private val optionalFlowable = behaviorProcessor.compose(UnwrapOptionalTransform())

    fun clear() {
        behaviorProcessor.onNext(Optional.none())
    }

    fun onNext(item: T) {
        behaviorProcessor.onNext(Optional.of(item))
    }

    fun clearable(): Flowable<T> = optionalFlowable

    companion object {
        fun <T> create() = ClearableBehaviorProcessor<T>()
    }
}