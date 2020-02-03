package com.quote.mosaic.core.manager

interface AdsManager {

    fun loadInterSplash(
        onWatched: () -> Unit,
        onLoaded: () -> Unit
    )

    fun showOnboardingNameInter()

    fun showOnboardingGameInter()

    fun showOnboardingCoinsInter()

    fun showGameCompleteInter()

    fun showPopupCloseInter()

    fun showBackButtonInter()

    fun showErrorRetryInter()

    fun showMainChangeTopicInter()

    fun showMainNavigationInter()

    fun showColorChaneInter()

}