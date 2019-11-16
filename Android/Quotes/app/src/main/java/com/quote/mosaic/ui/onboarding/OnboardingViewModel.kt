package com.quote.mosaic.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quote.mosaic.data.UserManager
import com.quote.mosaic.core.AppViewModel
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor

class OnboardingViewModel(
    private val userManager: UserManager
) : AppViewModel() {

    private val successTrigger = BehaviorProcessor.create<Unit>()

    val state = State(
        successTrigger = successTrigger
    )

    override fun initialise() {
        if (userManager.getSession().isNotEmpty()) {
            successTrigger.onNext(Unit)
        }
    }

    data class State(
        val successTrigger: Flowable<Unit>
    )

    class Factory(
        private val userManager: UserManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return OnboardingViewModel(userManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}