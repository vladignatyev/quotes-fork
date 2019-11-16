package com.quote.mosaic.data

import com.quote.mosaic.crypto.fs.sp.SecurePreferences
import com.quote.mosaic.data.model.UserDO
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
        userTrigger.onNext(User(user.balance, user.nickname))
    }

    fun setBalance(balance: Int) {
        userTrigger.onNext(userTrigger.value?.copy(balance = balance))
    }

    fun saveUserName(name: String) {
        securePreferences.putString(KEY_USERNAME, name)
    }

    fun getUserName(): String = securePreferences.getString(KEY_USERNAME, "").orEmpty()

    fun saveSession(token: String) {
        securePreferences.putString(KEY_ACCESS_TOKEN, token)
    }

    fun getSession(): String = securePreferences.getString(KEY_ACCESS_TOKEN, "").orEmpty()

    private companion object {
        private const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
        private const val KEY_USERNAME = "KEY_USERNAME"
    }

}

data class User(
    val balance: Int,
    val name: String
)