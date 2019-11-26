package com.quote.mosaic.game

interface GameListener {
    fun onQuoteOrderChanged(userVariant: ArrayList<String>)
}