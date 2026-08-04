package com.rlvault.ui.review

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rlvault.databinding.ActivityClipListBinding
import com.rlvault.di.ServiceLocator

/**
 * Pending tab of the Clips destination — every clip with `reviewed = false`, oldest import
 * first. Ported unchanged from the old standalone ClipListActivity: same ViewModel, same
 * queue-snapshot behavior when opening a clip for review.
 */
class PendingClipsFragment : Fragment() {

    private var _binding: ActivityClipListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ClipAdapter

    private val viewModel: ClipListViewModel by viewModels {
        ClipListViewModel.Factory(ServiceLocator.clipRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityClipListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ClipAdapter { clip ->
            // Snapshot the full unreviewed id list once, at queue-entry, so it doesn't reshuffle
            // under the user as clips drop out of the list mid-review.
            val queueIds = (adapter.currentList.map { it.id }).let { ids ->
                LongArray(ids.size) { i -> ids[i] }
            }
            val position = queueIds.indexOf(clip.id).let { if (it == -1) 0 else it }
            val intent = Intent(requireContext(), ClipDetailActivity::class.java)
                .putExtra(ClipDetailActivity.EXTRA_CLIP_ID, clip.id)
                .putExtra(ClipDetailActivity.EXTRA_QUEUE_IDS, queueIds)
                .putExtra(ClipDetailActivity.EXTRA_QUEUE_POSITION, position)
            startActivity(intent)
        }
        binding.clipRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.clipRecyclerView.adapter = adapter

        viewModel.clips.observe(viewLifecycleOwner) { clips ->
            adapter.submitList(clips)
            binding.emptyStateText.visibility = if (clips.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // A clip just marked reviewed in ClipDetailActivity needs to drop out of this list.
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
