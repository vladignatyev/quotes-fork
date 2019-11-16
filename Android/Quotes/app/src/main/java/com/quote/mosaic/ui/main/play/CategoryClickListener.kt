package com.quote.mosaic.ui.main.play

interface CategoryClickListener {
    fun onClosedClicked(id: Int)
    fun onCompletedClicked(id: Int)
    fun onOpenedClicked(id: Int)
    fun onRefreshClicked()
}