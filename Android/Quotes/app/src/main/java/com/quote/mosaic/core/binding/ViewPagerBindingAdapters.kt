package com.quote.mosaic.core.binding

import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class ViewPagerBindingAdapters {

    @BindingAdapter("viewPager")
    fun bindViewPagerTabs(tabs: TabLayout, pager: ViewPager) {
        tabs.setupWithViewPager(pager, true)
    }

    @BindingAdapter("adapter")
    fun setAdapter(view: ViewPager, adapter: PagerAdapter?) {
        if (view.adapter == adapter) return
        view.adapter = adapter
    }
}