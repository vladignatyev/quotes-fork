package com.quote.mosaic.ui.onboarding.category

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.quote.mosaic.ui.main.play.topic.TopicModel

class OOPagerAdapter(
    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var items: List<TopicModel> = emptyList()

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Fragment = OnboardingTopicFragment.newInstance()

    override fun getPageTitle(position: Int) = items[position].title.toUpperCase()

    fun submitList(items: List<TopicModel>) {
        this.items = items
        notifyDataSetChanged()
    }
}