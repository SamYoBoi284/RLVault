package com.rlvault.ui.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rlvault.databinding.FragmentSessionHistoryBinding
import com.rlvault.di.ServiceLocator

class SessionHistoryFragment : Fragment() {

    private var _binding: FragmentSessionHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SessionHistoryViewModel by viewModels {
        SessionHistoryViewModel.Factory(ServiceLocator.sessionRepository)
    }

    private lateinit var adapter: SessionHistoryAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSessionHistoryBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()
        observeSessions()
        viewModel.loadSessions()

    }


    private fun setupRecyclerView() {

        adapter = SessionHistoryAdapter()

        binding.sessionHistoryRecyclerView.apply {

            layoutManager =
                LinearLayoutManager(requireContext())

            adapter = this@SessionHistoryFragment.adapter

        }

    }


    private fun observeSessions() {

        viewModel.sessions.observe(
            viewLifecycleOwner
        ) { sessions ->


            if (sessions.isEmpty()) {

                binding.emptyStateText.visibility =
                    View.VISIBLE

                binding.sessionHistoryRecyclerView.visibility =
                    View.GONE

            } else {

                binding.emptyStateText.visibility =
                    View.GONE

                binding.sessionHistoryRecyclerView.visibility =
                    View.VISIBLE


                adapter.submitList(
                    sessions
                )

            }

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}