package com.quote.mosaic.core.manager

import io.reactivex.processors.BehaviorProcessor

class FirebaseManager {

    private val deviceIdEmitter = BehaviorProcessor.create<String>()

    val deviceId = deviceIdEmitter

    init {
//        FirebaseInstanceId.getInstance().instanceId
//            .addOnSuccessListener { deviceIdEmitter.onNext(it.id) }
//            .addOnFailureListener {
//                Timber.w(
//                    it,
//                    "Error retrieving current token in FirebaseManager"
//                )
//            }
    }

}