package com.quote.mosaic.ui.main.play

interface CategoryClickListener {
    fun onClosedClicked(id: Int, name: String)
    fun onCompletedClicked(id: Int)
    fun onOpenedClicked(id: Int, name: String)
    fun onRefreshClicked()
}