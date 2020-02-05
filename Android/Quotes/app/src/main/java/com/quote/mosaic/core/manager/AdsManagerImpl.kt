package com.quote.mosaic.core.manager

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.quote.mosaic.R

class AdsManagerImpl(
    private val context: Context
) : AdsManager {

    private val interOnboardingName = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_onboarding_name)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    private val interOnboardingGame = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_onboarding_game)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    private val interOnboardingCoins = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_onboarding_coins)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    private val interGameComplete = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_game_complete)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    private val interMainNavigation = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_popup_close)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    private val interColorChange = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_popup_close)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    private val interPopupClose = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_popup_close)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    private val interBackButton = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_back_button)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    private val interErrorRetry = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_error_retry)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    private val interMainChangeTopic = InterstitialAd(context).apply {
        adUnitId = context.getString(R.string.inter_main_change_topic)
        adListener = object : AdListener() {
            override fun onAdClosed() {
                loadAd(AdRequest.Builder().build())
            }
        }
    }

    override fun loadAds() {
        interOnboardingName.loadAd(AdRequest.Builder().build())
        interOnboardingGame.loadAd(AdRequest.Builder().build())
        interOnboardingCoins.loadAd(AdRequest.Builder().build())
        interGameComplete.loadAd(AdRequest.Builder().build())
        interMainNavigation.loadAd(AdRequest.Builder().build())
        interColorChange.loadAd(AdRequest.Builder().build())
        interPopupClose.loadAd(AdRequest.Builder().build())
        interBackButton.loadAd(AdRequest.Builder().build())
        interErrorRetry.loadAd(AdRequest.Builder().build())
        interMainChangeTopic.loadAd(AdRequest.Builder().build())
    }

    override fun loadInterSplash(
        onWatched: () -> Unit,
        onLoaded: () -> Unit
    ) {
        InterstitialAd(context).apply {
            adUnitId = context.getString(R.string.inter_splash)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    onLoaded()
                    show()
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    onWatched()
                }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    override fun showOnboardingNameInter() {
        if (interOnboardingName.isLoaded) {
            interOnboardingName.show()
        }
    }

    override fun showOnboardingGameInter() {
        if (interOnboardingGame.isLoaded) {
            interOnboardingGame.show()
        }
    }

    override fun showOnboardingCoinsInter() {
        if (interOnboardingCoins.isLoaded) {
            interOnboardingCoins.show()
        }
    }

    override fun showGameCompleteInter() {
        if (interGameComplete.isLoaded) {
            interGameComplete.show()
        }
    }

    override fun showPopupCloseInter() {
        if (interPopupClose.isLoaded) {
            interPopupClose.show()
        }
    }

    override fun showBackButtonInter() {
        if (interBackButton.isLoaded) {
            interBackButton.show()
        }
    }

    override fun showErrorRetryInter() {
        if (interErrorRetry.isLoaded) {
            interErrorRetry.show()
        }
    }

    override fun showMainChangeTopicInter() {
        if (interMainChangeTopic.isLoaded) {
            interMainChangeTopic.show()
        }
    }

    override fun showMainNavigationInter() {
        if (interMainNavigation.isLoaded) {
            interMainNavigation.show()
        }
    }

    override fun showColorChaneInter() {
        if (interColorChange.isLoaded) {
            interColorChange.show()
        }
    }

}