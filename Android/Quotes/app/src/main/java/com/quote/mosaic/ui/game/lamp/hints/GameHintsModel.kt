package com.quote.mosaic.ui.game.lamp.hints

sealed class GameHintsModel {
    data class Hint(val text: String, val amount: Int) : GameHintsModel()
}