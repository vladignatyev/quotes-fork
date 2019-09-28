package com.brain.words.puzzle.quotes.ui.onboarding.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brain.words.puzzle.data.api.ApiClient
import com.brain.words.puzzle.quotes.core.AppViewModel
import com.brain.words.puzzle.quotes.core.Schedulers

class LoginViewModel(
    private val schedulers: Schedulers,
    private val apiClient: ApiClient
) : AppViewModel() {

    override fun initialise() {

    }

    class Factory(
        private val schedulers: Schedulers,
        private val apiClient: ApiClient
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(schedulers, apiClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}