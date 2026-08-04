package com.rlvault.ui.review

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rlvault.R
import com.rlvault.databinding.ActivityClipListBinding
import com.rlvault.di.ServiceLocator

/**
 * Reviewed tab of the Clips destination — everything already reviewed (rated/tagged/notes
 * saved). Ported unchanged from the old standalone ReviewedClipListActivity: same ViewModel,
 * tapping a clip re-opens the same review form in single-edit mode (no queue extras).
 */
class ReviewedClipsFragment : Fragment() {

    private var _binding: ActivityClipListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ClipAdapter

    private val viewModel: ReviewedClipListViewModel by viewModels {
        ReviewedClipListViewModel.Factory(ServiceLocator.clipRepository)
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
        binding.emptyStateText.text = getString(R.string.reviewed_list_empty)

        adapter = ClipAdapter { clip ->
            val intent = Intent(requireContext(), ClipDetailActivity::class.java)
                .putExtra(ClipDetailActivity.EXTRA_CLIP_ID, clip.id)
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
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
