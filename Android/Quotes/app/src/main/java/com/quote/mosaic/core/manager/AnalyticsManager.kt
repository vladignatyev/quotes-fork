package com.quote.mosaic.core.manager

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger

interface AnalyticsManager {

    //------------ COMMON ------------ //
    fun logCurrentScreen(activity: Activity, screenName: String)

    fun logUserErrorAppeared(text: String)

    //------------ ONBOARDING ------------ //
    fun logOnboardingStarted()

    //Step one started
    fun logOnboardingNameStarted()

    fun logOnboardingNameFinished()

    //Step two started
    fun logOnboardingOpenCategoryStarted()

    fun logOnboardingClosedTapped()

    fun logOnboardingOpenedTapped()

    //Step two finished
    fun logOnboardingOpenCategoryFinished()

    //Step three started
    fun logOnboardingGameStarted()

    fun logOnboardingGameFinished()

    fun logOnboardingFinished()

    //------------ MONEY ------------ //
    fun logTopupScreenOpened(source: String)

    fun logTopupItemPurchaseStarted(sku: String)

    fun logTopupItemPurchaseSuccess(sku: String)

    fun logTopupItemPurchaseError(sku: String, errorMsg: String)

    //------------ OVERVIEW ------------ //
    fun logTopicChanged(topicName: String)

    fun logClosedCategoryTapped(categoryId: Int, categoryName: String)

    fun logOpenedCategoryTapped(categoryId: Int, categoryName: String)

    //------------ PROFILE ------------ //
    fun logShareClicked()

    fun logBackgroundColorChanged(newColor: String)

    fun logProblemFeedbackClicked()

    //------------ GAME ------------ //
    fun logGameStarted(levelId: Int, categoryId: Int)

    fun logGameCompleted(levelId: Int, categoryId: Int)

    fun logNextWordClicked(levelId: Int)

    fun logNextWordUsed(levelId: Int, isRewardedProduct: Boolean)

    fun logSkipClicked(levelId: Int)

    fun logSkipUsed(levelId: Int)

    fun logAuthorClicked(levelId: Int)

    fun logAuthorUsed(levelId: Int)

    fun logDoubleUpClicked(levelId: Int, currentBalance: Int)


}

