package com.brain.words.puzzle.quotes.ui.main.play

interface CategoryClickListener {
    fun onClosedClicked(id: Int)
    fun onCompletedClicked(id: Int)
    fun onOpenedClicked(id: Int)
}