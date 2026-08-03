package com.rlvault.ui.session

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rlvault.R

class SessionTrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_session_tracking)

        val tabs = findViewById<TabLayout>(R.id.sessionTabs)
        val pager = findViewById<ViewPager2>(R.id.sessionPager)

        pager.adapter = SessionPagerAdapter(this)

        TabLayoutMediator(tabs, pager) { tab, position ->
            tab.text = when(position) {
                0 -> "Manual"
                else -> "Automatic"
            }
        }.attach()
    }
}