package com.rlvault.ui.dev

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.rlvault.databinding.ActivityDeveloperModeBinding
import com.rlvault.di.ServiceLocator

/**
 * Developer Mode, first cut. Only the two actions the trackers already called out:
 * "Recalculate statistics" (re-run the achievement evaluator against current data) and
 * "Reset achievement progress" (clear unlock state, keep definitions — matches
 * AchievementRepository.resetAllProgress's own doc comment). Achievement definition
 * create/edit (upsert/delete) is a later pass — this screen is just the two actions that exist
 * today.
 */
class DeveloperModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeveloperModeBinding

    private val viewModel: DeveloperModeViewModel by viewModels {
        DeveloperModeViewModel.Factory(
            ServiceLocator.achievementEvaluator,
            ServiceLocator.achievementRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeveloperModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recalculateButton.setOnClickListener { viewModel.recalculateStatistics() }

        binding.resetProgressButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(com.rlvault.R.string.dev_reset_confirm_title))
                .setMessage(getString(com.rlvault.R.string.dev_reset_confirm_message))
                .setPositiveButton(getString(com.rlvault.R.string.dev_reset_confirm_positive)) { _, _ ->
                    viewModel.resetAchievementProgress()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        viewModel.statusText.observe(this) { binding.statusText.text = it }
    }
}
