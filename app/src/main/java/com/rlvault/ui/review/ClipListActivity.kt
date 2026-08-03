package com.rlvault.ui.review

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rlvault.databinding.ActivityClipListBinding
import com.rlvault.di.ServiceLocator

/** Review queue — every clip with `reviewed = false`, oldest import first. */
class ClipListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClipListBinding
    private lateinit var adapter: ClipAdapter

    private val viewModel: ClipListViewModel by viewModels {
        ClipListViewModel.Factory(ServiceLocator.clipRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClipListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ClipAdapter { clip ->
            // Snapshot the full unreviewed id list once, at queue-entry, so it doesn't reshuffle
            // under the user as clips drop out of the list mid-review.
            val queueIds = (adapter.currentList.map { it.id }).let { ids ->
                LongArray(ids.size) { i -> ids[i] }
            }
            val position = queueIds.indexOf(clip.id).let { if (it == -1) 0 else it }
            val intent = Intent(this, ClipDetailActivity::class.java)
                .putExtra(ClipDetailActivity.EXTRA_CLIP_ID, clip.id)
                .putExtra(ClipDetailActivity.EXTRA_QUEUE_IDS, queueIds)
                .putExtra(ClipDetailActivity.EXTRA_QUEUE_POSITION, position)
            startActivity(intent)
        }
        binding.clipRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.clipRecyclerView.adapter = adapter

        viewModel.clips.observe(this) { clips ->
            adapter.submitList(clips)
            binding.emptyStateText.visibility = if (clips.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // A clip just marked reviewed in ClipDetailActivity needs to drop out of this list.
        viewModel.refresh()
    }
}
