package com.rlvault.ui.review

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rlvault.databinding.ActivityClipListBinding
import com.rlvault.di.ServiceLocator

/**
 * Everything already reviewed (rated/tagged/notes saved) — proof that a reviewed clip's mechanic
 * tags are actually counted toward achievements, since ClipDetailActivity re-runs the evaluator
 * on every save including edits made here. Tapping a clip re-opens the same review form used for
 * unreviewed clips, so ratings/tags/notes can be corrected after the fact.
 */
class ReviewedClipListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClipListBinding
    private lateinit var adapter: ClipAdapter

    private val viewModel: ReviewedClipListViewModel by viewModels {
        ReviewedClipListViewModel.Factory(ServiceLocator.clipRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClipListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.emptyStateText.text = getString(com.rlvault.R.string.reviewed_list_empty)

        adapter = ClipAdapter { clip ->
            val intent = Intent(this, ClipDetailActivity::class.java)
                .putExtra(ClipDetailActivity.EXTRA_CLIP_ID, clip.id)
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
        viewModel.refresh()
    }
}
