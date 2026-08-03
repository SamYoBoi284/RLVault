package com.rlvault.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import com.rlvault.databinding.ActivityHomeBinding
import com.rlvault.di.ServiceLocator
import com.rlvault.ui.clip.ImportClipActivity
import com.rlvault.ui.dev.DeveloperModeActivity
import com.rlvault.ui.session.LogSessionActivity
import com.rlvault.ui.review.ClipListActivity
import com.rlvault.ui.review.ReviewedClipListActivity
import com.rlvault.ui.session.SessionTrackingActivity

/**
 * The app's launcher Activity. Shows live stats (pending review count, last session, latest
 * achievement) pulled through [HomeViewModel] from the repositories in [ServiceLocator].
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(
            clipRepository = ServiceLocator.clipRepository,
            sessionRepository = ServiceLocator.sessionRepository,
            achievementRepository = ServiceLocator.achievementRepository,
            achievementEvaluator = ServiceLocator.achievementEvaluator
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.pendingReviewText.observe(this) { binding.pendingReviewValue.text = it }
        viewModel.lastSessionText.observe(this) { binding.lastSessionValue.text = it }
        viewModel.latestAchievementText.observe(this) { binding.latestAchievementValue.text = it }

        binding.importClipButton.setOnClickListener {
            startActivity(Intent(this, ImportClipActivity::class.java))
        }
        binding.logSessionButton.setOnClickListener {
            startActivity(Intent(this, SessionTrackingActivity::class.java))
        }
        binding.devModeButton.setOnClickListener {
            startActivity(Intent(this, DeveloperModeActivity::class.java))
        }
        binding.reviewClipsButton.setOnClickListener {
            startActivity(Intent(this, ClipListActivity::class.java))
        }
        binding.reviewedClipsButton.setOnClickListener {
            startActivity(Intent(this, ReviewedClipListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Cheap enough at this data scale, and keeps stats fresh if the user imported clips
        // or logged a session in another screen and came back to Home.
        viewModel.refresh()
    }
}
