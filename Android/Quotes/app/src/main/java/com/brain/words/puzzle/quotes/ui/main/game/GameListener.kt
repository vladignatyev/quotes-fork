package com.brain.words.puzzle.quotes.ui.main.game

interface GameListener {
    fun onClosedClicked(id: Int)
    fun onCompletedClicked(id: Int)
    fun onOpenedClicked(id: Int)
}