package com.rlvault.ui.review

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.rlvault.R
import com.rlvault.databinding.ActivityClipDetailBinding
import com.rlvault.di.ServiceLocator
import com.rlvault.ui.player.PlayerActivity

/**
 * Review screen for a single clip.
 *
 * Two modes, branched on whether queue extras are present:
 * - Queue mode (opened from PendingClipsFragment's unreviewed list): EXTRA_QUEUE_IDS carries the full
 *   unreviewed id list snapshotted once at queue-entry, EXTRA_QUEUE_POSITION the starting index.
 *   Shows a Previous/Skip/Next nav row + "Clip X of N" progress line. Save persists then
 *   auto-advances instead of finishing. Skip/Next advance without saving. Previous steps back
 *   (no-op at position 0, discards unsaved edits with a toast). Walking off either end finishes
 *   back to the Pending tab with a "queue complete" toast.
 * - Single-edit mode (opened from ReviewedClipsFragment, or anywhere else that omits the queue
 *   extras): no nav row, Save finishes immediately, same as before this feature existed.
 */
class ClipDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClipDetailBinding
    private var filePath: String? = null

    private val viewModel: ClipDetailViewModel by viewModels {
        val clipId = intent.getLongExtra(EXTRA_CLIP_ID, -1L)
        val queueIds = intent.getLongArrayExtra(EXTRA_QUEUE_IDS)
        val queuePosition = intent.getIntExtra(EXTRA_QUEUE_POSITION, 0)
        ClipDetailViewModel.Factory(
            clipId,
            queueIds,
            queuePosition,
            ServiceLocator.clipRepository,
            ServiceLocator.mechanicRepository,
            ServiceLocator.achievementEvaluator
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClipDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.isQueueMode) {
            binding.queueNavRow.visibility = View.VISIBLE
            binding.queueProgressText.visibility = View.VISIBLE
            updateQueueProgressText()
        }

        viewModel.clip.observe(this) { clip ->
            if (clip == null) return@observe
            filePath = clip.filePath
            binding.clipTitleText.text = clip.title ?: "(untitled clip)"
            binding.ratingInput.setText(clip.rating?.toString() ?: "")
            binding.favoriteCheckbox.isChecked = clip.favorite
            binding.notesInput.setText(clip.notes ?: "")
            binding.mechanicsInput.setText(clip.mechanics.joinToString(", ") { it.name })
            if (viewModel.isQueueMode) updateQueueProgressText()
        }

        binding.playButton.setOnClickListener { playClip() }

        binding.markReviewedButton.setOnClickListener {
            val rating = binding.ratingInput.text.toString().toIntOrNull()
            val favorite = binding.favoriteCheckbox.isChecked
            val notes = binding.notesInput.text.toString().ifBlank { null }
            val mechanicsCsv = binding.mechanicsInput.text.toString()
            viewModel.saveAndMarkReviewed(rating, favorite, notes, mechanicsCsv)
        }

        binding.previousButton.setOnClickListener { viewModel.previous() }
        binding.skipButton.setOnClickListener { viewModel.skipOrNext() }
        binding.nextButton.setOnClickListener { viewModel.skipOrNext() }

        viewModel.event.observe(this) { event ->
            if (event == null) return@observe
            when (event) {
                is ClipDetailEvent.Saved -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_LONG).show()
                    if (!viewModel.isQueueMode) finish()
                }
                is ClipDetailEvent.DiscardedUnsavedChanges -> {
                    Toast.makeText(
                        this,
                        getString(R.string.clip_detail_previous_discard_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ClipDetailEvent.QueueComplete -> {
                    Toast.makeText(
                        this,
                        getString(R.string.clip_detail_queue_complete_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
            viewModel.consumeEvent()
        }
    }

    private fun updateQueueProgressText() {
        binding.queueProgressText.text = getString(
            R.string.clip_detail_queue_progress,
            viewModel.queuePositionDisplay,
            viewModel.queueSize
        )
    }

    private fun playClip() {
        val path = filePath ?: return
        startActivity(
            Intent(this, PlayerActivity::class.java).putExtra(PlayerActivity.EXTRA_FILE_PATH, path)
        )
    }

    companion object {
        const val EXTRA_CLIP_ID = "extra_clip_id"
        const val EXTRA_QUEUE_IDS = "extra_queue_ids"
        const val EXTRA_QUEUE_POSITION = "extra_queue_position"
    }
}
