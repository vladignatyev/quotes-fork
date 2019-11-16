package com.quote.mosaic.core.rx

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher

internal class UnwrapOptionalTransform<T> : FlowableTransformer<Optional<T>, T> {
    override fun apply(upstream: Flowable<Optional<T>>): Publisher<T> = upstream
            .filter {
                it is Optional.Some
            }
            .map {
                (it as Optional.Some).value
            }
}
