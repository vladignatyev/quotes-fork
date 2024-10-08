package com.quote.mosaic.core.manager

import com.google.firebase.iid.FirebaseInstanceId
import com.quote.mosaic.data.manager.UserManager
import timber.log.Timber

class FirebaseManager(
    private val userManager: UserManager
) {

    init {
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener {
                userManager.saveDeviceToken(it.token)
            }
            .addOnFailureListener {
                Timber.w(it, "Error retrieving device token in FirebaseManager")
            }
    }
}