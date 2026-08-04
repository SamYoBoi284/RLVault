package com.rlvault.ui.session

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Two constructors so this can be hosted either by SessionsFragment (the current nav
 * destination) or by the standalone SessionTrackingActivity (kept around as a fallback entry
 * point in case the bottom-nav refactor needs to be rolled back — see AndroidManifest.xml).
 */
class SessionPagerAdapter : FragmentStateAdapter {
    constructor(fragment: Fragment) : super(fragment)
    constructor(activity: FragmentActivity) : super(activity)

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {

    return when(position) {

        0 -> ManualSessionFragment()

        1 -> AutomaticSessionFragment()

        else -> SessionHistoryFragment()
        }
    }
}