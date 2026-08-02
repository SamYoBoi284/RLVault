package com.rlvault.ui.review

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.rlvault.databinding.ActivityClipDetailBinding
import com.rlvault.di.ServiceLocator
import com.rlvault.ui.player.PlayerActivity

/**
 * Review screen for a single clip. Also reused to re-edit an already-reviewed clip (opened from
 * ReviewedClipListActivity) — saving here re-marks reviewed and re-runs the achievement
 * evaluator either way, so editing an existing review is harmless.
 */
class ClipDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClipDetailBinding
    private var filePath: String? = null

    private val viewModel: ClipDetailViewModel by viewModels {
        val clipId = intent.getLongExtra(EXTRA_CLIP_ID, -1L)
        ClipDetailViewModel.Factory(
            clipId,
            ServiceLocator.clipRepository,
            ServiceLocator.mechanicRepository,
            ServiceLocator.achievementEvaluator
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClipDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.clip.observe(this) { clip ->
            if (clip == null) return@observe
            filePath = clip.filePath
            binding.clipTitleText.text = clip.title ?: "(untitled clip)"
            binding.ratingInput.setText(clip.rating?.toString() ?: "")
            binding.favoriteCheckbox.isChecked = clip.favorite
            binding.notesInput.setText(clip.notes ?: "")
            binding.mechanicsInput.setText(clip.mechanics.joinToString(", ") { it.name })
        }

        binding.playButton.setOnClickListener { playClip() }

        binding.markReviewedButton.setOnClickListener {
            val rating = binding.ratingInput.text.toString().toIntOrNull()
            val favorite = binding.favoriteCheckbox.isChecked
            val notes = binding.notesInput.text.toString().ifBlank { null }
            val mechanicsCsv = binding.mechanicsInput.text.toString()
            viewModel.saveAndMarkReviewed(rating, favorite, notes, mechanicsCsv)
        }

        viewModel.saveResult.observe(this) { result ->
            if (result != null) {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun playClip() {
        val path = filePath ?: return
        startActivity(
            Intent(this, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_FILE_PATH, path)
        )
    }

    companion object {
        const val EXTRA_CLIP_ID = "extra_clip_id"
    }
}
