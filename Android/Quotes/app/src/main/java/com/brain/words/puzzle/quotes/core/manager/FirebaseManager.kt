package com.brain.words.puzzle.quotes.core.manager

import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.processors.BehaviorProcessor
import timber.log.Timber

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