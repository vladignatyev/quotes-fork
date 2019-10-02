package com.brain.words.puzzle.quotes.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.brain.words.puzzle.quotes.ui.onboarding.game.OnboardingGameFragment
import com.brain.words.puzzle.quotes.ui.onboarding.login.LoginFragment

class OnboardingPagerAdapter(
    fm: FragmentManager
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> LoginFragment.newInstance()
        1 -> OnboardingGameFragment.newInstance()
        else -> throw IndexOutOfBoundsException("OnboardingPagerAdapter can't find index")
    }

    override fun getPageTitle(position: Int): CharSequence? = null
}