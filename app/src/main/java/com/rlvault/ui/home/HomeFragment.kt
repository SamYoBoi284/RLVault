package com.rlvault.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rlvault.databinding.FragmentHomeBinding
import com.rlvault.di.ServiceLocator

/**
 * Home destination: read-only dashboard. Shows Pending Review / Last Session / Latest
 * Achievement (all wired through [HomeViewModel], unchanged from the old HomeActivity) plus
 * placeholder rows for Current Goal and Career Summary, which have no logic yet.
 *
 * All the former navigation buttons (Import Clip, Log Session, Developer Mode, Review Clips,
 * Reviewed Clips) are gone — those destinations are reachable from the bottom nav / other tabs
 * now, per the navigation refactor.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(
            clipRepository = ServiceLocator.clipRepository,
            sessionRepository = ServiceLocator.sessionRepository,
            achievementRepository = ServiceLocator.achievementRepository,
            achievementEvaluator = ServiceLocator.achievementEvaluator
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pendingReviewText.observe(viewLifecycleOwner) { binding.pendingReviewValue.text = it }
        viewModel.lastSessionText.observe(viewLifecycleOwner) { binding.lastSessionValue.text = it }
        viewModel.latestAchievementText.observe(viewLifecycleOwner) { binding.latestAchievementValue.text = it }
    }

    override fun onResume() {
        super.onResume()
        // Cheap enough at this data scale, and keeps stats fresh if the user imported clips
        // or logged a session on another tab and came back to Home.
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}
