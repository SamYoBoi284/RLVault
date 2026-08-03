package com.rlvault.ui.session

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rlvault.R
import com.rlvault.databinding.FragmentManualSessionBinding
import com.rlvault.di.ServiceLocator

class ManualSessionFragment : Fragment(
    R.layout.fragment_manual_session
) {

    private var _binding: FragmentManualSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogSessionViewModel by viewModels {
        LogSessionViewModel.Factory(ServiceLocator.sessionRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentManualSessionBinding.bind(view)

        binding.saveButton.setOnClickListener {

            val wins =
                binding.winsInput.text.toString().toIntOrNull() ?: 0

            val losses =
                binding.lossesInput.text.toString().toIntOrNull() ?: 0

            val rank =
                binding.rankInput.text.toString()
                    .ifBlank { null }

            val notes =
                binding.notesInput.text.toString()
                    .ifBlank { null }

            viewModel.logSession(
                wins,
                losses,
                rank,
                notes
            )
        }


        viewModel.statusText.observe(viewLifecycleOwner) { status ->

            binding.statusText.text = status

            if (status != null && status.startsWith("Saved")) {

                binding.winsInput.text?.clear()
                binding.lossesInput.text?.clear()
                binding.rankInput.text?.clear()
                binding.notesInput.text?.clear()

            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}