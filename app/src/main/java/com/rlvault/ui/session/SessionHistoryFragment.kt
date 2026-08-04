package com.rlvault.ui.session

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rlvault.R
import com.rlvault.databinding.FragmentSessionHistoryBinding
import com.rlvault.di.ServiceLocator


class SessionHistoryFragment :
    Fragment(R.layout.fragment_session_history) {


    private var _binding: FragmentSessionHistoryBinding? = null

    private val binding
        get() = _binding!!


    private val viewModel:
            SessionHistoryViewModel by viewModels {

        SessionHistoryViewModel.Factory(
            ServiceLocator.sessionRepository
        )
    }


    private lateinit var adapter:
            SessionHistoryAdapter



    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        _binding =
            FragmentSessionHistoryBinding.bind(view)


        adapter =
            SessionHistoryAdapter()


        binding.sessionRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())


        binding.sessionRecyclerView.adapter =
            adapter



        viewModel.sessions.observe(
    viewLifecycleOwner
) { sessions ->


    adapter.submitList(
        sessions
    )


    if (sessions.isEmpty()) {

        binding.emptyText.visibility =
            View.VISIBLE

        binding.sessionRecyclerView.visibility =
            View.GONE

    } else {

        binding.emptyText.visibility =
            View.GONE

        binding.sessionRecyclerView.visibility =
            View.VISIBLE
    }
}



    override fun onResume() {

        super.onResume()

        viewModel.loadSessions()
    }



    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}