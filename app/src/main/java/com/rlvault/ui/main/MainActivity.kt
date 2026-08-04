package com.rlvault.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rlvault.R
import com.rlvault.databinding.ActivityMainBinding
import com.rlvault.ui.clip.ClipsFragment
import com.rlvault.ui.home.HomeFragment
import com.rlvault.ui.progress.ProgressFragment
import com.rlvault.ui.session.SessionsFragment
import com.rlvault.ui.settings.SettingsFragment

/**
 * App launcher Activity. Hosts a [com.google.android.material.bottomnavigation.BottomNavigationView]
 * with five destinations — Sessions, Clips, Home, Progress, Settings — swapping a single
 * Fragment in [R.id.navHostContainer] as the user taps between tabs.
 *
 * Plain manual FragmentTransactions rather than the Jetpack Navigation component: this project
 * doesn't depend on androidx.navigation, and adding it is out of scope for a navigation-only
 * refactor. Each destination Fragment is created once and kept alive (hide/show) so switching
 * tabs doesn't reset scroll position or re-trigger loading — same idea as the old TabLayout +
 * ViewPager2 screens already in this codebase.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            addDestination(SessionsFragment.TAG) { SessionsFragment() }
            addDestination(ClipsFragment.TAG) { ClipsFragment() }
            addDestination(HomeFragment.TAG) { HomeFragment() }
            addDestination(ProgressFragment.TAG) { ProgressFragment() }
            addDestination(SettingsFragment.TAG) { SettingsFragment() }
            showDestination(HomeFragment.TAG)
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val tag = when (item.itemId) {
                R.id.nav_sessions -> SessionsFragment.TAG
                R.id.nav_clips -> ClipsFragment.TAG
                R.id.nav_home -> HomeFragment.TAG
                R.id.nav_progress -> ProgressFragment.TAG
                R.id.nav_settings -> SettingsFragment.TAG
                else -> return@setOnItemSelectedListener false
            }
            showDestination(tag)
            true
        }
    }

    /** Adds a destination Fragment to the back stack-free container, hidden by default. */
    private fun addDestination(tag: String, factory: () -> Fragment) {
        val fragment = factory()
        supportFragmentManager.beginTransaction()
            .add(R.id.navHostContainer, fragment, tag)
            .hide(fragment)
            .commitNow()
    }

    /** Shows the Fragment with [tag], hiding every other already-added destination. */
    private fun showDestination(tag: String) {
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in supportFragmentManager.fragments) {
            if (fragment.tag == tag) {
                transaction.show(fragment)
            } else {
                transaction.hide(fragment)
            }
        }
        transaction.commitNow()
    }
}