class AnalyticsManagerImpl(
    private val context: Context
) : AnalyticsManager {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    private val facebookAnalytics = AppEventsLogger.newLogger(context)

    // Common
    override fun logCurrentScreen(activity: Activity, screenName: String) {
        firebaseAnalytics.setCurrentScreen(activity, screenName, null)
    }

    override fun logUserErrorAppeared(text: String) {
        firebaseAnalytics.logEvent("error_screen_appeared", Bundle().apply {
            putString("error_text", text)
        })
    }

    // Onboarding
    override fun logOnboardingStarted() {
        firebaseAnalytics.logEvent("onboarding_started", null)
        facebookAnalytics.logEvent("onboarding_started", null)
    }

    override fun logOnboardingNameStarted() {
        firebaseAnalytics.logEvent("onboarding_step_one_started", null)
        facebookAnalytics.logEvent("onboarding_step_one_started", null)
    }

    override fun logOnboardingNameFinished() {
        firebaseAnalytics.logEvent("onboarding_step_one_finished", null)
        facebookAnalytics.logEvent("onboarding_step_one_finished", null)
    }

    override fun logOnboardingOpenCategoryStarted() {
        firebaseAnalytics.logEvent("onboarding_step_two_started", null)
        facebookAnalytics.logEvent("onboarding_step_two_started", null)
    }

    override fun logOnboardingClosedTapped() {
        firebaseAnalytics.logEvent("onboarding_step_two_closed_tapped", null)
        facebookAnalytics.logEvent("onboarding_step_two_closed_tapped", null)
    }

    override fun logOnboardingOpenedTapped() {
        firebaseAnalytics.logEvent("onboarding_step_two_opened_tapped", null)
        facebookAnalytics.logEvent("onboarding_step_two_opened_tapped", null)
    }

    override fun logOnboardingOpenCategoryFinished() {
        firebaseAnalytics.logEvent("onboarding_step_two_finished", null)
        facebookAnalytics.logEvent("onboarding_step_two_finished", null)
    }

    override fun logOnboardingGameStarted() {
        firebaseAnalytics.logEvent("onboarding_step_three_started", null)
        facebookAnalytics.logEvent("onboarding_step_three_started", null)
    }

    override fun logOnboardingGameFinished() {
        firebaseAnalytics.logEvent("onboarding_step_three_finished", null)
        facebookAnalytics.logEvent("onboarding_step_three_finished", null)
    }

    override fun logOnboardingFinished() {
        firebaseAnalytics.logEvent("onboarding_finished", null)
        facebookAnalytics.logEvent("onboarding_finished", null)
    }

    // Money
    override fun logTopupScreenOpened(source: String) {
        firebaseAnalytics.logEvent("topup_screen_appeared", Bundle().apply {
            putString("source", source)
        })
        facebookAnalytics.logEvent("topup_screen_appeared", Bundle().apply {
            putString("source", source)
        })
    }

    override fun logTopupItemPurchaseStarted(sku: String) {
        firebaseAnalytics.logEvent("topup_item_purchase_started", Bundle().apply {
            putString("sku", sku)
        })
        facebookAnalytics.logEvent("topup_item_purchase_started", Bundle().apply {
            putString("sku", sku)
        })
    }

    override fun logTopupItemPurchaseSuccess(sku: String) {
        firebaseAnalytics.logEvent("topup_item_purchase_success", Bundle().apply {
            putString("sku", sku)
        })
        facebookAnalytics.logEvent("topup_item_purchase_success", Bundle().apply {
            putString("sku", sku)
        })
    }

    override fun logTopupItemPurchaseError(sku: String, errorMsg: String) {
        firebaseAnalytics.logEvent("topup_item_purchase_error", Bundle().apply {
            putString("sku", sku)
            putString("errorMsg", errorMsg)
        })
        facebookAnalytics.logEvent("topup_item_purchase_error", Bundle().apply {
            putString("sku", sku)
            putString("errorMsg", errorMsg)
        })
    }

    // Overview
    override fun logTopicChanged(topicName: String) {
        firebaseAnalytics.logEvent("overview_topic_appeared", Bundle().apply {
            putString("topicName", topicName)
        })
        facebookAnalytics.logEvent("overview_topic_appeared", Bundle().apply {
            putString("topicName", topicName)
        })
    }

    override fun logClosedCategoryTapped(categoryId: Int, categoryName: String) {
        firebaseAnalytics.logEvent("closed_category_tapped", Bundle().apply {
            putInt("categoryId", categoryId)
            putString("categoryName", categoryName)
        })

        facebookAnalytics.logEvent("closed_category_tapped", Bundle().apply {
            putInt("categoryId", categoryId)
            putString("categoryName", categoryName)
        })
    }

    override fun logOpenedCategoryTapped(categoryId: Int, categoryName: String) {
        firebaseAnalytics.logEvent("opened_category_tapped", Bundle().apply {
            putInt("categoryId", categoryId)
            putString("categoryName", categoryName)
        })
        facebookAnalytics.logEvent("opened_category_tapped", Bundle().apply {
            putInt("categoryId", categoryId)
            putString("categoryName", categoryName)
        })
    }

    // Profile
    override fun logShareClicked() {
        firebaseAnalytics.logEvent("share_tapped", null)
        facebookAnalytics.logEvent("share_tapped", null)
    }

    override fun logBackgroundColorChanged(newColor: String) {
        firebaseAnalytics.logEvent("background_color_changed", Bundle().apply {
            putString("newColor", newColor)
        })
        facebookAnalytics.logEvent("background_color_changed", Bundle().apply {
            putString("newColor", newColor)
        })
    }

    override fun logProblemFeedbackClicked() {
        firebaseAnalytics.logEvent("problem_feedback_tapped", null)
        facebookAnalytics.logEvent("problem_feedback_tapped", null)
    }

    // Game
    override fun logGameStarted(levelId: Int, categoryId: Int) {
        firebaseAnalytics.logEvent("game_started", Bundle().apply {
            putInt("categoryId", categoryId)
            putInt("levelId", levelId)
        })
        facebookAnalytics.logEvent("game_started", Bundle().apply {
            putInt("categoryId", categoryId)
            putInt("levelId", levelId)
        })
    }

    override fun logGameCompleted(levelId: Int, categoryId: Int) {
        firebaseAnalytics.logEvent("game_completed", Bundle().apply {
            putInt("categoryId", categoryId)
            putInt("levelId", levelId)
        })
        facebookAnalytics.logEvent("game_completed", Bundle().apply {
            putInt("categoryId", categoryId)
            putInt("levelId", levelId)
        })
    }

    override fun logNextWordClicked(levelId: Int) {
        firebaseAnalytics.logEvent("game_hint_next_word_tapped", Bundle().apply {
            putInt("levelId", levelId)
        })
        facebookAnalytics.logEvent("game_hint_next_word_tapped", Bundle().apply {
            putInt("levelId", levelId)
        })
    }

    override fun logNextWordUsed(levelId: Int, isRewardedProduct: Boolean) {
        firebaseAnalytics.logEvent("game_hint_next_word_used", Bundle().apply {
            putInt("levelId", levelId)
            putBoolean("isRewardedProduct", isRewardedProduct)
        })
        facebookAnalytics.logEvent("game_hint_next_word_used", Bundle().apply {
            putInt("levelId", levelId)
            putBoolean("isRewardedProduct", isRewardedProduct)
        })
    }

    override fun logSkipClicked(levelId: Int) {
        firebaseAnalytics.logEvent("game_hint_skip_tapped", Bundle().apply {
            putInt("levelId", levelId)
        })
        facebookAnalytics.logEvent("game_hint_skip_tapped", Bundle().apply {
            putInt("levelId", levelId)
        })
    }

    override fun logSkipUsed(levelId: Int) {
        firebaseAnalytics.logEvent("game_hint_skip_used", Bundle().apply {
            putInt("levelId", levelId)
        })
        facebookAnalytics.logEvent("game_hint_skip_used", Bundle().apply {
            putInt("levelId", levelId)
        })
    }

    override fun logAuthorClicked(levelId: Int) {
        firebaseAnalytics.logEvent("game_hint_author_tapped", Bundle().apply {
            putInt("levelId", levelId)
        })
        facebookAnalytics.logEvent("game_hint_author_tapped", Bundle().apply {
            putInt("levelId", levelId)
        })
    }

    override fun logAuthorUsed(levelId: Int) {
        firebaseAnalytics.logEvent("game_hint_author_used", Bundle().apply {
            putInt("levelId", levelId)
        })
        facebookAnalytics.logEvent("game_hint_author_used", Bundle().apply {
            putInt("levelId", levelId)
        })
    }

    override fun logDoubleUpClicked(levelId: Int, currentBalance: Int) {
        firebaseAnalytics.logEvent("game_double_up_tapped", Bundle().apply {
            putInt("levelId", levelId)
            putInt("currentBalance", currentBalance)
        })
        facebookAnalytics.logEvent("game_double_up_tapped", Bundle().apply {
            putInt("levelId", levelId)
            putInt("currentBalance", currentBalance)
        })
    }

}