package com.quote.mosaic.ui.game.hint

sealed class HintModel {

    data class SkipHint(
        val text: String,
        val price: String
    ) : HintModel()

    data class CoinHint(
        val type: HintType,
        val text: String,
        val price: String
    ) : HintModel()

    data class Balance(
        val balance: String
    ) : HintModel()

    object Close : HintModel()
}

enum class HintType {
    NEXT_WORD,
    AUTHOR
}