package com.rlvault.ui.clip

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rlvault.R

/**
 * Clips destination: replaces the old separate "Review Clips" and "Reviewed Clips" screens with
 * Pending / Reviewed tabs. The review workflow and reviewed-list logic are unchanged — they just
 * live in [PendingClipsFragment] and [ReviewedClipsFragment] now (ported from the old
 * ClipListActivity / ReviewedClipListActivity).
 *
 * Also hosts the Import Clip entry point (as a FAB) since removing the Home screen's button
 * needs a new home for it — Clips is the natural landing spot. This wasn't spelled out in the
 * navigation spec, so flagging the assumption here.
 */
class ClipsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_clips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabs = view.findViewById<TabLayout>(R.id.clipTabs)
        val pager = view.findViewById<ViewPager2>(R.id.clipPager)

        pager.adapter = ClipsPagerAdapter(this)

        TabLayoutMediator(tabs, pager) { tab, position ->
            tab.text = when (position) {
                0 -> "Pending"
                else -> "Reviewed"
            }
        }.attach()

        view.findViewById<FloatingActionButton>(R.id.importClipFab).setOnClickListener {
            startActivity(Intent(requireContext(), ImportClipActivity::class.java))
        }
    }

    companion object {
        const val TAG = "ClipsFragment"
    }
}
