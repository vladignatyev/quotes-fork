package com.quote.mosaic.data.manager

import com.quote.mosaic.crypto.fs.sp.SecurePreferences
import com.quote.mosaic.data.model.user.UserDO
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor

class UserManager(
    private val securePreferences: SecurePreferences
) {

    private val userTrigger = BehaviorProcessor.create<User>()
    private val lowBalanceTrigger = PublishProcessor.create<Unit>()

    fun user(): Flowable<User> = userTrigger

    fun lowBalanceTrigger(): Flowable<Unit> = lowBalanceTrigger

    fun hasEmptyBalance() {
        lowBalanceTrigger.onNext(Unit)
    }

    fun setUser(user: UserDO) {
        userTrigger.onNext(
            User(
                user.balance,
                user.nickname
            )
        )
    }

    fun saveUserName(name: String) {
        securePreferences.putString(KEY_USERNAME, name)
    }

    fun getUserName(): String = securePreferences.getString(KEY_USERNAME, "").orEmpty()

    fun getUserBalance(): String = (userTrigger.value?.balance ?: 0).toString()

    fun saveSession(token: String) {
        securePreferences.putString(KEY_ACCESS_TOKEN, token)
    }

    fun getSession(): String = securePreferences.getString(KEY_ACCESS_TOKEN, "").orEmpty()

    fun logout(): Completable = Completable.fromAction {
        securePreferences.remove(KEY_ACCESS_TOKEN)
        securePreferences.remove(KEY_USERNAME)
        securePreferences.remove(KEY_DEVICE_TOKEN)
    }

    fun saveDeviceToken(deviceToken: String) {
        securePreferences.putString(KEY_DEVICE_TOKEN, deviceToken)
    }

    fun getDeviceToken() = securePreferences.getString(KEY_DEVICE_TOKEN, "")

    private companion object {
        private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
        private const val KEY_USERNAME = "KEY_USERNAME"
        private const val KEY_DEVICE_TOKEN = "KEY_DEVICE_TOKEN"
    }

}

data class User(
    val balance: Int,
    val name: String
)