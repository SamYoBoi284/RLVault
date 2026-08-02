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
        // A clip just marked reviewed in ClipDetailActivity needs to drop out of this list.
        viewModel.refresh()
    }
}
