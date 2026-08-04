package com.rlvault.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rlvault.R

/**
 * Sessions destination: the existing Session Tracking UI (Manual / Automatic / History tabs),
 * moved here unchanged from the old standalone SessionTrackingActivity. Tab contents, their
 * ViewModels, and the review/session business logic are untouched.
 */
class SessionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_sessions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabs = view.findViewById<TabLayout>(R.id.sessionTabs)
        val pager = view.findViewById<ViewPager2>(R.id.sessionPager)

        pager.adapter = SessionPagerAdapter(this)

        TabLayoutMediator(tabs, pager) { tab, position ->
            tab.text = when (position) {
                0 -> "Manual"
                1 -> "Automatic"
                else -> "History"
            }
        }.attach()
    }

    companion object {
        const val TAG = "SessionsFragment"
    }
}
