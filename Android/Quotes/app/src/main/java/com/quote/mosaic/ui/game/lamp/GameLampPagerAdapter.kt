package com.quote.mosaic.ui.game.lamp

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.quote.mosaic.R
import com.quote.mosaic.ui.game.lamp.buy.GameBuyFragment
import com.quote.mosaic.ui.game.lamp.hints.GameHintsFragment

class GameLampPagerAdapter(
    private val fm: FragmentManager,
    private val context: Context
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = 2

    override fun getItem(position: Int) = when (position) {
        TAB_INDEX_BUY -> GameBuyFragment.newInstance()
        TAB_INDEX_HINTS -> GameHintsFragment.newInstance()
        else -> throw IllegalArgumentException("Unsupported position $position")
    }

    override fun getPageTitle(position: Int): String = when (position) {
        TAB_INDEX_BUY -> context.getString(R.string.buy_hints)
        TAB_INDEX_HINTS -> context.getString(R.string.use_hints)
        else -> throw IllegalArgumentException("Unsupported title position $position")
    }

    companion object {
        private const val TAB_INDEX_BUY = 0
        private const val TAB_INDEX_HINTS = 1
    }
}