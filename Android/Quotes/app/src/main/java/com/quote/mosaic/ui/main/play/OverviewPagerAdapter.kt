package com.quote.mosaic.ui.main.play

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.quote.mosaic.ui.main.play.topic.TopicFragment
import com.quote.mosaic.ui.main.play.topic.TopicModel

class OverviewPagerAdapter(
    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var items: List<TopicModel> = emptyList()

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Fragment = TopicFragment.newInstance(items[position])

    override fun getPageTitle(position: Int) = items[position].title.toUpperCase()

    fun submitList(items: List<TopicModel>) {
        this.items = items
        notifyDataSetChanged()
    }
}