package com.rlvault.ui.clip

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rlvault.ui.review.PendingClipsFragment
import com.rlvault.ui.review.ReviewedClipsFragment

class ClipsPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PendingClipsFragment()
            else -> ReviewedClipsFragment()
        }
    }
}
