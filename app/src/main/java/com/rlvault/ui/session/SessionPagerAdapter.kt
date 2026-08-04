package com.rlvault.ui.session

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SessionPagerAdapter(
    activity: SessionTrackingActivity
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {

    return when(position) {

        0 -> ManualSessionFragment()

        1 -> AutomaticSessionFragment()

        else -> SessionHistoryFragment()
        }
    }
}