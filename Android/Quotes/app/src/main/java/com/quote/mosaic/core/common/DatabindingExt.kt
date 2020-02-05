package com.quote.mosaic.core.common

import androidx.databinding.BaseObservable
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

fun <T> ObservableField<T>.toFlowable(): Flowable<T> = toFlowable { this.get()!! }

private fun <T> BaseObservable.toFlowable(getValueFunc: () -> T) = Flowable.create<T>({
    val callback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(dataBindingObservable: Observable, propertyId: Int) {
            it.onNext(getValueFunc())
        }
    }
    addOnPropertyChangedCallback(callback)
    it.setCancellable {
        removeOnPropertyChangedCallback(callback)
    }

}, BackpressureStrategy.LATEST)